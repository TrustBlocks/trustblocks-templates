(ns tasks
  "bb check and bb build. See README.md.

  check is the half of validation that needs nothing but babashka: every
  template's structure, its logic, its copies of the shared models, and the
  CommonMark rules its grammar must keep (docs/templatemark-conformance.md).
  The other half -- does it load and draft through Accord's own engine -- is
  bb conformance, which needs Node."
  (:require [babashka.fs :as fs]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.io PushbackReader]
           [java.util.zip ZipEntry ZipOutputStream]))

(defn- template-dirs-under [dir]
  (when (fs/exists? dir)
    (filter #(fs/exists? (fs/path % "package.json")) (fs/list-dir dir))))

(defn templates
  "Every template directory -- a directory with a package.json, which is what
  cicero-core keys on: those under templates/, and each package's, under
  packages/<package>/templates/."
  []
  (->> (concat (template-dirs-under "templates")
               (when (fs/exists? "packages")
                 (mapcat #(template-dirs-under (fs/path % "templates")) (fs/list-dir "packages"))))
       (sort-by str)))

(defn- label
  "A template's name for reports: its directory, after its package when it
  has one -- street-resurfacing/pay-application."
  [dir]
  (let [parts (map str (fs/components (fs/relativize (fs/cwd) (fs/absolutize dir))))]
    (if (= "packages" (first parts))
      (str (nth parts 1) "/" (last parts))
      (last parts))))

;; ------------------------------------------------------------------ check

(def ^:private accord-runtimes
  "What cicero-core's metadata accepts for accordproject.runtime. Anything
  else and the archive does not load at all, which is why a Clojure
  template still declares one of these -- see README.md."
  #{"es6" "typescript"})

(def ^:private trustblocks-runtimes #{"clojure"})

(def ^:private documented-format
  "The `as \"...\"` formats TemplateMark documents: DateTime tokens with
  punctuation and spaces, and amount patterns."
  #"(?:(?:YYYY|MMMM|MMM|MM|M|DD|D|HH|H|hh|h|mm|ss|SSS|Z|a|A)|[ ,./:-])+|K?0[ ,]0(?:[.,]0+)?(?: ?CCC)?")

(defn- read-forms
  "Every top-level form in a Clojure source file, read as data only."
  [f]
  (with-open [r (PushbackReader. (io/reader (str f)))]
    (binding [*read-eval* false]
      (loop [forms []]
        (let [form (read {:eof ::eof} r)]
          (if (= ::eof form) forms (recur (conj forms form))))))))

(defn- own-model-files
  "A template's own model files: every model/*.cto except the vendored
  imports -- Accord's (@models.accordproject.org.*) and copies of ours from
  shared/model/."
  [ctos]
  (let [shared (set (map (comp str fs/file-name) (fs/glob "shared/model" "*.cto")))]
    (->> ctos
         (remove #(str/starts-with? (str (fs/file-name %)) "@"))
         (remove #(shared (str (fs/file-name %)))))))

(defn- own-namespaces
  "The namespaces a template declares itself, in its own model files."
  [ctos]
  (->> (own-model-files ctos)
       (keep #(second (re-find #"(?m)^namespace\s+(\S+)" (slurp (str %)))))
       distinct))

(defn- literal-blanks
  "Blanks written into a grammar as text rather than held by a field: a run
  of underscores or a bracketed [PLACEHOLDER] anywhere outside an
  {{#optional}} block's {{else}} branch -- which is where a field's own
  blank belongs, rendered when the field is absent."
  [grammar]
  (let [outside (str/replace grammar #"(?s)\{\{else\}\}.*?\{\{/optional\}\}" "{{/optional}}")]
    (->> (re-seq #"(?:\\_){4,}|_{4,}|\\?\[[A-Z][A-Z ]{2,}\\?\]" outside)
         distinct)))

(defn- blank-values
  "Paths in `data` whose value is a blank written as data -- a string of
  nothing but underscores -- where the field should simply be absent."
  ([data] (blank-values [] data))
  ([path data]
   (cond
     (map? data) (mapcat (fn [[k v]] (blank-values (conj path k) v)) data)
     (sequential? data) (mapcat (fn [i v] (blank-values (conj path i) v)) (range) data)
     (and (string? data) (re-matches #"_+" data)) [path]
     :else nil)))

(def ^:private credential-types
  "What certifies a lifecycle event: a Receipt for a document received from
  someone with no identity of their own, an Attestation for an
  officeholder's own act."
  #{"Receipt" "Attestation"})

(defn- declared-names
  "Every type an own-namespace model declares, and those of them that are
  Request transactions -- the events a lifecycle may name."
  [texts]
  (let [text (str/join "\n" texts)]
    {:types    (set (map second (re-seq #"(?m)^\s*(?:abstract\s+)?(?:concept|asset|transaction|participant|event)\s+(\w+)" text)))
     :requests (set (map second (re-seq #"(?m)^\s*transaction\s+(\w+)\s+extends\s+Request\b" text)))}))

(defn- reachable
  "The states reachable from those a creating transition leads to."
  [transitions]
  (loop [seen (set (map #(get % "to") (filter #(empty? (get % "from")) transitions)))]
    (let [more (into seen (for [t transitions
                                :when (some seen (get t "from"))]
                            (get t "to")))]
      (if (= more seen) seen (recur more)))))

(defn- lifecycle-problems
  "Why lifecycle `lc` is not a sound state machine for a template whose own
  models declare `names`. See docs/model-conventions.md, Lifecycles."
  [lc {:keys [types requests]}]
  (let [states      (map #(get % "name") (get lc "states"))
        state?      (set states)
        accepting   (set (map #(get % "name") (filter #(get % "accepting") (get lc "states"))))
        transitions (get lc "transitions")
        outgoing    (set (mapcat #(get % "from") transitions))
        events      (set (map #(get % "event") transitions))]
    (concat
     (when (not= "com.trustblocks.lifecycle@1.0.0.Lifecycle" (get lc "$class"))
       ["lifecycle: $class must be com.trustblocks.lifecycle@1.0.0.Lifecycle"])
     (when-not (types (get lc "stateType"))
       [(str "lifecycle: stateType " (get lc "stateType") " is not declared in the template's namespace")])
     (for [[n c] (frequencies states) :when (> c 1)]
       (str "lifecycle: state " n " declared " c " times"))
     (when-not (some #(empty? (get % "from")) transitions)
       ["lifecycle: no transition begins the lifecycle (one with no from states)"])
     (mapcat
      (fn [{:strs [event from to requires afterDays]}]
        (let [at (str "lifecycle: " event)]
          (concat
           (when-not (requests event)
             [(str at " is not a Request transaction in the template's namespace")])
           (for [s (cons to from) :when (not (state? s))]
             (str at " names undeclared state " s))
           (for [{:strs [credential authority]} requires
                 :when (not (and (credential-types credential)
                                 (re-matches #"[A-Z][A-Z_]*" (str authority))))]
             (str at " requires " credential "/" authority
                  " -- a credential of " (sort credential-types) " and an AUTHORITY name"))
           (cond
             (and afterDays (seq requires))
             [(str at " is timed; a timed event is certified by nobody")]
             (and afterDays (empty? from))
             [(str at " is timed, so it needs a state to count from")]
             (and (nil? afterDays) (empty? requires))
             [(str at " is neither certified nor timed")]))))
      transitions)
     ;; Deterministic: one transition per event per state.
     (for [[event ts] (group-by #(get % "event") transitions)
           :let [froms (mapcat #(if (empty? (get % "from")) [::begin] (get % "from")) ts)]
           [s c] (frequencies froms) :when (> c 1)]
       (str "lifecycle: " event " has " c " transitions from " (if (= ::begin s) "the beginning" s)))
     (for [s states :when (not ((reachable transitions) s))]
       (str "lifecycle: state " s " is unreachable"))
     (for [s states :when (and (not (accepting s)) (not (outgoing s)))]
       (str "lifecycle: state " s " is a dead end -- no event leaves it, and it is not accepting"))
     (for [s states :when (and (accepting s) (outgoing s))]
       (str "lifecycle: accepting state " s " has events leaving it"))
     (for [r (sort requests) :when (not (events r))]
       (str "lifecycle: Request " r " is declared but no transition uses it")))))

(defn- problems
  "Why template `dir` is not a valid template, as a seq of strings."
  [dir]
  (let [f       #(fs/file dir %)
        pkg     (json/parse-string (slurp (f "package.json")))
        accord  (get pkg "accordproject")
        tb      (get pkg "trustblocks")
        grammar (when (fs/exists? (f "text/grammar.tem.md")) (slurp (f "text/grammar.tem.md")))
        sample  (when (fs/exists? (f "sample.json")) (json/parse-string (slurp (f "sample.json"))))
        ctos    (when (fs/exists? (f "model")) (fs/glob (f "model") "*.cto"))]
    (concat
     (for [k ["name" "version"] :when (str/blank? (get pkg k))]
       (str "package.json has no " k))
     (when-not (accord-runtimes (get accord "runtime"))
       [(str "accordproject.runtime must be one of " (sort accord-runtimes)
             " for Accord's tools to load the archive")])
     (when (str/blank? (get accord "cicero"))
       ["package.json has no accordproject.cicero range"])
     (when (empty? ctos) ["no model/*.cto"])
     ;; One namespace of its own, as in Accord's own template library --
     ;; everything else a template needs is imported. See
     ;; docs/model-conventions.md.
     (let [own (own-namespaces ctos)]
       (when (and (seq ctos) (not= 1 (count own)))
         [(str "a template declares exactly one namespace of its own; found "
               (count own) ": " (str/join ", " own))]))
     (when-not grammar ["no text/grammar.tem.md"])
     (when-not (get sample "$class") ["sample.json missing, or has no $class"])

     ;; Trustblocks' own section: a clause, a lifecycle, or both.
     (when tb
       (let [{:strs [logic lifecycle]} tb]
         (concat
          (when-not (or logic lifecycle)
            ["package.json's trustblocks section names neither logic nor a lifecycle"])
          (when logic
            (concat
             (when-not (trustblocks-runtimes (get tb "runtime"))
               [(str "trustblocks.runtime must be one of " (sort trustblocks-runtimes))])
             (if-not (fs/exists? (f logic))
               [(str "trustblocks.logic " logic " does not exist")]
               (let [n (count (read-forms (f logic)))]
                 (when (not= 1 n)
                   [(str logic " must be exactly one expression -- the clause -- not " n)])))
             (when-not (fs/exists? (f "request.json"))
               ["an executing template needs a request.json"])))
          (when lifecycle
            (cond
              (not (fs/exists? (f lifecycle)))
              [(str "trustblocks.lifecycle " lifecycle " does not exist")]
              (not (fs/exists? (f "model/lifecycle.cto")))
              ["a template with a lifecycle carries model/lifecycle.cto, copied from shared/model/"]
              :else
              (lifecycle-problems (json/parse-string (slurp (f lifecycle)))
                                  (declared-names (map #(slurp (str %))
                                                       (own-model-files ctos)))))))))

     ;; Every blank in a form is a place for data: a field, never text.
     (when grammar
       (when-let [blanks (seq (literal-blanks grammar))]
         [(str "grammar: " (count blanks) " blank(s) written as text, not held by a field: "
               (str/join ", " (take 3 blanks)))]))
     (for [path (blank-values sample)]
       (str "sample.json: " (str/join "." path) " is a blank written as data; leave the field out"))

     ;; The CommonMark TemplateMark rules.
     (when grammar
       (concat
        (when (re-find #"(?m)^>" grammar) ["grammar: blockquote used for indentation"])
        (when (str/includes? grammar "•") ["grammar: literal bullet character"])
        (when (re-find #"<[a-zA-Z/][^>]*>" grammar) ["grammar: raw HTML"])
        (for [[_ fmt] (re-seq #"as \"([^\"]*)\"" grammar)
              :when (not (re-matches documented-format fmt))]
          (str "grammar: undocumented format " (pr-str fmt))))))))

(defn- shared-copy-problems
  "A template's copy of a shared model that differs from shared/model/."
  []
  (for [shared (fs/glob "shared/model" "*.cto")
        dir    (templates)
        :let   [copy (fs/path dir "model" (fs/file-name shared))]
        :when  (and (fs/exists? copy) (not= (slurp (str shared)) (slurp (str copy))))]
    (str (label dir) ": model/" (fs/file-name shared)
         " differs from shared/model/" (fs/file-name shared))))

(defn check []
  (let [results (for [dir (templates)] [(label dir) (problems dir)])
        shared  (shared-copy-problems)]
    (println (str "Checking " (count results) " template(s) under templates/ and packages/"))
    (doseq [[name ps] results]
      (if (empty? ps)
        (println "  ok   " name)
        (do (println "  FAIL " name)
            (doseq [p ps] (println "        " p)))))
    (doseq [p shared] (println "  FAIL " p))
    (when (or (seq shared) (some (comp seq second) results))
      (System/exit 1))))

;; ------------------------------------------------------------------ build

(defn- archive-name [dir]
  (let [pkg (json/parse-string (slurp (fs/file dir "package.json")))]
    (str (get pkg "name") "@" (get pkg "version") ".cta")))

(defn build
  "Zips every template into dist/<name>@<version>.cta -- the template
  directory's own contents at the archive root, as Accord's archives lay
  them out."
  []
  (fs/create-dirs "dist")
  (doseq [dir (templates)]
    (let [out   (fs/file "dist" (archive-name dir))
          files (->> (fs/glob dir "**") (filter fs/regular-file?) (sort-by str))]
      (with-open [zip (ZipOutputStream. (io/output-stream out))]
        (doseq [f files]
          (.putNextEntry zip (ZipEntry. (str (fs/relativize dir f))))
          (io/copy (fs/file f) zip)
          (.closeEntry zip)))
      (println (str "  " out "  (" (count files) " files, " (fs/size out) " bytes)")))))

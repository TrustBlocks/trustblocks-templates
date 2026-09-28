(ns manifest
  "A package's manifest (packages/<package>/manifest.edn): read it, write it
  as Concerto JSON, and check it -- against shared/model/manifest.cto, and
  against the package it describes. See docs/manifest.md.

  The EDN is written for people: kebab-case keys and no $class, since each
  part's type follows from where it sits. The JSON is the same data in the
  model's own terms -- camelCase keys, every object's $class -- for Accord's
  tools and anything outside Clojure."
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def ^:private ns-prefix "com.trustblocks.manifest@1.0.0.")

(def ^:private nested
  "Which concept each array holds, by the property it sits under."
  {:phases :Phase :documents :DocumentRole :fields :StoredField
   :milestones :Milestone :references :LegalReference :intake :IntakeSection
   :guides :Guide})

(defn- camel [k]
  (let [[head & tail] (str/split (name k) #"-")]
    (apply str head (map str/capitalize tail))))

(defn read-manifest [f] (edn/read-string (slurp (str f))))

(defn ->concerto
  "The manifest as Concerto JSON data: camelCase keys, a $class on every
  object. Strings in arrays (roles, field names, ids) are left as they are."
  ([m] (->concerto :Manifest m))
  ([concept m]
   (into {"$class" (str ns-prefix (name concept))}
         (for [[k v] m]
           [(camel k)
            (if (and (sequential? v) (nested k) (every? map? v))
              (mapv #(->concerto (nested k) %) v)
              v)]))))

;; ------------------------------------------------------------------ the model

(defn- model
  "shared/model/manifest.cto as {concept {property {:type :optional :array}}}
  and {enum #{values}} -- enough to check a manifest without Node."
  []
  (let [text (slurp "shared/model/manifest.cto")
        blocks (re-seq #"(?s)(concept|enum)\s+(\w+)\s*\{(.*?)\n\}" text)]
    (reduce
     (fn [acc [_ kind name body]]
       (if (= "enum" kind)
         (assoc-in acc [:enums name] (set (map second (re-seq #"(?m)^\s*o\s+(\w+)" body))))
         (assoc-in acc [:concepts name]
                   (into {} (for [[_ type arr prop opt] (re-seq #"(?m)^\s*o\s+(\w+)(\[\])?\s+(\w+)(\s+optional)?" body)]
                              [prop {:type type :array (boolean arr) :optional (boolean opt)}])))))
     {} blocks)))

(def ^:private primitive?
  {"String" string? "Boolean" boolean? "Double" number? "Integer" integer?})

(declare type-problems-one)

(defn- type-problems
  "Where `value` (Concerto JSON data) does not fit `concept` of the model."
  [{:keys [concepts enums] :as mdl} concept value path]
  (let [props (get concepts concept)
        where #(str/join "." (conj path %))]
    (concat
     (for [k (keys value) :when (not= "$class" k) :when (not (contains? props k))]
       (str (where k) ": not a property of " concept))
     (mapcat
      (fn [[prop {:keys [type array optional]}]]
        (let [v (get value prop)]
          (cond
            (nil? v) (when-not optional [(str (where prop) ": required")])
            array    (if-not (sequential? v)
                       [(str (where prop) ": should be a list")]
                       (mapcat (fn [i x] (type-problems-one mdl type x (conj path (str prop "[" i "]"))))
                               (range) v))
            :else    (type-problems-one mdl type v (conj path prop)))))
      props))))

(defn- type-problems-one [{:keys [concepts enums] :as mdl} type v path]
  (cond
    (primitive? type) (when-not ((primitive? type) v) [(str (str/join "." path) ": should be a " type)])
    (enums type)      (when-not ((enums type) v)
                        [(str (str/join "." path) ": " (pr-str v) " is not one of " (sort (enums type)))])
    (concepts type)   (if (map? v) (type-problems mdl type v path)
                          [(str (str/join "." path) ": should be a " type)])
    :else             [(str (str/join "." path) ": unknown type " type)]))

;; ------------------------------------------------------------------ the package

(defn- terms-properties
  "Property names of the package's terms template's model -- its own
  namespace and the shared models it carries."
  [package-dir template]
  (let [dir (fs/path package-dir "templates" template "model")]
    (when (fs/exists? dir)
      (->> (fs/glob dir "*.cto")
           (remove #(str/starts-with? (str (fs/file-name %)) "@"))
           (mapcat #(map second (re-seq #"(?m)^\s*o\s+\w+(?:\[\])?\s+(\w+)" (slurp (str %)))))
           set))))

(defn problems
  "Why `package-dir`'s manifest.edn is not sound, as strings."
  [package-dir]
  (let [f (fs/path package-dir "manifest.edn")]
    (if-not (fs/exists? f)
      ["no manifest.edn"]
      (let [m        (read-manifest f)
            json     (->concerto m)
            typed    (type-problems (model) "Manifest" json [])
            docs     (:documents m)
            roles    (map :role docs)
            role?    (set roles)
            phase?   (set (map :name (:phases m)))
            ref?     (set (map :id (:references m)))
            terms    (first (filter #(= (:terms m) (:role %)) docs))
            props    (some->> (:template terms) (terms-properties package-dir))
            on-disk  (set (map (comp str fs/file-name)
                               (filter #(fs/exists? (fs/path % "package.json"))
                                       (fs/list-dir (fs/path package-dir "templates")))))
            named    (set (keep :template docs))]
        (concat
         (map #(str "manifest: " %) typed)
         (when-not (= (fs/file-name package-dir) (:package m))
           [(str "manifest: package is " (pr-str (:package m)) ", but it sits in " (fs/file-name package-dir))])
         (for [[r n] (frequencies roles) :when (> n 1)] (str "manifest: role " r " appears " n " times"))
         (cond
           (nil? terms) [(str "manifest: terms names " (pr-str (:terms m)) ", which is no document's role")]
           (not= "TERMS" (:treatment terms)) [(str "manifest: the terms document is not treated as TERMS")])
         (when (not= 1 (count (filter #(= "TERMS" (:treatment %)) docs)))
           ["manifest: exactly one document is the TERMS"])
         (mapcat
          (fn [{:keys [role treatment template phase refers-to references reads-terms fields]}]
            (let [at (str "manifest: " role)]
              (concat
               (cond
                 (and (#{"TERMS" "TEMPLATE"} treatment) (not (on-disk template)))
                 [(str at ": template " (pr-str template) " is not in this package")]
                 (and (= "STORED" treatment) template)
                 [(str at ": a stored document names no template")])
               (when-not (phase? phase) [(str at ": phase " phase " is not one of the manifest's phases")])
               (for [r refers-to :when (not (role? r))] (str at ": refers to " r ", which is no document's role"))
               (for [r references :when (not (ref? r))] (str at ": cites " r ", which is not in references"))
               (for [p reads-terms :when (and props (not (props p)))]
                 (str at ": reads " p ", which the terms model does not declare"))
               (for [{:keys [name type deadline]} fields
                     :when (or (not (#{"String" "Double" "Integer" "Boolean" "DateTime"} type))
                               (and deadline (not= "DateTime" type)))]
                 (str at ": field " name " -- a String, Double, Integer, Boolean or DateTime, and a deadline only if a DateTime")))))
          docs)
         (for [t (sort on-disk) :when (not (named t))]
           (str "manifest: the package's template " t " is no document's template"))
         (for [{:keys [name phase references]} (:milestones m)
               :let [bad (concat (when-not (phase? phase) [(str "phase " phase)])
                                 (remove ref? references))]
               :when (seq bad)]
           (str "manifest: milestone " (pr-str name) " names " (str/join ", " bad) ", not in the manifest"))
         (for [[id n] (frequencies (map :id (:references m))) :when (> n 1)]
           (str "manifest: reference " id " appears " n " times"))
         (for [{:keys [title phase fields]} (:intake m)
               p (cond-> (vec (remove #(or (nil? props) (props %)) fields))
                   (not (phase? phase)) (conj (str "phase " phase)))]
           (str "manifest: intake " (pr-str title) " asks for " p ", which the terms model does not declare")))))))

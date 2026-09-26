;; The Contractor Pay Request's clause: what follows from each event its
;; lifecycle (lifecycle.json) allows. The lifecycle decides whether an event
;; may happen and the status it leads to; this decides what it records and
;; whether the pay request's own figures permit it.
;;
;; InspectionCertified records the pay items the engineer approved.
;;
;; PayRequestCertified is the Director's verification of quantities and
;; amounts (Sec. 6), and is refused unless
;;   - every pay item with a quantity this period has been approved by the
;;     engineer (the "Approved by Engineer" box), and
;;   - the Contractor's figures agree: each amount is its quantity times the
;;     unit price, the gross is the sum of the amounts to date, and the
;;     retainage, liquidated damages, total deductions and net amount due
;;     follow from it -- to the cent, where the figure is filled in.
;;
;; Every other event records nothing beyond the new status.
(let [ns       "com.trustblocks.municipal.construction.payapplication@1.0.0."
      event    (get request "$class")
      lines    (or (get data "payItems") [])
      approved (or (get state "approvedPayItems") [])
      listing  (fn [xs] (reduce (fn [a b] (if (= a "") b (str a "; " b))) "" xs))
      money    (fn [x] (str "$" x))
      near?    (fn [a b] (let [d (- a b)] (and (< d 0.005) (> d -0.005))))
      ;; A disagreement between a figure written on the form and the one
      ;; computed from it, or nil when they agree or the figure is blank.
      differs  (fn [label written computed]
                 (when (and (some? written) (not (near? written computed)))
                   (str label " is " (money written) ", not " (money computed))))
      product  (fn [line qty amt]
                 (when (and (get line "unitPrice") (get line qty))
                   (differs (str "item " (get line "payItem") " " amt)
                            (get line amt) (* (get line qty) (get line "unitPrice")))))
      zero     (fn [k] (or (get data k) 0.0))]
  (cond
    (= event (str ns "InspectionCertified"))
    {:state {"approvedPayItems"
             (reduce (fn [acc item] (if (some (fn [a] (= a item)) acc) acc (conj acc item)))
                     (into [] approved) (get request "approvedPayItems"))}}

    (= event (str ns "PayRequestCertified"))
    (let [unapproved (filter (fn [line]
                               (and (pos? (or (get line "quantityThisPeriod") 0.0))
                                    (not (some (fn [a] (= a (get line "payItem"))) approved))))
                             lines)
          gross      (reduce + 0.0 (map (fn [line] (or (get line "amountToDate") 0.0)) lines))
          retainage  (divide (* (zero "grossAmount") (zero "retainagePercent")) 100 2)
          damages    (* (or (get data "liquidatedDamagesDays") 0) (zero "liquidatedDamagesPerDay"))
          deductions (+ (zero "retainageAmount") (zero "previousPayments")
                        (zero "liquidatedDamagesAmount") (zero "otherDeductions"))
          problems   (remove nil?
                             (into (into (into [] (map (fn [l] (product l "quantityThisPeriod" "amountThisPeriod")) lines))
                                         (map (fn [l] (product l "quantityToDate" "amountToDate")) lines))
                              [(differs "the gross amount" (get data "grossAmount") gross)
                               (differs "the retainage" (get data "retainageAmount") retainage)
                               (differs "the liquidated damages" (get data "liquidatedDamagesAmount") damages)
                               (differs "the total deductions" (get data "totalDeductions") deductions)
                               (differs "the net amount due" (get data "netAmountDue")
                                        (- (zero "grossAmount") (zero "totalDeductions")))]))]
      (when (seq unapproved)
        (throw (ex-info (str "Not approved by the engineer: pay item "
                             (listing (map (fn [l] (get l "payItem")) unapproved)))
                        {})))
      (when (seq problems)
        (throw (ex-info (str "The figures do not agree: " (listing problems)) {})))
      {})

    :else {}))

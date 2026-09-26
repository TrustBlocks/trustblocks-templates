;; The Town Manager employment contract's clause: what follows from each
;; request the contract answers. It is run in Trustblocks' sandbox with the
;; contract's data, the request, the contract's state as of the request's
;; own time, and that time -- no clock, no I/O -- so the same request
;; always has the same answer.
;;
;; Whether a request may happen at all, and who must certify it, is the
;; contract's lifecycle (lifecycle.json), checked before this runs: Board
;; approval once, certified by the Town Clerk; attestations only after it.
;; This says only what follows.
(let [employment "com.trustblocks.municipal.employment@1.0.0."
      event      (get request "$class")]
  (cond
    ;; BoardApprovalRequest -- called when the Town Clerk certifies that the
    ;; Town Council approved the contract. Records the approval, as of the
    ;; Council's action date, and the date its terms take effect (which may
    ;; be earlier: a retroactive salary).
    (= event (str employment "BoardApprovalRequest"))
    (let [approved-at (get data "councilActionDate")
          effective   (get data "effectiveDate")]
      {:response {"$class" (str employment "BoardApprovalResponse")
                  "$timestamp" now
                  "status" "APPROVED"
                  "approvedAt" approved-at
                  "effectiveDate" effective}
       :state {"$class" (str employment "ManagerEmploymentState")
               "contractId" (get data "contractId")
               "status" "APPROVED"
               "approvedAt" approved-at
               "effectiveDate" effective}})

    ;; AttestationRequest -- called when an officeholder attests to the
    ;; approved contract: the Town Clerk certifying the minutes that record
    ;; the Council's vote. What it answers is what gets signed.
    (= event "com.trustblocks.attestation@1.0.0.AttestationRequest")
    {:response {"$class" "com.trustblocks.attestation@1.0.0.AttestationResponse"
                "$timestamp" now
                "statement" (get request "statement")
                "documentHash" (get request "documentHash")}}

    ;; Anything else is not a request this contract answers.
    :else
    (throw (ex-info (str "This contract does not answer " event) {}))))

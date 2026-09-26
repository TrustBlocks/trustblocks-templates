;; The Town Manager employment contract's clause: what follows from each
;; request the contract answers. It is run in Trustblocks' sandbox with the
;; contract's data, the request, the contract's state as of the request's
;; own time, and that time -- no clock, no I/O -- so the same request
;; always has the same answer.
(let [employment "com.trustblocks.municipal.employment@1.0.0."
      event      (get request "$class")
      approved?  (and state (= (get state "status") "APPROVED"))]
  (cond
    ;; BoardApprovalRequest -- called when the Town Council approves the
    ;; contract. Records the approval, as of the Council's action date, and
    ;; the date its terms take effect (which may be earlier: a retroactive
    ;; salary). The Council approves once.
    (= event (str employment "BoardApprovalRequest"))
    (if approved?
      (throw (ex-info "Already approved by the Board" {}))
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
                 "effectiveDate" effective}}))

    ;; AttestationRequest -- called when an officeholder attests to the
    ;; contract: the Town Clerk certifying the Council's vote, then the
    ;; minutes that record it. The application has already checked that
    ;; they hold the authority package.json names (CERTIFY_MINUTES); the
    ;; contract's own rule is that there is nothing to attest to until the
    ;; Board has approved it. What it answers is what gets signed.
    (= event "com.trustblocks.attestation@1.0.0.AttestationRequest")
    (if approved?
      {:response {"$class" "com.trustblocks.attestation@1.0.0.AttestationResponse"
                  "$timestamp" now
                  "statement" (get request "statement")
                  "documentHash" (get request "documentHash")}}
      (throw (ex-info "Cannot attest to a contract the Board has not yet approved" {})))

    ;; Anything else is not a request this contract answers.
    :else
    (throw (ex-info (str "This contract does not answer " event) {}))))

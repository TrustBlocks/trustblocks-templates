(cond
     (= (get request "$class") "com.trustblocks.attestation@1.0.0.AttestationRequest")
     (if (and state (= (get state "status") "APPROVED"))
       {:response {"$class" "com.trustblocks.attestation@1.0.0.AttestationResponse"
                   "$timestamp" now
                   "statement" (get request "statement")
                   "documentHash" (get request "documentHash")}}
       (throw (ex-info "Cannot attest to a contract the Board has not yet approved" {})))

     (and state (= (get state "status") "APPROVED"))
     (throw (ex-info "Already approved by the Board" {}))

     :else
     (let [approved-at (get data "councilActionDate")
           effective   (get data "effectiveDate")]
       {:response {"$class" "com.trustblocks.municipal.employment.execution@1.0.0.BoardApprovalResponse"
                   "$timestamp" now
                   "status" "APPROVED"
                   "approvedAt" approved-at
                   "effectiveDate" effective}
        :state {"$class" "com.trustblocks.municipal.employment.execution@1.0.0.ManagerEmploymentState"
                "contractId" (get data "contractId")
                "status" "APPROVED"
                "approvedAt" approved-at
                "effectiveDate" effective}}))

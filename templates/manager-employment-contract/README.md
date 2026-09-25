# Manager employment contract

The Town of China Grove's employment contract with its Town Manager
(2025–2029, restated): a council-manager form agreement with the ICMA code
of ethics, without-cause severance with mitigation offsets, and a
service-credit purchase provision. `sample.json` is the contract as
restated.

Root model: `com.trustblocks.municipal.employment@1.0.0.ManagerEmployment`,
which extends the shared `MunicipalAgreement`.

## Logic

`logic/clause.clj` answers two requests:

- **`BoardApprovalRequest`** (`com.trustblocks.municipal.employment.execution`)
  records the Board's approval, once — a second approval is refused. The
  contract's state becomes `APPROVED`, valid from the meeting date.
  `request.json` is that request.
- **`AttestationRequest`** (`com.trustblocks.attestation`) lets an
  officeholder certify something about the contract — the council's vote,
  the minutes approving it — and is refused until the Board has approved the
  contract. The clause decides only whether the statement can be true; who
  is certifying, and their authority to, is checked by Trustblocks outside
  the clause.

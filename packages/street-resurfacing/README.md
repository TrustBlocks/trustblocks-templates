# Street resurfacing package

The documents of a unit-price street resurfacing matter -- in Trustblocks, one
matter per contract let, anchored to the executed contract, whose data every
other document shares and whose state they advance.

Derived from the Town of China Grove's Street Resurfacing bid package,
Contract No. STR-27, in
[`sources/`](../../sources/China_Grove_Street_Resurfacing_Bid_Package.docx).

| Template | Namespace | Document |
| --- | --- | --- |
| [`contract`](templates/contract/) | `com.trustblocks.municipal.construction@1.0.0` | The contract as executed: the matter's terms |
| [`pay-application`](templates/pay-application/) | `com.trustblocks.municipal.construction.payapplication@1.0.0` | Exhibit "E"'s Contractor Pay Request, one per month, with its lifecycle and clause |
| [`vendor-form`](templates/vendor-form/) | `…construction.vendorform@1.0.0` | Exhibit "E"'s Vendor Information Form, one per payee; received, then vendor number assigned |
| [`notice-of-award`](templates/notice-of-award/) | `…construction.noticeofaward@1.0.0` | Exhibit "I"; contract documents due within its response days |
| [`notice-to-proceed`](templates/notice-to-proceed/) | `…construction.noticetoproceed@1.0.0` | Exhibit "J"; the Completion Date counts from its Commence Date |
| [`bid-bond`](templates/bid-bond/), [`performance-bond`](templates/performance-bond/), [`payment-bond`](templates/payment-bond/) | `…construction.bidbond@1.0.0` etc. | Exhibit "F", on the Town's forms |

The whole matter -- every document, modelled or only received -- is in
[`manifest.edn`](manifest.edn); see [`docs/manifest.md`](../../docs/manifest.md).

## How the documents relate

A pay application carries its own period's quantities, and is rendered with
the contract's terms -- contractor, dates, pay items, unit prices -- as of its
own date. Its lines name the contract's pay items (`payItem`, e.g. `5.1`).

The workflow the documents record, under Sec. 6 of the contract and the
General Conditions:

1. The Contractor submits a pay request by the 25th of the month. The
   Contractor has no identity of its own in Trustblocks: the officeholder
   who receives the request signs a *receipt* naming the stored file's hash.
2. The engineer inspects each pay item; the engineer's certificate is received
   the same way, and each line is marked **Approved by Engineer**.
3. The Director of Public Works, or the Director's designated
   representative, verifies quantities and amounts; the Director and the Town
   Manager approve and recommend payment.
4. The Finance Officer certifies that payment was made. Trustblocks makes no
   payments.

## The pay application's lifecycle

[`lifecycle.json`](templates/pay-application/lifecycle.json), part of the
template:

| From | Event | To | Certified by |
|---|---|---|---|
| — | `PayRequestReceived` | RECEIVED | Receipt, `RECEIVE_PAY_APPLICATIONS` |
| RECEIVED | `InspectionCertified` | INSPECTED | Attestation, `CERTIFY_INSPECTIONS`; or Receipt, `RECEIVE_INSPECTION_REPORTS` |
| INSPECTED | `PayRequestCertified` | CERTIFIED | Attestation, `CERTIFY_PAY_APPLICATIONS` |
| CERTIFIED | `PaymentApproved` | APPROVED | Attestation, `APPROVE_PAYMENTS` |
| APPROVED | `PaymentCertified` | PAID (final) | Attestation, `CERTIFY_PAYMENTS` |
| RECEIVED, INSPECTED, CERTIFIED | `PayRequestReturned` | RETURNED (final) | Attestation, `CERTIFY_PAY_APPLICATIONS` |

A returned request is finished; the corrected one is a new document.

## Deadlines

Shown on the timeline and the dashboards; never acted on (see
[`docs/model-conventions.md`](../../docs/model-conventions.md#deadlines)).

| Template | Deadline | Counted | Met by |
|---|---|---|---|
| notice to proceed | Completion Date (Sec. 5); liquidated damages may be assessed after (GC 22) | Commence Date + `completionDays` | -- |
| notice of award | Contract documents due | the Notice's date + `responseDays` (14) | -- |
| pay application | Inspection Certificate due (GC 18(b)) | entering RECEIVED + the contract's `inspectionPeriodDays` | INSPECTED onward, or RETURNED |
| pay application | Payment due (Sec. 6) | entering APPROVED + 30 days | PAID |

## The pay application's clause

[`logic/clause.clj`](templates/pay-application/logic/clause.clj) decides what
follows from each event the lifecycle allows:

- `InspectionCertified` names the pay items the engineer approved, and the
  clause records them in the pay request's state: the **Approved by Engineer**
  boxes.
- `PayRequestCertified`, the Director's verification of quantities and
  amounts (Sec. 6), is refused unless every item with a quantity this period
  has been approved, and the Contractor's figures agree to the cent. Each
  amount must equal its quantity times the unit price. The gross must equal
  the sum of the amounts to date. Retainage, liquidated damages, total
  deductions and net due must follow from those figures.

A refused certification leaves the request where it was; the Director returns
it, and the corrected request is a new document.

## Not yet

- The matter's contract-to-date position: quantities paid to date against the
  contract's, retainage under N.C. Gen. Stat. § 143-134.1 (none on contracts
  under $100,000, none after 50% complete with the surety's consent), and
  liquidated damages from the Completion Date. These need the contract's
  terms and every earlier pay request, so they belong to the matter, not to
  one pay request.
- `model/`: the types these documents will share for that. Nothing is shared
  yet.

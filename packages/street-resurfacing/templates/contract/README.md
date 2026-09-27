# Street resurfacing contract (STR-27)

The Town of China Grove's Street Resurfacing contract, Contract No. STR-27,
formally bid under N.C. Gen. Stat. § 143-129. Converted from the bid package
in [`sources/`](../../../../sources/China_Grove_Street_Resurfacing_Bid_Package.docx).

Root model: `com.trustblocks.municipal.construction@1.0.0.StreetResurfacingContract`,
which extends the shared `MunicipalAgreement`, with `BidItem` (37 pay items
across the Bid Form's seven sections), `StreetSegment` and `Addendum`.

## What the text covers

The contract as executed: the Standard Form Construction Contract
(Sec. 1–18, signatures, pre-audit certificate), Exhibit "A" (Scope / Fee),
Exhibit "G" (General Conditions and the unit-price Bid Form), Exhibit "H"
(Special Provisions), and Attachment 1 (Street Schedule). The Invitation to
Bid and the fill-in forms (Exhibits "B"–"F", "I", "J", the bidder signature
and debarment sheets) are incorporated by reference in Sec. 11 but not
reproduced.

Every blank in the package is an optional property whose absence renders
the original blank, so `sample.json` is the package exactly as issued — no
contractor, prices or streets — and drafting it reproduces the source (98.8%
word for word; every difference is a table restated as a list or
paragraphs). An award is the same instance with those properties filled in -- down to
the signature page (each signature, printed name, title and date, the Town
Attorney's approval, the Finance Officer's pre-audit certificate) and the
bidder's own figures on the Bid Form (each extended amount, the section
totals, the Total Bid and its words).

## Logic

None yet. The contract's executable core is the monthly pay request:
quantities installed and accepted at the unit prices, statutory retainage
(§ 143-134.1), liquidated damages, certified by the Director of Public Works
and approved by the Town Manager.

## Revisions (0.3.0)

The Town Attorney's memo on STR-27 (September 2026) proposed five revisions
and noted five drafting defects. This version carries the revisions and
four of the corrections; the same changes are marked as tracked changes in
[`sources/`](../../../../sources/) (`… Bid Package - Revisions (redline).docx`)
for review against the original. The memo's bracketed figures are data, not
text: change them per contract.

| | Where | Change | As data |
|---|---|---|---|
| R1 | GC 1 | *Engineer* and *Inspection Certificate* defined | |
| R2 | GC 1 | A Designated Representative is appointed in writing, naming the contract, the authority and the period | |
| R3 | GC 18 | (a)–(d): inspection by tier; an Inspection Certificate within the inspection period after each pay request; only approved quantities paid; progress approval is not final acceptance | `inspectionPeriodDays` (10) |
| R4 | SC-16 (new) | The verification tier and who verifies each kind of work | `verificationTier` (4), `verificationRequirement`, `verifiers` |
| R5 | GC 1 | *Certificate of Final Acceptance* defined; the warranty runs from its date | `finalAcceptancePeriodDays` (30) |

Drafting defects:

1. **Corrected.** The Notice to Proceed is Exhibit "J", and every citation
   (six, in the preamble, Sec. 5, Exhibit A, General Conditions 4 and the
   Bid Sheet) now says so.
2. **Corrected.** Sec. 11 is re-lettered (a)–(k).
3. **Open -- needs the Town's decision.** Unit prices hold for 365 calendar
   days under General Conditions 17, but until 270 days from the Notice to
   Proceed under the Bid Sheet. Left as written until one period is chosen.
4. **Corrected.** General Conditions 20 now matches Sec. 15(a): Rowan County
   state courts, never federal court.
5. **In the Word document only.** The Notice to Proceed (Exhibit J) states a
   narrower scope than Sec. 2 and Exhibit A. The form is not reproduced in
   this template; the redline corrects it.

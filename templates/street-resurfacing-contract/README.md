# Street resurfacing contract (STR-27)

The Town of China Grove's Street Resurfacing contract, Contract No. STR-27,
formally bid under N.C. Gen. Stat. § 143-129. Converted from the bid package
in [`sources/`](../../sources/China_Grove_Street_Resurfacing_Bid_Package.docx).

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
paragraphs). An award is the same instance with those properties filled in.

## Logic

None yet. The contract's executable core is the monthly pay request:
quantities installed and accepted at the unit prices, statutory retainage
(§ 143-134.1), liquidated damages, certified by the Director of Public Works
and approved by the Town Manager.

## Found in the source document

Preserved as written, not corrected:

1. The Notice to Proceed is Exhibit "J" (Sec. 11(k)), but the preamble,
   Sec. 5, Exhibit A, General Conditions 4 and the Bid Sheet all cite
   Exhibit "K", which does not exist.
2. Sec. 11's list skips (i): (h), then (j), (k), (l).
3. Unit prices hold for 365 calendar days under General Conditions 17, but
   until 270 days from the Notice to Proceed under the Bid Sheet.
4. General Conditions 20 allows the U.S. District Court (M.D.N.C.);
   Sec. 15(a) says no action shall be commenced in or removed to federal
   court.
5. The Notice to Proceed (Exhibit J) describes a narrower scope than Sec. 2
   and Exhibit A.

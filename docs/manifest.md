# Package manifests

A formally bid Town project is several instruments, not one contract. STR-27's
bid package holds the Invitation to Bid, the contract and its exhibits, bid
security, bonds, an insurance certificate, certifications, finance forms,
notices, and -- once work begins -- a pay request every month with its
inspection certificate. A **manifest** says which is which and how they fit.
A **matter** is the live instance of a package; its manifest becomes the
matter's overview.

`packages/<package>/manifest.edn`, typed by
`com.trustblocks.manifest@1.0.0.Manifest` (`shared/model/manifest.cto`).

## What a manifest says

| Part | What it holds |
|---|---|
| `terms` | The role of the document whose data every other document shares -- for STR-27, the contract |
| `phases` | The stages, in order: BID, AWARD, CONSTRUCTION, CLOSEOUT |
| `documents` | Each kind of document: its role and title, exhibit, **treatment**, who supplies it, how many (`ONE`, `ONE_PER_MONTH`, `ONE_PER_PAYEE`, `ANY`), its phase, whether it is required, the authority that receives it, the terms fields it reads, the documents it refers to, the facts kept from it, and the law it rests on |
| `milestones` | Points in the matter's life its documents do not date themselves: bid opening, award, final acceptance, release of retainage, end of warranty |
| `references` | The statutes, ordinances and standards the matter rests on, each with what it governs here |
| `intake` | The terms fields the Town gathers, grouped by phase -- the intake form |

Three **treatments**:

- **TERMS** -- one template whose data the matter's documents share.
- **TEMPLATE** -- a modelled document: its own template, data and, perhaps, a
  lifecycle (the pay request).
- **STORED** -- a received file with a receipt, and at most a few **fields**
  kept as data: a bond's surety and penal sum, a certificate's expiration
  date. Enough to ask "what is the performance bond's penal sum?" or to watch
  an insurance certificate expire, without modelling the whole form. Any
  stored document can become a template later.

## EDN, and JSON

The manifest is written in EDN, for people: kebab-case keys, and no `$class`
-- each part's type follows from where it sits. `bb build` writes the same
data as `manifest.json` in the model's own terms (camelCase, a `$class` on
every object) for Accord's tools and anything outside Clojure, into
`dist/<package>/` beside the package's archives. The matter page will switch
between the two.

## Checks

`bb check` holds a manifest to the model -- every key a property, every enum
value real, nothing required missing -- and to the package it describes:
exactly one TERMS document; unique roles; templates that exist, and every
package template accounted for; phases, cross-references and legal references
that resolve; and every terms field a document reads or the intake form asks
for, declared in the terms model. `bb conformance` has Concerto validate
`manifest.json`.

## STR-27, as drafted

| Phase | Document | Exhibit | Treatment | Supplied by | How many | Kept as data |
|---|---|---|---|---|---|---|
| BID | Invitation to Bid | | stored | Town | one | |
| BID | Bid security (bid bond or deposit) | F | stored | surety | one | form, amount (5%), surety |
| BID | Debarred Firms Certification | G | stored | Contractor | one | |
| AWARD | Notice of Award | I | stored | Town | one | Council action date |
| AWARD | **Contract** -- Sec. 1-18, Exhibits A, G (with the Bid Form), H, Attachment 1 | | **terms** | Town | one | (the terms) |
| AWARD | Performance Bond | F | stored | surety | one | surety, bond number, penal sum (100%), date |
| AWARD | Payment Bond | F | stored | surety | one | surety, bond number, penal sum (100%), date |
| AWARD | Certificate of Insurance | D | stored | insurer | any (renewals) | insurer, **expiration date (a deadline)** |
| AWARD | E-Verify Affidavit and Certifications | B | stored | Contractor | one | |
| AWARD | Tax Forms (W-9) | C | stored | Contractor | one | |
| AWARD | Vendor Information Form | E | stored | Contractor | one per payee | vendor number, payment terms, remit-to |
| CONSTRUCTION | Notice to Proceed | J | stored | Town | one | Notice to Proceed date |
| CONSTRUCTION | **Contractor Pay Request** | E | **template** | Contractor | one a month | (its own data, lifecycle, clause) |
| CONSTRUCTION | Inspection Certificate | | stored | Engineer | one a month | date inspected |
| CONSTRUCTION | Sales and Use Tax Report | E | stored | Contractor | one a month | |
| CONSTRUCTION | Change Order | | stored | Town | any | amount, days |
| CLOSEOUT | Notice of completion | | stored | Contractor | one | |
| CLOSEOUT | Certificate of Final Acceptance | | stored | Town | one | accepted date |

## For the Town to decide

1. **The vendor form** -- one per payee, normally only the Contractor, or
   also subcontractors or suppliers the Town pays directly?
2. **What is required when.** The draft requires the E-Verify affidavit, W-9
   and vendor form at award; the package does not say whether any come with
   the bid. The bid security is required at bid and irrelevant after award.
3. **Which stored documents become templates.** The Notices of Award and to
   Proceed are the first candidates: the Town issues them, so their data can
   be ours from the start, and the Completion Date should count from the
   signed Notice to Proceed rather than a date typed into the contract.
4. **The facts kept from each stored document** -- enough to answer the
   questions the Town actually asks, and no more.
5. **Legal references.** Twenty cited by the package are listed. STR-27 cites
   no ordinance; a subdivision or development agreement would cite the UDO,
   and those citations would resolve to the ordinance text in force on the
   date that matters (G.S. 160D-108).

## Next

The matter page made from the manifest: what is received and what is
missing, the timeline, deadlines, notes, references, and an EDN/JSON switch.
Then the intake form, generated from `intake` and the terms model.

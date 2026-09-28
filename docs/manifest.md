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

## Guides

`guides` point past the documents: for a topic -- bidding and award, bonds,
payment and retainage, the standards of the work, contractor certifications
-- the law that governs it, where it lives in the package, and the questions
worth asking. Guidance, not rules: nothing checks or acts on it. It is what
a person, an MCP client or an agent reads to know where to dig. For STR-27,
conformance with the NCDOT Standard Specifications is the Engineer's to
verify; the guide says so, and points at the Inspection Certificates.

## STR-27

With the Town Attorney's decisions of September 2026:

| Phase | Document | Exhibit | Treatment | Supplied by | How many | Received by |
|---|---|---|---|---|---|---|
| BID | Invitation to Bid | | stored | Town | one | |
| BID | **Bid Bond** (or a deposit, stored) | F | **template** | surety | one | |
| BID | Debarred Firms Certification | G | stored | Contractor | one | |
| AWARD | **Notice of Award and Acceptance of Notice** -- documents due in 14 days | I | **template** | Town | one | |
| AWARD | **Contract** -- Sec. 1-18, Exhibits A, G (with the Bid Form), H, Attachment 1 | | **terms** | Town | one | |
| AWARD | **Performance Bond**, **Payment Bond** | F | **template** | surety | one each | TOWN: who? |
| AWARD | Certificate of Insurance | D | stored, received only | insurer | any (renewals) | Finance Director |
| AWARD | E-Verify Affidavit and Certifications | B | stored, received only | Contractor | one | Finance Director |
| AWARD | IRS Form W-9 | C | stored, received only | Contractor | one per payee | Finance Director |
| AWARD | **Vendor Information Form** -- received, then vendor number assigned | E | **template, with a lifecycle** | Contractor | one per payee | Finance Director |
| CONSTRUCTION | **Notice to Proceed** -- the Completion Date counts from it | J | **template** | Town | one | |
| CONSTRUCTION | **Contractor Pay Request** -- its payment certificate names the payee's vendor number and terms | E | **template** | Contractor | one a month | Zoning Administrator (designee) |
| CONSTRUCTION | Inspection Certificate | | stored | Engineer | one a month | Zoning Administrator (designee) |
| CONSTRUCTION | Sales and Use Tax Report | E | stored | Contractor | one a month | |
| CONSTRUCTION | Change Order | | stored, amount and days kept | Town | any | |
| CLOSEOUT | Notice of completion | | stored | Contractor | one | |
| CLOSEOUT | Certificate of Final Acceptance | | stored, accepted date kept | Town | one | |

The Town's forms are modelled -- every blank a field -- and the forms of
others (the insurer's certificate, the IRS's W-9) are received, not modelled.

## Reading forms filled in by others

A vendor form or bond comes back from a third party. Accord's current engine
drafts documents from data but no longer parses a filled-in document back
into data (older Cicero did). Until it does, the data reaches the matter one
of three ways: the Town enters it from the received file into the form the
model generates, with the Finance Director's receipt certifying the file;
the third party fills the form itself, through a link, so the data arrives
already structured; or an agent reads the file and proposes the values for a
person to confirm.

## Still for the Town

1. **Bonds** -- who receives and approves them (the Town Attorney, as to
   form?), which becomes the bonds' receiving authority and lifecycle.
2. **Bid security** -- whether a deposit needs more than a receipt.
3. **The Notice to Proceed and the terms** -- pay requests read the Commence
   Date from the terms; the Notice now carries it too, and should be the
   source once matters are opened from the manifest.

## Next

The matter page made from the manifest: what is received and what is
missing, the timeline, deadlines, notes, references and guides, and an
EDN/JSON switch. Then the intake form, generated from `intake` and the terms
model.

# TrustBlocks model conventions

Our own models, extending Accord Project's registry with types native to
municipal government agreements.

## Namespaces

Reverse domain, from `trustblocks.com`:

| Namespace | Holds |
| --- | --- |
| `com.trustblocks.municipal.agreement@1.0.0` | `MunicipalAgreement`, the abstract base every agreement extends |
| `com.trustblocks.attestation@1.0.0` | `AttestationRequest` / `AttestationResponse`, the standard pair a contract's clause adopts to support officeholder attestation |
| `com.trustblocks.municipal.employment@1.0.0` | `ManagerEmployment`, the manager employment contract's root model, and its Board-approval Request/Response/State |
| `com.trustblocks.municipal.construction@1.0.0` | `StreetResurfacingContract`, a unit-price public works construction contract, with `BidItem`, `StreetSegment` and `Addendum` |

**One namespace of its own per template.** As in Accord's own template
library -- where 35 of 37 active templates declare exactly one -- a template's
root type, its Request/Response/State types and any concepts only it uses all
live in its own namespace. Everything else it needs is imported: Accord's
published models (`contract`, `runtime`, `usa.state`, ...) and, until they are
published at stable URLs of their own, our shared models vendored from
`shared/model/`. `bb check` enforces this.

**Every blank is a field.** A municipal contract is itself a form: every
blank in it -- a date, a name, a signature line, an amount the bidder
writes -- is a place for data, so every blank is an optional property whose
absence renders the original blank:

```
{{#optional contractorSignedDate}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_{{/optional}}
```

A blank is never left as text in a grammar, and never stored as data (a
`"____"` value in `sample.json`): the field is simply absent. `bb check`
enforces both. Figures a party writes that could be computed -- a Bid
Form's extended amounts and totals -- are still that party's data, because
the contract itself says which figure governs when they disagree.

**No jurisdiction in the namespace.** The manager employment model was
inherited as `us.nc.municipal.manageremployment@0.1.0`, which bakes North
Carolina into the type name and forces a copy of every model per state.
Jurisdiction is data -- `stateCode` is an `org.accordproject.usa.state.USState`
-- so one set of models serves any state. We only operate in NC today; the
namespace shouldn't decide that we always will.

## Reuse is by inheritance, not composition

The shared jurisdiction fields (`stateCode`, `stateName`, `countyName`,
`municipalityName`, `municipalityType`, `governingBodyName`) live once, on
`MunicipalAgreement`, and every agreement extends it:

```
ManagerEmployment -> MunicipalAgreement -> org.accordproject.contract.Contract
```

The instinct is a nested `o Jurisdiction jurisdiction` concept. That does not
work for anything that appears in prose: TemplateMark can only reach into a
nested concept with `{{#with}}`, which is block-level and injects paragraph
breaks even mid-sentence, and this contract uses `{{municipalityType}}` inside
sentences about twenty times. Concerto flattens a subclass into one tagged
object, so inherited properties stay addressable exactly as if declared
locally. Accord's own registry is built this way too -- `Contract`, `Clause`,
`SignatureClause extends Clause`. See `docs/templatemark-conformance.md`.

The practical rule: **if a field appears in template prose, it must be a
property of the root concept or inherited into it.** Composition is fine for
anything the text never names.

## Where the files live

In this repository. Each template under `templates/` is a complete Accord
template directory -- `package.json`, `model/`, `text/grammar.tem.md`,
`sample.json`, and for templates that execute, `logic/` and `request.json` --
and `bb build` zips each one into `dist/<name>@<version>.cta`.

A template's `model/` is self-contained: Accord's own imports are vendored
alongside as `@models.accordproject.org.*.cto`, the same layout Accord's own
`.cta` archives use, so an archive loads with no network. Our own shared
models are vendored the same way. `shared/model/` holds the canonical copy of
each -- `agreement.cto` (`MunicipalAgreement`) and `attestation.cto` (the
standard attestation Request/Response pair) -- and `bb check` fails if any
template's copy differs from it, because the registry that loads them is
global and a drifted copy would silently shadow the other.

Nothing is published over HTTP yet, so our own models import each other by
namespace with no `from` URL; that works because cicero-core loads every
`model/*.cto` in the archive. `models.trustblocks.com` (raw `.cto` at stable
URLs, mirroring `models.accordproject.org`) and `templates.trustblocks.com`
(the built `.cta` archives) remain the plan for when someone outside
Trustblocks needs to import them; this repository is what they would serve.

## Checking

- `bb check` -- structure, shared-model copies, logic files, and the
  CommonMark rules in `docs/templatemark-conformance.md`. Needs only babashka.
- `bb conformance` -- every template must load and draft through Accord's own
  toolchain. Needs `npm install` once. Run it after any model or grammar
  change.

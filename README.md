# TrustBlocks templates

Municipal contract templates in [Accord Project](https://accordproject.org)'s
format — Concerto models, TemplateMark text — with their executable logic
written in Clojure.

Each template is a real contract: the Town of China Grove, North Carolina's
own documents, modelled so the text can be drafted from data and the
contract's clauses can run. They build into standard `.cta` archives that
Accord Project's own tools load and draft, and that
[Trustblocks](https://github.com/TrustBlocks) runs.

| Template | What it is | Logic |
| --- | --- | --- |
| [`manager-employment-contract`](templates/manager-employment-contract/) | The Town Manager's employment contract (2025–2029, restated): council approval, severance, service-credit purchase | Board approval; officeholder attestation |
| [`street-resurfacing-contract`](templates/street-resurfacing-contract/) | Contract No. STR-27, a formally bid unit-price street resurfacing contract: 37 pay items, statutory retainage, liquidated damages | Not yet — pay requests are next |

## Layout

```
templates/<name>/          one Accord template directory each
  package.json             Accord metadata, plus a "trustblocks" section for executing templates
  model/*.cto              Concerto models, with every import vendored beside them
  text/grammar.tem.md      TemplateMark (CommonMark + pipe tables)
  sample.json              a contract instance to draft from
  logic/clause.clj         the clause, for templates that execute
  request.json             a request the clause answers, for templates that execute
shared/model/              canonical copies of models more than one template carries
sources/                   the source documents templates were derived from
docs/                      model conventions; what Accord's engine actually requires
```

## Clojure logic in an Accord archive

Accord's tools accept only `es6` or `typescript` as a template's
`accordproject.runtime`, and refuse to load an archive declaring anything
else. So every template here keeps an Accord-valid runtime there — which is
what lets Accord's tools load and draft it — and a template whose logic is
Clojure says so in a section of its own that Accord ignores:

```json
"accordproject": { "runtime": "typescript", "template": "contract", "cicero": "^2.0.0" },
"trustblocks":   { "runtime": "clojure", "logic": "logic/clause.clj" }
```

In Accord Project's tools such an archive drafts, validates, and shows its
model; Trustblocks runs its logic.

`logic/clause.clj` is one expression — the clause — evaluated in Trustblocks'
sandbox with four bindings: `data` (the contract), `request`, `state` (the
contract's state as of the request's own time, or `nil`), and `now` (when the
request takes effect, as an ISO-8601 string). It returns `{:response ...}`,
and `:state ...` when the contract's state changes, both Concerto instances;
it refuses by throwing. There is no clock, I/O or interop in the sandbox, so a
clause's answer depends only on its inputs and an execution can be replayed.

## Checking and building

Needs [babashka](https://babashka.org); `bb conformance` also needs Node.

```sh
bb check          # structure, logic, shared-model copies, CommonMark rules
bb build          # dist/<name>@<version>.cta for every template
npm install       # once, for the Accord reference toolchain
bb conformance    # every template, and every built archive, drafts through Accord's engine
```

## Using the templates from Clojure

`deps.edn` puts every template directory on the classpath under its own name:

```clojure
io.github.TrustBlocks/trustblocks-templates {:git/sha "..."}

(clojure.java.io/resource "manager-employment-contract/package.json")
```

See [`docs/model-conventions.md`](docs/model-conventions.md) for namespaces
and how shared models are reused, and
[`docs/templatemark-conformance.md`](docs/templatemark-conformance.md) for
what Accord's engine actually requires of a template.

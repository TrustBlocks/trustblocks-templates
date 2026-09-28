# The clause runtime

`runtime/` is what a template's `logic/clause.clj` runs in: the vocabulary a
clause may use, the lifecycle that decides whether an event may happen, and
the step that puts the two together. It is the code the Trustblocks app runs
on every request, and -- compiled to ClojureScript as `trustblocks-logic.js`
-- the code the Template Playground runs in the browser. A request gets the
same answer in both, to the character, because it is the same code, and the
parity test holds it to that.

| Namespace | What it is |
|---|---|
| `com.trustblocks.clause` | The sandbox: an [SCI](https://github.com/babashka/sci) context holding exactly the vocabulary below, `check` (compile without running) and `evaluate` |
| `com.trustblocks.lifecycle` | A template's `lifecycle.json`, applied: may this event happen from this status, and under which certification |
| `com.trustblocks.runtime` | One step: the lifecycle's transition, the clause, the clause's state merged under the new status, the status checked |
| `com.trustblocks.logic-bundle` | `trustblocks-logic.js`: the three above as one browser script |

## The vocabulary

A clause is evaluated with `data`, `request`, `state` and `now` bound. What it
may call is a whitelist -- SCI refuses any other symbol, before anything runs:

- **Special forms:** `if do fn fn* let* quote recur throw`
- **clojure.core:** `+ - * < > <= >= = not= not min max mod rem quot inc dec
  when when-not cond and or let if-let when-let get get-in contains? keys vals
  count empty? seq first second last nth map filter remove reduce some every?
  sort-by odd? even? pos? neg? zero? assoc dissoc merge select-keys update str
  name keyword vector hash-map list conj into apply nil? some? true? false?
  string? number? boolean? map? vector? ex-info ex-message -> ->>`
- **Host functions:** `divide`, `plus-days`, `days-between`, `before?`,
  `after?`, `duration-days`

Not there, on purpose: `/`, anything random, any clock, `eval`, `def`,
`atom`, I/O, interop. Map destructuring compiles to a host call, so it is
unavailable -- Concerto instances are string-keyed anyway; use `get`.

### Why the same answer everywhere takes rules

A clause runs on the JVM in the app and in JavaScript in the browser, and
the two disagree in ways a clause would notice:

- **Division.** `(/ 1 3)` is an exact ratio on the JVM and a float in
  JavaScript. There is no `/`; `(divide a b scale)` is exact decimal
  division to `scale` places, rounded half up -- `BigDecimal` on the JVM,
  BigInt arithmetic in JavaScript.
- **Numbers as text.** The JVM writes `6000.0` and `1.0E7` where JavaScript
  writes `6000` and `10000000`. The vocabulary's `str` writes every number
  as plain decimal text with no trailing `.0` on both: `"$" 6000.0` is
  `"$6000"`.
- **Numbers compared.** JavaScript cannot tell `6` from `6.0`, so the
  vocabulary's `=` and `not=` compare numbers by value on both.
- **Dates.** Dates are ISO-8601 strings in and out; a clause never holds a
  platform date. `java.time` on the JVM; in JavaScript the same arithmetic,
  written out. A date without a zone is UTC. Output is written as
  `DateTimeFormatter/ISO_OFFSET_DATE_TIME` writes it -- seconds always, a
  fraction only as long as it needs, `Z` for UTC. Text that is not a date
  is refused as `Not a date-time: <text>`.

## The step

`com.trustblocks.runtime/step` is the part of a request that is the
contract's own:

1. The lifecycle (`lifecycle.json`) decides whether the event may happen
   from the document's status, and whether the certification given is one
   the transition accepts -- refused as `no-transition` or
   `certification-required`.
2. The clause decides what follows, or refuses by throwing.
3. With a lifecycle, the lifecycle owns the status: the clause's state is
   merged over the state it was given, under the transition's new status,
   and a clause with nothing to say may leave the answer to the transition.
4. A clause that leaves any other status is refused.

What a host does around the step is its own: the app verifies who certified
it and under what authority, validates the answer against the model, and
records it; the playground simulates the certification.

## trustblocks-logic.js

`bb logic-bundle` builds `target/trustblocks-logic.js` -- one classic script
(no modules), because the Template Playground's sandboxed worker evaluates
it from its text. It defines one global:

```js
TrustblocksLogic.check(logic)                  // null, or why the vocabulary refuses it
TrustblocksLogic.nextSteps(lifecycle, state)   // [{event, to, requires}]
TrustblocksLogic.identityField(model, concept) // "payRequestId", from the CTO text
TrustblocksLogic.trigger({logic, lifecycle, model, data, request, state, now, certification})
// -> {ok: true, response, state, transition, certifiedAs, now}
//    {ok: false, code, error, requires?}          never throws
```

Without a `certification`, a step is certified the first way its transition
accepts, and `certifiedAs` says which -- the playground has no signer.
Without a `now`, the step is taken at the current time, and `now` says which.

## Tests

```sh
bb runtime-test     # JVM: the parity fixture
bb cljs-test        # the same test, compiled to ClojureScript, under Node
bb logic-bundle     # build the bundle; load it as the worker does and run a pay request received to paid
bb parity-fixtures  # regenerate the fixture from the JVM -- only when the vocabulary is meant to change
```

`runtime/test/com/trustblocks/clause_parity.edn` is 1,223 cases with the
answers the JVM gives them: generated date-times in every form, `divide`
over generated operands and scales, the number rules, and the templates'
own clauses, refusals included. Deterministic -- regenerating without a
change to the vocabulary changes nothing -- and one case to a line, so a
change to the vocabulary shows as a readable diff.

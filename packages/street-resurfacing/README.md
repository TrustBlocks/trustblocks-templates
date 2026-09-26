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
| [`pay-application`](templates/pay-application/) | `com.trustblocks.municipal.construction.payapplication@1.0.0` | Exhibit "E"'s Contractor Pay Request, one per month |

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

## Not yet

- Logic: the matter's contract-to-date position (quantity paid to date per
  pay item, retainage under N.C. Gen. Stat. § 143-134.1, liquidated damages),
  and the lifecycle each document moves through, as data.
- `model/`: the types these documents will share once pay requests execute.
  Nothing is shared yet -- each template's model is its own.

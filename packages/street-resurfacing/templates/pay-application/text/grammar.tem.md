# CONTRACTOR PAY REQUEST

Town of China Grove | 333 North Main Street | China Grove, North Carolina 28023

**PROJECT:** {{contractNumber}}

**DESCRIPTION:** Street Resurfacing, Contract No. {{contractNumber}}

**Date Notice to Proceed:** {{#optional noticeToProceedDate}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

**Completion Date:** {{#optional completionDate}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

**Days Remaining in Contract:** {{#optional daysRemaining}}{{this}}{{else}}\_\_\_\_\_\_{{/optional}}

**Percent Work Complete:** {{#optional percentWorkComplete}}{{this as "0,0.00"}}{{else}}\_\_\_\_\_\_{{/optional}}

**Percent Time Complete:** {{#optional percentTimeComplete}}{{this as "0,0.00"}}{{else}}\_\_\_\_\_\_{{/optional}}

**Percent Payment Complete:** {{#optional percentPaymentComplete}}{{this as "0,0.00"}}{{else}}\_\_\_\_\_\_{{/optional}}

**Application for Payment No.:** {{#optional applicationNumber}}{{this}}{{else}}\_\_\_\_\_\_{{/optional}} — Sheet No. {{#optional sheetNumber}}{{this}}{{else}}\_\_\_\_\_\_{{/optional}} of {{#optional sheetCount}}{{this}}{{else}}\_\_\_\_\_\_{{/optional}}

**Period From:** {{#optional periodFrom}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}} **To:** {{#optional periodTo}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

## SCHEDULE OF PAY ITEMS

*Added by Trustblocks for the Town Attorney's review. Sec. 6 of the Contract requires the Director or the Director's designated representative to verify quantities, and the General Conditions pay only for work completed and accepted. Each pay item for which payment is requested is listed with its quantities and the status of its inspection.*

{{#ulist payItems}}
**{{payItem}}** {{description}} ({{unit}}) — Contract Quantity: {{#optional contractQuantity}}{{this as "0,0.00"}}{{else}}\_\_\_\_\_\_\_\_{{/optional}} — Unit Price: {{#optional unitPrice}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}} — This Period: {{#optional quantityThisPeriod}}{{this as "0,0.00"}}{{else}}\_\_\_\_\_\_\_\_{{/optional}} ({{#optional amountThisPeriod}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}) — To Date: {{#optional quantityToDate}}{{this as "0,0.00"}}{{else}}\_\_\_\_\_\_\_\_{{/optional}} ({{#optional amountToDate}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}) — Completed {{#if completed}}\[X\]{{else}}\[ \]{{/if}} Approved by Engineer {{#if approvedByEngineer}}\[X\]{{else}}\[ \]{{/if}}
{{/ulist}}

## CERTIFICATE OF THE CONTRACTOR

To the best of my knowledge and belief, I certify that this periodical estimate is correct and that all work has been performed and materials supplied in full accordance with the terms and conditions of the contract documents between the undersigned Contractor and the Town of China Grove.

GROSS AMOUNT OF PARTIAL PAYMENT: {{#optional grossAmount}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

LESS: Retainage at {{#optional retainagePercent}}{{this as "0,0.00"}}{{else}}\_\_\_\_\_\_{{/optional}} percent: {{#optional retainageAmount}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

LESS: Previous payments: {{#optional previousPayments}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

LESS: Liquidated damages — {{#optional liquidatedDamagesDays}}{{this}}{{else}}\_\_\_\_\_\_{{/optional}} days @ {{#optional liquidatedDamagesPerDay}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}: {{#optional liquidatedDamagesAmount}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

LESS: Other deductions: {{#optional otherDeductions}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

TOTAL DEDUCTIONS: {{#optional totalDeductions}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

NET AMOUNT DUE THIS ESTIMATE: {{#optional netAmountDue}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

Name of Contractor: {{#optional contractorName}}{{this}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

Address: {{#optional contractorAddress}}{{this}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

Signed: {{#optional contractorSignature}}{{this}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

Title: {{#optional contractorSignatoryTitle}}{{this}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}} Date: {{#optional contractorSignedDate}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

## APPROVED AND PAYMENT RECOMMENDED — TOWN OF CHINA GROVE

Director of Public Works: {{#optional publicWorksDirectorSignature}}{{this}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}} Date: {{#optional publicWorksDirectorSignedDate}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

Town Manager: {{#optional managerSignature}}{{this}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}} Date: {{#optional managerSignedDate}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

## PAYMENT CERTIFICATE

*Added by Trustblocks for the Town Attorney's review.*

I certify that payment of {{#optional amountPaid}}${{this as "0,0.00"}}{{else}}\$\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}} was made to the Contractor on {{#optional paidDate}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}} on this Pay Request, by check or electronic funds transfer No. {{#optional paymentReference}}{{this}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}.

Finance Officer: {{#optional financeOfficerSignature}}{{this}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}} Date: {{#optional financeOfficerSignedDate}}{{this as "MMMM D, YYYY"}}{{else}}\_\_\_\_\_\_\_\_\_\_\_\_{{/optional}}

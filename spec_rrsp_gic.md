# RRSP & GIC Feature Specification

This document is the unified specification for the RRSP and GIC feature, covering both the backend business rules and API contract and the frontend UI/UX behaviour.

**Feature Branch:** spec/rrsp-gic
**Status:** Draft
**User Story:** "As a customer, I want to open an RRSP account and invest in GICs so that I can grow my retirement savings securely."

---

## 1. Overview

An RRSP (Registered Retirement Savings Plan) is a registered account type implemented as an extension of the existing Account model. GIC (Guaranteed Investment Certificate) investments are separate financial instruments linked to RRSP accounts.

Key characteristics:

- A customer may hold only one active RRSP account at a time.
- KYC (identity verification) must be completed before an RRSP can be created.
- The opening balance and interest rate are not supplied by the customer. The backend always receives a balance of zero and a fixed default rate.
- RRSP accounts cannot be deleted through the normal delete flow. They are closed via a dedicated close endpoint.
- An RRSP account acts as a container for GIC investments, managed from the account detail page.
- GIC funds are locked until maturity; on maturity the principal and earned interest are credited back to the RRSP balance.

---

## 2. Scope

### In Scope

- Creating RRSP accounts using the existing accounts API
- Enforcing RRSP-specific validation rules (KYC, one-per-customer)
- Creating GIC investments linked to RRSP accounts
- Deducting and restoring balance through the GIC lifecycle
- Tracking GIC maturity lifecycle (ACTIVE → MATURED)
- Viewing RRSP accounts with associated GIC investments in the frontend
- Early GIC redemption via the frontend

### Explicitly Excluded

- A separate RRSP entity, table, or service/controller
- Tax calculations or contribution limits
- Withdrawal rules or penalties
- RRIF conversion
- External investment products (stocks, mutual funds)

---

## 3. Actors and Preconditions

**Actors:**

- **CUSTOMER** — can operate only on accounts they own
- **ADMIN** — has full access across all customers

**Preconditions:**

- The user is authenticated via JWT.
- For RRSP creation: the customer must have completed KYC and must not already have an active RRSP account.
- For GIC creation: the RRSP account must exist and have sufficient balance.

---

## 4. Account Creation

### 4.1 Backend Rules

- RRSP is a valid value for the existing `accountType` field on the Account entity.
- The system validates KYC status before creating the account.
- The system rejects the request if the customer already has an active RRSP.
- On success, the account is created with status ACTIVE and becomes visible in the account list.

**Endpoint:** `POST /customers/{customerId}/accounts`

| Field | Value sent to backend |
|-------|-----------------------|
| `accountType` | `"RRSP"` |
| `balance` | Always `0` — not user-configurable |
| `interestRate` | Always `0.5` — fixed default, not user-configurable |

### 4.2 Frontend — Trigger

A **"Create Account"** button appears in the top-right corner of the My Accounts panel header. It is hidden when there is an active error on the accounts or customer query. Clicking it opens the Create Account modal and clears any prior error or action message.

### 4.3 Frontend — Modal Layout

The modal overlays the full screen. Clicking outside the modal panel closes it. All form fields are arranged in a single vertical column. The modal cannot be closed while a create request is in flight.

### 4.4 Frontend — Inline Validation Banners

The following banners can appear inside the modal form, above the fields:

1. **General / API error** — shown when the form is submitted and rejected (duplicate type or API error). Suppressed from the page-level banner area while the modal is open.
2. **Duplicate RRSP** — "You already have an active RRSP account." Shown when the customer already holds an RRSP. Enforced on submit only, not on type selection.

The Submit button is disabled while an error condition is active or a request is in flight.

### 4.5 Frontend — Form Fields

#### Account Type Selector

All supported account types are listed in a dropdown. When RRSP is selected, a hint line below the dropdown shows the current RRSP rate fetched from `GET /api/interest-rates`, e.g. "Current RRSP rate: 0.50% APY". The hint is hidden if no rate is available.

#### Opening Balance

Hidden when RRSP is selected. The backend always receives zero regardless of user input.

#### Interest Rate

Hidden when RRSP is selected. The backend receives a fixed default rate automatically; the customer cannot configure it.

---

## 5. RRSP Account Detail

### 5.1 Account Overview Panel

The standard overview panel shows account type and balance. For RRSP accounts, the Delete Account button is replaced by a **"Close RRSP"** button. Closing the account calls `POST /accounts/{accountId}/close` and redirects the customer to their account list with a success flash message.

### 5.2 GIC Portfolio Section

The GIC Portfolio section appears below the overview panel on RRSP account pages only. It has a visually distinct background to differentiate it from the standard panel.

The section header contains:
- Title: "GIC Portfolio"
- Subtitle: "Guaranteed Investment Certificates linked to this RRSP."
- **"Open GIC from this Account"** button (top-right of the header)

GIC data is fetched from the backend only when viewing an RRSP account. The response is normalised to an array: a top-level array is used directly; if the response is an object with a `gics` array property, that property is used instead.

---

## 6. GIC Business Rules

- Only RRSP accounts can hold GICs.
- Only one ACTIVE GIC per RRSP account at a time.
- The GIC principal is deducted from the RRSP balance on creation.
- Funds are locked until maturity; the GIC is non-redeemable before maturity under normal rules (see §9 for early redemption via the frontend).
- The interest rate is fixed for the duration of the term.
- Maturity amount = principal + accrued interest.
- On maturity: the GIC status changes to MATURED and the maturity amount is credited back to the RRSP balance.

---

## 7. GIC Data Model

| Field | Description |
|-------|-------------|
| GIC ID | Unique identifier |
| Account ID | Foreign key to the parent RRSP account |
| Principal Amount | Amount invested |
| Interest Rate | Fixed rate for the term |
| Term | Duration enum (see §10) |
| Start Date | Date the GIC was opened |
| Maturity Date | Calculated from start date and term |
| Maturity Amount | Principal + interest |
| Status | ACTIVE or MATURED |

---

## 8. GIC Table (Frontend)

When the customer has active GICs, they are displayed in a table.

### 8.1 Columns

| Column | Description |
|--------|-------------|
| GIC ID | Unique identifier for the GIC. |
| Amount | The principal amount, prefixed with `$`. The `principalAmount` field is preferred; `amount` is used as a fallback. |
| Interest Rate | The locally defined rate for the term (see §10) takes precedence over any rate returned by the API. Displayed with two decimal places and a `%` suffix. Shows `—` if no rate is resolvable. |
| Term | Human-readable term label (e.g. "1 Year Term"). Falls back to the raw enum value if no label is defined. |
| Status | The GIC's current status, displayed as a green pill badge. |
| Actions | A "Redeem" button for early redemption. Disabled while a redemption request is in flight. |

### 8.2 Table Styling

The table is wrapped in a container with a white background, 12px border radius, a single-pixel border, and hidden overflow to clip corners cleanly. Numeric columns (Amount, Interest Rate) are right-aligned. The Redeem button gains an accent-coloured border and text on hover when not disabled.

---

## 9. GIC Empty State (Frontend)

When no GICs exist, a centred panel is shown instead of the table. It displays the message "No active GIC investments for this RRSP." and an "Open your first GIC" button that opens the GIC modal.

---

## 10. Open GIC Modal (Frontend)

### 10.1 Positioning

The modal is rendered as a sibling of the RRSP portfolio section, not nested inside it, to ensure the full-screen backdrop is positioned correctly.

### 10.2 Layout

The modal overlays the full screen. Clicking outside the panel closes it. Fields are arranged in a single vertical column.

### 10.3 Form Fields

- **Amount** — the amount to invest. Hint reads "Will be deducted from your RRSP balance."
- **Term** — a dropdown listing the five available terms (see §12). Each option displays the term label and its rate together, e.g. "1 Year Term with 3.50%".

### 10.4 Actions

- **Open GIC** — submits the form. Disabled while a request is in flight.
- **Reset** — clears the form fields without closing the modal.

### 10.5 Success

On success, the modal closes, the form resets, and both the GIC list and the account balance are refreshed. A success banner is shown in the portfolio section.

### 10.6 Error

On failure, an error banner is shown inside the modal above the form.

---

## 11. GIC Redemption (Frontend)

Clicking the Redeem button on a GIC row triggers an early redemption request. On success, funds are returned to the RRSP balance, and both the GIC list and account balance refresh. A success banner is shown in the portfolio section.

---

## 12. API Endpoints

| Action | Method | Path |
|--------|--------|------|
| Create an RRSP account | POST | `/customers/{customerId}/accounts` |
| List GICs for an RRSP | GET | `/accounts/{accountId}/gic` |
| Open a new GIC | POST | `/accounts/{accountId}/gic` |
| Redeem a GIC | POST | `/accounts/{accountId}/gic/{gicId}/redeem` |
| Close an RRSP account | POST | `/accounts/{accountId}/close` |

When opening a GIC, the request body contains the investment amount and a term enum value from the table in §13.

---

## 13. GIC Term Reference

The following are the only valid term values. Interest rates are defined as frontend constants and take precedence over any rate returned by the API.

| Term Value    | Display Label  | Interest Rate |
|---------------|----------------|---------------|
| `SIX_MONTHS`  | 6 Month Term   | 3.00%         |
| `ONE_YEAR`    | 1 Year Term    | 3.50%         |
| `TWO_YEARS`   | 2 Year Term    | 4.00%         |
| `THREE_YEARS` | 3 Year Term    | 4.50%         |
| `FIVE_YEARS`  | 5 Year Term    | 5.00%         |

---

## 14. Non-Functional Requirements

- 95% of operations must complete within 2 seconds.
- All operations must be authenticated (JWT).
- All financial operations must be auditable.
- High-precision decimal handling is required for all monetary values.
- System must maintain 99.5% availability.
- RBAC must be enforced across all endpoints.

---

## 15. Error Conditions

| Code | Condition | Trigger |
|------|-----------|---------|
| 401 | Unauthorized | Request made without valid authentication |
| 404 | Customer not found | Invalid customer ID |
| 400 | Account type not supported | Invalid `accountType` value |
| 422 | KYC not verified | RRSP creation attempted without KYC |
| 409 | RRSP_ALREADY_EXISTS | Customer already has an active RRSP |
| 400 | Insufficient funds | GIC principal exceeds RRSP balance |
| 400 | INVALID_ACCOUNT_TYPE_FOR_GIC | GIC creation attempted on a non-RRSP account |
| 409 | ACTIVE_GIC_ALREADY_EXISTS | Customer already has an active GIC on this RRSP |
| 503 | System unavailable | Backend service error |

**Frontend error display:**

| Scenario | Where Displayed |
|----------|-----------------|
| Customer already has an RRSP (client-side check) | Error banner inside the Create Account modal |
| Any API error on account creation | Error banner inside the Create Account modal |
| Error opening a GIC | Error banner inside the GIC modal |
| Error redeeming a GIC | Error banner in the GIC portfolio section |
| Error loading account data | Page-level banner stack, outside any modal |
| Error closing the RRSP | Page-level error banner |

---

## 16. Acceptance Criteria

**Scenario 1 — RRSP Account Creation (happy path)**
Given a KYC-verified customer with no existing RRSP, when they submit the Create Account form with type RRSP, then an ACTIVE RRSP account is created and appears in their account list.

**Scenario 2 — GIC Creation (happy path)**
Given an ACTIVE RRSP account with sufficient balance, when a valid GIC request is submitted, then the principal is deducted from the RRSP balance, a GIC linked to the account is created with status ACTIVE, and the frontend GIC table updates.

**Scenario 3 — GIC Maturity**
Given a GIC has reached its maturity date, when the system processes maturity, then the GIC status becomes MATURED and the maturity amount (principal + interest) is credited back to the RRSP balance.

**Scenario 4 — KYC Not Verified**
Given an unverified customer, when RRSP creation is attempted, then the backend returns 422 and the frontend shows an appropriate error.

**Scenario 5 — RRSP Already Exists**
Given a customer who already has an active RRSP, when they attempt to create another, then submission is blocked by the client-side duplicate guard and, if bypassed, the backend returns 409.

**Scenario 6 — Insufficient Funds for GIC**
Given an RRSP balance lower than the requested GIC principal, when the form is submitted, then the backend returns 400 (insufficientFunds) and the frontend shows an error banner inside the GIC modal.

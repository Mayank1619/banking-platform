# Project Deep Dive

Last reviewed locally: 2026-06-22

This document explains what the banking platform does, how the frontend and
backend fit together, and how security is currently implemented. It is meant to
help new contributors understand the product and the important code paths before
they begin changing features.

## 1. Product Summary

This repository contains a full-stack digital banking platform. The frontend is
a React/Vite app branded as Voltio. The backend is a Spring Boot API that owns
authentication, customer records, bank accounts, monetary operations, statements,
standing orders, savings goals, GICs, spending insights, and notification
evaluation.

The product is designed around two main user groups:

- Customers, who manage their own profile, accounts, money movement, statements,
  goals, insights, and standing orders.
- Admins, who can see broader system data and perform account control actions
  such as freezing or unfreezing accounts.

The merged `main` branch now contains the frontend work from `feature/frontend`
and backend work from `feature/group123`. The `onboarding` branch contains living
project documentation that can continue to evolve separately.

## 2. Architecture At A Glance

```text
Browser
  |
  | React/Vite frontend
  | - routes, layouts, auth state, API clients
  | - stores JWT session data in localStorage
  v
Spring Boot backend
  |
  | Spring MVC controllers
  | Spring Security JWT filter
  | service-layer business rules
  | Spring Data JPA repositories
  v
Local H2 database in development
```

Frontend:

- Source lives under `src/`.
- `src/App.jsx` defines routes, protected routes, admin-only routes, and the
  authenticated application shell.
- `src/auth/` stores auth context, public/protected route guards, admin route
  guards, and localStorage-backed auth state.
- `src/api/axiosClient.js` creates Axios clients, attaches bearer tokens, maps
  backend errors, and logs users out on session-expiring `401` responses.
- `src/pages/` contains route-level screens.
- `src/hooks/` wraps backend calls in React Query hooks.

Backend:

- Source lives under `backend/src/main/java/com/group1/banking/`.
- Controllers expose the HTTP API.
- Services contain most business rules and ownership checks.
- Repositories use Spring Data JPA.
- Security uses stateless JWTs with Spring Security method authorization.
- Local development uses a file-backed H2 database by default.

Development/spec assets:

- `specs/` and `SpecFiles/` hold feature specifications.
- `.specify/`, `.github/agents/`, and `.github/prompts/` contain the Spec Kit
  workflow assets used to drive implementation from specs, tasks, and plans.

## 3. Main Feature Areas

### Authentication

Authentication is implemented in the backend under `AuthController`,
`AuthServiceImpl`, `JwtService`, `JwtAuthenticationFilter`, and the Spring
Security configuration.

Current capabilities:

- Register a user with username, password, and roles.
- Default new users to the `CUSTOMER` role when no role is supplied.
- Normalize usernames to lowercase.
- Hash passwords with BCrypt.
- Login with username and password.
- Reject login for inactive users.
- Return access and refresh JWTs.

Important current detail:

- The registration service accepts requested roles if they are sent in the
  request. If registration remains public, this should be tightened before a
  production release so public callers cannot self-assign elevated roles.

### Customer Management

Customer management is exposed through `/api/customers`.

Current capabilities:

- Create a customer profile.
- Link a newly created customer profile to the authenticated user when that user
  does not already have a `customerId`.
- View a customer profile.
- Patch editable customer fields.
- Admin list of all customers.
- Admin soft delete of customers.

Business rules:

- Customers can read and update only their own customer profile.
- Admins can read, list, update, and delete across customers.
- Customer delete is blocked while active accounts still exist.
- Some sensitive fields are explicitly blocked from patch requests in the
  service layer.

### Account Management

Account management is exposed through routes such as `/accounts`,
`/accounts/{accountId}`, and `/customers/{customerId}/accounts`.

Account types:

- `CHECKING`
- `SAVINGS`
- `TFSA`
- `RRSP`

Account statuses:

- `ACTIVE`
- `FROZEN`
- `CLOSED`

Current capabilities:

- Create accounts for a customer.
- List a customer's accounts.
- Admin list of all accounts.
- View account details.
- Update account details.
- Close accounts.
- Delete eligible accounts.
- Admin freeze and unfreeze of accounts.
- Admin view of account control history.

Business rules:

- Customers can act only on accounts owned by their linked customer profile.
- Admins can act across customers and accounts.
- Closed accounts are hidden or restricted in most customer-facing flows.
- Closing or deleting an account requires a zero balance.
- Frozen accounts block withdrawals and outgoing transfers.
- TFSA creation requires the customer to be at least 18 and KYC verified.
- TFSA and RRSP accounts are limited to one active account of each type per
  customer.
- RRSP accounts cannot be closed while active GICs exist.
- Savings accounts can carry an interest rate; checking accounts cannot.

### Deposits, Withdrawals, And Transfers

Money movement is implemented by `MonetaryOperationService` and exposed through
account controller endpoints.

Current capabilities:

- Deposit to an account.
- Withdraw from an account.
- Transfer between accounts.
- Persist transaction records.
- Return operation results with success or business error details.

Business rules:

- Requests require authentication.
- Customers can operate only on their own accounts.
- Admins can perform operations across accounts.
- Withdrawals and outgoing transfers require sufficient funds.
- Withdrawals and outgoing transfers are blocked for frozen accounts.
- `Idempotency-Key` is required for monetary operations.
- Idempotency records prevent repeated operations for the same user and key.
- A scheduled purge job removes old idempotency records.

### Transaction History And PDF Export

Transaction history is exposed through `/accounts/{accountId}/transactions`.

Current capabilities:

- View transaction history for an account.
- Filter by date range.
- Export transaction history as a PDF.
- Include transaction metadata such as category and idempotency key.

Business rules:

- Requires authenticated access and customer-read authority.
- Customers can view only their own account history.
- Admins can view any account history.
- Date ranges are validated.
- The default history window is the last 28 days.
- Date ranges cannot exceed one year.
- Closed-account history is retained only inside the configured retention window.
- PDF exports are cached by a hash of the request context.

### Monthly Statements

Monthly statements are exposed through `/accounts/{accountId}/statements/{period}`.

Current capabilities:

- Generate a monthly statement PDF on demand.
- Use live transaction data for the requested month.
- Cache generated statement PDFs.

Business rules:

- Period format is `yyyy-MM`.
- Future months are rejected.
- Customers can generate statements only for owned accounts.
- Admins can generate statements for any account.

### Spending Insights

Spending insights are exposed through `/accounts/{accountId}/insights` and
transaction recategorization through
`/accounts/{accountId}/transactions/{transactionId}/category`.

Current capabilities:

- Summarize spending for a selected month.
- Group spending by category.
- Identify top transactions.
- Return a six-month trend.
- Auto-categorize transactions through backend category rules.
- Allow users to recategorize transactions.

Allowed categories:

- Housing
- Transport
- Food & Drink
- Entertainment
- Shopping
- Utilities
- Health
- Income

Business rules:

- Requires authenticated access.
- The service accepts `CUSTOMER_READ`, `INSIGHTS:READ`, or admin role. The
  current principal builder grants `CUSTOMER_READ` but does not define a
  dedicated `INSIGHTS:READ` permission.
- Customers can inspect only their own accounts.
- Admins can inspect any account.
- Invalid or future months are rejected.
- Only valid categories are accepted during recategorization.

### Standing Orders

Standing orders are exposed through:

- `POST /accounts/{accountId}/standing-orders`
- `GET /accounts/{accountId}/standing-orders`
- `DELETE /standing-orders/{standingOrderId}`
- `GET /test-standing-order`

Frequencies:

- `DAILY`
- `WEEKLY`
- `MONTHLY`
- `QUARTERLY`

Statuses:

- `ACTIVE`
- `CANCELLED`
- `LOCKED`
- `TERMINATED`
- `RETRY_PENDING`
- `FAILED_INSUFFICIENT_FUNDS`

Current capabilities:

- Create recurring transfers.
- List standing orders for an account.
- Cancel a standing order.
- Execute scheduled orders.
- Retry insufficient-funds orders.
- Emit internal notification events for final standing-order failure.

Business rules:

- Create requires customer-create authority and account ownership.
- List requires customer-read authority and account ownership.
- Cancel requires customer-update authority and ownership.
- Start date must be at least 24 hours in the future.
- End date must be after start date.
- Payee account must exist.
- Amount must fit inside transfer limits.
- Duplicate active orders are blocked.
- Cancellation is blocked within 24 hours of the next run.
- Scheduling is enabled by `SchedulerConfig`.
- The main execution job runs daily at 00:00:01 UTC.
- Retry attempts run at 08:00 UTC and 16:00 UTC.
- A Canadian holiday service is used when advancing run dates.

### Savings Goals

Savings goals are exposed under `/api/goals`.

Current capabilities:

- Create a savings goal for an account.
- Read a goal by account.
- List goals by customer.
- Update a goal.
- Soft delete a goal.
- Calculate derived goal status and progress on read.

Statuses:

- `NOT_STARTED`
- `IN_PROGRESS`
- `ACHIEVED`
- `OVERDUE`

Business rules:

- Requests require authentication.
- Account ownership is validated in the service layer.
- Only one active goal is allowed per customer/account.
- Target amount must be greater than zero.
- Target date must be today or in the future.
- Goal name is required.
- Progress is derived from the current account balance and capped at 100%.
- Delete is soft delete through `deletedAt`.

### GICs

GIC endpoints are exposed under `/accounts/{accountId}/gic`.

Current capabilities:

- Create a GIC investment.
- List GICs for an account.
- Redeem a GIC.

GIC terms:

- `SIX_MONTHS` at 3.00 percent annual rate.
- `ONE_YEAR` at 5.00 percent annual rate.
- `TWO_YEARS` at 5.50 percent annual rate.
- `THREE_YEARS` at 6.00 percent annual rate.
- `FIVE_YEARS` at 7.00 percent annual rate.

Statuses:

- `ACTIVE`
- `REDEEMED`

Business rules:

- Requests require authentication.
- Customers can manage GICs only for owned accounts.
- Admins can manage GICs across accounts.
- GICs can be created only from active RRSP accounts.
- Creating a GIC deducts principal from the RRSP balance.
- Maturity amount is calculated from principal, annual rate, and term length.
- Redeeming a GIC credits the maturity amount back to the account.

### Notifications

Notifications are exposed through `/notifications/evaluate`.

Current capabilities:

- Evaluate whether a notification should be raised for an event.
- Store notification decisions.
- Audit evaluation results.
- Internal services can evaluate events directly through
  `NotificationEvaluationService.evaluateInternal`.

Event behavior:

- Mandatory events, such as `StandingOrderFailure` and
  `UnusualAccountActivity`, are raised even when customers opt out.
- Optional events, such as `StatementAvailability` and
  `StandingOrderCreation`, respect customer notification preferences.
- Missing preferences default to allowing optional notifications.
- Duplicate event IDs are rejected.
- Unknown event types are rejected.
- Customer/account linkage is validated.

Important current detail:

- `NotificationController` has a comment saying its security context is
  populated by a `ServiceApiKeyFilter`, but no such filter is present in the
  current source. Under the active `SecurityConfig`, `/notifications/evaluate`
  is authenticated like any other non-public endpoint and expects a JWT unless a
  future service-key security chain is added.

## 4. Frontend Experience

The frontend is route-driven and uses React Router.

Public routes:

- `/`
- `/login`
- `/register`
- `/password-reset`

Admin-only routes:

- `/admin/customers`
- `/admin/accounts`

Authenticated customer routes:

- `/customer/create`
- `/customer/:customerId`
- `/customer/:customerId/edit`
- `/customer-profile`
- `/customer/:customerId/accounts`
- `/customer/:customerId/accounts/create`
- `/accounts/:accountId`
- `/accounts/:accountId/edit`
- `/accounts/:accountId/deposit`
- `/accounts/:accountId/withdraw`
- `/accounts/:accountId/transactions`
- `/accounts/:accountId/standing-orders`
- `/accounts/:accountId/statements`
- `/accounts/:accountId/insights`
- `/accounts/transfer`

Frontend auth behavior:

- `AuthProvider` reads and writes auth state through localStorage.
- `isAuthenticated` is true when an access token exists and has not expired.
- `isAdmin` is true when roles include `ADMIN` or `ROLE_ADMIN`.
- Axios request interceptors attach `Authorization: Bearer <token>`.
- Axios response interceptors clear auth state and redirect to login on relevant
  `401` responses.
- `403` responses do not log the user out because the user is authenticated but
  lacks permission.
- Admin and protected routes are enforced in the UI, but backend authorization is
  the source of truth.

Important frontend navigation patterns:

- Admin users get admin navigation for customers and accounts.
- Customer users get navigation for profile, accounts, transfers, transactions,
  monthly statements, spending insights, and standing orders.
- Some account-specific feature routes use a feature guard and account picker so
  users choose an account before navigating to transaction, statement, insight,
  or standing-order screens.

## 5. Spring Security Implementation

Security is configured in `SecurityConfig`.

Core behavior:

- `@EnableWebSecurity` enables Spring Security.
- `@EnableMethodSecurity` enables annotations such as `@PreAuthorize`.
- CSRF is disabled because the application uses stateless bearer tokens.
- Session creation policy is `STATELESS`.
- CORS is enabled through `CorsConfig`.
- `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`.
- A BCrypt password encoder is registered as the `PasswordEncoder`.
- Authentication failures return a JSON `401` response.
- Authorization failures return a JSON `403` response.

Public backend paths:

- `/api/auth/**`
- `/v3/api-docs/**`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/h2-console/**`
- `/actuator/health`

All other backend paths require authentication.

JWT flow:

1. Login succeeds in `AuthServiceImpl`.
2. `JwtService` generates access and refresh tokens.
3. Token subject is the backend user UUID.
4. Token claims include `roles` and `customerId`.
5. The client sends the access token in `Authorization: Bearer <token>`.
6. `JwtAuthenticationFilter` validates the token signature and expiry.
7. The filter loads the user from the database by UUID.
8. A `CustomUserPrincipal` is created from the database user.
9. Spring Security receives a `UsernamePasswordAuthenticationToken` containing
   the principal and authorities.

Token configuration:

- `jwt.secret` defaults to a development fallback in `application.properties`.
- `jwt.access-token-expiry` is currently `3600` seconds.
- `jwt.refresh-token-expiry` is currently `604800` seconds.

Production note:

- Production environments should provide a strong `JWT_SECRET` and avoid relying
  on the development fallback secret.

## 6. Roles, Authorities, And Ownership

Role enum:

```text
CUSTOMER
ADMIN
```

Permission enum:

```text
CUSTOMER_CREATE
CUSTOMER_READ
CUSTOMER_UPDATE
CUSTOMER_DELETE
```

Authorities are built in `CustomUserPrincipal`.

| Role | Granted role authority | Granted permissions |
| --- | --- | --- |
| `CUSTOMER` | `ROLE_CUSTOMER` | `CUSTOMER_CREATE`, `CUSTOMER_READ`, `CUSTOMER_UPDATE` |
| `ADMIN` | `ROLE_ADMIN` | `CUSTOMER_CREATE`, `CUSTOMER_READ`, `CUSTOMER_UPDATE`, `CUSTOMER_DELETE` |

The current permission model is intentionally small. Many domain features reuse
the customer permissions rather than defining feature-specific permissions for
transactions, statements, insights, standing orders, goals, or GICs.

### Customer Role

A customer can generally:

- Create and maintain their own customer profile.
- View and update their own customer record.
- Create accounts for their own linked customer profile when service rules pass.
- View and manage their own accounts.
- Deposit into, withdraw from, and transfer from owned accounts when business
  rules pass.
- View transaction history for owned accounts.
- Export transaction history PDFs for owned accounts.
- Generate monthly statements for owned accounts.
- View spending insights for owned accounts.
- Recategorize transactions for owned accounts.
- Create, list, and cancel standing orders for owned accounts.
- Create, view, update, and delete savings goals for owned accounts.
- Manage GICs on owned RRSP accounts.

The customer role is still constrained by ownership. Having
`CUSTOMER_READ` or `CUSTOMER_UPDATE` does not give a customer access to another
customer's profile or account.

### Admin Role

An admin can generally:

- List all customers.
- Read and manage customer records.
- Delete customers when delete rules pass.
- List all accounts.
- View account details across customers.
- Update accounts across customers.
- Freeze and unfreeze accounts.
- View account control history.
- Perform monetary operations across accounts.
- View transaction history and statements across accounts.
- View insights across accounts.
- Manage standing orders, savings goals, and GICs across accounts where the
  service layer grants admin bypass.

Admin access is implemented in two ways:

- Controller-level `@PreAuthorize("hasRole('ADMIN')")` on admin-only endpoints.
- Service-level ownership checks that return early when the principal is admin.

### Ownership Model

The system uses both user identity and customer identity:

- `User.userId` is a UUID and becomes the JWT subject.
- `User.customerId` links the login user to a customer profile.
- A customer's accounts are linked through the customer domain entity.

Ownership checks happen in multiple places:

- `OwnershipService.canAccessCustomer(authentication, customerId)` supports
  method-security checks in customer endpoints.
- `OwnershipValidator.assertOwnership(accountId, principal)` validates account
  ownership for transaction, statement, and insight services.
- Account and monetary services read the authenticated principal from the
  security context and compare the principal customer ID to the account customer.

The practical rule is simple:

- Admins can cross customer boundaries.
- Customers cannot cross customer boundaries.

## 7. Endpoint Access Summary

| Area | Endpoint examples | Main access rule |
| --- | --- | --- |
| Auth | `/api/auth/register`, `/api/auth/login` | Public |
| Health/docs/dev | `/actuator/health`, `/swagger-ui/**`, `/h2-console/**` | Public in current config |
| Customers | `/api/customers/**` | Customer permissions plus ownership, or admin |
| Account list | `GET /accounts` | Admin only |
| Customer accounts | `/customers/{customerId}/accounts` | Authenticated; service enforces ownership/admin |
| Account detail/update/delete | `/accounts/{accountId}` | Authenticated; service enforces ownership/admin |
| Account freeze/unfreeze | `/accounts/{accountId}/freeze`, `/unfreeze` | Admin only |
| Account control history | `/accounts/{accountId}/control-history` | Admin only |
| Money movement | `/accounts/{accountId}/deposit`, `/withdraw`, `/accounts/transfer` | Authenticated; ownership/admin plus business rules |
| Transactions | `/accounts/{accountId}/transactions` | Customer-read plus ownership, or admin |
| Statements | `/accounts/{accountId}/statements/{period}` | Customer-read plus ownership, or admin |
| Insights | `/accounts/{accountId}/insights` | Customer-read or admin, plus ownership |
| Standing orders | `/accounts/{accountId}/standing-orders` | Customer permissions plus ownership/admin |
| Savings goals | `/api/goals/**` | Authenticated; service validates ownership/admin |
| GICs | `/accounts/{accountId}/gic/**` | Authenticated; service validates ownership/admin |
| Notifications | `/notifications/evaluate` | Authenticated JWT in current config |

## 8. Data And Domain Model

Important domain objects:

- `User`: login identity, password hash, roles, active flag, linked customer ID.
- `Customer`: banking customer profile.
- `Account`: customer account with type, status, balance, and product rules.
- `Transaction`: persisted movement or event on an account.
- `IdempotencyRecord`: replay protection for monetary operations.
- `StandingOrder`: recurring transfer setup and lifecycle state.
- `SavingsGoal`: goal metadata attached to a customer/account.
- `Gic`: RRSP-backed investment with term, maturity date, and maturity amount.
- `NotificationDecision`: stored notification evaluation result.
- Account control audit records: freeze/unfreeze audit trail.

Soft-delete patterns:

- Customer deletion is soft delete.
- Savings goal deletion is soft delete.
- Redeemed GICs are marked with state and deletion metadata rather than hard
  removed from the historical record.

Scheduled jobs:

- `StandingOrderExecutionJob` processes standing orders and retries.
- `IdempotencyPurgeJob` removes old idempotency records.

## 9. Local Development Behavior

Backend defaults:

- Port: `8080`
- Local database: `jdbc:h2:file:./data/digitalbankdb;AUTO_SERVER=TRUE`
- H2 console: `/h2-console`
- Health endpoint: `/actuator/health`
- Swagger UI: `/swagger-ui/index.html`

Frontend defaults:

- Dev server: `http://localhost:5173`
- API base URL comes from Vite environment variables when set.
- In development, the fallback base URL is `/`, so a Vite proxy or matching
  deployment routing may be needed depending on how the frontend is run.

Useful verification commands:

```powershell
# Frontend
npm test
npm run build

# Backend
cd backend
.\mvnw.cmd test
```

When running the backend locally for documentation or testing, prefer an
isolated H2 path if you do not want the tracked development database under
`backend/data/` to change.

## 10. Current Security Notes And Risks

These are not criticisms of the work. They are useful map markers for future
hardening.

- Public registration currently accepts requested roles. If registration is
  exposed publicly, role assignment should be controlled server-side.
- The JWT secret has a development fallback. Production must provide a strong
  secret through environment configuration.
- `/h2-console/**` is public in the current security config. That is convenient
  for local development but should be disabled or heavily restricted outside
  development.
- Swagger/OpenAPI paths are public. That may be acceptable for development but
  should be reviewed before production.
- `JwtAuthenticationFilter` prints authenticated user, roles, and authorities to
  stdout. Replace this with structured debug logging or remove it before
  production.
- The notification controller references a service API key filter in a comment,
  but the filter is not implemented in the current source.
- Refresh tokens are generated, but contributors should verify the complete
  refresh lifecycle before depending on refresh behavior in clients.
- Authorization is a mix of controller annotations and service-layer checks.
  That is workable, but future features should be consistent and covered by
  security tests.
- Feature-specific permissions are not fully modeled. Most customer-facing
  features rely on `CUSTOMER_READ`, `CUSTOMER_CREATE`, or `CUSTOMER_UPDATE` plus
  ownership checks.
- Frontend route guards improve user experience but should never be treated as a
  security boundary. The backend remains the authority.

## 11. How To Read The Codebase

For a new developer, a useful first pass is:

1. Read `README.md`.
2. Read `docs/onboarding.md`.
3. Read this document.
4. Run the backend and frontend locally.
5. Open Swagger UI and compare endpoints to the controller classes.
6. Walk through login/register in the frontend.
7. Follow one feature end to end, for example transactions:
   - frontend page in `src/pages/`
   - hook in `src/hooks/`
   - API client in `src/api/`
   - controller in `backend/.../controller/`
   - service in `backend/.../service/`
   - repository/entity in `backend/.../repository/` and `entity/`
8. Check the related spec under `specs/` or `SpecFiles/`.

## 12. Git And Documentation Strategy

Branch meanings:

- `main`: integrated branch for the latest shared frontend and backend code.
- `feature/frontend`: historical frontend feature branch that has been merged
  into `main`.
- `feature/group123`: historical backend feature branch that has been merged
  into `main`.
- `onboarding`: living documentation branch for onboarding and project docs.

Recommended documentation workflow:

1. Update docs on `onboarding`.
2. Review the docs as a team.
3. Merge `onboarding` into `main` when the team wants docs visible to everyone
   by default.
4. Keep feature work on short-lived feature branches from `main`.
5. Merge feature branches back into `main` through review after tests pass.

## 13. What To Keep Improving

Good next project improvements:

- Add a clear admin-user bootstrap process.
- Lock public registration to customer-only users.
- Add or remove the intended service API key security chain for notifications.
- Move development-only endpoints and H2 console behind a dev profile.
- Add end-to-end tests for role boundaries.
- Add backend tests for every ownership-sensitive endpoint.
- Document production environment variables.
- Decide whether refresh tokens need a full endpoint, persistence, revocation,
  and rotation flow.
- Align permissions with feature domains if the system needs more granular
  authorization than customer/admin.

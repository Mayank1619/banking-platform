# Project Onboarding Guide

Last verified locally: 2026-06-22

## 1. What This Project Is

This repository contains a digital banking platform. The current codebase combines the latest frontend work from `feature/frontend` and backend work from `feature/group123` into `main`.

The product currently includes:

- Customer authentication and JWT-based session handling.
- Customer and account management.
- Deposits, withdrawals, transfers, and transaction history.
- Monthly statements and PDF statement generation.
- Spending insights and transaction recategorization.
- Standing orders.
- Savings goals.
- GIC-related backend endpoints.
- Local H2-backed development database support.

The frontend brand shown in the app is Voltio.

For a deeper explanation of the product features, backend security model,
Spring Security roles, ownership checks, endpoint access rules, and known
security notes, read [Project Deep Dive](project-deep-dive.md).

## 2. Technology Stack

Frontend:

- React 18.
- Vite.
- React Router.
- TanStack Query.
- Axios.
- Vitest and Testing Library.

Backend:

- Java 21.
- Spring Boot 3.5.x.
- Spring MVC, Spring Security, Spring Data JPA.
- JWT authentication.
- H2 for local development.
- MySQL connector available for non-local environments.
- OpenAPI/Swagger via Springdoc.
- Maven wrapper.
- JUnit, Mockito, Spring test support, JaCoCo.

Deployment and operations assets:

- Frontend Docker and Nginx files at the repo root.
- Backend Docker and cloud build files under `backend/`.
- Kubernetes manifests under `k8s/` and `backend/k8s/`.
- Cloud Build files at the repo root and under `backend/`.

## 3. Repository Map

```text
.
|-- src/                         Frontend React app
|-- backend/                     Spring Boot backend
|-- docs/                        Onboarding and project reference documents
|-- specs/                       Spec Kit feature specs
|-- SpecFiles/                   Legacy or merged spec documents
|-- .specify/                    Spec Kit configuration, templates, scripts, workflows
|-- .github/agents/              Speckit/Copilot agent instructions
|-- .github/prompts/             Speckit/Copilot prompt entry points
|-- k8s/                         Frontend Kubernetes manifests
|-- backend/k8s/                 Backend Kubernetes manifests
|-- dist/                        Frontend build output, do not edit by hand
|-- data/ and logs/              Local runtime data/logs, do not rely on for source changes
```

Important frontend paths:

- `src/App.jsx` - app shell, routing layout, nav behavior.
- `src/main.jsx` - React entry point.
- `src/api/` - frontend API clients and Axios setup.
- `src/auth/` - auth context and protected/admin route helpers.
- `src/components/` - shared UI components.
- `src/hooks/` - query/mutation hooks.
- `src/pages/` - route-level pages.
- `src/theme/` - theme state and provider.
- `src/test/setup.js` - Vitest setup.

Important backend paths:

- `backend/pom.xml` - backend dependencies and build plugins.
- `backend/src/main/resources/application.properties` - local backend config.
- `backend/src/main/resources/db/migration/` - database migration scripts.
- `backend/src/main/java/com/group1/banking/controller/` - REST controllers.
- `backend/src/main/java/com/group1/banking/service/` - service layer.
- `backend/src/main/java/com/group1/banking/service/impl/` - service implementations.
- `backend/src/main/java/com/group1/banking/entity/` - JPA entities.
- `backend/src/main/java/com/group1/banking/repository/` - Spring Data repositories.
- `backend/src/main/java/com/group1/banking/security/` - JWT, principal, and access helpers.
- `backend/src/test/java/com/group1/banking/` - backend tests.

## 4. Local Setup

Required:

- Java 21.
- Node.js 20 or newer. The app was verified with Node 26.3.1.
- npm.
- Git.

Clone and enter the repo:

```powershell
git clone https://github.com/Mayank1619/banking-platform.git
cd banking-platform
git switch main
```

Install frontend dependencies:

```powershell
npm ci
```

Run frontend tests:

```powershell
npm test
```

Run frontend production build:

```powershell
npm run build
```

Run backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Expected test baseline after the June 22, 2026 merge:

- Frontend: 20 test files, 133 tests passing.
- Backend: 582 tests passing.

## 5. Running Locally

Start the backend from `backend/`:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend URLs:

- Health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console

Default local backend configuration:

- Port: `8080`.
- H2 URL: `jdbc:h2:file:./data/digitalbankdb;AUTO_SERVER=TRUE`.
- Username: `sa`.
- Password: empty.
- JWT secret: supplied by `JWT_SECRET`, with a development fallback in `application.properties`.

Start the frontend from the repo root in another terminal:

```powershell
npm run dev
```

Frontend URL:

- http://localhost:5173

The Vite dev server proxies these paths to the backend target:

- `/api`
- `/accounts`
- `/customers`
- `/standing-orders`

By default the proxy target is `http://localhost:8080/`. Override it when needed:

```powershell
$env:VITE_DEV_BACKEND_TARGET = "http://localhost:8081/"
npm run dev
```

Useful local smoke checks:

```powershell
curl.exe http://localhost:8080/actuator/health
curl.exe -i http://localhost:5173/
curl.exe -i http://localhost:5173/api/auth/login -H "Content-Type: application/json" -d "{}"
```

The last command should be proxied to the backend and return validation JSON.

## 6. Spec Kit / Speckit Workflow

This repo is set up for spec-driven development using the Spec Kit assets checked into the repository. You will see the workflow referred to as Spec Kit or Speckit in file and folder names.

Where to look:

- `.specify/` - core templates, scripts, workflow config, and project memory.
- `.github/agents/` - agent instructions for specify, plan, tasks, implement, checklist, and Git helpers.
- `.github/prompts/` - prompt entry points for the same workflow.
- `specs/001-savings-goals/` - complete example feature spec with spec, plan, research, data model, contracts, tasks, and checklist.
- `SpecFiles/` - additional project-level and RRSP-related specification material.

Typical feature workflow:

1. Start from an issue, user story, or feature request.
2. Create or update a feature spec under `specs/<number>-<feature-name>/`.
3. Capture requirements in `spec.md`.
4. Capture technical direction in `plan.md`.
5. Capture implementation work in `tasks.md`.
6. Add or update contracts, data model, quickstart, and checklists when the feature needs them.
7. Implement source changes in `src/` and/or `backend/`.
8. Add focused tests near the changed frontend/backend code.
9. Run the relevant test suites before opening a PR.

When in doubt, use `specs/001-savings-goals/` as the model for how much detail a well-formed feature folder should contain.

## 7. Branches and Git Strategy

Current important branches:

- `main` - integrated branch for new team members and normal development base.
- `feature/frontend` - latest frontend feature branch that was merged into `main` on 2026-06-22.
- `feature/group123` - latest backend feature branch that was merged into `main` on 2026-06-22.
- `onboarding` - documentation branch for this onboarding guide and related docs.

Recommended strategy:

1. Always branch from an updated `main`.

   ```powershell
   git switch main
   git pull origin main
   git switch -c feature/<area>-<short-description>
   ```

2. Keep branches short-lived and focused.

3. Use branch names that show the area:

   ```text
   feature/frontend-account-summary
   feature/backend-standing-order-validation
   fix/frontend-theme-storage
   fix/backend-auth-principal
   docs/onboarding-update
   ```

4. Prefer pull requests into `main` for shared work.

5. Re-sync with `main` before asking for review:

   ```powershell
   git fetch origin
   git merge origin/main
   ```

6. Do not commit generated local artifacts unless a deployment workflow explicitly requires them.

Common files/folders to avoid committing:

- `node_modules/`
- `backend/target/`
- local H2 database changes under `data/` or `backend/data/`
- logs under `logs/` or `backend/logs/`
- temporary run output
- new build artifacts under `dist/` unless the team intentionally tracks them

Pull request checklist:

- Link the issue/spec/task.
- Explain frontend and backend impact separately when both changed.
- Note any database or config changes.
- Include screenshots for user-facing UI changes.
- Include API examples for backend endpoint changes.
- Run `npm test` for frontend changes.
- Run `npm run build` for frontend build-impacting changes.
- Run `backend\mvnw.cmd test` for backend changes.

## 8. Backend Development Notes

Authentication:

- The backend uses JWT authentication.
- Runtime principals are represented by `CustomUserPrincipal`.
- Controllers should prefer `@AuthenticationPrincipal CustomUserPrincipal principal` when they need the authenticated caller.

Error handling:

- Shared exception translation lives in `GlobalExceptionHandler`.
- Business errors should preserve meaningful error codes for the frontend.

Database:

- Local development uses H2 by default.
- Migration scripts live under `backend/src/main/resources/db/migration/`.
- The default config uses `spring.jpa.hibernate.ddl-auto=update` for local development.
- Avoid treating local `.mv.db` files as source of truth.

OpenAPI:

- Swagger UI is available locally at `/swagger-ui/index.html`.
- Use it to inspect available backend endpoints while onboarding.

Known backend cleanup item:

- `backend/pom.xml` currently emits a Maven warning about duplicate `maven-compiler-plugin` declarations. It does not block tests today, but it should be cleaned up in a dedicated maintenance PR.

## 9. Frontend Development Notes

Routing and layout:

- `src/App.jsx` contains the app shell, route layout, navbar, subnav, and route definitions.

API access:

- `src/api/axiosClient.js` centralizes Axios configuration.
- Feature-specific API wrappers live in `src/api/`.
- Data access hooks live in `src/hooks/`.

Auth:

- `src/auth/AuthContext.jsx` owns auth state at runtime.
- `src/auth/authState.js` handles persistence and customer context helpers.
- Protected/admin route helpers live under `src/auth/`.

Theme:

- Theme state lives in `src/theme/`.
- Theme storage must be defensive because tests and some runtime environments may not expose `localStorage`.

Testing:

- Frontend tests are written with Vitest and Testing Library.
- Shared test setup lives in `src/test/setup.js`.

Known frontend cleanup item:

- `npm ci` currently reports audit findings. Review with `npm audit` and address in a separate dependency-maintenance PR so feature work stays focused.

## 10. First Day Checklist

1. Clone the repo and switch to `main`.
2. Read this onboarding guide.
3. Read `specs/001-savings-goals/spec.md`, `plan.md`, and `tasks.md` to understand the spec-driven workflow.
4. Run `npm ci`.
5. Run `npm test`.
6. Run `npm run build`.
7. Run `backend\mvnw.cmd test`.
8. Start the backend and open Swagger UI.
9. Start the frontend and open the app.
10. Pick a small bug, docs update, or test improvement to learn the PR flow.

## 11. Local Verification Snapshot

Verified on 2026-06-22 after merging `feature/frontend` and `feature/group123` into `main`:

- Backend started on port `8080`.
- `GET /actuator/health` returned `{"status":"UP"}`.
- Swagger UI returned HTTP 200.
- Frontend Vite dev server started on port `5173`.
- Frontend HTML returned HTTP 200.
- Vite proxy successfully forwarded `/api/auth/login` to the backend and returned validation JSON.
- Browser-level check confirmed the React root rendered login/register home content without console errors.

## 12. How To Keep This Guide Fresh

Update this document when:

- A new branch strategy is adopted.
- A new feature area is added.
- Local setup commands change.
- Ports, environment variables, or database defaults change.
- Spec Kit workflow conventions change.
- Test baselines change.
- Deployment flow changes.

Small documentation updates can go directly on the `onboarding` branch. Larger changes should use a docs branch and PR into `onboarding`, then merge or cherry-pick into `main` when the team wants the public default branch updated.

# Tasks: Global Dark Neon Design-System Update for Voltio

**Input**: Design documents from `/specs/001-global-dark-neon/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Create the frontend theme structure under frontend/src/components/, frontend/src/pages/, and frontend/src/styles/
- [ ] T002 [P] Define the shared dark neon token set in frontend/src/styles/tokens.css
- [ ] T003 [P] Create the global theme entry point in frontend/src/styles/global-theme.css

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 Implement shared button and form state styling in frontend/src/components/ui/Button.css
- [ ] T005 Implement shared card, panel, table, modal, badge, alert, progress, empty, and loading styles in frontend/src/components/ui/SurfaceStyles.css
- [ ] T006 Wire the global theme into the app shell in frontend/src/components/layout/AppShell.tsx
- [ ] T007 Add accessibility helpers for focus-visible states and readable contrast in frontend/src/styles/accessibility.css

---

## Phase 3: User Story 1 - Review the dark neon experience on core pages (Priority: P1) 🎯 MVP

**Goal**: Apply the shared dark neon theme to the agreed customer/admin page shells and shared controls.

**Independent Test**: Open the agreed pages and confirm the theme renders without changing page logic or flows.

### Implementation for User Story 1

- [ ] T008 [P] [US1] Apply the shared token theme to the primary page shell in frontend/src/components/layout/MainLayout.tsx
- [ ] T009 [US1] Update shared navigation and header styling in frontend/src/components/layout/Header.tsx
- [ ] T010 [US1] Update shared button variants and states in frontend/src/components/ui/Button.tsx
- [ ] T011 [US1] Update shared card and panel surfaces in frontend/src/components/ui/Card.tsx

---

## Phase 4: User Story 2 - Preserve existing banking flows while updating the visual system (Priority: P1)

**Goal**: Ensure current customer and admin flows continue to work with the updated theme and unchanged business behavior.

**Independent Test**: Execute the existing banking journeys and confirm the same actions, visibility rules, and outcomes still work after the visual update.

### Implementation for User Story 2

- [ ] T012 [P] [US2] Apply the theme to forms and input states in frontend/src/components/forms/InputField.tsx
- [ ] T013 [US2] Apply the theme to tables, badges, and status pills in frontend/src/components/ui/Table.tsx
- [ ] T014 [US2] Apply the theme to modals, alerts, banners, and empty/loading states in frontend/src/components/ui/Modal.tsx
- [ ] T015 [US2] Verify no logic or API behavior changes were introduced in frontend/src/pages/ accounts, deposit, withdraw, transfer, profile, statements, and standing-orders screens

---

## Phase 5: User Story 3 - Reuse the same design system for future Sprint 3 screens (Priority: P2)

**Goal**: Make the theme reusable for future screens and shared UI components.

**Independent Test**: Add or update one new screen using the shared components and confirm it inherits the same theme automatically.

### Implementation for User Story 3

- [ ] T016 [P] [US3] Document the token and component usage guidance in frontend/src/styles/README.md
- [ ] T017 [US3] Refactor shared UI components to consume the token file in frontend/src/components/ui/
- [ ] T018 [US3] Validate that new Sprint 3 screen patterns inherit the shared theme in frontend/src/pages/

---

## Phase 6: Polish & Cross-Cutting Concerns

- [ ] T019 [P] Review contrast, disabled-state clarity, and focus-visible behavior across frontend/src/components/ and frontend/src/pages/
- [ ] T020 [P] Run the quickstart validation checklist from specs/001-global-dark-neon/quickstart.md
- [ ] T021 Confirm that no API behavior, customer/admin visibility rules, or banking flows were changed during the visual update

---

## Dependencies & Execution Order

- Phase 1 must complete before Phase 2.
- Phase 2 must complete before any user-story tasks begin.
- US1 can be validated independently for MVP.
- US2 and US3 can proceed after the shared foundation is in place.

## Parallel Opportunities

- T002 and T003 can run in parallel.
- T004 and T005 can run in parallel once Phase 1 is complete.
- T008, T012, and T016 are good candidates for parallel work after the foundational styles exist.

## Implementation Strategy

- MVP first: complete Phase 1 + Phase 2 + US1, then validate the core pages.
- Incremental delivery: add US2 and US3 after the shared design system is stable.

# Research Notes: Global Dark Neon Design-System Update

## Decision
- Treat this work as a reusable theme-and-token update applied through shared UI components rather than page-by-page overrides.
- Keep the implementation strictly visual: no API, business-rule, or access-control changes.

## Rationale
- The feature is explicitly a design-system update, so the safest implementation path is to centralize tokens and shared component states in one place.
- The current repository snapshot does not expose the underlying frontend framework or existing styling layer, so the plan must assume the app already has reusable components that can consume design tokens.

## Alternatives considered
- Per-page CSS overrides: rejected because it would create drift and make future Sprint 3 screens inconsistent.
- Inline component styling only: rejected because it would not support reuse across customer/admin screens.
- Full logic rewrites: rejected because the scope explicitly forbids functional changes.

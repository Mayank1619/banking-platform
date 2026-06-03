# Data Model: Dark Neon Design System

## Entities

### Design Tokens
- Purpose: define the reusable visual language for the theme update.
- Core fields:
  - background, surface, elevated-panel, text, muted-text, border, disabled
  - primary neon, secondary neon, success, warning, error
- Validation rules:
  - All text must maintain WCAG-readable contrast on dark surfaces.
  - Neon accents must support non-colour cues for meaning.

### Shared UI Components
- Purpose: apply the theme consistently to reusable elements.
- Core fields:
  - buttons, forms, cards, tables, modals, alerts, badges, progress bars, empty states, loading states
- Validation rules:
  - State variants must include default, hover, focus, active, disabled, and loading.
  - Destructive/warning styles must remain visually distinct from primary and secondary actions.

### Theme States
- Purpose: capture user-facing state treatment.
- Core fields:
  - error, warning, success, required, helper text, disabled/read-only
- Validation rules:
  - Colour only is not sufficient; labels/icons/text cues are required.

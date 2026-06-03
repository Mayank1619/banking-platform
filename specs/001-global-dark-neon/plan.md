# Implementation Plan: Global Dark Neon Design-System Update for Voltio

**Branch**: `001-global-dark-neon` | **Date**: 2026-06-03 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-global-dark-neon/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

This feature applies a reusable dark neon design-system update to agreed customer/admin pages and future Sprint 3 screens without changing API behavior or customer/admin visibility rules. The implementation should centralize theme tokens, shared component states, and visual validation so the design remains consistent across the app.

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: NEEDS CLARIFICATION (the current workspace does not expose the app framework or runtime)  
**Primary Dependencies**: NEEDS CLARIFICATION (existing UI library/theme system not present in this repo snapshot)  
**Storage**: N/A  
**Testing**: NEEDS CLARIFICATION  
**Target Platform**: Web frontend (assumed from the banking app scope; confirm in implementation)  
**Project Type**: web application / frontend design-system update  
**Performance Goals**: Maintain current interaction performance; no functional regression  
**Constraints**: Visual-only change set; must preserve existing banking flows, visibility rules, and accessibility  
**Scale/Scope**: Current customer/admin pages plus reusable shared components to support future Sprint 3 screens

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- No project-specific constitution rules are defined in the current repo snapshot; the placeholder constitution file does not impose additional implementation constraints.
- Gate result: PASS for planning purposes, with the note that implementation details must be confirmed once the actual frontend stack is available.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── styles/
└── tests/

specs/001-global-dark-neon/
├── plan.md
├── research.md
├── data-model.md
└── quickstart.md
```

**Structure Decision**: The repository snapshot is minimal, so the implementation plan assumes a standard frontend structure with reusable components and styles. The design-system work should be applied in the existing frontend component and styling directories once those are available in the implementation repo.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |

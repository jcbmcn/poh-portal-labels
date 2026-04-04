---
phase: 03-pet-false-positive-fix
plan: 01
subsystem: overlay
tags: [runelite, java, portal, game-objects, bug-fix]

# Dependency graph
requires:
  - phase: 02-new-destination-ids
    provides: Updated PORTAL_LABELS map with Feb 2026 portal IDs already populated
provides:
  - Tightened isPortalObject() guard — only IDs in PORTAL_LABELS (or Feb 2026 fast-path ranges) pass
affects: []

# Tech tracking
tech-stack:
  added: []
  patterns: [PORTAL_LABELS.containsKey(id) as authoritative portal identity check]

key-files:
  created: []
  modified:
    - src/main/java/com/portalname/PortalNameOverlay.java

key-decisions:
  - "Replace 13615–13633 range bypass with PORTAL_LABELS.containsKey(id) — makes isPortalObject() self-consistent with the render loop's originalLabel != null guard"
  - "Preserve Feb 2026 fast-path ranges (60774–60783, 60790–60819) unchanged — those ranges are fully mapped and the range check is safe"
  - "Preserve ObjectComposition name fallback — future unmapped portal IDs can still be caught by 'Portal' in the object name"

patterns-established:
  - "Portal identity: isPortalObject() now acts as a definitive 'is this ID known to us?' check, not a broad range filter"

requirements-completed: [ACC-01]

# Metrics
duration: 1min
completed: 2026-04-04
---

# Phase 03 Plan 01: Pet False-Positive Fix Summary

**Eliminated false-positive portal labels by replacing the over-broad 13615–13633 range bypass in `isPortalObject()` with an explicit `PORTAL_LABELS.containsKey(id)` check.**

## Performance

- **Duration:** ~1 min
- **Started:** 2026-04-04T01:36:30Z
- **Completed:** 2026-04-04T01:37:30Z
- **Tasks:** 1
- **Files modified:** 1

## Accomplishments

- Removed the `if (id >= 13615 && id <= 13633) return true` range bypass that admitted 7 IDs (13621, 13622, 13625, 13627, 13628, 13629, 13632) with no portal mapping
- Replaced it with `PORTAL_LABELS.containsKey(id)` — approves exactly the 12 legacy IDs that have known portal entries, nothing more
- Made `isPortalObject()` structurally consistent with the render loop: both now require the ID to be in `PORTAL_LABELS` for the legacy range
- Build passes: `BUILD SUCCESSFUL` with no new warnings

## Task Commits

1. **Task 1: Replace over-broad 13615–13633 bypass with PORTAL_LABELS.containsKey guard** - `56c4cf4` (fix)

**Plan metadata:** (docs commit pending)

## Files Created/Modified

- `src/main/java/com/portalname/PortalNameOverlay.java` — `isPortalObject()` method tightened: old range bypass removed, `PORTAL_LABELS.containsKey(id)` guard added

## Deviations from Plan

None — plan executed exactly as written.

## Known Stubs

None.

## Self-Check: PASSED

- `56c4cf4` confirmed in git log
- `PORTAL_LABELS.containsKey` confirmed at line 545 of PortalNameOverlay.java
- `id >= 13615 && id <= 13633` range block not present in method logic (only in a comment)
- Build: `BUILD SUCCESSFUL`

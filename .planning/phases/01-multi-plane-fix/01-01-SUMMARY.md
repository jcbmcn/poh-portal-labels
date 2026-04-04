---
phase: 01-multi-plane-fix
plan: 01
subsystem: ui
tags: [runelite, overlay, rendering, tilemap]

requires: []
provides:
  - Multi-plane portal tile scan covering all 4 POH floors
  - Correct Perspective projection using tile's own plane
  - Upper-floor portal label rendering (planes 1-3)
affects: []

tech-stack:
  added: []
  patterns:
    - Loop over Constants.MAX_Z planes before iterating x/y when scanning POH tiles

key-files:
  created: []
  modified:
    - src/main/java/com/portalname/PortalNameOverlay.java

key-decisions:
  - "Use Constants.MAX_Z (= 4) as the plane loop bound instead of a hardcoded literal — stays correct if RuneLite ever changes the max plane count"
  - "Pass tile.getPlane() to Perspective.localToCanvas instead of player's plane — ensures labels project to the correct screen position for portals on floors the player is not currently on"

patterns-established:
  - "Multi-plane tile scan: Tile[][][] allTiles = scene.getTiles(); for (int plane = 0; plane < Constants.MAX_Z; plane++) { Tile[][] tiles = allTiles[plane]; ... }"

requirements-completed:
  - FLOOR-01
  - FLOOR-02
  - DATA-01
  - ACC-02

duration: 5min
completed: 2026-04-03
---

# Phase 01: multi-plane-fix Summary

**3D tile scan over all 4 POH planes via Constants.MAX_Z, fixing upper-floor portal label rendering and inPoh detection**

## Performance

- **Duration:** ~5 min
- **Started:** 2026-04-03T00:00:00Z
- **Completed:** 2026-04-03T00:05:00Z
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments
- Both `scene.getTiles()[player.getPlane()]` single-plane slices replaced with loops over all 4 planes (`Constants.MAX_Z`)
- `Perspective.localToCanvas` now passes `tile.getPlane()` instead of the player's plane — labels project correctly even when the player is on a different floor
- `import net.runelite.api.Constants` added
- Build passes cleanly with no regressions; all 139 `PORTAL_LABELS` entries unchanged
- Issue #35 (Yannille) closes as a side-effect — IDs were already in `PORTAL_LABELS`, the missing piece was the floor scan

## Task Commits

1. **Task 1 + Task 2: Multi-plane tile scan fix + build verification** - `2f4ff28` (fix)

## Files Created/Modified
- `src/main/java/com/portalname/PortalNameOverlay.java` — Added Constants import; wrapped inPoh detection and render loops over Constants.MAX_Z planes; fixed Perspective.localToCanvas to use tile.getPlane()

## Decisions Made
- Constants.MAX_Z used as loop bound (not hardcoded 4) for forward-compatibility
- tile.getPlane() passed to Perspective.localToCanvas to ensure correct projection for portals on floors the player isn't on

## Deviations from Plan

None — plan executed exactly as written.

## Issues Encountered

None — build passed first attempt, no brace imbalance issues.

## User Setup Required

None — no external service configuration required.

## Next Phase Readiness
- Phase 01 complete; multi-plane scan is now the foundation for all portal label rendering
- Phase 02 (ID audit) can proceed — all portal IDs in PORTAL_LABELS are now reachable by the scan

---
*Phase: 01-multi-plane-fix*
*Completed: 2026-04-03*

# Roadmap: POH Portal Labels — Bug Fix Milestone

**Created:** 2026-04-03
**Phases:** 3
**Requirements:** 14 v1 requirements

## Phase Overview

| # | Phase | Goal | Requirements | Success Criteria |
|---|-------|------|--------------|------------------|
| 1 | Multi-Plane Fix | Portals on every floor of a POH are labeled correctly | FLOOR-01, FLOOR-02, DATA-01, ACC-02 | 4 criteria |
| 2 | New Destination IDs | All 10 missing teleport destinations display labels | DATA-02, DATA-03, DATA-04, DATA-05, DATA-06, DATA-07, DATA-08, DATA-09, DATA-10, DATA-11 | 3 criteria |
| 3 | Pet False Positive Fix | 1/1 | Complete   | 2026-04-04 |

## Phases

- [ ] **Phase 1: Multi-Plane Fix** - Fix the core tile scan bug so portals on upper floors are labeled; verify #35 closes as side effect
- [ ] **Phase 2: New Destination IDs** - Add 40 object IDs for 10 teleport destinations added in the February 2026 game update
- [x] **Phase 3: Pet False Positive Fix** - Reproduce, identify root cause, and eliminate phantom labels appearing over pets (completed 2026-04-04)

## Phase Details

### Phase 1: Multi-Plane Fix

**Goal:** Portals built on any floor of a POH are labeled correctly, and the POH detection check works regardless of which floor the player is standing on.
**Depends on:** Nothing (first phase)
**Requirements:** FLOOR-01, FLOOR-02, DATA-01, ACC-02
**UI hint:** no

**Success Criteria:**
1. A portal built on plane 1 or plane 2 of a POH displays its destination label without requiring the player to be on the same floor
2. The `inPoh` detection activates correctly when the player enters a POH while on an upper floor
3. Issue #35 (Yannille) is confirmed closed or triaged — re-test with a player who has the Hard Ardougne Diary; if still broken, an investigation task is created for Phase 1 follow-up
4. All previously-working portal destinations continue to show correct labels after the plane loop refactor (no regression)

**Implementation Notes:**
- Root cause confirmed: `PortalNameOverlay.java` slices `scene.getTiles()[client.getLocalPlayer().getWorldLocation().getPlane()]` in two places (~L272 `inPoh` loop and ~L300 render loop), scanning only one plane
- Fix: replace both slices with a triple-nested loop over `Constants.MAX_Z` (= 4) planes
- **Critical:** pass `tile.getPlane()` (not the player's plane) to `Perspective.localToCanvas()` — using the player's plane computes label height from the wrong floor
- **Critical:** null-check every tile (`if (tile == null) continue;`) — `getTiles()` allocates the full `4×104×104` array but not all cells are populated
- Issue #35 most likely self-closes: all 8 Yanille/Watchtower IDs are already present in `PORTAL_LABELS`; the bug is almost certainly explained by the upper-floor scan gap

**Plans:** 1 plan
Plans:
- [ ] 01-01-PLAN.md — Fix multi-plane tile scan + build verification

---

### Phase 2: New Destination IDs

**Goal:** All 10 teleport destinations added in the February 2026 game update (Trollheim, Paddewwa, Lassar, Dareeyak, Ourania, Barbarian, Khazard, Ice Plateau, Respawn, Teleport to Boat) display correct labels across all four portal tiers.
**Depends on:** Phase 1
**Requirements:** DATA-02, DATA-03, DATA-04, DATA-05, DATA-06, DATA-07, DATA-08, DATA-09, DATA-10, DATA-11

**Plans:** 1 plan
Plans:
- [ ] 02-01-PLAN.md — Add 40 new portal IDs for 10 February 2026 destinations

---

### Phase 2: New Destination IDs

**Goal:** All 10 teleport destinations added in the February 2026 game update (Trollheim, Paddewwa, Lassar, Dareeyak, Ourania, Barbarian, Khazard, Ice Plateau, Respawn, Teleport to Boat) display correct labels across all four portal tiers.
**Depends on:** Phase 1
**Requirements:** DATA-02, DATA-03, DATA-04, DATA-05, DATA-06, DATA-07, DATA-08, DATA-09, DATA-10, DATA-11
**UI hint:** no

**Success Criteria:**
1. Each of the 10 new destinations shows its label on all four portal tiers (Raging Echoes, Teak, Mahogany, Marble) — 40 object IDs total
2. Labels render in the correct color in all three color modes (SINGLE, MULTI, MULTI + UNIQUE)
3. No existing portal destination labels regress

**Implementation Notes:**
- All 40 IDs are confirmed from OSRS wiki infoboxes; IDs are 60774–60783 (Raging Echoes variants) and 60790–60819 (Teak/Mahogany/Marble variants)
- **Three files must be updated atomically — missing any one causes silent failure in `MULTI + UNIQUE` mode:**
  1. `PORTAL_LABELS` map in `PortalNameOverlay.java` — 40 new `put()` entries
  2. `isPortalObject()` in `PortalNameOverlay.java` — add range checks: `(id >= 60774 && id <= 60783) || (id >= 60790 && id <= 60819)`
  3. `updatePortalColors()` in `PortalNameOverlay.java` — add color entry for each new destination name
  4. `PortalNameConfig.java` — add `@ConfigItem` method for each new destination's default color (missing entries can cause null-pointer crash)
- "Teleport to Boat" label may be truncated in-game; test and consider shortening to `"Boat"` if needed

---

### Phase 3: Pet False Positive Fix

**Goal:** Pets and other NPCs standing in a POH no longer cause portal destination labels to appear as if floating above the NPC.
**Depends on:** Phase 1
**Requirements:** ACC-01
**UI hint:** no

**Success Criteria:**
1. A player with a pet inside their POH observes no portal label hovering above or near the pet (when the pet is not standing on a portal tile)
2. The fix is applied only after the triggering pet and label are confirmed via in-game reproduction — no speculative code changes
3. All legitimate portal labels continue to render correctly after the fix

**Implementation Notes:**
- NPCs (including pets) are architecturally impossible to appear in `tile.getGameObjects()` — the `Tile` API has no NPC accessor; do NOT add NPC filtering to the tile scan
- Most likely explanation: a portal `GameObject` renders its label at its world position while a pet NPC stands on the same tile, making the text appear to hover over the pet
- Three candidate root causes (in likelihood order): **(C)** a wrong object ID in `PORTAL_LABELS` corresponds to a pet-spawned decoration; **(A)** an NPC placeholder decoration shares an ID with a portal entry; **(B)** a pet-related object has "Portal" in its `ObjectComposition` name
- Fix path: reproduce first → cross-reference triggering object ID against OSRS wiki pet pages → if Root Cause C, remove the offending ID; if Root Cause A/B, replace the `13615–13633` bypass in `isPortalObject()` with an explicit `PORTAL_LABELS.containsKey(id)` guard
- The `13615–13633` bypass is already over-broad (8 of 19 IDs have no portal entry in `PORTAL_LABELS`); tightening it is a good cleanup regardless of root cause

**Plans:** 1/1 plans complete
Plans:
- [x] 03-01-PLAN.md — Tighten isPortalObject() guard to eliminate false positives from over-broad ID range

---

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Multi-Plane Fix | 0/1 | Not started | - |
| 2. New Destination IDs | 0/? | Not started | - |
| 3. Pet False Positive Fix | 0/? | Not started | - |

## Requirement Coverage

| Requirement | Phase | Status |
|-------------|-------|--------|
| FLOOR-01 | Phase 1 | Pending |
| FLOOR-02 | Phase 1 | Pending |
| DATA-01 | Phase 1 | Pending |
| ACC-02 | Phase 1 | Pending |
| DATA-02 | Phase 2 | Pending |
| DATA-03 | Phase 2 | Pending |
| DATA-04 | Phase 2 | Pending |
| DATA-05 | Phase 2 | Pending |
| DATA-06 | Phase 2 | Pending |
| DATA-07 | Phase 2 | Pending |
| DATA-08 | Phase 2 | Pending |
| DATA-09 | Phase 2 | Pending |
| DATA-10 | Phase 2 | Pending |
| DATA-11 | Phase 2 | Pending |
| ACC-01 | Phase 3 | Pending |

**Coverage: 14/14 v1 requirements mapped ✓**

---
*Roadmap created: 2026-04-03*

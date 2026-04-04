# Research Summary — POH Portal Labels Bug Fixes
> Synthesized: 2026-04-03

---

## Overview

All four open bugs trace to the same underlying category of problem: the plugin was written with single-plane, ground-floor assumptions that silently break in edge cases. Issue #41 is a structural scan bug — the tile loop only reads `getTiles()[playerPlane]` when it must iterate all four planes. Issue #40 is straightforward data entry — 40 new object IDs from a February 2026 game update are missing from the portal label map. Issue #35 turns out to be a non-bug for the IDs themselves (all 8 Yanille IDs are already in the codebase), making it a display/rendering investigation rather than a data fix. Issue #39 is the most ambiguous: pets cannot appear in `tile.getGameObjects()` by API design, so the reported "pet label" is almost certainly a portal label rendered at the correct game object position while a pet NPC stands on top of that tile — the fix is in `isPortalObject()` guard logic, not in NPC filtering.

All findings are **HIGH confidence** sourced from official RuneLite API source (`Scene.java`, `Tile.java`, `Constants.java`, `Perspective.java`, `ObjectComposition.java`) and the OSRS wiki object infoboxes. The one **LOW confidence** area is the specific root cause of #39 — in-game reproduction is required before a targeted fix can be applied.

---

## Key Findings by Issue

### #41 — Upper Floor Portals (Plane Detection)

**Root cause:** Confirmed. `PortalNameOverlay.java` calls `scene.getTiles()[client.getLocalPlayer().getWorldLocation().getPlane()]` in two places — the `inPoh` detection loop (~lines 272–293) and the portal label render loop (~lines 300–376). This slices to one plane only. `Scene.getTiles()` returns a `Tile[][][]` indexed as `[plane][x][y]`; planes 1–3 are never scanned.

**Fix:** Replace both single-plane slices with a triple-nested loop over all four planes using `Constants.MAX_Z` (= 4) as the bound. In the render loop, pass `tile.getPlane()` (not the player's plane) to `Perspective.localToCanvas()` — the `plane` argument drives 3D→2D height calculation and must match the portal's actual floor.

**Risk:**
- Null tiles are normal at upper planes — always guard `if (tile == null) continue;`
- Using `tile.getPlane()` (not the loop variable `plane`) is the more robust approach and the idiomatic RuneLite pattern
- Bridge tiles (plane 1 floors acting as walkable surfaces) have height data derived from plane 2 — `localToCanvas` handles this automatically as long as the correct plane is passed

---

### #40 — New Teleport Destinations (Missing IDs)

**Status:** All 40 IDs confirmed from OSRS wiki infoboxes. Ten new destinations were added on 25 February 2026. Each has 4 variants (teak, mahogany, marble, Raging Echoes).

**New destinations and IDs:**

| Destination | Label | Raging Echoes | Teak | Mahogany | Marble |
|-------------|-------|---------------|------|----------|--------|
| Trollheim | `"Trollheim"` | 60774 | 60790 | 60800 | 60810 |
| Paddewwa | `"Paddewwa"` | 60775 | 60791 | 60801 | 60811 |
| Lassar | `"Lassar"` | 60776 | 60792 | 60802 | 60812 |
| Dareeyak | `"Dareeyak"` | 60777 | 60793 | 60803 | 60813 |
| Ourania | `"Ourania"` | 60778 | 60794 | 60804 | 60814 |
| Barbarian Outpost | `"Barbarian"` | 60779 | 60795 | 60805 | 60815 |
| Port Khazard | `"Khazard"` | 60780 | 60796 | 60806 | 60816 |
| Ice Plateau | `"Ice Plateau"` | 60781 | 60797 | 60807 | 60817 |
| Respawn | `"Respawn"` | 60782 | 60798 | 60808 | 60818 |
| Teleport to Boat | `"Teleport to Boat"` | 60783 | 60799 | 60809 | 60819 |

**Fix — three places must be updated in lockstep:**
1. `PORTAL_LABELS` map in `PortalNameOverlay.java` — add all 40 `put()` entries
2. `isPortalObject()` in `PortalNameOverlay.java` — add two range checks: `(id >= 60774 && id <= 60783) || (id >= 60790 && id <= 60819)`
3. `updatePortalColors()` in `PortalNameOverlay.java` — add color entries for each new destination name
4. `PortalNameConfig.java` — add `@ConfigItem` method for each new destination's default color

**Risk:**
- Forgetting step 3 or 4 is a silent failure — the label renders but falls back to `Color.WHITE` in `MULTI + UNIQUE` mode, with no compile-time or runtime warning
- "Teleport to Boat" label may be truncated in-game; consider `"Boat"` as a fallback after in-game testing
- Wiki IDs should be cross-checked against the OSRS cache before release (wiki data occasionally lags updates)

---

### #39 — Pets Showing Portal Labels (False Positive)

**Root cause:** Hypothesis, not confirmed. NPCs (including pets) are architecturally impossible to appear in `tile.getGameObjects()` — the `Tile` API has no NPC accessor and NPCs are tracked separately via `client.getNpcs()`. The most likely explanation: the portal `GameObject` is rendered with a label at its world position, and the pet NPC happens to stand on the same tile, making the floating text appear to hover over the pet.

**Three candidate root causes (in order of likelihood):**
- **Root Cause C** — a wrong object ID was added to `PORTAL_LABELS` that corresponds to a pet-spawned decoration object
- **Root Cause A** — an NPC's placeholder decoration object shares an ID or name with a portal entry
- **Root Cause B** — a pet-related object in the POH has "Portal" in its `ObjectComposition` name, passing the `isPortalObject()` name check

**Fix approach:**
1. **Reproduce reliably first** — identify which pet(s) trigger the bug and which label appears
2. **Cross-reference the triggering object ID** against the OSRS wiki pet pages
3. **If Root Cause C:** remove the offending ID from `PORTAL_LABELS`
4. **If Root Cause A/B:** replace the `isPortalObject()` `13615–13633` bypass with an explicit `PORTAL_LABELS.containsKey(id)` guard (Option B from ARCHITECTURE.md) — this is the most defensive and correct long-term approach regardless of root cause

**Risk:**
- Do not ship a fix without first reproducing the issue — the bug's specific mechanism determines the correct change
- The `13615–13633` bypass in `isPortalObject()` is over-broad: 8 of the 19 IDs in that range have no portal entry in `PORTAL_LABELS`, creating unnecessary exposure; tightening this is a good cleanup regardless of #39's outcome

---

### #35 — Yannille Not Labeled (Missing IDs)

**Status:** IDs are NOT missing. All 8 Yanille-related IDs are already present in `PORTAL_LABELS`:
- Watchtower variants (pre-Hard Ardougne Diary): 33096, 33102, 33108, 56047 → `"Watchtower"`
- Yanille variants (post-Hard Ardougne Diary): 33097, 33103, 33109, 56048 → `"Yanille"`

**Fix:** No ID additions needed. Investigate as a display/rendering bug:
1. Verify in-game with a player who has completed the Hard Ardougne Diary
2. Check whether the `isPortalObject()` guard is filtering out the IDs (composition name check failure)
3. Add debug logging to `PortalNameEventSubscriber` to confirm which object ID is observed in-game vs. what the map contains
4. If the portal is on an upper floor — this would be fixed by the #41 multi-plane fix

**Risk:** Issue #35 may self-resolve once #41 is fixed, if the player's Yanille portal chamber was built on an upper floor. This is the most likely explanation given that all 8 IDs are accounted for in the code.

---

## Implementation Order (Recommended)

Fix these in dependency order — #41 first because it may close #35 as a side effect, and #40 is self-contained data entry. Hold #39 until reproduced.

### 1. Fix #41 — Multi-plane scan (highest leverage, may close #35)

Change `PortalNameOverlay.java` in two places:
- `inPoh` detection loop: scan all planes
- Portal render loop: scan all planes, pass `tile.getPlane()` to `localToCanvas()`

This is the highest-risk change (refactoring the core scan loop) but has the most impact: it fixes the structural bug and likely resolves #35 without any further changes.

### 2. Fix #40 — Add 40 new IDs (self-contained data entry)

Add IDs to `PORTAL_LABELS`, update `isPortalObject()` range checks, update `updatePortalColors()`, and add config items. This is purely additive — zero risk of breaking existing behavior.

### 3. Investigate #35 — Verify post-#41

After shipping #41, ask the #35 reporter to re-test. If still broken, proceed with debug logging to identify the in-game object ID.

### 4. Investigate #39 — Reproduce before fixing

Do not attempt a code fix until the triggering pet and label are confirmed. Once reproduced, apply the targeted fix (ID removal or `isPortalObject()` guard tightening).

---

## Critical Warnings

> These pitfalls from PITFALLS.md must not be ignored during implementation.

**⚠ #41 — Pass the portal's plane, not the player's plane, to `localToCanvas()`**
Using `client.getLocalPlayer().getWorldLocation().getPlane()` for upper-floor portals will compute label height from the wrong floor. Labels will appear at ground-level height regardless of which floor the portal is on. Use `tile.getPlane()` instead.

**⚠ #41 — Always null-check tiles**
`getTiles()` allocates the full `4×104×104` array but not all cells are populated. Not checking `if (tile == null) continue;` causes `NullPointerException` at runtime on upper-plane tiles. The existing single-plane code already does this — extend the same guard to the outer plane loop.

**⚠ #40 — Three files must be updated atomically**
Adding IDs to `PORTAL_LABELS` without adding entries to `updatePortalColors()` and `PortalNameConfig.java` is a silent failure in `MULTI + UNIQUE` color mode. There is no compile-time enforcement of this invariant. Missing `PortalNameConfig` entries can cause a null-pointer crash at color assignment.

**⚠ #40 — The `isPortalObject()` range must be extended**
The new IDs (60774–60819) fall outside the existing range checks. Without adding `(id >= 60774 && id <= 60783) || (id >= 60790 && id <= 60819)` to `isPortalObject()`, the new IDs will fall through to the composition name check — which should work (these objects are named "Portal" in the game cache), but the range check is more reliable and consistent with the existing pattern.

**⚠ #39 — Do not assume NPCs appear in `tile.getGameObjects()`**
They do not. The API makes this structurally impossible. Any fix that adds NPC filtering to the tile scan is solving the wrong problem and adds dead code.

**⚠ #39 — Do not ship a fix without reproducing the issue first**
The root cause is unconfirmed. Guessing at which ID to remove or which guard to add could introduce new bugs or silently remove valid portal labels.

---

## Files to Change

| File | Issues | Change |
|------|--------|--------|
| `src/.../PortalNameOverlay.java` | #41 | Replace single-plane `getTiles()[playerPlane]` slices with triple-nested all-plane loops (two locations: `inPoh` detection ~L272 and render loop ~L300) |
| `src/.../PortalNameOverlay.java` | #41 | Update `Perspective.localToCanvas()` call to use `tile.getPlane()` instead of player's plane |
| `src/.../PortalNameOverlay.java` | #40 | Add 40 `PORTAL_LABELS.put()` entries for the 10 new destinations |
| `src/.../PortalNameOverlay.java` | #40 | Extend `isPortalObject()` with two new range checks for 60774–60783 and 60790–60819 |
| `src/.../PortalNameOverlay.java` | #40 | Add color entries in `updatePortalColors()` for all 10 new destinations |
| `src/.../PortalNameConfig.java` | #40 | Add `@ConfigItem` methods for each new destination's default label color |
| `src/.../PortalNameOverlay.java` | #39 | TBD after reproduction — likely ID removal from `PORTAL_LABELS` or tightening `isPortalObject()` bypass |

**#35 — No code change expected.** Likely closes as a duplicate of #41 once the multi-plane scan is shipped.

---

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| #41 root cause | HIGH | Directly confirmed from `Scene.java` Javadoc and current code; fix pattern is unambiguous |
| #41 fix correctness | HIGH | `Constants.MAX_Z`, `tile.getPlane()`, and `localToCanvas()` plane semantics all verified from RuneLite source |
| #40 new IDs | HIGH | All 40 IDs sourced from OSRS wiki infoboxes; sequential pattern unambiguous |
| #40 label strings | MEDIUM | Based on existing codebase short-label convention; "Teleport to Boat" may need shortening |
| #35 non-bug conclusion | HIGH | All 8 IDs verified present in `PortalNameOverlay.java` lines 31–171 |
| #35 actual root cause | LOW | In-game verification required; most likely explained by #41 fix |
| #39 root cause | MEDIUM | Mechanism is understood (NPCs not in `getGameObjects()`), specific colliding ID unconfirmed |
| #39 fix | LOW | Cannot specify exact fix without in-game reproduction |

---

## Sources

- `Scene.java` — https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Scene.java
- `Constants.java` — https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Constants.java
- `Tile.java` — https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Tile.java
- `Perspective.java` — https://github.com/runelite/runelite/raw/refs/heads/master/runelite-api/src/main/java/net/runelite/api/Perspective.java
- `ObjectComposition.java` — https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/ObjectComposition.java
- `WorldView.java` — https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/WorldView.java
- `NpcID.java` — https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/gameval/NpcID.java
- `ObjectID.java` — https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/gameval/ObjectID.java
- OSRS Wiki — Portal (Construction): https://oldschool.runescape.wiki/w/Portal_(Construction)
- `PortalNameOverlay.java` — codebase, lines 31–485
- `PortalNameEventSubscriber.java` — codebase, lines 40–64

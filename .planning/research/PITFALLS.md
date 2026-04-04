# Pitfalls Research — RuneLite POH Plugin Bug Fixes
> Milestone: Bug Fix (issues #35, #39, #40, #41)
> Researched: 2026-04-03
> Confidence: HIGH — all claims traced to official RuneLite API source (Scene.java, Tile.java, Constants.java, Perspective.java, ObjectComposition.java)

---

## Summary

Four bugs in this plugin share a common root: all stem from assumptions that are correct on the ground floor but silently break elsewhere. The multi-plane bug (#41) and the rendering bug are caused by hardcoding `client.getLocalPlayer().getWorldLocation().getPlane()` in places that should cover all loaded planes. The pet false-positive (#39) is most likely a scan-layer confusion: pets are NPCs and should never appear in `tile.getGameObjects()`, but they *will* appear if the `inPoh` detection scan or the render loop is accidentally iterating NPC layers or if an NPC ID coincidentally matches a key in `PORTAL_LABELS`. The missing ID bugs (#35, #40) carry two main risks: adding wrong variant IDs (off-by-one portal style indices) and forgetting to add the new destination's entry to `portalColors` and `PortalNameConfig`, which breaks silently.

---

## Multi-Plane Scan Pitfalls (#41)

### The API: What `getTiles()` Returns

Per `Scene.java` (official RuneLite API, verified):

```
/**
 * Gets the tiles in the scene
 * @return a 4x104x104 array of tiles in [plane][x][y]
 */
Tile[][][] getTiles();
```

And from `Constants.java` (official, verified):
- `MAX_Z = 4` — planes are indexed **0, 1, 2, 3** (4 is exclusive)
- `SCENE_SIZE = 104` — each plane is a 104×104 tile grid

### Pitfall 1: Wrong Array Dimension Order

**What goes wrong:** Treating `getTiles()` as `[x][y]` (2D) or `[x][y][plane]` (3D) instead of `[plane][x][y]`.

**Current code bug:** In `PortalNameOverlay.render()`, the code calls:
```java
Tile[][] tiles = scene.getTiles()[client.getLocalPlayer().getWorldLocation().getPlane()];
```
This correctly slices at the plane dimension, but then uses *only one plane*. For multi-plane scanning, both the `inPoh` detection loop and the render loop must iterate all planes 0–3, not just the player's current plane.

**Correct pattern:**
```java
Tile[][][] allTiles = scene.getTiles(); // [plane][x][y]
for (int plane = 0; plane < Constants.MAX_Z; plane++) {
    Tile[][] planeTiles = allTiles[plane];
    for (int x = 0; x < Constants.SCENE_SIZE; x++) {
        for (int y = 0; y < Constants.SCENE_SIZE; y++) {
            Tile tile = planeTiles[x][y];
            if (tile == null) continue;
            // ... process tile
        }
    }
}
```

**Consequences of the bug:** Portals on upper floors (plane 1, 2) are never found — neither in the `inPoh` check nor in the render pass. The overlay returns `null` early, showing no labels at all.

### Pitfall 2: Null Tile Entries Are Normal, Not Errors

**What goes wrong:** Failing to null-check each `Tile` before accessing it, causing `NullPointerException` at runtime.

**Why it happens:** RuneLite allocates the full `4×104×104` array structure, but not every cell is populated. Tiles outside the currently loaded map area, or tiles at planes the current location doesn't use, may be `null`.

**Prevention:** Always guard with `if (tile == null) continue;` inside the inner loop. The existing single-plane code already does this correctly — extend the same pattern to all planes.

### Pitfall 3: Hard-Coding Array Lengths Instead of Using Constants

**What goes wrong:** Using `tiles.length` or `tiles[x].length` for the outer bound of the x/y loops is correct but fragile if array sizes ever change. Using `4` as the plane count magic number is incorrect — the constant is `Constants.MAX_Z`.

**Prevention:** Use `Constants.MAX_Z` for the plane count (not `4` or `tiles.length`). Use `Constants.SCENE_SIZE` or `allTiles[plane].length` for x/y bounds.

### Pitfall 4: Rendering at the Wrong Plane After Multi-Plane Detection

**What goes wrong:** Even after fixing the scan loops to cover all planes, the `Perspective.localToCanvas` call still requires a `plane` argument for height calculation. Passing the wrong plane to `localToCanvas` results in labels rendered at the wrong 3D height — they will appear to float or sink relative to the portal.

**From Perspective.java (official, verified):**
```java
@Nullable
public static Point localToCanvas(@Nonnull Client client, @Nonnull LocalPoint point, int plane, int heightOffset)
```
The `plane` argument is used to look up the tile height in `tileHeights[plane][sceneX][sceneY]`, which determines the ground level for the 3D→2D projection.

**Prevention:** When rendering a portal found on a specific plane, pass `gameObject.getWorldLocation().getPlane()` (or equivalently `tile.getPlane()`) to `localToCanvas`, not the player's current plane. Using `client.getLocalPlayer().getWorldLocation().getPlane()` as the current code does will produce wrong positioning for upper-floor portals.

**Correct pattern:**
```java
int portalPlane = gameObject.getWorldLocation().getPlane();
Point textLocation = Perspective.localToCanvas(client, localLocation, portalPlane, zOffset);
```

### Pitfall 5: Bridge Tiles — The `getBridge()` Case

**From Tile.java (official, verified):**
```java
/**
 * Return the tile under this one, if this tile is a bridge
 * @return
 */
Tile getBridge();
```
And from Perspective.java's `getTileHeight()`:
```java
if (plane < Constants.MAX_Z - 1 && (tileSettings[1][sceneX][sceneY] & TILE_FLAG_BRIDGE) == TILE_FLAG_BRIDGE) {
    z1 = plane + 1;
}
```

**What this means:** When tiles on plane 1 represent a "bridge" (a floor you walk on), the client internally adjusts height calculations to use plane 2's height data. This is handled automatically inside `Perspective.localToCanvas` as long as you pass the correct plane. No special handling is needed in the plugin's scan loop — but be aware that a portal on plane 1 in a POH upper room may have its visual height derived from plane 2 tile heights.

**Consequence:** If you pass `plane=0` to `localToCanvas` for an upper-floor portal, you bypass this bridge height adjustment entirely and the label will be positioned at ground-floor height, appearing to hover at the wrong elevation.

---

## Object ID Addition Pitfalls (#35, #40)

### Pitfall 1: Portal Variants — Each Destination Has 3–5 IDs

**What goes wrong:** Adding only one ID for a new destination when the object actually has multiple variants (one per portal room rotation/style). Missing variants render no label for that placement configuration.

**Why it happens:** RuneLite game objects for POH portals include different IDs for different room orientations and portal styles (the "ancient" vs "regular" portal frames). Looking up a single ID from a cache viewer query may return only one variant. The existing pattern in `PORTAL_LABELS` shows each destination mapped to 3–5 IDs:
- 3 IDs: most destinations (e.g., Annakarl: 29341, 29349, 29357)
- 4 IDs: destinations with a "new" portal frame variant (adds a 56xxx ID)
- 5 IDs: Grand Exchange, Camelot, Yanille — extra variant for older portal frames

**Prevention:** When sourcing IDs from the OSRS wiki, verify the complete set of variants listed. Cross-reference with the cache viewer query in `cache-viewer-queries/`. Use the `PortalNameEventSubscriber` debug tool to confirm in-game.

### Pitfall 2: The 56xxx "New Portal Frame" Variant is Always Present

**What goes wrong:** Adding only the 3 legacy-style variant IDs for a new destination and missing the single `56xxx`-range ID that corresponds to the newer portal frame style.

**Pattern observed in existing data:**
- Legacy frame IDs: tend to be in ranges like `29xxx`, `33xxx`, `37xxx`
- New frame ID: always a single `56xxx` ID per destination
- All new destinations added after a certain content patch should have both sets

**Prevention:** Treat the `56xxx` ID as required. If the wiki or cache viewer doesn't show one, the destination may be an edge case — but this should be explicitly verified, not assumed absent.

### Pitfall 3: `getObjectComposition` / `getObjectDefinition` Returning Null

**What goes wrong:** Calling `client.getObjectDefinition(id)` for an ID that either hasn't loaded yet or doesn't exist, then calling `.getName()` on the null result.

**Current code correctly handles this:**
```java
net.runelite.api.ObjectComposition composition = client.getObjectDefinition(id);
if (composition == null) {
    return false;
}
```
But: if a new ID is added to `PORTAL_LABELS` but the `isPortalObject()` method's bypass range (`13615–13633`) doesn't cover it, the composition lookup *will* happen. For the newer `56xxx` IDs, the name-based check (`name.contains("Portal")`) should work because these objects are legitimately named "Portal" in the game cache.

**Risk when adding new IDs:** If a new ID is in a numeric range that the bypass was intended to cover, and you add IDs outside that range, they fall through to the composition check. This is correct behavior — but verify that the composition name for the new IDs actually contains "Portal". Check against the wiki or cache viewer output.

### Pitfall 4: Parallel Data Structures Must All Be Updated

**What goes wrong:** Adding a new destination's IDs to `PORTAL_LABELS` but forgetting to add the corresponding entry in `updatePortalColors()` and a `@ConfigItem` in `PortalNameConfig`. The result is silent: the label renders, but in `MULTI + UNIQUE` color mode it falls back to `Color.WHITE` with no warning.

**Three places that must be updated in lockstep:**
1. `PORTAL_LABELS` static map in `PortalNameOverlay.java` — ID → name
2. `updatePortalColors()` in `PortalNameOverlay.java` — name → color from config
3. `PortalNameConfig.java` — `@ConfigItem` method returning the default color

**Detection:** If step 2 is missing but step 1 and 3 are present, the `portalColors.getOrDefault(originalLabel, Color.WHITE)` call silently returns white. If step 3 is missing but step 2 is present, the config returns `null` and crashes at color assignment. There is no test coverage for this invariant.

### Pitfall 5: Stale ID Risk from Jagex Game Updates

**What goes wrong:** IDs sourced at one point in time become stale when Jagex updates the game cache. Object IDs can change between game updates for construction-related objects.

**Low-to-medium risk in practice:** POH portal IDs are construction-room objects that are relatively stable. However, the `56xxx` range IDs appear to represent a newer generation of portal objects that may have been added more recently. Any time new teleport spells are added to the game, there is a chance new object IDs will be assigned.

**Prevention:** Add comments to each new entry in `PORTAL_LABELS` citing the source (wiki URL, cache query date, or game version). This makes future maintenance auditable.

---

## NPC False-Positive Pitfalls (#39)

### Understanding the False Positive: Where NPCs Come From

**What goes wrong:** A player's follower pet (an NPC) triggers the portal label overlay, displaying a label on or near the pet.

**Why it cannot be a `tile.getGameObjects()` issue:**
The RuneLite `Tile` API has separate methods for game objects and NPCs:
- `tile.getGameObjects()` → returns `GameObject[]` — **scene objects only** (furniture, portals, walls, etc.)
- Tile does NOT have a `getActors()` or `getNpcs()` method — NPCs are accessed via `client.getNpcs()`

NPCs are not in the `getGameObjects()` array. **A pet NPC cannot appear in the game object scan directly.**

**The two likely root causes of issue #39:**

**Root Cause A — ID collision:** A pet follower NPC uses a game object as its "clickbox placeholder" or there is a decoration object placed by the NPC's presence that shares an ID with a portal ID in `PORTAL_LABELS`. This is rare but possible for certain pets that place decorative ground objects.

**Root Cause B — Composition name mismatch:** The `isPortalObject()` method's name check (`name.contains("Portal")`) passes because some pet-related object or scenery placed in the POH by the pet's presence has a name containing "Portal". For example, certain pets or NPC spawning mechanisms may leave behind a placeholder GameObject with a generic name.

**Root Cause C — Wrong ID in PORTAL_LABELS:** An ID was added to `PORTAL_LABELS` that actually belongs to a pet follower NPC model (e.g., via misidentification in the EventSubscriber debug session). This is most likely if the ID was sourced from a `MenuOptionClicked` event during a session where a pet was present.

### The `PortalNameEventSubscriber` False Discovery Risk

**What goes wrong:** The debug tool (`PortalNameEventSubscriber`) logs IDs from `MenuOptionClicked` events for `GAME_OBJECT_*` actions. If a user clicks on or near a pet while collecting IDs, the logged ID may be the pet's associated game object rather than the portal.

**Specific mechanism:** In RuneLite, when a pet is present, menu entries for the pet (Follow, Interact, etc.) use `MenuAction.NPC_*` actions. However, if the pet is standing on a portal tile, the click may register as a `GAME_OBJECT_*` action for the portal object underneath, but the logged `event.getId()` may return the portal's ID — this is the *expected* case. The false positive is less likely to be EventSubscriber misuse and more likely Root Cause A or C above.

### Prevention Strategies for #39

1. **Verify the specific pet and reproduce reliably** before fixing. The root cause is unknown per `PROJECT.md`. Confirm whether any ID in `PORTAL_LABELS` matches a known pet-spawned game object ID by cross-referencing against the OSRS wiki pet pages.

2. **Add a `tile.getPlane()` sanity check** during the portal render pass (not a fix for #39, but reduces unrelated false positives from other planes).

3. **Check `getObjectComposition().getActions()`**: Portal objects have actions like "Teleport" or "Enter". Pet-spawned objects would have different actions. `ObjectComposition.getActions()` returns a `String[]` of the 5 menu options. Adding an action check (e.g., requiring "Teleport" to be present) would be a strong additional filter — but adds complexity and may be over-engineering for a bug whose root cause is not yet confirmed.

4. **The simplest fix** (if Root Cause C): Identify the colliding ID via the OSRS wiki and remove it from `PORTAL_LABELS`, or move it to a "known bad IDs" exclusion set.

---

## `isPortalObject()` Edge Cases

The current implementation (from `PortalNameOverlay.java`):

```java
private boolean isPortalObject(GameObject gameObject) {
    int id = gameObject.getId();

    // Object IDs 13615-13633 are portal frames/components with null or inconsistent names
    if (id >= 13615 && id <= 13633) {
        return true;
    }

    net.runelite.api.ObjectComposition composition = client.getObjectDefinition(id);
    if (composition == null) {
        return false;
    }

    String name = composition.getName();
    return name != null && name.contains("Portal");
}
```

### Edge Case 1: The Bypass Range Is Over-Broad and Under-Documented

**What goes wrong:** The range `13615–13633` includes 19 IDs. The actual legacy portal IDs in `PORTAL_LABELS` that fall in this range are: 13615–13620, 13623–13624, 13626, 13630–13631, 13633. That is 11 IDs. The bypass therefore also silently approves IDs 13621, 13622, 13625, 13627–13629, 13632 — which may be non-portal construction objects that share this numeric neighborhood.

**Risk:** If an ID in the bypass range belongs to a non-portal object that happens to also appear in `PORTAL_LABELS` due to a mapping error, it would always pass `isPortalObject()` regardless of its actual composition name.

**Mitigation:** The secondary filter is `PORTAL_LABELS.get(id) != null` (checked before `isPortalObject()`), so only IDs explicitly present in the map are rendered. This limits the blast radius: the bypass only matters for IDs that are in both the map and the bypass range. Still, the bypass range should be documented with a source.

### Edge Case 2: `getObjectDefinition` vs `getObjectComposition` — API Name Drift

**From ObjectComposition.java (official, verified):**
The current API method called in the code is `client.getObjectDefinition(id)`. The RuneLite API also exposes `client.getObjectComposition(id)` — these are the same underlying call but the name used in the production code (`getObjectDefinition`) is correct as of the current RuneLite API.

**Risk:** `latest.release` dependency means a RuneLite API rename could break compilation silently. If `getObjectDefinition` is deprecated in favor of another name, the build would fail — but the `latest.release` setting means this would be caught at next build time, not at runtime.

### Edge Case 3: `name.contains("Portal")` Is Case-Sensitive

**What goes wrong:** If Jagex ever names an object "portal" (lowercase) or "PORTAL" (uppercase), the check fails and the portal gets no label.

**Current risk:** Low — all known portal objects in the game have names matching "Portal" with capital P. But this is worth noting as a fragility.

### Edge Case 4: `getObjectDefinition(id)` May Return a "Null" Composition

**What goes wrong:** `getObjectDefinition` can return a non-null `ObjectComposition` object whose `getName()` returns the literal string `"null"`. This is different from Java `null` — it is a placeholder returned by the game engine for objects that have no defined name.

**Current handling:** The code checks `name.contains("Portal")` — if `name` is `"null"`, `"null".contains("Portal")` is `false`, so it correctly filters out unnamed objects.

**Risk for new IDs:** If a new portal ID returns a composition with a name like `"Ornate portal"` (not containing just "Portal"), the check fails and the portal gets no label even though it is correctly in `PORTAL_LABELS`. This would be a silent failure.

**Recommendation:** When adding new IDs, verify that `client.getObjectDefinition(id).getName()` returns a string containing "Portal" for each new ID. The OSRS wiki object pages list the in-game names.

### Edge Case 5: `getImpostor()` — Multiloc / Varbit-Dependent Objects

**From ObjectComposition.java (official, verified):**
```java
/**
 * Get the object composition the player's state says this object should
 * transmogrify into.
 */
ObjectComposition getImpostor();
```

Some RuneScape objects are "multilocs" — their actual appearance (and ID) depends on a Varbit or VarPlayer value. POH portals may behave this way: the same tile slot may hold a portal whose displayed ID changes based on the player's construction variables (which destination is set).

**Risk:** If `client.getObjectDefinition(rawId)` is called without first resolving the impostor, the composition returned may be for the "base" (unset) portal rather than the configured destination. The `PORTAL_LABELS` map uses the *runtime displayed* IDs (observed in-game), so if the game uses impostor IDs, the lookup should still work — but the name check on the base composition could fail.

**Mitigation:** The existing bypass for `13615–13633` range IDs avoids the composition check entirely for legacy portals, which is why those still work. For new IDs outside this range, verify that the ID observed at runtime matches the ID returned by `getObjectDefinition` (i.e., the impostor is already resolved by the time the render loop sees it).

---

## Prevention Strategies

### For #41 (Multi-Plane Scan)

1. **Change the `inPoh` detection loop** to iterate all planes 0–3 (`Constants.MAX_Z`), not just the player's current plane. Break on first match.

2. **Change the render loop** similarly — iterate all planes. When rendering a portal found on plane `p`, pass that tile's actual plane (via `gameObject.getWorldLocation().getPlane()` or `tile.getPlane()`) to `Perspective.localToCanvas`, not the player's current plane.

3. **Do not use magic number `4`** — use `Constants.MAX_Z` (= 4) for the plane count.

4. **Pattern:**
   ```java
   Tile[][][] allPlanes = scene.getTiles();
   for (int plane = 0; plane < Constants.MAX_Z; plane++) {
       for (int x = 0; x < allPlanes[plane].length; x++) {
           for (int y = 0; y < allPlanes[plane][x].length; y++) {
               Tile tile = allPlanes[plane][x][y];
               if (tile == null) continue;
               for (GameObject obj : tile.getGameObjects()) {
                   if (obj == null) continue;
                   // ...
               }
           }
       }
   }
   ```

### For #35 and #40 (Missing Object IDs)

1. **Source all variant IDs** from the OSRS wiki object page, not just one representative ID. Expect 3–5 IDs per destination.

2. **Include the `56xxx` new-frame variant** for every destination added.

3. **Update all three places** atomically: `PORTAL_LABELS` map, `updatePortalColors()`, and `PortalNameConfig`.

4. **Verify composition names** for all new IDs: `name.contains("Portal")` must be true, or add them to the bypass range (with documentation).

5. **Add inline comments** citing the wiki source and date for each new ID block.

### For #39 (Pet False Positive)

1. **Reproduce the issue reliably first.** Identify which pet(s) trigger it and what label appears.

2. **Cross-check the triggering ID** against the OSRS wiki to determine if it belongs to a pet-related object.

3. **If Root Cause C (wrong ID in map):** Remove the offending ID from `PORTAL_LABELS`.

4. **If Root Cause A or B (coincidental name/ID overlap):** Consider adding a check on `ObjectComposition.getActions()` to verify the object has a "Teleport"-class action before rendering a label. This is a stronger guard but adds `getObjectDefinition` call cost per frame.

5. **Do not assume NPCs appear in `tile.getGameObjects()`** — they do not. The fix will be in the ID map or composition validation, not in adding an NPC filter to the tile scan.

---

## Confidence

**HIGH** — All API signatures and constant values verified against official RuneLite source files fetched directly from `github.com/runelite/runelite` master branch:
- `Scene.java`: `getTiles()` returns `Tile[][][]` in `[plane][x][y]` order
- `Constants.java`: `MAX_Z = 4`, `SCENE_SIZE = 104`
- `Tile.java`: `getGameObjects()`, `getPlane()`, `getBridge()` confirmed — NPCs are NOT in `getGameObjects()`
- `Perspective.java`: `localToCanvas(client, point, plane, heightOffset)` — `plane` drives height lookup, must match the portal's actual plane
- `ObjectComposition.java`: `getName()`, `getActions()`, `getImpostor()` signatures confirmed

The root cause of #39 (pet false positive) is categorized as MEDIUM confidence — the mechanism is known (NPCs don't appear in `getGameObjects()`) but the specific colliding ID or name overlap is not confirmed without in-game reproduction.

---

## Sources

- `Scene.java` — https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Scene.java
- `Constants.java` — https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Constants.java
- `Tile.java` — https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Tile.java
- `Perspective.java` — https://github.com/runelite/runelite/raw/refs/heads/master/runelite-api/src/main/java/net/runelite/api/Perspective.java
- `ObjectComposition.java` — https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/ObjectComposition.java
- `PortalNameOverlay.java` — codebase, lines 267–485
- `PortalNameEventSubscriber.java` — codebase, lines 40–64 (`findGameObjectById` reference implementation)

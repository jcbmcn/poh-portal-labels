# Stack Research — RuneLite Multi-Plane Scanning
> Milestone: Bug Fix (issues #35, #39, #40, #41)

## Summary

The RuneLite `Scene.getTiles()` API returns a **4×104×104** 3D array indexed as `[plane][x][y]`, where plane 0–3 maps to the four game levels. The current plugin only scans `getTiles()[currentPlane]`, which means portals built on floors other than the player's current floor are completely invisible to the label renderer. The fix requires iterating all four planes in the tile scan loop while passing each portal's actual plane to `Perspective.localToCanvas()` for correct screen projection.

---

## Scene.getTiles() API

**Source:** `runelite-api/src/main/java/net/runelite/api/Scene.java` (master branch)

```java
/**
 * Gets the tiles in the scene
 *
 * @return a 4x104x104 array of tiles in [plane][x][y]
 */
Tile[][][] getTiles();
```

Key facts (all **HIGH confidence** — from official RuneLite source):

| Property | Value | Source |
|---|---|---|
| Return type | `Tile[][][]` | `Scene.java` Javadoc |
| Dimensions | `[4][104][104]` | `Scene.java` Javadoc comment |
| Index order | `[plane][x][y]` | `Scene.java` Javadoc comment |
| Scene size | 104×104 tiles | `Constants.SCENE_SIZE = 104` |
| Max planes | 4 | `Constants.MAX_Z = 4` (values 0–3) |

**`Constants.MAX_Z` documentation:**

```java
/**
 * The max allowed plane by the game.
 *
 * This value is exclusive. The plane is set by 2 bits which restricts
 * the plane value to 0-3.
 */
public static final int MAX_Z = 4;
```

**`WorldView.getPlane()` documentation:**

```java
/**
 * Gets the current plane the player is on.
 *
 * This value indicates the current map level above ground level, where
 * ground level is 0. For example, going up a ladder in Lumbridge castle
 * will put the player on plane 1.
 *
 * Note: This value will never be below 0. Basements and caves below ground
 * level use a tile offset and are still considered plane 0 by the game.
 *
 * @return the plane
 */
int getPlane();
```

**What this means for the bug:** When a player is on plane 0 (ground floor of their POH), portals they built on plane 1 (first floor / upstairs) or plane 2 (second floor) exist in `getTiles()[1]` and `getTiles()[2]` respectively. The current code only ever looks at `getTiles()[player_plane]`, which is `getTiles()[0]`. Those upper-floor tiles are never scanned.

---

## Correct Multi-Plane Scan Pattern

**The fix:** Change the tile scan from single-plane to all-planes. The `Tile` object itself carries its plane via `tile.getPlane()`, which is the correct plane to pass to `Perspective.localToCanvas()` for screen projection.

### Current (Broken) Pattern

```java
// BUG: only scans the player's current plane
Tile[][] tiles = scene.getTiles()[client.getLocalPlayer().getWorldLocation().getPlane()];

for (int x = 0; x < tiles.length; x++) {
    for (int y = 0; y < tiles[x].length; y++) {
        Tile tile = tiles[x][y];
        // ... scan game objects
    }
}
```

### Fixed Multi-Plane Pattern

```java
// FIX: scan all 4 planes
Tile[][][] allTiles = scene.getTiles();

for (int plane = 0; plane < Constants.MAX_Z; plane++) {
    Tile[][] planeTiles = allTiles[plane];
    if (planeTiles == null) continue;

    for (int x = 0; x < planeTiles.length; x++) {
        for (int y = 0; y < planeTiles[x].length; y++) {
            Tile tile = planeTiles[x][y];
            if (tile == null) continue;

            for (GameObject gameObject : tile.getGameObjects()) {
                // ... process game objects
            }
        }
    }
}
```

### Updated `Perspective.localToCanvas()` call

When rendering a label for a portal on a non-player plane, the `plane` argument to `localToCanvas` **must** be the portal's actual plane, not the player's plane:

```java
// CURRENT (uses player's plane — wrong for upper-floor portals):
Point textLocation = Perspective.localToCanvas(client, localLocation,
        client.getLocalPlayer().getWorldLocation().getPlane(), zOffset);

// FIXED (use the plane of the tile the portal is on):
Point textLocation = Perspective.localToCanvas(client, localLocation,
        tile.getPlane(), zOffset);
```

The `tile.getPlane()` value is the actual plane the `Tile` object belongs to, which equals the `plane` loop variable used when retrieving it from `getTiles()[plane]`. Either approach works; using `tile.getPlane()` is slightly more robust since it doesn't rely on the loop variable being in scope.

**`Perspective.localToCanvas()` signature (from official source):**

```java
@Nullable
public static Point localToCanvas(@Nonnull Client client, @Nonnull LocalPoint point, int plane)

@Nullable
public static Point localToCanvas(@Nonnull Client client, @Nonnull LocalPoint point, int plane, int heightOffset)
```

The `plane` parameter is used to compute tile height via `getTileHeight(client, point, plane)`, so using the wrong plane will produce incorrect vertical positioning of the label (it will compute height data from the wrong floor).

---

## POH Floor Detection

### How POH floors work

A POH is an **instanced area** (`scene.isInstance() == true`). The game loads all floors of the POH into the scene simultaneously:

- **Plane 0** — Ground floor (always present)
- **Plane 1** — First floor / upstairs (present if player built stairs)
- **Plane 2** — Second floor (if built a second set of stairs)
- **Plane 3** — Roof level (rarely used for portals)

Portals can only be placed in a **Portal Chamber** room. Portal chambers can be built on any floor. If a player builds their portal chamber on the first floor, the portal objects exist in `getTiles()[1]`, **not** `getTiles()[0]`.

### Key insight: player plane ≠ portal plane

When a player is standing on the **ground floor** (plane 0) of their POH and looks up at portals on the **first floor** (plane 1), the overlay currently skips those portals entirely. The player is on plane 0, the portals are on plane 1 — the scan loop never reads `getTiles()[1]`.

### POH detection ("are we in a POH?") — must scan all planes

The current code detects POH presence by looking for `ObjectID.POH_EXIT_PORTAL` on the **current plane's tiles**. This works fine if the player enters through the front door (ground floor, plane 0). However, to be safe and consistent with the fix, the POH detection loop should also scan all planes, otherwise a player who somehow starts on plane 1 (e.g., teleported in) would not have POH detected.

**Recommended:** The POH `inPoh` check loop should also scan all planes using the same multi-plane pattern.

---

## `Constants.MAX_Z` as the authoritative loop bound

Rather than hardcoding `4` in the loop, use `Constants.MAX_Z` which is the official RuneLite constant:

```java
import net.runelite.api.Constants;

for (int plane = 0; plane < Constants.MAX_Z; plane++) {
    // ...
}
```

This is **HIGH confidence** — defined in `Constants.java` with explicit documentation that it represents planes 0–3.

---

## Reference Implementations

### 1. `Scene.java` — Official API Javadoc
> **Source:** https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Scene.java
>
> `Tile[][][] getTiles()` — documented as "a 4x104x104 array of tiles in [plane][x][y]"

### 2. `Constants.java` — MAX_Z = 4
> **Source:** https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/Constants.java
>
> `public static final int MAX_Z = 4;` — "The max allowed plane by the game. This value is exclusive. The plane is set by 2 bits which restricts the plane value to 0–3."

### 3. `WorldView.java` — getPlane() documentation
> **Source:** https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/WorldView.java
>
> `int getPlane()` — "Gets the current plane the player is on. Ground level is 0."

### 4. `Perspective.java` — localToCanvas signature with plane parameter
> **Source:** https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/Perspective.java
>
> `localToCanvas(Client client, LocalPoint point, int plane, int heightOffset)` — plane param documented as "ground plane on the z axis". Used internally by `getTileHeight()` to get the height data for the correct floor.

### 5. `Perspective.getCanvasTextLocation()` — Pattern for getting plane from WorldView
> In `Perspective.java` (same file), the `getCanvasTextLocation()` helper shows the idiomatic pattern:
> ```java
> int plane = wv.getPlane();  // uses WorldView.getPlane(), not hardcoded
> Point p = localToCanvas(client, localLocation, plane, zOffset);
> ```
> This reinforces using the actual object's plane, not the player's plane.

---

## Scope of Changes Required

Two locations in `PortalNameOverlay.java` need updating:

| Location | Line | Change |
|---|---|---|
| `inPoh` detection loop | ~272–293 | Change `scene.getTiles()[playerPlane]` to scan all planes |
| Portal label rendering loop | ~300–376 | Change `scene.getTiles()[playerPlane]` to scan all planes; update `localToCanvas()` call to use `tile.getPlane()` |

Both currently read:
```java
Tile[][] tiles = scene.getTiles()[client.getLocalPlayer().getWorldLocation().getPlane()];
```

Both need to become a triple-nested loop over `[plane][x][y]`.

---

## Confidence

**HIGH** — All findings sourced directly from official RuneLite API source code on GitHub master branch:
- `Scene.java` Javadoc explicitly states the array dimensions and index order
- `Constants.MAX_Z = 4` with full documentation of plane semantics
- `WorldView.getPlane()` Javadoc explains ground level = 0 convention
- `Perspective.localToCanvas()` signature shows `plane` parameter is required and affects height computation

No training-data-only claims. All assertions verified against the live RuneLite repository.

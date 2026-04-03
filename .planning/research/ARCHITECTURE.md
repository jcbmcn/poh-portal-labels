# Architecture Research — NPC vs Game Object Disambiguation
> Milestone: Bug Fix (issue #39)

## Summary

In RuneLite's API, NPCs and game objects occupy completely separate collections — `tile.getGameObjects()` cannot return NPC entities. However, NPC IDs and Object IDs share the same integer namespace (both start at 0), so numeric values overlap between the two tables. The direct cause of issue #39 is not that NPCs appear in the game object scan, but that the `isPortalObject()` bypass for IDs 13615–13633 unconditionally returns `true` for any game object in that range — and some IDs in that range are legitimately shared between portal objects and non-portal objects that may appear in a POH scene (such as decorative or structural objects that happen to co-occupy the same ID range).

---

## How RuneLite Differentiates NPCs from Game Objects

### Separate Collections — NPCs Are NOT in `tile.getGameObjects()`

The `Tile` interface (verified from `runelite-api/src/main/java/net/runelite/api/Tile.java`) exposes:

| Method | Returns | Contains NPCs? |
|--------|---------|----------------|
| `tile.getGameObjects()` | `GameObject[]` | **No** — static/interactive world objects only |
| `tile.getGroundObject()` | `GroundObject` | **No** |
| `tile.getDecorativeObject()` | `DecorativeObject` | **No** |
| `tile.getWallObject()` | `WallObject` | **No** |
| `tile.getGroundItems()` | `List<TileItem>` | **No** |

NPCs are accessed exclusively via `Client`:
- `client.getNpcs()` — returns `List<NPC>` for all NPCs in the scene
- `NPC` extends `Actor`, has its own `getId()` that returns from the NPC ID namespace

**Conclusion:** It is architecturally impossible for an NPC to appear in `tile.getGameObjects()`. The plugin's tile scan cannot accidentally include NPCs.

### ID Namespaces Overlap Numerically

Despite being stored in separate collections, NPC IDs and Object IDs share the same integer value range — both grow from 0 into the tens of thousands. **The same integer (e.g., 13615) refers to a different entity depending on whether it is used as an NPC ID or an Object ID.** This is documented in separate `gameval/NpcID.java` and `gameval/ObjectID.java` files.

For the range 13615–13633 specifically:

| ID | As `NpcID` | As `ObjectID` |
|----|-----------|--------------|
| 13615 | `WARGUILD_HARRALLAK_NPC` | `POH_PORTAL_TEAK_VARROCK` |
| 13616 | `WARGUILD_SLOANE_NPC` | `POH_PORTAL_TEAK_LUMBRIDGE` |
| 13617 | `WARGUILD_YADECH_NPC` | `POH_PORTAL_TEAK_FALADOR` |
| 13618 | `SLAYER_MASTER_1_TUREAL` | `POH_PORTAL_TEAK_CAMELOT` |
| 13619 | `SLAYER_MASTER_1_AYA` | `POH_PORTAL_TEAK_ARDOUGNE` |
| 13620 | `SLAYER_MASTER_2_MAZCHNA` | `POH_PORTAL_TEAK_YANILLE` |
| 13621 | `SLAYER_MASTER_2_ACHTRYN_VIS` | *(not a portal object)* |
| 13622 | `SLAYER_MASTER_5_DURADEL` | `POH_PORTAL_MAG_VARROCK` |
| 13623 | `SLAYER_MASTER_5_KURADAL` | `POH_PORTAL_MAG_LUMBRIDGE` |
| 13624 | `RAT_LOWWANDER` | `POH_PORTAL_MAG_FALADOR` |
| 13625 | `WGS_ARMADYL_GUARDIAN_FEMALE_MULTI` | `POH_PORTAL_MAG_CAMELOT` |
| 13626 | `WGS_IDRIA_MULTINPC` | `POH_PORTAL_MAG_ARDOUGNE` |
| 13627 | `WGS_IDRIA_MULTINPC2` | `POH_PORTAL_MAG_YANILLE` |
| 13628 | `WGS_AKRISAE_MULTI` | *(not a named portal object)* |
| 13629 | `WGS_LUCIEN_SPY` | `POH_PORTAL_MARBLE_VARROCK` |
| 13630 | `WGS_SPY_MULTINPC` | `POH_PORTAL_MARBLE_LUMBRIDGE` |
| 13631 | `WGS_FARMER_DRUID` | `POH_PORTAL_MARBLE_FALADOR` |
| 13632 | `WGS_SPY2` | `POH_PORTAL_MARBLE_CAMELOT` |
| 13633 | `WGS_GHOMMAL_MULTI` | `POH_PORTAL_MARBLE_ARDOUGNE` |

The same integer values that correspond to NPC IDs in the NPC namespace correspond to portal objects in the Object namespace — but since these are different ID namespaces, NPC IDs are irrelevant to `tile.getGameObjects()`.

---

## Root Cause Analysis for Issue #39

### What is Actually Happening

The bug title says "pets in POH are displaying names of random portal destinations." However, since NPCs cannot appear in `tile.getGameObjects()`, the label is not being drawn **on** a pet. Instead:

**The label is drawn at a game object's world location, and that game object happens to be co-located with or near a pet NPC.** When a pet stands on a portal tile, both the portal game object and the pet NPC occupy the same tile. The portal is labeled normally — but visually, the floating text appears to be above the pet because the pet is standing right on top of the portal.

### The `isPortalObject()` Bypass — Where the Real Bug Lives

```java
private boolean isPortalObject(GameObject gameObject)
{
    int id = gameObject.getId();

    // Object IDs 13615-13633 are portal frames/components with null or inconsistent names
    // These are valid portal objects that should display labels
    if (id >= 13615 && id <= 13633)
    {
        return true;  // ← NO name check for this range
    }

    net.runelite.api.ObjectComposition composition = client.getObjectDefinition(id);
    if (composition == null)
    {
        return false;
    }

    String name = composition.getName();
    // Portal objects should have "Portal" in their name
    return name != null && name.contains("Portal");
}
```

This bypass was added because teak/mahogany/marble portal objects in this range have inconsistent or null `ObjectComposition` names. However, the bypass is **too broad**:

1. IDs **13621**, **13625**, **13628** have no portal object entry in `ObjectID.java` — yet the range bypass returns `true` for any game object with those IDs, even if it is not a portal.
2. The range includes **all three tiers** of portal objects (teak 13615–13620, mahogany 13622–13627, marble 13629–13634), but also IDs that Jagex may have reassigned to non-portal objects in newer game updates.
3. The `PORTAL_LABELS` map does **not** contain all 19 IDs in the range. Specifically, IDs `13621`, `13625`, `13628` are absent from `PORTAL_LABELS`, yet `isPortalObject()` would return `true` for game objects with those IDs — meaning if the `PORTAL_LABELS.get(id)` lookup precedes `isPortalObject()`, those won't render. But if a Jagex game update adds a non-portal object with one of the listed IDs (13615, 13619, 13626, etc.), the label would wrongly appear.

### The Visual Illusion: Why It Looks Like the Pet Has a Label

When a pet NPC sits on a portal tile:
1. The portal `GameObject` is on the tile
2. The plugin draws a label at `gameObject.getLocalLocation()` with a Z offset
3. The pet NPC is rendered at the same position
4. The floating text appears to hover over the **pet** when the pet obscures the portal

This is a **display positioning issue**, not an ID collision issue. The fix is not about NPC disambiguation — the plugin never touches NPCs.

### How `client.getObjectDefinition(id)` Behaves for NPC IDs

`getObjectDefinition(id)` queries the **object cache** using the provided integer as an object ID. If you pass an NPC ID (e.g., 13615 as `WARGUILD_HARRALLAK_NPC`), it will look up object 13615 in the object cache — which is `POH_PORTAL_TEAK_VARROCK`, not the NPC. The NPC definition is completely separate. Calling `getObjectDefinition` with an NPC ID returns the object at that integer in the object table — which may or may not be null, and may or may not have "Portal" in its name. In the 13615–13633 range, those object IDs genuinely ARE portal objects, so the composition lookup would actually work correctly if attempted — the bypass is unnecessary and overly permissive.

---

## Fix Strategy

### Option A: Remove the Bypass, Fix the Name Check (Recommended)

The `isPortalObject()` bypass was added to handle portals with null/inconsistent `ObjectComposition` names. But the actual behavior should be verified:

```java
private boolean isPortalObject(GameObject gameObject)
{
    int id = gameObject.getId();
    net.runelite.api.ObjectComposition composition = client.getObjectDefinition(id);

    if (composition == null)
    {
        return false;
    }

    String name = composition.getName();
    if (name == null)
    {
        return false;
    }

    // Check for "Portal" in name, OR check by explicit ID membership in PORTAL_LABELS
    return name.contains("Portal");
}
```

Since `PORTAL_LABELS.get(id)` is already checked before `isPortalObject()` in `render()`, the `isPortalObject()` guard only needs to confirm that the game object is actually a portal-type object — not a gate, decoration, or other structure that shares an ID. The existing `name.contains("Portal")` check for non-bypassed IDs is the correct approach.

**Why the bypass may have been wrong:** Teak portal objects (13615–13620) in OSRS actually have the name "Portal" — Jagex did not use null names for them. The bypass was likely added due to a misdiagnosis of an earlier bug. Testing without the bypass is the correct first step.

### Option B: Use an Explicit Allowlist (Most Defensive)

Replace the range bypass with an explicit set membership check against the `PORTAL_LABELS` keySet:

```java
private boolean isPortalObject(GameObject gameObject)
{
    int id = gameObject.getId();

    // Quick check: if the ID isn't even in our portal labels map, it can't be a portal we care about
    if (!PORTAL_LABELS.containsKey(id))
    {
        return false;
    }

    net.runelite.api.ObjectComposition composition = client.getObjectDefinition(id);
    if (composition == null || composition.getName() == null)
    {
        // For known portal IDs with inconsistent names, trust the PORTAL_LABELS map
        return true;
    }

    return composition.getName().contains("Portal");
}
```

This is the most defensive approach: an object must be in `PORTAL_LABELS` AND (have "Portal" in its name OR be a known portal ID with null/empty name). Since `render()` already calls `PORTAL_LABELS.get(id)` before `isPortalObject()`, this double-check adds zero false positives.

### Option C: Accept the Visual Behavior (Not Recommended)

Since the labels are drawn on portals (not pets), and pets just happen to stand on portals, one could argue this is expected behavior. However, users clearly find it confusing, so some mitigation is warranted. Options A or B should be implemented.

---

## Tile API Reference

```java
// RuneLite API — Tile interface (confirmed from source)
public interface Tile {
    GameObject[]       getGameObjects();       // static/interactive world objects
    DecorativeObject   getDecorativeObject();  // decorative overlay objects
    GroundObject       getGroundObject();      // ground-level objects  
    WallObject         getWallObject();        // wall objects
    ItemLayer          getItemLayer();         // dropped items
    List<TileItem>     getGroundItems();       // ground items
    WorldPoint         getWorldLocation();
    LocalPoint         getLocalLocation();
    int                getPlane();
    Tile               getBridge();            // tile below (for bridges)
    // NO getNpcs() — NPCs are not on tiles, they are accessed via Client
}

// Accessing NPCs (NOT via Tile):
// client.getNpcs() → List<NPC>
// Each NPC has: getId(), getName(), getWorldLocation(), getLocalLocation()
```

Key distinction: `tile.getGameObjects()` returns fixed scene objects placed by the game server on load. NPCs are mobile entities tracked separately in the client's NPC array, updated each game tick.

---

## Confidence

**HIGH** — based on direct inspection of official RuneLite source files:

- `Tile.java` from `runelite/runelite` master: confirms `getGameObjects()` returns `GameObject[]` with no NPC access
- `NPC.java` from `runelite/runelite` master: confirms NPCs extend `Actor`, separate from `TileObject`
- `NpcID.java` from `runelite/runelite` master: confirms NPC IDs 13615–13633 map to War Guild NPCs / Slayer Masters / WGS quest NPCs — completely unrelated to portals
- `ObjectID.java` from `runelite/runelite` master: confirms Object IDs 13615–13633 map to `POH_PORTAL_TEAK_*`, `POH_PORTAL_MAG_*`, `POH_PORTAL_MARBLE_*` — the exact portals in `PORTAL_LABELS`
- `PortalNameOverlay.java` (this repo): confirms the range bypass in `isPortalObject()` and that `PORTAL_LABELS.get(id)` is checked before `isPortalObject()` is called

**LOW confidence** on one point: whether teak/mahogany/marble portal `ObjectComposition` names are actually null/inconsistent in the current game cache (which motivated the bypass). This requires in-game verification. If portal names are non-null and contain "Portal", the bypass can be removed entirely without changing behavior for correctly-named portals.

### Sources
- `Tile.java`: https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Tile.java
- `NPC.java`: https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/NPC.java
- `NpcID.java`: https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/gameval/NpcID.java
- `ObjectID.java`: https://raw.githubusercontent.com/runelite/runelite/master/runelite-api/src/main/java/net/runelite/api/gameval/ObjectID.java
- `Client.java`: https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Client.java

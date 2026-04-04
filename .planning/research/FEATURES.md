# Feature Landscape: Portal Object ID Research

**Domain:** RuneLite Plugin — POH Portal Labels
**Researched:** 2026-04-03
**Issues:** #35 (Yanille portal not labeled), #40 (10 new teleport destinations added 2026-02-25)

---

## Summary

Two open bug reports require new portal object IDs to be added to `PortalNameOverlay.java`.

**Issue #35** concerns the Yanille portal not being labeled. Investigation shows the codebase already
contains all 8 Yanille-related IDs (both Watchtower and Yanille variants). Issue #35 may be a
labeling/display bug rather than purely missing IDs — needs in-game verification before closing.

**Issue #40** concerns 10 new teleport destinations added to OSRS on 25 February 2026. None of these
portal object IDs exist in the codebase yet. All 40 new object IDs (4 per destination × 10 destinations)
must be added to the PORTAL_LABELS map and the `isPortalObject()` range check.

---

## Yanille Portal IDs (Issue #35)

Source: https://oldschool.runescape.wiki/w/Portal_(Construction)

The wiki distinguishes two portals on the same article:

| Variant | Condition | Teak | Mahogany | Marble | Raging Echoes |
|---------|-----------|------|----------|--------|----------------|
| Yanille Watchtower Portal | Pre-Hard Ardougne Diary | 33096 | 33102 | 33108 | 56047 |
| Yanille Portal | Post-Hard Ardougne Diary | 33097 | 33103 | 33109 | 56048 |

### Current Code Status

**All 8 IDs are already present** in `PortalNameOverlay.java`:

```java
PORTAL_LABELS.put(33096, "Watchtower");
PORTAL_LABELS.put(33102, "Watchtower");
PORTAL_LABELS.put(33108, "Watchtower");
PORTAL_LABELS.put(56047, "Watchtower");

PORTAL_LABELS.put(33097, "Yanille");
PORTAL_LABELS.put(33103, "Yanille");
PORTAL_LABELS.put(33109, "Yanille");
PORTAL_LABELS.put(56048, "Yanille");
```

### Recommendation for Issue #35

The IDs are present. The bug is likely one of:
1. A display/rendering issue where the label doesn't show in-game
2. The portal object ID returned in-game differs from what the wiki documents
3. The `isPortalObject()` gate is filtering out a valid ID

**Action:** Verify in-game with a player who has completed the Hard Ardougne Diary. If the portal
renders with no label, add debug logging to confirm which object ID is observed vs. what the map contains.

---

## New Teleport Destination IDs (Issue #40)

Source: https://oldschool.runescape.wiki/w/Portal_(Construction)
Update date: 25 February 2026

Ten new teleport destinations were added to Construction portals. Each destination has 4 object IDs
covering 3 portal frame tiers (teak, mahogany, marble) plus the Raging Echoes variant.

### Standard Spellbook Additions

| Destination | Label (recommended) | Teak | Mahogany | Marble | Raging Echoes |
|-------------|---------------------|------|----------|--------|----------------|
| Trollheim | `"Trollheim"` | 60790 | 60800 | 60810 | 60774 |
| Teleport to Boat | `"Teleport to Boat"` | 60799 | 60809 | 60819 | 60783 |

### Ancient Magicks Spellbook Additions

| Destination | Label (recommended) | Teak | Mahogany | Marble | Raging Echoes |
|-------------|---------------------|------|----------|--------|----------------|
| Paddewwa | `"Paddewwa"` | 60791 | 60801 | 60811 | 60775 |
| Lassar | `"Lassar"` | 60792 | 60802 | 60812 | 60776 |
| Dareeyak | `"Dareeyak"` | 60793 | 60803 | 60813 | 60777 |

### Lunar Spellbook Additions

| Destination | Label (recommended) | Teak | Mahogany | Marble | Raging Echoes |
|-------------|---------------------|------|----------|--------|----------------|
| Ourania | `"Ourania"` | 60794 | 60804 | 60814 | 60778 |
| Barbarian Outpost | `"Barbarian"` | 60795 | 60805 | 60815 | 60779 |
| Port Khazard | `"Khazard"` | 60796 | 60806 | 60816 | 60780 |
| Ice Plateau | `"Ice Plateau"` | 60797 | 60807 | 60817 | 60781 |

### Arceuus Spellbook Additions

| Destination | Label (recommended) | Teak | Mahogany | Marble | Raging Echoes |
|-------------|---------------------|------|----------|--------|----------------|
| Respawn | `"Respawn"` | 60798 | 60808 | 60818 | 60782 |

### Label Name Rationale

- **Barbarian Outpost** → `"Barbarian"` — matches the existing pattern in the codebase where destinations
  use short single-word labels (e.g., `"Trollheim"`, `"Camelot"`, `"Falador"`). The wiki calls it
  "Barbarian Outpost Portal" but the label string should stay concise.
- **Port Khazard** → `"Khazard"` — same rationale; matches existing short-label convention.
- **Teleport to Boat** → `"Teleport to Boat"` — this is a multi-word name with no obvious shortened form;
  use full name. Consider `"Boat"` if label space is tight in the overlay.
- All other destinations use their canonical single-word OSRS spell name.

---

## ID Range Analysis

### Existing ID Bands

| Range | Count | Description |
|-------|-------|-------------|
| 13615–13633 | 19 | Special bypass range — `isPortalObject()` treats as always valid; no name lookup |
| 33080–33115 | ~36 | Original portal set (teak/mahogany/marble, all destinations) |
| 56038–56073 | ~36 | Second-generation portal set (Raging Echoes variants and newer additions) |

### New ID Band (Issue #40)

| Range | Count | Description |
|-------|-------|-------------|
| 60774–60783 | 10 | Raging Echoes variants for all 10 new destinations (sequential) |
| 60790–60799 | 10 | Teak variants for all 10 new destinations (sequential) |
| 60800–60809 | 10 | Mahogany variants for all 10 new destinations (sequential) |
| 60810–60819 | 10 | Marble variants for all 10 new destinations (sequential) |

The new IDs fall well outside any existing range. The `isPortalObject()` method must be updated to
include these ranges, or each ID must be checked individually. Given the sequential structure, a
range check `(id >= 60774 && id <= 60783) || (id >= 60790 && id <= 60819)` is the most efficient
approach and mirrors how the existing 13615–13633 bypass is implemented.

---

## Required Code Changes

### 1. `PortalNameOverlay.java` — PORTAL_LABELS map additions

Add 40 new entries to the static initializer (4 IDs × 10 destinations):

```java
// Standard Spellbook — New 2026-02-25
PORTAL_LABELS.put(60774, "Trollheim");
PORTAL_LABELS.put(60790, "Trollheim");
PORTAL_LABELS.put(60800, "Trollheim");
PORTAL_LABELS.put(60810, "Trollheim");

PORTAL_LABELS.put(60783, "Teleport to Boat");
PORTAL_LABELS.put(60799, "Teleport to Boat");
PORTAL_LABELS.put(60809, "Teleport to Boat");
PORTAL_LABELS.put(60819, "Teleport to Boat");

// Ancient Magicks — New 2026-02-25
PORTAL_LABELS.put(60775, "Paddewwa");
PORTAL_LABELS.put(60791, "Paddewwa");
PORTAL_LABELS.put(60801, "Paddewwa");
PORTAL_LABELS.put(60811, "Paddewwa");

PORTAL_LABELS.put(60776, "Lassar");
PORTAL_LABELS.put(60792, "Lassar");
PORTAL_LABELS.put(60802, "Lassar");
PORTAL_LABELS.put(60812, "Lassar");

PORTAL_LABELS.put(60777, "Dareeyak");
PORTAL_LABELS.put(60793, "Dareeyak");
PORTAL_LABELS.put(60803, "Dareeyak");
PORTAL_LABELS.put(60813, "Dareeyak");

// Lunar Spellbook — New 2026-02-25
PORTAL_LABELS.put(60778, "Ourania");
PORTAL_LABELS.put(60794, "Ourania");
PORTAL_LABELS.put(60804, "Ourania");
PORTAL_LABELS.put(60814, "Ourania");

PORTAL_LABELS.put(60779, "Barbarian");
PORTAL_LABELS.put(60795, "Barbarian");
PORTAL_LABELS.put(60805, "Barbarian");
PORTAL_LABELS.put(60815, "Barbarian");

PORTAL_LABELS.put(60780, "Khazard");
PORTAL_LABELS.put(60796, "Khazard");
PORTAL_LABELS.put(60806, "Khazard");
PORTAL_LABELS.put(60816, "Khazard");

PORTAL_LABELS.put(60781, "Ice Plateau");
PORTAL_LABELS.put(60797, "Ice Plateau");
PORTAL_LABELS.put(60807, "Ice Plateau");
PORTAL_LABELS.put(60817, "Ice Plateau");

// Arceuus Spellbook — New 2026-02-25
PORTAL_LABELS.put(60782, "Respawn");
PORTAL_LABELS.put(60798, "Respawn");
PORTAL_LABELS.put(60808, "Respawn");
PORTAL_LABELS.put(60818, "Respawn");
```

### 2. `PortalNameOverlay.java` — `isPortalObject()` range check

Extend the method to cover the new ID bands. Current pattern uses a range check for 13615–13633.
Add equivalent checks:

```java
// Existing (example):
if (id >= 13615 && id <= 13633) return true;

// Add:
if (id >= 60774 && id <= 60783) return true;  // Raging Echoes variants (new 2026-02-25)
if (id >= 60790 && id <= 60819) return true;  // Teak/mahogany/marble variants (new 2026-02-25)
```

---

## Confidence Assessment

| Area | Confidence | Source | Notes |
|------|------------|--------|-------|
| Yanille IDs already in code | HIGH | Direct code read | Verified in `PortalNameOverlay.java` lines 31–171 |
| New destination IDs | HIGH | OSRS wiki infoboxes | All 40 IDs extracted from canonical wiki source |
| ID sequential pattern | HIGH | OSRS wiki | Pattern is unambiguous from infobox data |
| Issue #35 root cause | LOW | Inference only | In-game verification required |
| Label strings for new destinations | MEDIUM | Convention analysis | Based on existing codebase patterns; may need adjustment |

---

## Gaps / Open Questions

1. **Issue #35 root cause** — The IDs exist in code. In-game testing needed to confirm whether the
   label renders correctly for both pre- and post-Hard Ardougne Diary players.

2. **"Teleport to Boat" label length** — The label may be truncated in the overlay UI. Consider
   `"Boat"` as a fallback if in-game testing shows display issues.

3. **Config enum** — If the plugin exposes portal destinations as a config dropdown (check
   `PortalNameConfig.java`), the 10 new destinations may need corresponding enum entries.

4. **In-game ID validation** — Wiki IDs should be cross-checked against in-game cache data
   (e.g., RuneLite's cache viewer or the OSRS cache repository) before release, as wiki data
   occasionally lags behind or contains transcription errors.

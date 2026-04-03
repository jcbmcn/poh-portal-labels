# POH Portal Labels — Bug Fix Milestone

## What This Is

POH Portal Labels is a RuneLite external plugin for Old School RuneScape that renders destination labels above Player-Owned House portals. It is a focused, single-purpose UI tool for OSRS players who want to see which teleport destination each of their POH portals is set to without mousing over them.

## Core Value

Players see correct labels on all portals in their POH — always, on every floor, for every supported destination.

## Requirements

### Validated

- ✓ Display portal destination labels as 2D overlay above portal game objects — existing
- ✓ Support color customization (single color, per-portal colors, game model color extraction) — existing
- ✓ Custom name overrides via config text input — existing
- ✓ Label position control (top / middle / bottom) — existing
- ✓ Text outline for readability — existing

### Active

- [ ] **#41** Portals on upper floors of POH are correctly labeled (plane detection fixed)
- [ ] **#40** All 10 new teleport destinations are supported: Trollheim (Standard), Paddewwa (Ancient), Lassar (Ancient), Dareeyak (Ancient), Ourania (Lunar), Barbarian (Lunar), Khazard (Lunar), Ice Plateau (Lunar), Respawn (Arceuus), Teleport to Boat (Standard)
- [ ] **#39** Pets in POH no longer trigger portal label display (false-positive NPC/object match fixed)
- [ ] **#35** Yannille portal is correctly labeled (missing portal object IDs added)

### Out of Scope

- Portal Nexus support — significant feature; separate milestone after bugs are resolved
- Performance refactoring (render-loop `updatePortalColors` calls) — tech debt, not blocking
- Unit test coverage — worthwhile but not part of this bug-fix release
- Removing/refactoring dead code (`PortalNameEventSubscriber`) — cleanup, not blocking

## Context

**Existing codebase state:**
- ~600 lines of production Java across 4 classes (`PortalNamePlugin`, `PortalNameConfig`, `PortalNameOverlay`, `PortalNameEventSubscriber`)
- Portal ID→label mapping lives in a static initializer block in `PortalNameOverlay.java` (lines 31–171), currently ~170 hardcoded IDs across all known destinations
- POH detection checks for `ObjectID.POH_EXIT_PORTAL` on the current plane only — confirmed root cause of issue #41
- Pet NPC false-positive cause unknown; likely NPC IDs colliding with portal object IDs in the PORTAL_LABELS map or the object scan logic picking up NPC entities
- Yannille portal IDs not present in PORTAL_LABELS; IDs need to be sourced from the OSRS wiki Object IDs page
- New teleport destination IDs also need sourcing from the wiki

**Investigation resources:**
- OSRS Wiki Object IDs page: https://oldschool.runescape.wiki/w/Object_IDs
- RuneLite `PortalNameEventSubscriber` debug tool (commented out in production) can be re-enabled to discover IDs in-game

**Build / release:**
- Gradle 8.10, Java 11, RuneLite `latest.release`
- Semantic-release via `.releaserc` — commit message convention drives version bumps and CHANGELOG
- CI (GitHub Actions) runs semantic-release on merge to main

## Constraints

- **Tech Stack**: Java 11 + RuneLite client API — no changes to the stack; plugin must remain compatible with RuneLite's overlay and config APIs
- **Compatibility**: Must work with current RuneLite `latest.release`; no API shims or workarounds
- **Scope**: This milestone is bug fixes only — no new features, no structural refactoring
- **ID sourcing**: Portal object IDs must be sourced from the OSRS wiki or discovered via the EventSubscriber debug tool; IDs cannot be guessed

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Fix plane detection for upper floors | Root cause of #41 is confirmed single-plane scan | — Pending |
| Source new teleport IDs from OSRS wiki | Wiki Object IDs page is the canonical source; EventSubscriber as fallback | — Pending |
| Investigate pet false-positive before fixing | Root cause unknown — could be ID collision or NPC scan leak | — Pending |

---

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-03 after initialization*

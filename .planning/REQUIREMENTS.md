# Requirements: POH Portal Labels — Bug Fix Milestone

**Defined:** 2026-04-03
**Core Value:** Players see correct labels on all portals in their POH — always, on every floor, for every supported destination.

## v1 Requirements

### Floor Coverage

- [ ] **FLOOR-01**: Portals built on upper floors (plane 1, 2) of a POH are labeled correctly — closes #41
- [ ] **FLOOR-02**: POH detection (`inPoh` check) works regardless of which floor the player is currently standing on

### Portal Data

- [ ] **DATA-01**: Yannille portal objects display a label — closes #35 (re-test after FLOOR-01; may self-close)
- [ ] **DATA-02**: Trollheim (Standard) portal displays a label — closes #40 partial
- [ ] **DATA-03**: Paddewwa (Ancient) portal displays a label — closes #40 partial
- [ ] **DATA-04**: Lassar (Ancient) portal displays a label — closes #40 partial
- [ ] **DATA-05**: Dareeyak (Ancient) portal displays a label — closes #40 partial
- [ ] **DATA-06**: Ourania (Lunar) portal displays a label — closes #40 partial
- [ ] **DATA-07**: Barbarian (Lunar) portal displays a label — closes #40 partial
- [ ] **DATA-08**: Khazard (Lunar) portal displays a label — closes #40 partial
- [ ] **DATA-09**: Ice Plateau (Lunar) portal displays a label — closes #40 partial
- [ ] **DATA-10**: Respawn (Arceuus) portal displays a label — closes #40 partial
- [ ] **DATA-11**: Teleport to Boat (Standard) portal displays a label — closes #40 partial

### Accuracy

- [ ] **ACC-01**: Pets and other NPCs in POH do not show portal destination labels — closes #39
- [ ] **ACC-02**: No existing portal destination labels regress (all currently-working destinations continue to work after changes)

## v2 Requirements

### Portal Nexus

- **NEXUS-01**: Portal Nexus destinations are displayed with labels
- **NEXUS-02**: Portal Nexus supports all standard teleport destinations

### Performance

- **PERF-01**: `updatePortalColors()` is not called inside the render loop (moved to startup + config-change event)
- **PERF-02**: `updateCustomNames()` is not called per-portal per-frame (called once per frame max)

### Tests

- **TEST-01**: Unit tests cover `updateCustomNames()` string parsing edge cases
- **TEST-02**: Unit tests cover HSL→RGB color math in `getPortalColor()`

## Out of Scope

| Feature | Reason |
|---------|--------|
| Portal Nexus support | Significant new feature; separate milestone after bugs fixed |
| Performance refactoring | Tech debt; not causing user-visible bugs; separate milestone |
| Unit test coverage | Valuable but not part of this bug-fix release |
| Removing `PortalNameEventSubscriber` dead code | Cleanup; not blocking any bug fix |
| Config section/enum refactoring | `public` modifier inconsistency; cosmetic; out of scope |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| FLOOR-01 | Phase 1: Multi-Plane Fix | Pending |
| FLOOR-02 | Phase 1: Multi-Plane Fix | Pending |
| DATA-01 | Phase 1: Multi-Plane Fix | Pending |
| ACC-02 | Phase 1: Multi-Plane Fix | Pending |
| DATA-02 | Phase 2: New Destination IDs | Pending |
| DATA-03 | Phase 2: New Destination IDs | Pending |
| DATA-04 | Phase 2: New Destination IDs | Pending |
| DATA-05 | Phase 2: New Destination IDs | Pending |
| DATA-06 | Phase 2: New Destination IDs | Pending |
| DATA-07 | Phase 2: New Destination IDs | Pending |
| DATA-08 | Phase 2: New Destination IDs | Pending |
| DATA-09 | Phase 2: New Destination IDs | Pending |
| DATA-10 | Phase 2: New Destination IDs | Pending |
| DATA-11 | Phase 2: New Destination IDs | Pending |
| ACC-01 | Phase 3: Pet False Positive Fix | Pending |

**Coverage:**
- v1 requirements: 14 total
- Mapped to phases: 14
- Unmapped: 0 ✓

---
*Requirements defined: 2026-04-03*
*Last updated: 2026-04-03 after initial definition*

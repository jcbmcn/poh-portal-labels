# Milestones

## v1.0 Bug Fix (Shipped: 2026-04-04)

**Phases completed:** 3 phases, 3 plans, 5 tasks

**Key accomplishments:**

- 3D tile scan over all 4 POH planes via Constants.MAX_Z, fixing upper-floor portal label rendering and inPoh detection
- 40 portal IDs for 10 February 2026 teleport destinations added across all 4 portal tiers with full MULTI+UNIQUE color mode support
- Eliminated false-positive portal labels by replacing the over-broad 13615–13633 range bypass in `isPortalObject()` with an explicit `PORTAL_LABELS.containsKey(id)` check.

---

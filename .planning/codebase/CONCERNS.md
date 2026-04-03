# CONCERNS
> Last updated: 2026-04-03 | Focus: concerns

## Summary

This is a small, focused RuneLite plugin (~600 lines of production Java) that is generally well-structured and functional. The most significant concern is the large volume of hardcoded magic numbers (raw game object IDs in `PortalNameOverlay`) and a nearly non-existent test suite. A secondary concern is redundant work performed every render frame, which could accumulate cost over time.

---

## Technical Debt

### Hardcoded Object IDs — No Symbolic Constants
- **Issue:** `PortalNameOverlay.java` contains a static initializer block (lines 31–171) mapping ~170 raw integer IDs to portal name strings. No constants, no enum, no external data file.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java`
- **Impact:** When Jagex adds or renumbers game objects (which happens regularly), finding and updating the right IDs is entirely manual. There is no mechanism to detect stale IDs. The ID `13620` even has an inline comment "pulled from event subscriber" — indicating it was added ad-hoc rather than through a systematic process.
- **Fix approach:** Consider loading portal mappings from a bundled JSON/CSV resource file, or at minimum define each ID as a named constant or an enum entry, so purpose is self-documenting.

### `build.gradle` Version Mismatch
- **Issue:** `build.gradle` declares `version = '1.1.0'` (line 30), but the latest release per `CHANGELOG.md` is `v1.1.2`. The build file version is stale.
- **Files:** `build.gradle`
- **Impact:** Produces incorrectly named shadow JAR artifacts (`poh-portal-labels-1.1.0-all.jar`). Misleading to contributors.
- **Fix approach:** Keep `version` in sync with the CHANGELOG on each release, or drive it from a single source-of-truth (e.g., a `version.txt` or via semantic-release writing it back).

### `updatePortalColors()` Called Inside the Render Loop
- **Issue:** In `PortalNameOverlay.render()`, `updatePortalColors()` is called on line 364 on *every render frame* when the MULTI + UNIQUE color mode is active. `updatePortalColors()` itself calls `updateCustomNames()`, which allocates a new string array by splitting the config string. This work belongs in a setup/refresh step, not per-frame.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java` (lines 361–368)
- **Impact:** Unnecessary allocation and CPU work each render frame (~60× per second). Minor for a simple plugin but wasteful.
- **Fix approach:** Call `updatePortalColors()` only on plugin startup and on config change events (`@Subscribe` on `ConfigChanged`), never inside `render()`.

### `updateCustomNames()` Also Called Inside the Render Loop
- **Issue:** Separate from the above, `updateCustomNames()` is *also* called directly on line 318 inside the inner loop over game objects — every portal, every frame.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java` (line 318)
- **Impact:** The `customNameOverrides` map is rebuilt repeatedly within a single frame, once per portal object found. The result does not change between portals in the same frame.
- **Fix approach:** Move `updateCustomNames()` to be called once per frame at most (or only on config change), and cache the result.

### Commented-Out Dead Code in `PortalNamePlugin`
- **Issue:** `PortalNamePlugin.java` contains commented-out injections and `eventBus` calls for `PortalNameEventSubscriber` (lines 17–21, 36, 46). These are left with an instruction to "UNCOMMENT BELOW TO GET LOGS FOR OBJECT IDs".
- **Files:** `src/main/java/com/portalname/PortalNamePlugin.java`
- **Impact:** `PortalNameEventSubscriber` is a pure developer/debug tool that ships in the released plugin JAR but is never activated in production. This is dead code from the user perspective, adds confusion, and inflates the build.
- **Fix approach:** Either remove `PortalNameEventSubscriber` entirely from production code (move to a dev branch or a separate debug build profile), or activate it via a hidden config flag so it can be toggled at runtime without code changes.

---

## Code Smells / Anti-patterns

### Magic Numbers for `zOffset` Text Positioning
- **Issue:** `render()` uses bare integer literals `250`, `-50`, and `100` for vertical label offsets (lines 328–336). No comments explain the coordinate system or unit (RuneLite local coordinate units).
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java`
- **Fix approach:** Extract named constants: `Z_OFFSET_TOP = 250`, `Z_OFFSET_MIDDLE = 100`, `Z_OFFSET_BOTTOM = -50`.

### Magic Number in `isPortalObject()` Range Check
- **Issue:** The range `id >= 13615 && id <= 13633` (lines 470–472) is a hardcoded workaround documented only in a comment. The comment says these IDs have "null or inconsistent names" but provides no source.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java`
- **Fix approach:** Define constants (`LEGACY_PORTAL_ID_MIN`, `LEGACY_PORTAL_ID_MAX`) with a link to the RuneLite issue or cache query that established this range.

### `getPortalColor()` Only Reads `faceColors1[0]`
- **Issue:** Portal color extraction reads only the first face's first color channel (`colors[0]`, line 392). This is a simplification that may return wrong results for portals where the primary color is not in `faceColors1` or where the first face is not representative.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java` (lines 388–396)
- **Fix approach:** Sample a wider set of face colors (e.g., median or mode) or document why `colors[0]` is always representative.

### Wildcard Import in `PortalNameEventSubscriber`
- **Issue:** `import net.runelite.api.*;` uses a wildcard on line 4.
- **Files:** `src/main/java/com/portalname/PortalNameEventSubscriber.java`
- **Fix approach:** Replace with explicit imports. This is minor but inconsistent with the rest of the codebase which uses explicit imports.

### `public` Modifier on Interface Enum and `enum ColorSelection`
- **Issue:** `PortalNameConfig.java` declares `public enum ColorStyle` (line 12) and `public enum ColorSelection` (line 76) inside an interface. The `public` modifier is redundant (interface members are implicitly public) and inconsistent — `enum TextPosition` on line 19 does not use `public`.
- **Files:** `src/main/java/com/portalname/PortalNameConfig.java`

---

## Missing Tests

### The Test File Is Not a Test
- **Issue:** `src/test/java/com/portalname/PortalNamePluginTest.java` is named as a test but contains only a `main()` method that launches the full RuneLite client with the plugin loaded. It is a manual integration harness, not an automated test. There are zero JUnit assertions, zero `@Test`-annotated methods, and no test of any logic.
- **Files:** `src/test/java/com/portalname/PortalNamePluginTest.java`
- **Risk:** Any regression in portal ID lookup, color selection logic, custom name parsing, or text positioning is invisible to CI.

### No Tests for Custom Name Parsing
- **Issue:** The `updateCustomNames()` method in `PortalNameOverlay` parses a freeform string config value. Edge cases (blank lines, missing `=`, extra whitespace, empty values, duplicate keys) are handled in code but never verified by a test.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java` (lines 229–264)
- **Priority:** High — this is user-supplied input with several handled branches.

### No Tests for `hslToRgb` / `brighten` Color Math
- **Issue:** The HSL→RGB conversion and the `brighten()` utility are pure functions with no side effects — ideal candidates for unit tests. No tests exist.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java` (lines 406–462)
- **Priority:** Medium.

### No Tests for Portal ID Coverage
- **Issue:** There is no test validating that every portal name in `PORTAL_LABELS` has a corresponding entry in `portalColors` (populated by `updatePortalColors()`). A mismatch produces a silent fallback to `Color.WHITE`.
- **Priority:** Medium — a new portal could be added to one map but forgotten in the other.

---

## Dependency Risks

### RuneLite Dependency Pinned to `latest.release`
- **Issue:** `build.gradle` sets `runeLiteVersion = 'latest.release'` (line 16). This resolves to whatever the RuneLite Maven repo publishes as the latest at build time.
- **Files:** `build.gradle`
- **Impact:** Builds are not reproducible. A breaking RuneLite API change will silently fail the next build with no notice. The `api.JagexColor`, `gameval.ObjectID`, and `Scene`/`Tile`/`Perspective` APIs are all subject to change.
- **Fix approach:** Pin to a specific known-good RuneLite version (e.g., `1.11.X`) and update deliberately.

### JUnit 4 in Use
- **Issue:** `testImplementation 'junit:junit:4.12'` — JUnit 4.12 from 2014. JUnit 4 is in maintenance-only mode; JUnit 5 has been the standard since 2017.
- **Files:** `build.gradle`
- **Impact:** Low immediate risk given there are no real tests, but any future test development should target JUnit 5.

### Lombok Pinned at `1.18.30`
- **Issue:** Lombok `1.18.30` is reasonably recent but should be verified against the Java 11 toolchain for compatibility. Currently only `@Slf4j` is used — a very minimal Lombok footprint.
- **Files:** `build.gradle`
- **Impact:** Low. Lombok is annotation-processing only and not a runtime dependency.

### GitHub Actions Using `actions/checkout@v3` and `actions/setup-node@v3`
- **Issue:** Release workflow uses pinned-to-major-version (`@v3`) rather than a specific commit SHA. `v3` may receive breaking updates.
- **Files:** `.github/workflows/release.yml`
- **Impact:** Low in practice, but best practice is to pin to a full SHA for supply-chain security.

### Release Workflow Installs npm Packages Without a Lockfile
- **Issue:** The release workflow runs `npm init -y && npm install semantic-release ...` on every run without a `package-lock.json`.
- **Files:** `.github/workflows/release.yml`
- **Impact:** Non-reproducible releases. A patch/minor bump to any `@semantic-release/*` package could silently change release behavior.

---

## TODOs & FIXMEs

### Inline Developer Instructions Committed to Production
- `PortalNamePlugin.java`, lines 35–36 and 45–46: Comments read "UNCOMMENT BELOW TO GET LOGS FOR OBJECT IDs". These are developer workflow instructions left in released code.
- `PortalNameOverlay.java`, line 170: Comment `// pulled from event subscriber` on ID `13620` — traces an informal discovery process but provides no reproducible reference.
- `cache-viewer-queries/get-poh-portal-ids-names.ts`, line 45: Commented-out `console.log` call — minor, but the script ships with unused code.

No `// TODO` or `// FIXME` tags exist anywhere in the source; the above are informal equivalents.

---

## Maintainability

### Portal ID Maintenance Is Entirely Manual
- When Jagex modifies or adds portal variants, a developer must: run the cache viewer query, identify new IDs, add entries to the `PORTAL_LABELS` static map, add a `@ConfigItem` to `PortalNameConfig`, and add the portal to `updatePortalColors()` — three separate files with no compile-time linking. Missing any step fails silently.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java`, `src/main/java/com/portalname/PortalNameConfig.java`
- **Fix approach:** Define portals as a single data structure (enum or record list) containing the IDs, display name, config key, and default color, then derive all three uses from that single source.

### Parallel Data Structures Out of Sync Risk
- `PORTAL_LABELS` (static map of IDs→names, ~35 destinations × 3–5 variant IDs), `updatePortalColors()` (34 manual `put()` calls), and `PortalNameConfig` (34 `@ConfigItem` color methods) must all be updated in lockstep. There is no compile-time or test-time enforcement.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java` (lines 193–227), `src/main/java/com/portalname/PortalNameConfig.java` (lines 113–418)

### `PortalNameEventSubscriber` Serves No Production Purpose
- The class is compiled and shipped but never registered (all wiring is commented out). New contributors may be confused about its role or try to activate it not understanding it is a debug tool.
- **Files:** `src/main/java/com/portalname/PortalNameEventSubscriber.java`, `src/main/java/com/portalname/PortalNamePlugin.java`

### No Config Change Listener
- There is no `@Subscribe ConfigChanged` handler. Color and custom name changes only take effect after the plugin is restarted (or the next call path that triggers `updatePortalColors()`). Users may not see config changes reflected live.
- **Files:** `src/main/java/com/portalname/PortalNamePlugin.java`, `src/main/java/com/portalname/PortalNameOverlay.java`
- **Fix approach:** Subscribe to `ConfigChanged` in `PortalNamePlugin` (or `PortalNameOverlay`) and call `overlay.updatePortalColors()` when the config group matches.

---

## Gaps & Unknowns

### No Portal Nexus Support
- The README lists individual POH portals but does not mention the Portal Nexus (a different construction room that also teleports). The cache viewer query explicitly excludes "portal nexus" objects (line 20). Users with a Portal Nexus rather than individual portals will see no labels.
- **Risk:** A significant portion of high-level players use the Portal Nexus. This is likely the largest feature gap.

### Portal Variant ID Coverage Uncertain
- Some portals have 3 variants, some have 4, and Camelot/Grand Exchange have 5. The exact set of valid IDs is derived from the external cache viewer tool, not from any verified game source. If additional placement variants exist they will silently show no label.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java` (static init block)

### Color Extraction Correctness Not Verified
- The HSL→RGB color path for "Portal Colors" mode reads only `faceColors1[0]` and applies a hardcoded `0.4f` brightness boost. There are no tests or screenshots documenting that this produces accurate portal color matching. The comment on line 401 acknowledges colors "can be quite dark" but the specific `0.4f` value is arbitrary.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java` (lines 381–404)

### No Font Size or Style Configuration
- Labels are drawn using whatever the default `Graphics2D` font is at the time of render. There is no option for font size, bold, or drop-shadow depth (only a hardcoded 1px black outline). Power users may find the default font too small or too large.

### `inPoh` Detection Depends on Exit Portal Presence
- The plugin determines whether the player is in a POH by scanning for `ObjectID.POH_EXIT_PORTAL` in the scene (lines 276–293). If the exit portal is not loaded into the current scene tile range (possible in large or custom house layouts), the entire overlay is suppressed.
- **Files:** `src/main/java/com/portalname/PortalNameOverlay.java`

### `build.gradle` `version` Field Not Authoritative
- `build.gradle` declares `version = '1.1.0'` but `runelite-plugin.properties` does not include a version field (RuneLite uses the repo tag for versioning). The JAR version is therefore purely cosmetic and currently stale (should be `1.1.2`).

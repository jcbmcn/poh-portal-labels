# Codebase Structure

> Last updated: 2026-04-03 | Focus: arch

## Summary

POH Portal Labels is a compact RuneLite external plugin with exactly 5 Java source files across one package. The project follows the standard RuneLite external plugin layout with Gradle as the build system. There are no sub-packages, no generated sources, and no non-Java resource files beyond the plugin properties manifest.

---

## Directory Layout

```
poh-portal-labels/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/portalname/       # All plugin source code (1 package, 4 classes)
│   │           ├── PortalNamePlugin.java
│   │           ├── PortalNameConfig.java
│   │           ├── PortalNameOverlay.java
│   │           └── PortalNameEventSubscriber.java
│   └── test/
│       └── java/
│           └── com/portalname/       # Test/launcher code (1 file)
│               └── PortalNamePluginTest.java
├── cache-viewer-queries/             # Developer tooling (TypeScript query)
│   └── get-poh-portal-ids-names.ts
├── assets/                           # README screenshots and plugin icon
│   ├── logo.png
│   ├── icon.png (root)
│   ├── example_poh_labels.png
│   ├── single.png
│   ├── portal_color.png
│   ├── unique.png
│   └── config.png
├── .planning/
│   └── codebase/                     # GSD planning documents
├── .github/                          # GitHub Actions / workflows (if any)
├── gradle/
│   └── wrapper/                      # Gradle wrapper files
├── build.gradle                      # Build configuration, dependencies
├── settings.gradle                   # Project name declaration
├── gradlew / gradlew.bat             # Gradle wrapper scripts
├── runelite-plugin.properties        # RuneLite plugin manifest (name, author, entry class)
├── icon.png                          # Plugin icon shown in RuneLite hub
├── README.md                         # User-facing documentation
├── CHANGELOG.md                      # Version history
├── LICENSE                           # License file
└── .releaserc                        # Semantic release configuration
```

---

## Directory Purposes

**`src/main/java/com/portalname/`:**
- Purpose: All production plugin code
- Contains: Plugin lifecycle, config interface, overlay renderer, (disabled) event subscriber
- Key files: All 4 `.java` files listed below

**`src/test/java/com/portalname/`:**
- Purpose: RuneLite launcher for manual in-client plugin testing
- Contains: `PortalNamePluginTest.java` — not a unit test suite, a launch harness
- Note: No JUnit-based unit tests exist

**`cache-viewer-queries/`:**
- Purpose: Developer utility for discovering portal game object IDs from the RuneScape game cache
- Contains: `get-poh-portal-ids-names.ts` — a TypeScript query used against https://abextm.github.io/cache2/
- Note: Not part of the build; purely a development reference tool

**`assets/`:**
- Purpose: Images for README documentation
- Generated: No
- Committed: Yes

**`.planning/codebase/`:**
- Purpose: GSD codebase analysis documents
- Generated: Yes (by GSD mapper agent)
- Committed: TBD

---

## Package Structure

**Single package:** `com.portalname`

All classes live in this one flat package. There is no sub-package separation (e.g., no `model`, `util`, or `ui` sub-packages). The package name reflects the Gradle group id (`com.portalname`) declared in `build.gradle`.

---

## Key File Descriptions

### `PortalNamePlugin.java` — Plugin Entry Point
- Annotated with `@PluginDescriptor(name = "POH Portal Labels")` and `@Slf4j`
- Extends `net.runelite.client.plugins.Plugin`
- `startUp()`: Calls `overlay.updatePortalColors()`, registers overlay with `OverlayManager`
- `shutDown()`: Removes overlay from `OverlayManager`
- `provideConfig()`: `@Provides`-annotated method binding `PortalNameConfig` via Guice
- Commented-out code for `PortalNameEventSubscriber` wiring (intentionally disabled)
- **56 lines**

### `PortalNameConfig.java` — User Configuration Interface
- Annotated with `@ConfigGroup("PortalName")`
- Extends `net.runelite.client.config.Config`
- Declares 3 inner enums: `ColorStyle` (SINGLE/MULTI), `TextPosition` (TOP/MIDDLE/BOTTOM), `ColorSelection` (PORTAL_COLORS/UNIQUE_COLORS)
- Declares 4 `@ConfigSection` groups: `singleStyle`, `multiStyle`, `uniqueColors`, `customNames`
- Declares 37 `@ConfigItem` default methods: 1 for color style, 1 for text position, 1 for color selection, 1 for single color, 33 per-destination unique colors (all default to `Color.GREEN`), 2 for custom name settings
- All per-destination color methods annotated `@Alpha` (supports transparency)
- **448 lines**

### `PortalNameOverlay.java` — Rendering Engine
- Extends `net.runelite.client.ui.overlay.Overlay`
- Annotated with `@Slf4j`
- `PORTAL_LABELS`: Static `Map<Integer, String>` — 140+ entries mapping game object IDs to destination names (33 destinations × ~4 IDs each)
- `portalColors`: Instance `Map<String, Color>` — color lookup cache per destination name
- `customNameOverrides`: Instance `Map<String, String>` — parsed user custom name mappings
- `updatePortalColors()`: Populates `portalColors` from config; calls `updateCustomNames()`
- `updateCustomNames()`: Parses `config.customNamesList()` string into `customNameOverrides` map
- `render(Graphics2D)`: Main render method — POH detection, portal scan, label draw with color and position logic
- `getPortalColor(GameObject)`: Extracts dominant color from game model via Jagex HSL → RGB conversion
- `hslToRgb()`, `hueToRgb()`, `clamp()`, `brighten()`: Color math utilities (all private/static)
- `isPortalObject(GameObject)`: Validates game object is actually a portal (name contains "Portal" or ID in 13615–13633 range)
- Constructor: Sets `OverlayPosition.DYNAMIC` and `OverlayLayer.ABOVE_SCENE`
- **486 lines** — the largest and most complex file

### `PortalNameEventSubscriber.java` — Developer Debug Utility
- Annotated with `@Slf4j`
- `@Subscribe onMenuOptionClicked(MenuOptionClicked)`: Logs clicked game object ID, class, and world location
- `findGameObjectById(int)`: Full scene tile scan to locate a game object by ID
- **Status: Disabled** — not registered to the RuneLite event bus in production
- **73 lines**

### `PortalNamePluginTest.java` — Manual Test Launcher
- Not a JUnit test class (no `@Test` annotations)
- `main()` method calls `ExternalPluginManager.loadBuiltin(PortalNamePlugin.class)` then `RuneLite.main(args)`
- Used with the `shadowJar` Gradle task to launch RuneLite with the plugin pre-loaded
- **13 lines**

---

## Key File Locations

**Entry Points:**
- `src/main/java/com/portalname/PortalNamePlugin.java`: Plugin lifecycle, registered via `runelite-plugin.properties`
- `runelite-plugin.properties`: RuneLite manifest declaring `com.portalname.PortalNamePlugin` as the plugin class

**Configuration:**
- `build.gradle`: Gradle build, dependency versions (RuneLite `latest.release`, Lombok `1.18.30`, JUnit `4.12`), Java 11 target
- `settings.gradle`: Project name
- `runelite-plugin.properties`: Plugin metadata (display name, author, description, tags)
- `.releaserc`: Semantic release config (automated versioning)

**Core Logic:**
- `src/main/java/com/portalname/PortalNameOverlay.java`: All rendering, portal detection, color resolution, custom names
- `src/main/java/com/portalname/PortalNameConfig.java`: All user-configurable settings

**Developer Tools:**
- `cache-viewer-queries/get-poh-portal-ids-names.ts`: Query for finding portal object IDs in the RS cache
- `src/test/java/com/portalname/PortalNamePluginTest.java`: RuneLite launcher for local testing

---

## Naming Conventions

**Files:**
- All Java files use `PascalCase` matching their class name: `PortalNamePlugin.java`, `PortalNameConfig.java`, etc.
- The `PortalName` prefix is used consistently across all class names (a legacy naming artifact — the plugin displays portal destinations, not portal names per se)

**Classes:**
- Plugin: `[Feature]Plugin` extending `Plugin`
- Config: `[Feature]Config` extending `Config` (interface)
- Overlay: `[Feature]Overlay` extending `Overlay`
- Event subscriber: `[Feature]EventSubscriber`

**Config keys:**
- `camelCase` for `keyName` values matching the method name: `colorStyle`, `singleColor`, `annakarlColor`, etc.
- Section name constants are `camelCase` string fields: `singleStyle`, `multiStyle`, `uniqueColors`, `customNames`

**Map keys in `PORTAL_LABELS`:**
- Destination names use title case matching in-game names: `"Ape Atoll Dungeon"`, `"Fenkenstrain's Castle"`, `"Seers' Village"`
- These exact strings are used as keys in `portalColors` and `customNameOverrides` — they must match exactly

---

## Where to Add New Code

**New Portal Destination:**
1. Add 3–5 object ID → label entries to the static `PORTAL_LABELS` map in `PortalNameOverlay.java` (lines 31–171)
2. Add a `@ConfigItem` color method to `PortalNameConfig.java` in the `uniqueColors` section
3. Add a `portalColors.put("Destination Name", config.destinationColor())` call in `updatePortalColors()` in `PortalNameOverlay.java`
4. Update `README.md` portal table

**New Config Setting:**
- Add `@ConfigItem` default method to `PortalNameConfig.java`
- Read it in `PortalNameOverlay.render()` or `updatePortalColors()`
- New config sections: add `@ConfigSection` string constant + `position` ordering

**New Utility Logic:**
- Add private/static methods to `PortalNameOverlay.java` (current pattern for color math)
- If substantial, consider a new class in `src/main/java/com/portalname/`

**New Developer Debug Feature:**
- Add `@Subscribe` methods to `PortalNameEventSubscriber.java`
- Re-enable event bus registration in `PortalNamePlugin.java` (commented-out lines 22, 36, 46)

---

## Special Directories

**`.planning/`:**
- Purpose: GSD planning and codebase analysis documents
- Generated: Yes
- Committed: Depends on project preference

**`cache-viewer-queries/`:**
- Purpose: Developer reference scripts for RS cache inspection
- Generated: No
- Committed: Yes (as developer tooling documentation)

**`build/`:**
- Purpose: Gradle build output (compiled classes, JARs)
- Generated: Yes
- Committed: No (in `.gitignore`)

---

## Gaps & Unknowns

- **`settings.gradle` not read**: Project name not verified — likely `poh-portal-labels` matching the repo name
- **`.github/` contents unknown**: Workflow files not inspected — CI/CD details undocumented
- **`cache-viewer-queries/get-poh-portal-ids-names.ts` not read**: TypeScript query contents not reviewed
- **No `resources/` directory**: There is no `src/main/resources/` — the plugin does not use any bundled resource files (no JSON, no images loaded at runtime)
- **`CHANGELOG.md` not read**: Version history not reviewed; current version is `1.1.0` per `build.gradle`

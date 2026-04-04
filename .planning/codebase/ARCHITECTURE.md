# Architecture

> Last updated: 2026-04-03 | Focus: arch

## Summary

POH Portal Labels is a minimal RuneLite external plugin that renders text labels above Player-Owned House (POH) portals in Old School RuneScape. The architecture follows the standard RuneLite plugin pattern: a plugin class manages lifecycle, a config interface exposes settings, and an overlay class handles all rendering each game frame. An event subscriber exists as a disabled developer utility only.

---

## Pattern Overview

**Overall:** RuneLite Plugin (event-driven, dependency-injected overlay plugin)

**Key Characteristics:**
- Guice DI wires all components together — no manual instantiation
- Overlay-only rendering: no panels, no widgets, no custom UI
- Config is a pure interface; RuneLite's `ConfigManager` proxies it at runtime
- Event subscription is present in the codebase but intentionally disabled in production

---

## Layers

**Plugin Entry Point (`PortalNamePlugin`):**
- Purpose: Lifecycle manager for the plugin
- Location: `src/main/java/com/portalname/PortalNamePlugin.java`
- Contains: `startUp()`, `shutDown()`, `@Provides` config binding
- Depends on: `PortalNameConfig`, `OverlayManager`, `PortalNameOverlay`
- Used by: RuneLite client via `@PluginDescriptor`

**Configuration Layer (`PortalNameConfig`):**
- Purpose: Declares all user-configurable settings as a typed Java interface
- Location: `src/main/java/com/portalname/PortalNameConfig.java`
- Contains: Enums (`ColorStyle`, `TextPosition`, `ColorSelection`), `@ConfigSection` groups, `@ConfigItem` annotated default methods
- Depends on: `net.runelite.client.config.*`
- Used by: `PortalNamePlugin` (via `@Provides`), `PortalNameOverlay` (direct `@Inject`)

**Rendering Layer (`PortalNameOverlay`):**
- Purpose: Draws destination labels above POH portal game objects every frame
- Location: `src/main/java/com/portalname/PortalNameOverlay.java`
- Contains: Static portal ID→label map, per-portal color map, custom name overrides, all rendering and color logic
- Depends on: `Client`, `PortalNameConfig`
- Used by: `OverlayManager` (registered/removed by plugin)

**Developer Utility (`PortalNameEventSubscriber`):**
- Purpose: Debug tool for discovering portal object IDs by logging clicked object info
- Location: `src/main/java/com/portalname/PortalNameEventSubscriber.java`
- Contains: `@Subscribe onMenuOptionClicked`, scene traversal helper
- Status: **Disabled in production** — all wiring in `PortalNamePlugin` is commented out

---

## Data Flow

**Startup Flow:**
1. RuneLite loads plugin via `runelite-plugin.properties` → `com.portalname.PortalNamePlugin`
2. Guice injects `PortalNameConfig`, `OverlayManager`, `PortalNameOverlay` into `PortalNamePlugin`
3. `startUp()` calls `overlay.updatePortalColors()` (populates color cache from config)
4. `startUp()` calls `overlayManager.add(overlay)` — overlay is now active

**Render Flow (per frame):**
1. RuneLite calls `PortalNameOverlay.render(Graphics2D)`
2. Guard: returns early if player is not `LOGGED_IN`
3. POH detection: scans all tiles on the current plane for `ObjectID.POH_EXIT_PORTAL`; returns early if not in a POH
4. Portal scan: iterates all tiles/game objects and checks IDs against `PORTAL_LABELS` static map
5. For each matching portal:
   a. Resolves display name (checks `customNameOverrides` map first, falls back to canonical label)
   b. Computes Z offset from `config.textPosition()` (TOP=250, MIDDLE=100, BOTTOM=-50)
   c. Projects 3D world position to 2D canvas via `Perspective.localToCanvas()`
   d. Resolves text color based on `config.colorStyle()`:
      - `SINGLE` → `config.singleColor()`
      - `MULTI + PORTAL_COLORS` → `getPortalColor()` (samples HSL from game model)
      - `MULTI + UNIQUE_COLORS` → per-destination color from `portalColors` map
   e. Draws black outline (+1px offset), then colored label text

**Shutdown Flow:**
1. `shutDown()` calls `overlayManager.remove(overlay)` — rendering stops immediately

---

## Key Abstractions

**`PORTAL_LABELS` static map:**
- Purpose: Maps raw RuneScape game object IDs to human-readable destination names
- Location: `PortalNameOverlay.java` static initializer block (lines 31–171)
- Pattern: Each destination has 3–5 object IDs (portal variants for different room rotations/styles), all mapping to the same string
- Note: IDs in range `56038–56073` appear to be a newer set; IDs `13615–13633` get a special bypass in `isPortalObject()`

**`portalColors` instance map:**
- Purpose: Caches config color lookups per destination name to avoid redundant config calls
- Built by: `updatePortalColors()` — called on startup and on every MULTI+UNIQUE render pass
- Key: canonical destination name string; Value: `java.awt.Color`

**`customNameOverrides` instance map:**
- Purpose: Stores user's `OriginalName=CustomName` override pairs
- Built by: `updateCustomNames()` — called inside `updatePortalColors()` and also on every render pass for PORTAL_LABELS matched objects
- Note: `updateCustomNames()` is called on every matching portal during render (minor inefficiency — see CONCERNS)

**Portal Color Extraction (`getPortalColor`):**
- Reads `Model.getFaceColors1()[0]` from the game object's renderable model
- Unpacks Jagex HSL encoding via `JagexColor.unpackHue/Saturation/Luminance`
- Converts to RGB then brightens by +0.4 brightness factor for readability

---

## Entry Points

**Plugin Main:**
- Location: `src/main/java/com/portalname/PortalNamePlugin.java`
- Triggers: RuneLite plugin manager via `@PluginDescriptor(name = "POH Portal Labels")`
- Responsibilities: Register/deregister overlay, provide config binding

**Plugin Properties:**
- Location: `runelite-plugin.properties`
- Triggers: RuneLite external plugin loader
- Responsibilities: Declares display name, author, description, tags, main plugin class

**Test Launcher:**
- Location: `src/test/java/com/portalname/PortalNamePluginTest.java`
- Triggers: Manual execution or `shadowJar` Gradle task
- Responsibilities: Launches RuneLite client with this plugin loaded for manual testing

---

## Error Handling

**Strategy:** Defensive nulls + early returns; no exceptions thrown, no error dialogs

**Patterns:**
- `render()` returns `null` (expected by RuneLite) on all early-exit conditions
- `getPortalColor()` returns `Color.WHITE` if model or face colors are null
- `isPortalObject()` returns `false` if `ObjectComposition` is null
- `updateCustomNames()` silently skips malformed/empty lines without logging
- Tile nulls checked before accessing game objects in all scene-scan loops

---

## Cross-Cutting Concerns

**Logging:** Lombok `@Slf4j` on all classes; only `log.debug()` calls present — startup/shutdown messages and developer debug output in `EventSubscriber`

**Validation:** Inline in `updateCustomNames()` — checks for `=` separator, non-empty parts; no schema validation elsewhere

**Authentication:** Not applicable — plugin is read-only UI; relies on RuneLite client session state (`GameState.LOGGED_IN` check)

**Config Refresh:** No `@Subscribe` on `ConfigChanged` event — config is read on demand each frame for position/style; colors are refreshed via explicit `updatePortalColors()` call on startup and inside the render loop for UNIQUE mode

---

## Gaps & Unknowns

- **No `ConfigChanged` listener**: If a user changes color settings while in a POH, SINGLE mode reads them live per frame (fine), but UNIQUE mode only re-reads inside the render loop (also fine, but triggers `updatePortalColors()` every matching portal found — see CONCERNS)
- **`updateCustomNames()` is called twice per portal**: Once inside `updatePortalColors()` at startup, and again on each portal match during render. This is a minor inefficiency.
- **EventSubscriber is dead code in production**: The class exists and is fully implemented but all wiring is commented out. It is not clear if it will ever be re-enabled or should be removed.
- **POH detection method**: Scans the entire tile grid to find `POH_EXIT_PORTAL` on every frame. This could be cached with a `ConfigChanged` / scene change event.
- **No test coverage for rendering logic**: `PortalNamePluginTest` is a RuneLite launcher, not a unit test.

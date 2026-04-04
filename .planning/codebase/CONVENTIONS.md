# Coding Conventions
> Last updated: 2026-04-03 | Focus: conventions

## Summary
This is a small RuneLite external plugin (5 source files) following standard RuneLite plugin conventions: Guice dependency injection via `@Inject`/`@Provides`, Lombok for logging, and a config interface for user settings. Code style is consistent with the RuneLite plugin template — Allman-style braces, 4-space (tab) indentation, and `camelCase` naming throughout.

---

## Naming Conventions

**Classes:**
- `PascalCase` with the plugin name as prefix: `PortalNamePlugin`, `PortalNameConfig`, `PortalNameOverlay`, `PortalNameEventSubscriber`
- The "PortalName" prefix is a template artifact from the RuneLite external plugin template; the real plugin is "POH Portal Labels"

**Methods:**
- `camelCase` throughout: `updatePortalColors()`, `updateCustomNames()`, `isPortalObject()`, `getPortalColor()`
- Config interface methods mirror their `keyName` in camelCase: `annakarlColor()`, `colorStyle()`, `textPosition()`
- Event handler methods follow RuneLite convention: `on` + event class name → `onMenuOptionClicked(MenuOptionClicked event)`

**Fields and variables:**
- `camelCase` for instance fields: `portalColors`, `customNameOverrides`, `client`
- `UPPER_SNAKE_CASE` for static final constants: `PORTAL_LABELS`
- Config section string constants use `camelCase` field names: `singleStyle`, `multiStyle`, `uniqueColors`, `customNames`

**Config keys (`keyName`):**
- `camelCase` string: `"colorStyle"`, `"singleColor"`, `"annakarlColor"`, `"enableCustomNames"`

**Enums:**
- Type names in `PascalCase`: `ColorStyle`, `TextPosition`, `ColorSelection`
- Enum constants in `UPPER_SNAKE_CASE`: `SINGLE`, `MULTI`, `TOP`, `MIDDLE`, `BOTTOM`, `PORTAL_COLORS`, `UNIQUE_COLORS`

---

## Formatting & Style

**Brace style:** Allman (opening brace on its own line), used consistently in the main plugin class and overlay:
```java
public class PortalNamePlugin extends Plugin
{
    @Override
    protected void startUp() throws Exception
    {
        ...
    }
}
```

**Exception:** The config interface uses inline braces for single-expression `default` methods:
```java
default Color annakarlColor() { return Color.GREEN; }
```

**Indentation:** Tabs (4-space equivalent), consistent throughout.

**Line length:** No enforced limit; long `@ConfigItem` annotation blocks are broken across lines for readability.

**Import style:** Specific imports preferred; wildcard imports used only in `PortalNameEventSubscriber` (`net.runelite.api.*`) and for `java.awt.*`.

---

## Java Idioms & Libraries

**Lombok (`org.projectlombok:lombok:1.18.30`):**
- `@Slf4j` on `PortalNamePlugin`, `PortalNameOverlay`, and `PortalNameEventSubscriber` — provides a `log` field
- No other Lombok annotations are used (no `@Data`, `@Builder`, etc.)

**Guice (via RuneLite's bundled Guice):**
- `@Inject` on constructor or field — used in all classes
- Constructor injection preferred for required collaborators (`Client` in `PortalNameOverlay` and `PortalNameEventSubscriber`)
- Field injection used for config and overlay manager in `PortalNamePlugin`

**Collections:**
- `java.util.HashMap` used directly (no Guava)
- Static initializer block used to populate the large `PORTAL_LABELS` map

**`javax.inject.Inject`** (not `com.google.inject.Inject`) — following RuneLite convention.

---

## RuneLite-Specific Conventions

**Plugin class (`PortalNamePlugin.java`):**
- Extends `net.runelite.client.plugins.Plugin`
- Annotated with `@PluginDescriptor(name = "POH Portal Labels")`
- `@Slf4j` for debug-level startup/shutdown logging
- `startUp()` / `shutDown()` override the lifecycle hooks; overlay is added/removed here
- Config provided via `@Provides` method calling `configManager.getConfig(PortalNameConfig.class)`

```java
@Provides
PortalNameConfig provideConfig(ConfigManager configManager)
{
    return configManager.getConfig(PortalNameConfig.class);
}
```

**Config interface (`PortalNameConfig.java`):**
- `interface` extending `net.runelite.client.config.Config`
- Annotated with `@ConfigGroup("PortalName")`
- All options are `default` methods returning the default value
- Sections declared as `String` constants annotated with `@ConfigSection`
- Color options use `@Alpha` to enable alpha-channel color picker
- Enums defined as inner types within the config interface

```java
@ConfigItem(
    keyName = "singleColor",
    name = "Color",
    description = "Single color for all labels",
    section = singleStyle
)
@Alpha
default Color singleColor()
{
    return Color.GREEN;
}
```

**Overlay (`PortalNameOverlay.java`):**
- Extends `net.runelite.client.ui.overlay.Overlay`
- Position set in constructor: `setPosition(OverlayPosition.DYNAMIC)`
- Layer set in constructor: `setLayer(OverlayLayer.ABOVE_SCENE)`
- `render(Graphics2D graphics)` is the main draw method; returns `null` (no bounding box)
- `Client` injected via constructor; `config` injected as field
- Guards against non-logged-in state and non-POH scenes at the top of `render()`

**Event subscriber (`PortalNameEventSubscriber.java`):**
- Plain class (not extending any base)
- `@Subscribe` from `net.runelite.client.eventbus.Subscribe` on handler methods
- Currently **disabled** — registration is commented out in `PortalNamePlugin`; used only as a developer diagnostic tool for logging object IDs
- Must be manually registered/unregistered via `eventBus.register()` / `eventBus.unregister()`

---

## Config Structure Pattern

Config sections are used to group related options in the RuneLite settings panel:

| Section constant | `@ConfigSection` name | position |
|---|---|---|
| `singleStyle` | "Single Style" | 0 |
| `multiStyle` | "Multi Style" | 1 |
| `uniqueColors` | "Unique Portal Colors" | 2 |
| `customNames` | "Custom Portal Names" | 3 |

Each `@ConfigItem` references its section by the **string value** of the constant (e.g., `section = "uniqueColors"` or `section = singleStyle`). Both forms are present in the codebase (inconsistency noted in Gaps).

---

## Overlay Registration Pattern

Overlays are added/removed in the plugin lifecycle:

```java
// startUp()
overlay.updatePortalColors();  // pre-warm cache before rendering
overlayManager.add(overlay);

// shutDown()
overlayManager.remove(overlay);
```

`updatePortalColors()` is called from `startUp()` and also lazily inside `render()` when the MULTI+UNIQUE_COLORS path is taken. This dual-call pattern means the overlay works correctly even if config changes mid-session.

---

## Comment Style

- Inline comments used liberally to explain RuneLite-specific decisions (e.g., why certain object ID ranges are special-cased)
- Commented-out code left in place as toggle instructions for developers enabling diagnostic mode
- No Javadoc present

---

## Gaps & Unknowns

- **Inconsistent `section` reference style:** Some `@ConfigItem` annotations use the string constant reference (e.g., `section = singleStyle`) while others use the string literal (e.g., `section = "multiStyle"`, `section = "uniqueColors"`). Both work at runtime but are inconsistent.
- **No formatter config file** (no `.editorconfig`, `checkstyle.xml`, or `google-java-format` setup) — style is maintained by convention only.
- **`updateCustomNames()` called twice per frame** in `render()` when custom names are enabled: once at the top of the portal loop, and once inside each matching portal. This is a minor performance issue.
- **`PortalNameEventSubscriber`** is fully implemented but permanently disabled. Its intended purpose (developer diagnostics) is not documented beyond code comments.

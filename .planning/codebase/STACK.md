# Technology Stack

> Last updated: 2026-04-03 | Focus: tech

## Summary

POH Portal Labels is a RuneLite external plugin written in Java 11, built with Gradle 8.10. It uses RuneLite's latest release client API for overlays and config, Lombok for boilerplate reduction, and JUnit 4 for testing. The plugin has no external service dependencies — it is entirely self-contained within the RuneLite plugin ecosystem.

---

## Languages

**Primary:**
- Java 11 (`options.release.set(11)` in `build.gradle`) — all plugin logic in `src/main/java/com/portalname/`

**Secondary:**
- None

---

## Runtime

**Environment:**
- Java 11 (required by compiler target; compatible with RuneLite's client JVM)
- RuneLite version: `latest.release` (resolved at build time from `https://repo.runelite.net`)

**Package Manager / Build:**
- Gradle 8.10 (via Gradle Wrapper)
- Wrapper config: `gradle/wrapper/gradle-wrapper.properties`
- Distribution URL: `https://services.gradle.org/distributions/gradle-8.10-all.zip`
- Lockfile: No lockfile present (uses `latest.release` version for RuneLite)

---

## Frameworks

**Core:**
- RuneLite Client API (`net.runelite:client:latest.release`) — plugin lifecycle, config, overlays, game events
  - Declared `compileOnly` (provided at runtime by the RuneLite launcher)

**Dependency Injection:**
- Google Guice (via RuneLite's embedded DI) — `@Inject`, `@Provides` annotations
  - `com.google.inject.Provides` imported in `PortalNamePlugin.java`
  - `javax.inject.Inject` used throughout

**Boilerplate Reduction:**
- Lombok 1.18.30 (`org.projectlombok:lombok`)
  - `@Slf4j` logging annotation used in all four main source files
  - Declared as both `compileOnly` and `annotationProcessor`

**Testing:**
- JUnit 4.12 (`junit:junit:4.12`) — `testImplementation`
- RuneLite client and jshell (`net.runelite:client`, `net.runelite:jshell`) — used to launch the full client in test mode via `PortalNamePluginTest.java`

---

## Key Dependencies

**Critical:**
- `net.runelite:client:latest.release` — the entire plugin API surface; provides `Plugin`, `Overlay`, `OverlayManager`, `ConfigManager`, `Client`, `EventBus`, `Subscribe`, and all game object types

**Developer Ergonomics:**
- `org.projectlombok:lombok:1.18.30` — reduces logging boilerplate via `@Slf4j`; without it, SLF4J logger fields would need manual declaration

**Testing:**
- `junit:junit:4.12` — test runner (only one test class exists: `PortalNamePluginTest`)
- `net.runelite:jshell:latest.release` — supports running a live RuneLite client session for manual/integration testing

---

## Build Configuration

**Build file:** `build.gradle`
**Project name:** `poh-portal-labels` (set in `settings.gradle`)
**Group:** `com.portalname`
**Version:** `1.1.0`
**Encoding:** UTF-8

**Custom Task:**
- `shadowJar` — creates a fat JAR (`poh-portal-labels-1.1.0-all.jar`) bundling test + main output + all `testRuntimeClasspath` dependencies. Entry point: `com.portalname.PortalNamePluginTest`. Used to run the plugin in a live RuneLite client locally.

**Repositories:**
1. `mavenLocal()` — for local development overrides
2. `https://repo.runelite.net` — RuneLite-specific artifacts only (filtered by group regex `net\.runelite.*`)
3. `mavenCentral()` — standard dependencies (Lombok, JUnit)

---

## Dev Tools

**IDE support (inferred from `.gitignore`):**
- IntelliJ IDEA (`.idea/` excluded)
- Eclipse (`.project`, `.settings/`, `.classpath` excluded)
- NetBeans (`nbproject/`, `nbactions.xml`, `nb-configuration.xml` excluded)
- VS Code (`.vscode/` excluded)

**Linting/Formatting:**
- No dedicated linter or formatter configured (no Checkstyle, SpotBugs, or Spotless plugins in `build.gradle`)

**Logging:**
- SLF4J via Lombok's `@Slf4j` — logging calls use `log.debug(...)` pattern

---

## Source Structure

```
src/
├── main/java/com/portalname/
│   ├── PortalNamePlugin.java          # Plugin entry point (lifecycle)
│   ├── PortalNameConfig.java          # Config interface (RuneLite @ConfigGroup)
│   ├── PortalNameOverlay.java         # Overlay rendering logic
│   └── PortalNameEventSubscriber.java # Game event listener (currently disabled)
└── test/java/com/portalname/
    └── PortalNamePluginTest.java      # Launches full RuneLite client for manual testing
```

---

## Gaps & Unknowns

- RuneLite version is `latest.release` — this means the build resolves to whatever RuneLite publishes as latest, which may cause non-reproducible builds over time. No version pin or lockfile.
- No static analysis tools (Checkstyle, PMD, SpotBugs) are configured.
- No code formatting enforcement (Spotless or similar) is present.
- Java version is set via `options.release.set(11)` but not enforced via `.java-version` or a toolchain declaration — the build relies on the local JVM being compatible.

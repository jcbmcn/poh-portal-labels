# Testing Patterns
> Last updated: 2026-04-03 | Focus: testing

## Summary
This plugin has essentially no automated tests. The single test file (`PortalNamePluginTest.java`) is a RuneLite integration launcher — it boots the full RuneLite client with the plugin loaded for manual in-game testing. No unit tests, no mocks, and no assertions exist anywhere in the test source tree.

---

## Test Framework

**Declared dependencies (`build.gradle`):**
```groovy
testImplementation 'junit:junit:4.12'
testImplementation group: 'net.runelite', name:'client', version: runeLiteVersion
testImplementation group: 'net.runelite', name:'jshell', version: runeLiteVersion
```

- **JUnit 4.12** is declared as a test dependency but is **not used** — no `@Test` annotations exist anywhere.
- The `net.runelite:jshell` dependency provides `ExternalPluginManager`, which is used by the test launcher.

---

## Test File

**Location:** `src/test/java/com/portalname/PortalNamePluginTest.java`

**What it is:** A manual RuneLite client launcher, not an automated test:

```java
public class PortalNamePluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(PortalNamePlugin.class);
        RuneLite.main(args);
    }
}
```

**How it's used:**
- Run via the `shadowJar` task (see `build.gradle`), which bundles the plugin with the full RuneLite client into `poh-portal-labels-{version}-all.jar`
- The JAR's `Main-Class` is set to `com.portalname.PortalNamePluginTest`
- Developer runs the JAR locally to test the plugin inside a live RuneLite session
- This is the **standard RuneLite external plugin testing approach** — no in-process automated testing of rendering or game state

---

## Build Task for Manual Testing

```groovy
tasks.register('shadowJar', Jar) {
    manifest {
        attributes('Main-Class': 'com.portalname.PortalNamePluginTest', 'Multi-Release': true)
    }
    archiveFileName.set("${rootProject.name}-${project.version}-all.jar")
    // bundles main + test + all testRuntimeClasspath dependencies
}
```

Run with:
```bash
./gradlew shadowJar
java -jar build/libs/poh-portal-labels-<version>-all.jar
```

---

## What Is Tested

| Area | Coverage |
|---|---|
| Plugin startup/shutdown lifecycle | ❌ Not tested |
| Overlay rendering logic | ❌ Not tested |
| Config defaults and option structure | ❌ Not tested |
| Custom name parsing (`OriginalName=CustomName`) | ❌ Not tested |
| Portal color lookup (SINGLE / MULTI / UNIQUE) | ❌ Not tested |
| `isPortalObject()` filtering logic | ❌ Not tested |
| HSL→RGB color conversion math | ❌ Not tested |
| Event subscriber (disabled) | ❌ Not tested |
| Manual in-game rendering | ✅ Via launcher JAR |

---

## CI Pipeline

`.github/workflows/release.yml` runs **semantic-release** on push to `main`/`master`. It does **not** run `./gradlew test` or `./gradlew build` — CI is release-only, not quality-gating.

---

## How RuneLite Plugin Testing Works (Context)

RuneLite external plugins cannot easily be unit-tested in isolation because:
1. `Overlay.render()` depends on a live `Client` instance and active game state
2. RuneLite's Guice injector wires everything together at startup
3. The `Scene`, `Tile[][]`, `GameObject`, and `Perspective` APIs have no mock-friendly interfaces in the public API

The `ExternalPluginManager.loadBuiltin()` + `RuneLite.main()` pattern is the **canonical RuneLite approach** for external plugin development testing. True unit testing would require either:
- Mockito mocks for `Client`, `Scene`, `Tile`, `GameObject`
- A test-only Guice module
- Neither of which exists in this codebase

---

## Gaps & Unknowns

- **The custom name parsing logic** in `PortalNameOverlay.updateCustomNames()` is a pure string-processing function with no game dependencies — it is a strong unit-test candidate but has no tests.
- **HSL→RGB conversion** (`hslToRgb`, `hueToRgb`, `clamp`, `brighten`) are pure static math functions — trivially testable with JUnit but untested.
- **JUnit 4 is declared but unused.** If unit tests are added, JUnit 4 is already available via `testImplementation 'junit:junit:4.12'`.
- **No Mockito dependency declared.** Adding mocks for `Client`/`GameObject` would require adding Mockito to `build.gradle`.
- **No coverage tooling** configured (no JaCoCo or similar).
- **No test is run in CI** — a broken build would not be caught automatically before release.

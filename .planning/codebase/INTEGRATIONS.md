# External Integrations

> Last updated: 2026-04-03 | Focus: integrations

## Summary

POH Portal Labels integrates exclusively with the RuneLite plugin ecosystem — there are no external HTTP APIs, databases, or SaaS services used at runtime. The only external integrations are: (1) the RuneLite client API consumed at build/runtime, (2) a GitHub Actions CI pipeline for automated semantic versioning and releases, and (3) the RuneLite plugin hub manifest for plugin discovery.

---

## RuneLite Plugin API

**Dependency:**
- `net.runelite:client:latest.release` (Maven artifact, `https://repo.runelite.net`)
- Declared `compileOnly` — provided by the RuneLite launcher at runtime; not bundled in plugin JARs

**RuneLite APIs used:**

| Package / Class | Usage |
|---|---|
| `net.runelite.client.plugins.Plugin` | Base class for `PortalNamePlugin` |
| `net.runelite.client.plugins.PluginDescriptor` | Plugin metadata annotation (`name = "POH Portal Labels"`) |
| `net.runelite.client.config.ConfigManager` | Injected to wire `PortalNameConfig` via `@Provides` |
| `net.runelite.client.config.*` | Config group, items, sections (all config annotations) |
| `net.runelite.client.ui.overlay.OverlayManager` | Adding/removing the `PortalNameOverlay` on startup/shutdown |
| `net.runelite.client.ui.overlay.Overlay` | Base class for `PortalNameOverlay` |
| `net.runelite.client.ui.overlay.OverlayLayer` | Overlay layer assignment |
| `net.runelite.client.ui.overlay.OverlayPosition` | Overlay position assignment |
| `net.runelite.client.eventbus.Subscribe` | Event subscription in `PortalNameEventSubscriber` |
| `net.runelite.api.Client` | Game client access (scene, tiles, game objects) |
| `net.runelite.api.GameObject` | Portal object interaction |
| `net.runelite.api.Scene`, `Tile`, `Point`, `Model` | Scene traversal and rendering |
| `net.runelite.api.Perspective` | 3D-to-screen coordinate conversion |
| `net.runelite.api.JagexColor` | Color format used by RuneLite's rendering engine |
| `net.runelite.api.gameval.ObjectID` | Game object ID constants for portal identification |
| `net.runelite.api.coords.LocalPoint` | Local coordinate system |
| `net.runelite.api.events.MenuOptionClicked` | Menu event used in the (currently disabled) event subscriber |
| `net.runelite.client.RuneLite` | Entry point used in `PortalNamePluginTest` to launch live client |
| `net.runelite.client.externalplugins.ExternalPluginManager` | Used in test to load the plugin into a live client session |

**Test-only:**
- `net.runelite:jshell:latest.release` — enables launching a full RuneLite client from the `shadowJar` for local development testing

---

## Plugin Manifest

**File:** `runelite-plugin.properties`

```
displayName=POH Portal Labels
author=jcbmcn
description=Adds labels of destinations to POH portals.
tags=portal,poh,construction,name,ui
plugins=com.portalname.PortalNamePlugin
support=https://github.com/jcbmcn/poh-portal-labels
```

This file is the RuneLite plugin hub manifest. It declares the plugin's entry class (`com.portalname.PortalNamePlugin`), which RuneLite uses to load the plugin. The `support` URL points to the GitHub repository.

---

## CI/CD Pipeline

**Platform:** GitHub Actions
**Workflow file:** `.github/workflows/release.yml`
**Trigger:** Push to `main` or `master` branch

**Pipeline steps:**
1. Checkout repository (`actions/checkout@v3`)
2. Set up Node.js 18 (`actions/setup-node@v3`)
3. Install semantic-release packages via `npm install` (ephemeral, no `package.json` committed):
   - `semantic-release`
   - `@semantic-release/git`
   - `@semantic-release/changelog`
   - `@semantic-release/github`
   - `@semantic-release/commit-analyzer`
   - `@semantic-release/release-notes-generator`
   - `conventional-changelog-conventionalcommits`
4. Run `npx semantic-release`

**Note:** The pipeline does NOT build the Java project. It only handles versioning and release publishing. There is no `./gradlew build` or `./gradlew test` step in CI.

**Required secret:**
- `GITHUB_TOKEN` — standard GitHub Actions token, used by `@semantic-release/github` to create GitHub releases

---

## Semantic Release Configuration

**File:** `.releaserc`

- **Branch:** `master` (releases triggered from `master` only)
- **Commit convention:** `conventionalcommits` preset (e.g., `feat:`, `fix:`, `chore:` prefixes)
- **Changelog:** Auto-updated in `CHANGELOG.md` and committed back with message `chore(release): <version> [skip ci]`
- **GitHub release:** Created automatically with generated release notes

**Semantic-release plugin chain:**
1. `@semantic-release/commit-analyzer` — determines next version from commit messages
2. `@semantic-release/release-notes-generator` — generates release notes
3. `@semantic-release/changelog` — writes/updates `CHANGELOG.md`
4. `@semantic-release/git` — commits `CHANGELOG.md` back to repo
5. `@semantic-release/github` — publishes GitHub release

---

## Data Storage

- **Databases:** None
- **File storage:** None
- **Caching:** None
- **Config persistence:** Handled entirely by RuneLite's built-in `ConfigManager` (stores to local RuneLite profile directory on the user's machine)

---

## Authentication & Identity

- No auth systems used by the plugin at runtime
- GitHub Actions uses the standard `GITHUB_TOKEN` secret for release publishing (no custom secrets required)

---

## Monitoring & Observability

- **Error tracking:** None (no Sentry, Rollbar, etc.)
- **Logging:** SLF4J via Lombok `@Slf4j` — logs to RuneLite's client logger at `DEBUG` level (e.g., `log.debug("Portal Name plugin started!")`)

---

## Webhooks & Callbacks

- **Incoming:** None
- **Outgoing:** None

---

## Gaps & Unknowns

- The CI pipeline has no build or test step — if the Java compilation breaks, it will not be caught before release. Consider adding `./gradlew build` as a CI step.
- `npm install` in CI is run without a lockfile (`package-lock.json`), so semantic-release package versions are not pinned. This could cause CI failures if a breaking change is published upstream.
- RuneLite version is `latest.release` with no pin — the plugin could silently break if RuneLite API surface changes between builds.
- No plugin hub submission workflow is visible (RuneLite external plugin PRs require a separate submission to the `runelite-external-plugins` repo); this process is not automated here.

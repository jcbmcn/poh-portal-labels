# POH Portal Labels

![logo.png](assets/logo.png)
Adds basic label names to POH portals. When I first created portals I often forgot which portal went where, and for speed I had wished there was a way to see where the portals went without having to hover or right-click. 

## Color Options
- Single - One color for all portals
- Multi - Portal Colors or Unique Colors
  - Portal - Matches color of portals for label text
  - Unique  - Unique color for each portal set by user

## Positioning
- Top
- Center
- Bottom

## Current supported portal locations

<!--lint ignore html_table-->
| Location |
|----------|
| Annakarl |
| Ape Atoll Dungeon |
| Arceuus Library |
| Ardougne |
| Barrows |
| Battlefront |
| Camelot |
| Carrallanger |
| Catherby |
| Cemetery |
| Civitas illa Fortis |
| Draynor Manor |
| Falador |
| Fenkenstrain's Castle |
| Fishing Guild |
| Ghorrock |
| Grand Exchange |
| Harmony Island |
| Kharyrll |
| Kourend |
| Lumbridge |
| Lunar Isle |
| Marim |
| Mind Altar |
| Salve Graveyard |
| Seers' Village |
| Senntisten |
| Troll Stronghold |
| Varrock |
| Waterbirth Island |
| Weiss |
| West Ardougne |
| Yanille |
| Yanille Watchtower |


![example_poh_labels.png](assets/example_poh_labels.png)

### Single Color
![single.png](assets/single.png)

## Multi - Portal Color
![portal_color.png](assets/portal_color.png)

## Multi - Unique Color
![unique.png](assets/unique.png)

## Configuration Settings
![config.png](assets/config.png)

## Developer Use: Helpful Tools

### What does `PortalNameEventSubscriber` do?

The [`PortalNameEventSubscriber`](src/main/java/com/portalname/PortalNameEventSubscriber.java) class listens for in-game menu option click events related to POH portals. When you click on a portal (or any game object), it checks if the click was on a game object and tries to find the corresponding object in the scene. If found, it logs information about the clicked object, including its ID, class, and world location. This is useful for debugging or for discovering portal object IDs to add new labels or support additional portals.

Uncomment lines in `PortalNamePlugin` to enable feature.

### Cache Viewer

https://abextm.github.io/cache2/#/editor is a RuneScape cache viewer and editor. It allows you to browse, search, and inspect game assets and data stored in the RuneScape cache, such as object definitions, models, textures, and more. This tool is useful for finding object IDs, names, and properties—helpful when developing plugins that interact with in-game objects like POH portals.


See [cache-viewer-queries](cache-viewer-queries) for queries.

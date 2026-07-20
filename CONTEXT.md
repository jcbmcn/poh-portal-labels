# Context — POH Portal Labels

Glossary of domain terms. No implementation details.

## Portal
A teleport object built in a Player-Owned House. Each Portal is configured by the
player to send them to one **Destination**. Portals only exist inside a POH.

## Destination
The place a Portal teleports to (e.g. "Ardougne", "Grand Exchange"). The label the
plugin draws is the Destination name. A single Portal can hold more than one
Destination and toggle between them (e.g. Varrock ↔ Grand Exchange); the active
Destination is whatever the Portal is currently set to.

## Multiloc
A game object that presents as one base object but transmogrifies into a
variant-specific object based on the player's game state (a varbit/varp value).
Every configurable Portal is a Multiloc: its base form is a nameless stub, and its
real Destination lives on the resolved variant.

## Impostor
The resolved variant a Multiloc currently transmogrifies into, given live game
state. Resolving a Portal's Impostor yields the current **Destination** name. This
is how a Destination is read dynamically — with no hardcoded list of object IDs —
and why it stays correct across house themes, floors, and Portal reconfiguration.

## House Theme
A cosmetic style applied to the whole POH (e.g. Wilderness). A theme changes the
underlying object IDs of Portals but not their nature as Multilocs, so Destination
resolution is theme-independent.

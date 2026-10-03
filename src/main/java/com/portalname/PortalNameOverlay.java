package com.portalname;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.api.Model;
import net.runelite.api.JagexColor;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import lombok.extern.slf4j.Slf4j;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;


@Slf4j
public class PortalNameOverlay extends Overlay
{


    /* Destination names are read dynamically from each portal's resolved impostor
     * composition (see resolvePortalDestination). No hardcoded ID→name map. */


    private final Map<String, Color> portalColors = new HashMap<>();
    private final Map<String, String> customNameOverrides = new HashMap<>();

    private final Set<GameObject> portalCandidates = new HashSet<>();
    private final Set<GameObject> homePortals = new HashSet<>();

    private final Client client;

    @Inject
    private PortalNameConfig config;

    @Inject
    public PortalNameOverlay(Client client)
    {
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    void updatePortalColors()
    {
        portalColors.clear();
        updateCustomNames();
        portalColors.put("Annakarl", config.annakarlColor());
        portalColors.put("Ape Atoll Dungeon", config.apeAtollColor());
        portalColors.put("Arceuus Library", config.arceuusLibraryColor());
        portalColors.put("Ardougne", config.ardougneColor());
        portalColors.put("Barrows", config.barrowsColor());
        portalColors.put("Battlefront", config.battlefrontColor());
        portalColors.put("Camelot", config.camelotColor());
        portalColors.put("Carrallanger", config.carrallangerColor());
        portalColors.put("Catherby", config.catherbyColor());
        portalColors.put("Cemetery", config.cemeteryColor());
        portalColors.put("Civitas illa Fortis", config.civitasColor());
        portalColors.put("Draynor Manor", config.draynorColor());
        portalColors.put("Falador", config.faladorColor());
        portalColors.put("Fenkenstrain's Castle", config.fenkenstrainColor());
        portalColors.put("Fishing Guild", config.fishingGuildColor());
        portalColors.put("Ghorrock", config.ghorrockColor());
        portalColors.put("Grand Exchange", config.grandExchangeColor());
        portalColors.put("Harmony Island", config.harmonyIslandColor());
        portalColors.put("Kharyrll", config.kharyrllColor());
        portalColors.put("Kourend", config.kourendColor());
        portalColors.put("Lumbridge", config.lumbridgeColor());
        portalColors.put("Lunar Isle", config.lunarIsleColor());
        portalColors.put("Marim", config.marimColor());
        portalColors.put("Mind Altar", config.mindAltarColor());
        portalColors.put("Salve Graveyard", config.salveGraveyardColor());
        portalColors.put("Seers' Village", config.seersVillageColor());
        portalColors.put("Senntisten", config.senntistenColor());
        portalColors.put("Troll Stronghold", config.trollStrongholdColor());
        portalColors.put("Varrock", config.varrockColor());
        portalColors.put("Waterbirth Island", config.waterbirthColor());
        portalColors.put("Weiss", config.weissColor());
        portalColors.put("West Ardougne", config.westArdougneColor());
        portalColors.put("Yanille", config.yanilleColor());
        portalColors.put("Yanille Watchtower", config.yanilleWatchtowerColor());
        portalColors.put("Trollheim", config.trollheimColor());
        portalColors.put("Paddewwa", config.paddewwaColor());
        portalColors.put("Lassar", config.lassarColor());
        portalColors.put("Dareeyak", config.dareeyakColor());
        portalColors.put("Ourania", config.ouraniaColor());
        portalColors.put("Barbarian", config.barbarianColor());
        portalColors.put("Khazard", config.khazardColor());
        portalColors.put("Ice Plateau", config.icePlateauColor());
        portalColors.put("Respawn", config.respawnColor());
        portalColors.put("Boat", config.boatColor());
    }

    private void updateCustomNames()
    {
        customNameOverrides.clear();
        
        if (!config.enableCustomNames())
        {
            return;
        }
        
        String customNamesList = config.customNamesList();
        if (customNamesList == null || customNamesList.trim().isEmpty())
        {
            return;
        }
        
        String[] lines = customNamesList.split("\n");
        for (String line : lines)
        {
            line = line.trim();
            if (line.isEmpty() || !line.contains("="))
            {
                continue;
            }
            
            String[] parts = line.split("=", 2);
            if (parts.length == 2)
            {
                String originalName = parts[0].trim();
                String customName = parts[1].trim();
                if (!originalName.isEmpty() && !customName.isEmpty())
                {
                    customNameOverrides.put(originalName, customName);
                }
            }
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.getGameState() != net.runelite.api.GameState.LOGGED_IN)
            return null;

        if (client.getLocalPlayer() == null)
            return null;

        // Gate labeling on being inside an actual POH instance. A POH is always an
        // instanced region, while world house portals (e.g. the Prifddinas portal at
        // 3239,6077, which sits next to the Gauntlet entrance) are not. The exit-portal
        // scan alone is not sufficient: those open-world portals share the "Portal"
        // name and "Home" menu option the scan looks for, so labels could render in
        // the regular world (see #49).
        WorldView worldView = client.getLocalPlayer().getWorldView();
        if (!client.isInInstancedRegion() || !isInPoh(worldView))
        {
            return null;
        }

        for (GameObject gameObject : portalCandidates)
        {
            if (gameObject.getWorldView() != worldView)
                continue;

            String originalLabel = resolvePortalDestination(gameObject);

            if (originalLabel != null)
            {
                // Check for custom name override
                String label = customNameOverrides.getOrDefault(originalLabel, originalLabel);
                LocalPoint localLocation = gameObject.getLocalLocation();
                if (localLocation != null)
                {
                    // Offset determines where the label is drawn on the portal
                    int zOffset;
                    switch (config.textPosition())
                    {
                        case TOP:
                            zOffset = 250;
                            break;
                        case BOTTOM:
                            zOffset = -50;
                            break;
                        case MIDDLE:
                        default:
                            zOffset = 100;
                            break;
                    }
                    FontMetrics metrics = graphics.getFontMetrics();
                    int xOffset = -(metrics.stringWidth(label) / 2);
                    Point textLocation = Perspective.localToCanvas(client, localLocation,
                            gameObject.getPlane(), zOffset);
                    if (textLocation != null)
                    {
                        graphics.setColor(Color.BLACK); // outline
                        graphics.drawString(label, textLocation.getX() + xOffset + 1, textLocation.getY() + 1);

                        // Determine color style and set colors
                        if (config.colorStyle() == PortalNameConfig.ColorStyle.SINGLE)
                        {
                            Color color = config.singleColor();
                            graphics.setColor(color);
                        }
                        else if (config.colorStyle() == PortalNameConfig.ColorStyle.MULTI)
                        {
                            // Determine if user wants unique colors or portal colors
                            if (config.colorSelection() == PortalNameConfig.ColorSelection.PORTAL_COLORS)
                            {
                                Color portalColor = getPortalColor(gameObject);
                                graphics.setColor(portalColor);
                            }
                            // Use colors set by user per portal.
                            else
                            {
                                // Use original label for color lookup to maintain consistency
                                Color textColor = portalColors.getOrDefault(originalLabel, Color.WHITE);
                                graphics.setColor(textColor);
                            }
                        }
                        graphics.drawString(label, textLocation.getX() + xOffset, textLocation.getY());
                    }
                }
            }
        }

        return null;
    }

    private Color getPortalColor(GameObject gameObject)
    {
        Model model = gameObject.getRenderable().getModel();
        if (model == null)
        {
            return Color.WHITE;
        }

        int[] colors = model.getFaceColors1();
        if (colors == null || colors.length == 0)
        {
            return Color.WHITE;
        }

        int h = JagexColor.unpackHue((short) colors[0]);
        int s = JagexColor.unpackSaturation((short) colors[0]);
        int l = JagexColor.unpackLuminance((short) colors[0]);

        Color color = hslToRgb(h, s, l);

        // Some portal colors can be quite dark. Brighten them for better
        // readability while maintaining the original hue and saturation.
        return brighten(color, 0.4f);
    }

    private static Color hslToRgb(int hue, int sat, int lum)
    {
        float h = (float) hue / JagexColor.HUE_MAX;
        float s = (float) sat / JagexColor.SATURATION_MAX;
        float l = (float) lum / JagexColor.LUMINANCE_MAX;

        float q = l < 0.5f ? l * (1 + s) : (l + s - l * s);
        float p = 2 * l - q;

        float r = hueToRgb(p, q, h + 1f / 3f);
        float g = hueToRgb(p, q, h);
        float b = hueToRgb(p, q, h - 1f / 3f);

        return new Color(
                clamp(Math.round(r * 255)),
                clamp(Math.round(g * 255)),
                clamp(Math.round(b * 255))
        );
    }

    private static float hueToRgb(float p, float q, float t)
    {
        if (t < 0)
        {
            t += 1;
        }
        if (t > 1)
        {
            t -= 1;
        }
        if (t < 1f / 6f)
        {
            return p + (q - p) * 6f * t;
        }
        if (t < 1f / 2f)
        {
            return q;
        }
        if (t < 2f / 3f)
        {
            return p + (q - p) * (2f / 3f - t) * 6f;
        }
        return p;
    }

    private static int clamp(int value)
    {
        return Math.min(255, Math.max(0, value));
    }

    private static Color brighten(Color color, float factor)
    {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[2] = Math.min(1f, hsb[2] + factor);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color(rgb);
    }

    /**
     * True while inside a Player-Owned House instance. Detected by the presence of
     * the POH exit portal in the scene. Must be combined with
     * {@link Client#isInInstancedRegion()} because open-world house portals (which
     * share the "Portal" name and "Home" option) also satisfy this scan; the
     * instanced-region check is what actually proves we are inside a POH.
     */
    private boolean isInPoh(WorldView worldView)
    {
        for (GameObject gameObject : homePortals)
        {
            if (gameObject.getWorldView() == worldView)
            {
                return true;
            }
        }
        return false;
    }

    void onGameObjectSpawned(GameObject gameObject)
    {
        if (gameObject == null || gameObject.getId() == -1)
        {
            return;
        }

        if (isHomePortal(gameObject))
        {
            homePortals.add(gameObject);
        }
        else if (isPortalCandidate(gameObject))
        {
            portalCandidates.add(gameObject);
        }
    }

    void onGameObjectDespawned(GameObject gameObject)
    {
        homePortals.remove(gameObject);
        portalCandidates.remove(gameObject);
    }

    void clearTrackedObjects()
    {
        homePortals.clear();
        portalCandidates.clear();
    }

    /**
     * Objects that could resolve to a teleport portal label. Multilocs are kept
     * regardless of their current impostor because the destination follows a
     * varbit and can change without the object respawning (see
     * resolvePortalDestination).
     */
    private boolean isPortalCandidate(GameObject gameObject)
    {
        net.runelite.api.ObjectComposition comp = client.getObjectDefinition(gameObject.getId());
        if (comp == null)
        {
            return false;
        }
        if (comp.getImpostorIds() != null)
        {
            return true;
        }
        String name = comp.getName();
        return name != null && name.endsWith(PORTAL_SUFFIX);
    }

    /**
     * The POH home/exit portal — named exactly "Portal". Detected by object ID
     * because the two known exit portals (classic 4525 and the Wilderness theme's
     * 60789, added 25 Feb 2026) are both named "Portal" but carry no identifying
     * menu action ("Home" belongs to the open-world house portals, not the in-POH
     * exit portal — see #48, #49). A name+action fallback is kept below for any
     * future exit portal variant.
     */
    // 60789 = RuneLite ObjectID1.POH_EXIT_PORTAL_WILDERNESS (package-private, so
    // inlined); sources: OSRS wiki "Exit portal" (id2 = 60789) and
    // RuneScape:Cache/2026/236.4 (new object "Portal 60789 poh_exit_portal_wilderness").
    private static final int POH_EXIT_PORTAL_WILDERNESS = 60789;

    private boolean isHomePortal(GameObject gameObject)
    {
        int id = gameObject.getId();
        if (id == net.runelite.api.gameval.ObjectID.POH_EXIT_PORTAL
                || id == POH_EXIT_PORTAL_WILDERNESS)
        {
            return true;
        }

        net.runelite.api.ObjectComposition comp = client.getObjectDefinition(id);
        if (comp == null || !"Portal".equals(comp.getName()))
        {
            return false;
        }

        String[] actions = comp.getActions();
        if (actions == null)
        {
            return false;
        }
        for (String action : actions)
        {
            if ("Home".equals(action))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads a POH teleport portal's current destination directly from the game,
     * with no hardcoded ID→name table. Returns the destination name (e.g.
     * "Ardougne", "Grand Exchange") or null if the object is not a configured
     * teleport portal.
     *
     * POH portals are multiloc objects: the base composition is a nameless stub
     * that transmogrifies into a destination-specific "impostor" based on the
     * player's varbit state. Resolving the impostor gives the live destination,
     * so this works on every floor, every house theme, and correctly follows a
     * portal that toggles between destinations (e.g. Varrock ↔ Grand Exchange).
     */
    private static final String PORTAL_SUFFIX = " Portal";

    private String resolvePortalDestination(GameObject gameObject)
    {
        net.runelite.api.ObjectComposition comp = client.getObjectDefinition(gameObject.getId());
        if (comp == null)
        {
            return null;
        }

        // Dual-destination portals (e.g. Grand Exchange/Varrock) are multilocs: the
        // base stub is nameless and the active destination lives on the impostor,
        // which follows the player's varbit — so toggling the portal relabels live.
        if (comp.getImpostorIds() != null)
        {
            comp = comp.getImpostor();
            if (comp == null)
            {
                return null;
            }
        }

        // Teleport portals are named "<Destination> Portal" (e.g. "Ardougne Portal").
        // This excludes the POH home portal (named exactly "Portal"), incense
        // burners, bookcases and every other object — no hardcoded ID list.
        String name = comp.getName();
        if (name == null || !name.endsWith(PORTAL_SUFFIX))
        {
            return null;
        }

        return name.substring(0, name.length() - PORTAL_SUFFIX.length());
    }
}

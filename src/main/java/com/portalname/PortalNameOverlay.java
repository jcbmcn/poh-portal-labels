package com.portalname;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.Model;
import net.runelite.api.JagexColor;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import lombok.extern.slf4j.Slf4j;
import java.awt.*;
import java.util.HashMap;
import javax.inject.Inject;
import java.util.Map;
import com.portalname.PortalNameConfig.TextPosition;


@Slf4j
public class PortalNameOverlay extends Overlay
{


    private static final Map<Integer, String> PORTAL_LABELS = new HashMap<>();

    static {
        PORTAL_LABELS.put(29341, "Annakarl");
        PORTAL_LABELS.put(29349, "Annakarl");
        PORTAL_LABELS.put(29357, "Annakarl");
        PORTAL_LABELS.put(56052, "Annakarl");
        PORTAL_LABELS.put(37592, "Ape Atoll Dungeon");
        PORTAL_LABELS.put(37604, "Ape Atoll Dungeon");
        PORTAL_LABELS.put(37616, "Ape Atoll Dungeon");
        PORTAL_LABELS.put(56073, "Ape Atoll Dungeon");
        PORTAL_LABELS.put(41416, "Arceuus Library");
        PORTAL_LABELS.put(41417, "Arceuus Library");
        PORTAL_LABELS.put(41418, "Arceuus Library");
        PORTAL_LABELS.put(56063, "Arceuus Library");
        PORTAL_LABELS.put(13619, "Ardougne");
        PORTAL_LABELS.put(13626, "Ardougne");
        PORTAL_LABELS.put(13633, "Ardougne");
        PORTAL_LABELS.put(56045, "Ardougne");
        PORTAL_LABELS.put(37591, "Barrows");
        PORTAL_LABELS.put(37603, "Barrows");
        PORTAL_LABELS.put(37615, "Barrows");
        PORTAL_LABELS.put(56072, "Barrows");
        PORTAL_LABELS.put(37584, "Battlefront");
        PORTAL_LABELS.put(37596, "Battlefront");
        PORTAL_LABELS.put(37608, "Battlefront");
        PORTAL_LABELS.put(56065, "Battlefront");
        PORTAL_LABELS.put(13618, "Camelot");
        PORTAL_LABELS.put(33094, "Camelot");
        PORTAL_LABELS.put(33100, "Camelot");
        PORTAL_LABELS.put(33106, "Camelot");
        PORTAL_LABELS.put(56043, "Camelot");
        PORTAL_LABELS.put(33434, "Carrallanger");
        PORTAL_LABELS.put(33437, "Carrallanger");
        PORTAL_LABELS.put(33440, "Carrallanger");
        PORTAL_LABELS.put(56061, "Carrallanger");
        PORTAL_LABELS.put(33432, "Catherby");
        PORTAL_LABELS.put(33435, "Catherby");
        PORTAL_LABELS.put(33438, "Catherby");
        PORTAL_LABELS.put(56059, "Catherby");
        PORTAL_LABELS.put(37590, "Cemetery");
        PORTAL_LABELS.put(37602, "Cemetery");
        PORTAL_LABELS.put(37614, "Cemetery");
        PORTAL_LABELS.put(56071, "Cemetery");
        PORTAL_LABELS.put(50713, "Civitas illa Fortis");
        PORTAL_LABELS.put(50714, "Civitas illa Fortis");
        PORTAL_LABELS.put(50715, "Civitas illa Fortis");
        PORTAL_LABELS.put(56057, "Civitas illa Fortis");
        PORTAL_LABELS.put(37583, "Draynor Manor");
        PORTAL_LABELS.put(37595, "Draynor Manor");
        PORTAL_LABELS.put(37607, "Draynor Manor");
        PORTAL_LABELS.put(56064, "Draynor Manor");
        PORTAL_LABELS.put(13617, "Falador");
        PORTAL_LABELS.put(13624, "Falador");
        PORTAL_LABELS.put(13631, "Falador");
        PORTAL_LABELS.put(56041, "Falador");
        PORTAL_LABELS.put(37587, "Fenkenstrain's Castle");
        PORTAL_LABELS.put(37599, "Fenkenstrain's Castle");
        PORTAL_LABELS.put(37611, "Fenkenstrain's Castle");
        PORTAL_LABELS.put(56068, "Fenkenstrain's Castle");
        PORTAL_LABELS.put(29343, "Fishing Guild");
        PORTAL_LABELS.put(29351, "Fishing Guild");
        PORTAL_LABELS.put(29359, "Fishing Guild");
        PORTAL_LABELS.put(56054, "Fishing Guild");
        PORTAL_LABELS.put(33433, "Ghorrock");
        PORTAL_LABELS.put(33436, "Ghorrock");
        PORTAL_LABELS.put(33439, "Ghorrock");
        PORTAL_LABELS.put(56060, "Ghorrock");
        PORTAL_LABELS.put(13615, "Grand Exchange");
        PORTAL_LABELS.put(33093, "Grand Exchange");
        PORTAL_LABELS.put(33099, "Grand Exchange");
        PORTAL_LABELS.put(33105, "Grand Exchange");
        PORTAL_LABELS.put(56039, "Grand Exchange");
        PORTAL_LABELS.put(37589, "Harmony Island");
        PORTAL_LABELS.put(37601, "Harmony Island");
        PORTAL_LABELS.put(37613, "Harmony Island");
        PORTAL_LABELS.put(56070, "Harmony Island");
        PORTAL_LABELS.put(29338, "Kharyrll");
        PORTAL_LABELS.put(29346, "Kharyrll");
        PORTAL_LABELS.put(29354, "Kharyrll");
        PORTAL_LABELS.put(56049, "Kharyrll");
        PORTAL_LABELS.put(29345, "Kourend");
        PORTAL_LABELS.put(29353, "Kourend");
        PORTAL_LABELS.put(29361, "Kourend");
        PORTAL_LABELS.put(56056, "Kourend");
        PORTAL_LABELS.put(13616, "Lumbridge");
        PORTAL_LABELS.put(13623, "Lumbridge");
        PORTAL_LABELS.put(13630, "Lumbridge");
        PORTAL_LABELS.put(56040, "Lumbridge");
        PORTAL_LABELS.put(29339, "Lunar Isle");
        PORTAL_LABELS.put(29347, "Lunar Isle");
        PORTAL_LABELS.put(29355, "Lunar Isle");
        PORTAL_LABELS.put(56050, "Lunar Isle");
        PORTAL_LABELS.put(29344, "Marim");
        PORTAL_LABELS.put(29352, "Marim");
        PORTAL_LABELS.put(29360, "Marim");
        PORTAL_LABELS.put(56055, "Marim");
        PORTAL_LABELS.put(37585, "Mind Altar");
        PORTAL_LABELS.put(37597, "Mind Altar");
        PORTAL_LABELS.put(37609, "Mind Altar");
        PORTAL_LABELS.put(56066, "Mind Altar");
        PORTAL_LABELS.put(37586, "Salve Graveyard");
        PORTAL_LABELS.put(37598, "Salve Graveyard");
        PORTAL_LABELS.put(37610, "Salve Graveyard");
        PORTAL_LABELS.put(56067, "Salve Graveyard");
        PORTAL_LABELS.put(33095, "Seers' Village");
        PORTAL_LABELS.put(33101, "Seers' Village");
        PORTAL_LABELS.put(33107, "Seers' Village");
        PORTAL_LABELS.put(56044, "Seers' Village");
        PORTAL_LABELS.put(29340, "Senntisten");
        PORTAL_LABELS.put(29348, "Senntisten");
        PORTAL_LABELS.put(29356, "Senntisten");
        PORTAL_LABELS.put(56051, "Senntisten");
        PORTAL_LABELS.put(33179, "Troll Stronghold");
        PORTAL_LABELS.put(33180, "Troll Stronghold");
        PORTAL_LABELS.put(33181, "Troll Stronghold");
        PORTAL_LABELS.put(56058, "Troll Stronghold");
        PORTAL_LABELS.put(33092, "Varrock");
        PORTAL_LABELS.put(33098, "Varrock");
        PORTAL_LABELS.put(33104, "Varrock");
        PORTAL_LABELS.put(56038, "Varrock");
        PORTAL_LABELS.put(29342, "Waterbirth Island");
        PORTAL_LABELS.put(29350, "Waterbirth Island");
        PORTAL_LABELS.put(29358, "Waterbirth Island");
        PORTAL_LABELS.put(56053, "Waterbirth Island");
        PORTAL_LABELS.put(37581, "Weiss");
        PORTAL_LABELS.put(37593, "Weiss");
        PORTAL_LABELS.put(37605, "Weiss");
        PORTAL_LABELS.put(56062, "Weiss");
        PORTAL_LABELS.put(37588, "West Ardougne");
        PORTAL_LABELS.put(37600, "West Ardougne");
        PORTAL_LABELS.put(37612, "West Ardougne");
        PORTAL_LABELS.put(56069, "West Ardougne");
        PORTAL_LABELS.put(33097, "Yanille");
        PORTAL_LABELS.put(33102, "Yanille");
        PORTAL_LABELS.put(33103, "Yanille");
        PORTAL_LABELS.put(33109, "Yanille");
        PORTAL_LABELS.put(56048, "Yanille");
        PORTAL_LABELS.put(33096, "Yanille Watchtower");
        PORTAL_LABELS.put(13620, "Yanille Watchtower"); // pulled from event subscriber
        PORTAL_LABELS.put(33108, "Yanille Watchtower");
        PORTAL_LABELS.put(56047, "Yanille Watchtower");
    }

    private final Map<String, Color> portalColors = new HashMap<>();
    private final Map<String, String> customNameOverrides = new HashMap<>();

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

        Scene scene = client.getLocalPlayer().getWorldView().getScene();
        Tile[][] tiles = scene.getTiles()[client.getLocalPlayer().getWorldLocation().getPlane()];

        boolean inPoh = false;
        outer:
        for (int x = 0; x < tiles.length; x++)
        {
            for (int y = 0; y < tiles[x].length; y++)
            {
                Tile tile = tiles[x][y];
                if (tile == null) continue;

                for (GameObject gameObject : tile.getGameObjects())
                {
                    if (gameObject != null && gameObject.getId() == ObjectID.POH_EXIT_PORTAL)
                    {
                        inPoh = true;
                        break outer;
                    }
                }
            }
        }

        if (!inPoh)
        {
            return null;
        }

        for (int x = 0; x < tiles.length; x++)
        {
            for (int y = 0; y < tiles[x].length; y++)
            {
                Tile tile = tiles[x][y];
                if (tile == null) continue;

                for (GameObject gameObject : tile.getGameObjects())
                {
                    if (gameObject == null || gameObject.getId() == -1)
                        continue;

                    int id = gameObject.getId();
                    String originalLabel = PORTAL_LABELS.get(id);

                    if (originalLabel != null && isPortalObject(gameObject))
                    {
                        // Check for custom name override
                        updateCustomNames();
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
                                    client.getLocalPlayer().getWorldLocation().getPlane(), zOffset);
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
                                        updatePortalColors();   // pull current color config
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

    private boolean isPortalObject(GameObject gameObject)
    {
        net.runelite.api.ObjectComposition composition = client.getObjectDefinition(gameObject.getId());
        if (composition == null)
        {
            return false;
        }

        String name = composition.getName();
        // Portal objects should have "Portal" in their name
        // This filters out pets and other objects that might share the same ID
        return name != null && name.contains("Portal");
    }
}

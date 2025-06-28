package com.portalname;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import lombok.extern.slf4j.Slf4j;
import java.awt.*;
import java.util.HashMap;
import javax.inject.Inject;
import java.util.Map;


@Slf4j
public class PortalNameOverlay extends Overlay
{


    /// Old Stuff
    private static final Map<Integer, String> PORTAL_LABELS = new HashMap<>();

    static {
        PORTAL_LABELS.put(13615, "Varrock/G.E.");
        PORTAL_LABELS.put(33179, "Troll Stronghold");
        PORTAL_LABELS.put(13616, "Lumbridge");
        PORTAL_LABELS.put(13617, "Falador");
        PORTAL_LABELS.put(13618, "Camelot");
        PORTAL_LABELS.put(50713, "Valamore");
        PORTAL_LABELS.put(37581, "Weiss");
        PORTAL_LABELS.put(13619, "Ardougne");
        PORTAL_LABELS.put(29345, "Kourend");
    }

    private final Client client;

    @Inject
    public PortalNameOverlay(Client client)
    {
        this.client = client;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (client.getGameState() != net.runelite.api.GameState.LOGGED_IN)
            return null;

        Scene scene = client.getLocalPlayer().getWorldView().getScene();
        Tile[][] tiles = scene.getTiles()[client.getLocalPlayer().getWorldLocation().getPlane()];

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
                    String label = PORTAL_LABELS.get(id);

                    if (label != null)
                    {
                        LocalPoint localLocation = gameObject.getLocalLocation();
                        if (localLocation != null)
                        {
                            // Use localToCanvas with height offset so it appears *above* the portal
                            int zOffset = 100;
                            int xOffset = -15;
                            Point textLocation = Perspective.localToCanvas(client, localLocation, client.getLocalPlayer().getWorldLocation().getPlane()
                                    , zOffset);
                            if (textLocation != null)
                            {
                                graphics.setColor(Color.BLACK); // outline
                                graphics.drawString(label, textLocation.getX() + xOffset + 1, textLocation.getY() + 1);


                                graphics.setColor(Color.GREEN);
                                graphics.drawString(label, textLocation.getX() + xOffset, textLocation.getY());
                            }
                        }
                    }
                }
            }
        }

        return null;
    }}
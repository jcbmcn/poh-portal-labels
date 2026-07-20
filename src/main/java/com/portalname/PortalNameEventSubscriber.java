package com.portalname;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import javax.inject.Inject;

@Slf4j
public class PortalNameEventSubscriber
{
    private final Client client;

    @Inject
    public PortalNameEventSubscriber(Client client)
    {
        this.client = client;
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        MenuAction action = event.getMenuAction();

        if (action == MenuAction.GAME_OBJECT_FIRST_OPTION
                || action == MenuAction.GAME_OBJECT_SECOND_OPTION
                || action == MenuAction.GAME_OBJECT_THIRD_OPTION
                || action == MenuAction.GAME_OBJECT_FOURTH_OPTION
                || action == MenuAction.GAME_OBJECT_FIFTH_OPTION)
        {
            int id = event.getId();
            GameObject clickedObject = findGameObjectById(id);

            if (clickedObject != null)
            {
                dumpComposition(clickedObject.getId());
            }
            else
            {
                log.info("[PORTAL-DIAG] Clicked GameObject ID={}, but couldn't find matching GameObject in scene.", id);
            }
        }
    }

    private void dumpComposition(int id)
    {
        ObjectComposition base = client.getObjectDefinition(id);
        if (base == null)
        {
            log.info("[PORTAL-DIAG] id={} base composition null", id);
            return;
        }

        int[] impostorIds = base.getImpostorIds();
        log.info("[PORTAL-DIAG] id={} baseName='{}' baseActions={} varbit={} varp={} impostorIds={}",
                id,
                base.getName(),
                java.util.Arrays.toString(base.getActions()),
                base.getVarbitId(),
                base.getVarPlayerId(),
                impostorIds == null ? "null" : java.util.Arrays.toString(impostorIds));

        if (impostorIds != null)
        {
            ObjectComposition imp = base.getImpostor();
            if (imp == null)
            {
                log.info("[PORTAL-DIAG]   impostor=null");
            }
            else
            {
                log.info("[PORTAL-DIAG]   impostorName='{}' impostorActions={}",
                        imp.getName(), java.util.Arrays.toString(imp.getActions()));
            }
        }
    }

    private GameObject findGameObjectById(int id)
    {
        int plane = client.getPlane();
        Tile[][] tiles = client.getScene().getTiles()[plane];

        for (int x = 0; x < tiles.length; x++)
        {
            for (int y = 0; y < tiles[x].length; y++)
            {
                Tile tile = tiles[x][y];
                if (tile == null)
                    continue;

                for (GameObject obj : tile.getGameObjects())
                {
                    if (obj != null && obj.getId() == id)
                    {
                        return obj;
                    }
                }
            }
        }

        return null;
    }
}

package com.portalname;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
		name = "POH Portal Labels"
)
public class PortalNamePlugin extends Plugin
{
	// Diagnostic tool for discovering portal object compositions. Disabled in
	// production; uncomment the register/unregister calls below to enable logging.
	@Inject
	private net.runelite.client.eventbus.EventBus eventBus;

	@Inject
	private PortalNameEventSubscriber eventSubscriber;

	@Inject
	private PortalNameConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PortalNameOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp() throws Exception
	{
		// DIAGNOSTIC: uncomment to log base/impostor composition of clicked objects.
		//eventBus.register(eventSubscriber);
                log.debug("Portal Name plugin started!");
                overlay.updatePortalColors();
                overlayManager.add(overlay);
		// Spawn events only fire on scene load, so pick up objects already
		// present when the plugin is enabled mid-session.
		clientThread.invokeLater(this::scanScene);
	}

	@Override
	protected void shutDown() throws Exception
	{
		//eventBus.unregister(eventSubscriber);
                log.debug("Portal Name plugin stopped!");
                overlayManager.remove(overlay);
		overlay.clearTrackedObjects();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		overlay.onGameObjectSpawned(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		overlay.onGameObjectDespawned(event.getGameObject());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			overlay.clearTrackedObjects();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if ("PortalName".equals(event.getGroup()))
		{
			overlay.updatePortalColors();
		}
	}

	private void scanScene()
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
		{
			return;
		}

		Scene scene = client.getLocalPlayer().getWorldView().getScene();
		for (Tile[][] plane : scene.getTiles())
		{
			for (Tile[] column : plane)
			{
				for (Tile tile : column)
				{
					if (tile == null)
						continue;

					for (GameObject gameObject : tile.getGameObjects())
					{
						overlay.onGameObjectSpawned(gameObject);
					}
				}
			}
		}
	}

	@Provides
	PortalNameConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PortalNameConfig.class);
	}
}

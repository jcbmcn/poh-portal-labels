package com.portalname;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.eventbus.EventBus;

@Slf4j
@PluginDescriptor(
		name = "POH Portal Labels"
)
public class PortalNamePlugin extends Plugin
{
	// @Inject
	// private Client client;

	// @Inject
	// private PortalNameEventSubscriber eventSubscriber;

	// @Inject
	// private EventBus eventBus;

	@Inject
	private PortalNameConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PortalNameOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		// UNCOMMENT BELOW TO GET LOGS FOR OBJECT IDs
		//eventBus.register(eventSubscriber);
		log.debug("Portal Name plugin started!");
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		// UNCOMMENT BELOW TO GET LOGS FOR OBJECT IDs
		//eventBus.unregister(eventSubscriber);
		log.debug("Portal Name plugin stopped!");
		overlayManager.remove(overlay);
	}

	@Provides
	PortalNameConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PortalNameConfig.class);
	}
}

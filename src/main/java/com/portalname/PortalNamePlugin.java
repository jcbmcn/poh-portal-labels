package com.portalname;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
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

	@Override
	protected void startUp() throws Exception
	{
		// DIAGNOSTIC: uncomment to log base/impostor composition of clicked objects.
		//eventBus.register(eventSubscriber);
                log.debug("Portal Name plugin started!");
                overlay.updatePortalColors();
                overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
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

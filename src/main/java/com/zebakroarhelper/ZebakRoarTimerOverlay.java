package com.zebakroarhelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;

public class ZebakRoarTimerOverlay extends OverlayPanel
{

	private final ZebakRoarPlugin plugin;
	private final ZebakRoarConfig config;

	@Inject
	public ZebakRoarTimerOverlay(Client client, ZebakRoarPlugin plugin, ZebakRoarConfig config)
	{

		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);

	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();

		if (config.timerMode() == ZebakRoarConfig.TimerMode.OFF)
		{
			return null;
		}

		if (config.timerMode() == ZebakRoarConfig.TimerMode.IN_ROAR_PHASE && !plugin.isInRoarPhase())
		{
			return null;
		}

		int attacksLeft = plugin.isInRoarPhase() ? Math.max(0, 4 - plugin.getZebakAttackCount()) : 4;

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Zebak Attacks until Roar:")
			.right(String.valueOf(attacksLeft))
			.rightColor(attacksLeft <= 1 ? Color.RED : Color.WHITE)
			.build());

		if (config.flashTimer() && attacksLeft <= 1 && (System.currentTimeMillis() % 1000) < 500) {
			panelComponent.setBackgroundColor(config.timerFlashColor());
		} else {
			panelComponent.setBackgroundColor(ComponentConstants.STANDARD_BACKGROUND_COLOR);
		}

		return super.render(graphics);
	}
}

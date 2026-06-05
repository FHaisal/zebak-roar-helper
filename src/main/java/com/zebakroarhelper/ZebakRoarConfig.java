package com.zebakroarhelper;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("zebakroar")
public interface ZebakRoarConfig extends Config
{
	@ConfigSection(
		name = "Zebak Roar",
		description = "Zebak Roar plugin settings",
		position = 0
	)
	String zebakRoarSection = "zebakRoar";

	@ConfigItem(
		keyName = "showHitOnly",
		name = "Show Hit-Only Tactics",
		description = "Show orange highlight on jugs whose splash radius already covers a safe zone",
		position = 1,
		section = zebakRoarSection
	)
	default boolean showHitOnly()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "hitOnlyColor",
		name = "Hit-Only Jug Color",
		description = "Color to highlight jugs that are already in a safe zone",
		position = 2,
		section = zebakRoarSection
	)
	default Color hitOnlyColor()
	{
		return new Color(255, 128, 0, 255); // Orange
	}

	@ConfigItem(
		keyName = "showPush",
		name = "Show Push Tactics",
		description = "Show purple highlight on jugs that can be pushed straight to a rock",
		position = 3,
		section = zebakRoarSection
	)
	default boolean showPush()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "pushColor",
		name = "Push Jug Color",
		description = "Color to highlight jugs that align with a rock",
		position = 4,
		section = zebakRoarSection
	)
	default Color pushColor()
	{
		return new Color(144, 0, 255, 255); // Purple
	}

	@ConfigItem(
		keyName = "showPushToHit",
		name = "Show Push-to-Hit Tactics",
		description = "Show cyan highlight on jugs that can be pushed and then attacked mid-roll",
		position = 5,
		section = zebakRoarSection
	)
	default boolean showPushToHit()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "pushToHitColor",
		name = "Push-to-Hit Color",
		description = "Color to highlight jugs that can be pushed past a safe zone",
		position = 6,
		section = zebakRoarSection
	)
	default Color pushToHitColor()
	{
		return new Color(0, 255, 255, 255); // Cyan
	}

	@ConfigItem(
		keyName = "timerMode",
		name = "Timer Mode",
		description = "Configure when the attack countdown timer shows",
		position = 7,
		section = zebakRoarSection
	)
	default TimerMode timerMode()
	{
		return TimerMode.IN_ROAR_PHASE;
	}

	@ConfigItem(
		keyName = "flashTimer",
		name = "Flash Timer UI",
		description = "Flash the timer UI panel when attacks remaining hit the threshold",
		position = 8,
		section = zebakRoarSection
	)
	default boolean flashTimer()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "timerFlashColor",
		name = "Timer Flash Color",
		description = "Color the timer flashes",
		position = 9,
		section = zebakRoarSection
	)
	default Color timerFlashColor()
	{
		return new Color(255, 0, 0, 255);
	}

	@Range(min = 1, max = 4)
	@ConfigItem(
		keyName = "flashThreshold",
		name = "Flash Threshold",
		description = "Number of attacks remaining when the timer background starts flashing",
		position = 10,
		section = zebakRoarSection
	)
	default int flashThreshold()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "showRollingTrueTile",
		name = "Show Rolling Jug True Tile",
		description = "Highlight the server true tile of moving jugs to time attacks",
		position = 11,
		section = zebakRoarSection
	)
	default boolean showRollingTrueTile()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "rollingTrueTileColor",
		name = "Rolling Jug True Tile Color",
		description = "Color of the moving jug true tile",
		position = 12,
		section = zebakRoarSection
	)
	default Color rollingTrueTileColor()
	{
		return new Color(0, 255, 255, 150);
	}

	@ConfigItem(
		keyName = "upsetStomach",
		name = "Upset Stomach",
		description = "Toggle if the Upset Stomach invocation is active (reduces jug splash from 5x5 to 3x3)",
		position = 13,
		section = zebakRoarSection
	)
	default boolean upsetStomach()
	{
		return false;
	}

	public enum TimerMode
	{
		ALWAYS_ON, IN_ROAR_PHASE, OFF
	}
}

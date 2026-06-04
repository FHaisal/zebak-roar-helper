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
		keyName = "jugMode",
		name = "Jug Highlight Mode",
		description = "Configure which jugs to highlight:\nNEAREST: Absolute closest jug to the player.\nOPTIMAL: Best jug that clears the most rocks.\nALL: Highlights and draws paths for all valid jugs.",
		position = 1,
		section = zebakRoarSection
	)
	default JugHighlightMode jugMode()
	{
		return JugHighlightMode.OPTIMAL;
	}

	@ConfigItem(
		keyName = "timerMode",
		name = "Timer Mode",
		description = "Configure when the attack countdown timer shows",
		position = 2,
		section = zebakRoarSection
	)
	default TimerMode timerMode()
	{
		return TimerMode.IN_ROAR_PHASE;
	}

	@ConfigItem(
		keyName = "showSafeZone",
		name = "Show Safe Zone",
		description = "Toggle the rendering of the safe zone behind rocks",
		position = 3,
		section = zebakRoarSection
	)
	default boolean showSafeZone()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "safeZoneColor",
		name = "Safe Zone Color",
		description = "Color of the safe zone behind rocks",
		position = 4,
		section = zebakRoarSection
	)
	default Color safeZoneColor()
	{
		return new Color(46, 219, 46, 255);
	}
	@Alpha
	@ConfigItem(
		keyName = "stanceTileColor",
		name = "Stance Tile Color",
		description = "Color of the tile you must stand on to push the jug",
		position = 5,
		section = zebakRoarSection
	)
	default Color stanceTileColor()
	{
		return new Color(144, 0, 255, 255);
	}

	@ConfigItem(
		keyName = "flashTimer",
		name = "Flash Timer UI",
		description = "Flash the timer UI panel when 1 attack is remaining",
		position = 6,
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
		description = "Color the timer flashes when 1 attack remains",
		position = 7,
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
		position = 7,
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
		position = 8,
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
		position = 9,
		section = zebakRoarSection
	)
	default Color rollingTrueTileColor()
	{
		return new Color(0, 255, 255, 150);
	}

	@Alpha
	@ConfigItem(
		keyName = "attackJugColor",
		name = "Attack Jug Color",
		description = "Color to highlight the jug model when it should be attacked instead of pushed",
		position = 10,
		section = zebakRoarSection
	)
	default Color attackJugColor()
	{
		return new Color(144, 0, 255, 255);
	}

	@ConfigItem(
		keyName = "upsetStomach",
		name = "Upset Stomach",
		description = "Toggle if the Upset Stomach invocation is active (reduces jug splash from 5x5 to 3x3)",
		position = 11,
		section = zebakRoarSection
	)
	default boolean upsetStomach()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showPushToHit",
		name = "Show Push-to-Hit Tactics",
		description = "Show cyan paths for jugs that can be pushed and then attacked mid-roll to clear a safe spot",
		position = 12,
		section = zebakRoarSection
	)
	default boolean showPushToHit()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "cyanHighlightColor",
		name = "Push-to-Hit Color",
		description = "Color of the push-to-hit stance tile and path",
		position = 13,
		section = zebakRoarSection
	)
	default Color cyanHighlightColor()
	{
		return new Color(0, 255, 255, 255);
	}

	public enum JugHighlightMode
	{
		NEAREST, OPTIMAL, ALL
	}

	public enum TimerMode
	{
		ALWAYS_ON, IN_ROAR_PHASE, OFF
	}
}

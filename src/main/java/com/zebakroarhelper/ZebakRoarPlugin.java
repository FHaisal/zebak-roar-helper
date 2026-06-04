package com.zebakroarhelper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@PluginDescriptor(
	name = "Zebak Roar Helper",
	description = "Highlights jugs and draws true-tile paths to help dodge Zebak's great roar.",
	tags = {"ToA", "Zebak", "Tombs of Amascut", "Raids", "helper", "jugs"}
)
public class ZebakRoarPlugin extends Plugin
{
	private static final int STATIONARY_JUG_ID = 11735;
	private static final int ROLLING_JUG_ID = 11736;
	private static final int ROCK_ID = 11737;

	private static final int ZEBAK_RANGED_ATTACK = 2487;
	private static final int ZEBAK_MAGIC_ATTACK = 2489;
	private static final int ZEBAK_GREAT_ROAR_ANIMATION = 9624;

	private static final Set<Integer> ACID_IDS = Set.of(45570, 45571, 45572, 45573, 45574, 45575);

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ZebakRoarOverlay overlay;

	@Inject
	private ZebakRoarTimerOverlay timerOverlay;

	@Getter
	private final List<NPC> activeJugs = new ArrayList<>();

	// Assuming rocks are NPCs for now. Note: verify IDs in-game, they might be GameObjects instead.
	@Getter
	private final List<NPC> activeRocks = new ArrayList<>(); 

	@Getter
	private final Set<WorldPoint> activeAcid = new java.util.HashSet<>();

	@Getter
	private int zebakAttackCount = 0;

	@Getter
	private boolean inRoarPhase = false;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		overlayManager.add(timerOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(timerOverlay);
		activeJugs.clear();
		activeRocks.clear();
		activeAcid.clear();
		zebakAttackCount = 0;
		inRoarPhase = false;
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		
		if (npc.getId() == STATIONARY_JUG_ID || npc.getId() == ROLLING_JUG_ID)
		{
			activeJugs.add(npc);
		}
		else if (npc.getId() == ROCK_ID)
		{
			if (!inRoarPhase)
			{
				inRoarPhase = true;
				zebakAttackCount = 0;
			}
			activeRocks.add(npc);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		
		if (npc.getId() == STATIONARY_JUG_ID || npc.getId() == ROLLING_JUG_ID)
		{
			activeJugs.remove(npc);
		}
		else if (npc.getId() == ROCK_ID)
		{
			activeRocks.remove(npc);
		}

		if (activeJugs.isEmpty() && activeRocks.isEmpty())
		{
			inRoarPhase = false;
			zebakAttackCount = 0;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!(event.getActor() instanceof NPC))
		{
			return;
		}

		NPC npc = (NPC) event.getActor();
		if (npc.getName() != null && npc.getName().equals("Zebak"))
		{
			int anim = npc.getAnimation();
			log.debug("Zebak Animation: {}", anim);
			
			if (anim == ZEBAK_GREAT_ROAR_ANIMATION)
			{
				if (inRoarPhase)
				{
					zebakAttackCount++;
				}
			}
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (ACID_IDS.contains(gameObject.getId()))
		{
			activeAcid.add(gameObject.getWorldLocation());
		}
		
		// If rocks end up being GameObjects instead of NPCs, 
		// you will check for ROCK_ID here and add to a List<GameObject> activeRocks
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (ACID_IDS.contains(gameObject.getId()))
		{
			activeAcid.remove(gameObject.getWorldLocation());
		}
		
		// If rocks end up being GameObjects instead of NPCs,
		// you will check for ROCK_ID here and remove from a List<GameObject> activeRocks
	}

	@Provides
	ZebakRoarConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ZebakRoarConfig.class);
	}
}

package com.zebakroarhelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class ZebakRoarOverlay extends Overlay
{
	private final Client client;
	private final ZebakRoarPlugin plugin;
	private final ZebakRoarConfig config;

	@Inject
	public ZebakRoarOverlay(Client client, ZebakRoarPlugin plugin, ZebakRoarConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null)
		{
			return null;
		}

		List<NPC> jugs = plugin.getActiveJugs();
		List<NPC> rocks = plugin.getActiveRocks();

		if (!jugs.isEmpty())
		{
			if (plugin.isInRoarPhase())
			{
				boolean isJugRolling = false;
				for (NPC jug : jugs)
				{
					if (jug.getId() == 11736) // ROLLING_JUG_ID
					{
						isJugRolling = true;
						if (config.showRollingTrueTile())
						{
							LocalPoint trueLocal = LocalPoint.fromWorld(client, jug.getWorldLocation());
							if (trueLocal != null)
							{
								Polygon trueTilePoly = Perspective.getCanvasTilePoly(client, trueLocal);
								if (trueTilePoly != null)
								{
									OverlayUtil.renderPolygon(graphics, trueTilePoly, config.rollingTrueTileColor());
								}
							}
						}
					}
				}

				if (!isJugRolling)
				{
					WorldPoint playerLocation = localPlayer.getWorldLocation();
					int[] scores = new int[jugs.size()];
					NPC[] targetRocks = new NPC[jugs.size()];
					
					int maxScore = -1;
					NPC optimalJug = null;
					int minDistanceForOptimal = Integer.MAX_VALUE;
					
					for (int i = 0; i < jugs.size(); i++)
					{
						NPC jug = jugs.get(i);
						WorldPoint jugLoc = jug.getWorldLocation();
						int score = 0;
						NPC currentJugAlignedRock = null;

						for (NPC rock : rocks)
						{
							WorldPoint rockLoc = rock.getWorldLocation();
							int dx = Math.abs(jugLoc.getX() - rockLoc.getX());
							int dy = Math.abs(jugLoc.getY() - rockLoc.getY());
							
							if (dx == 0 || dy == 0 || dx == dy)
							{
								score++;
								if (currentJugAlignedRock == null)
								{
									currentJugAlignedRock = rock;
								}
							}
						}
						scores[i] = score;
						targetRocks[i] = currentJugAlignedRock;

						int distance = jugLoc.distanceTo(playerLocation);

						if (score > 0)
						{
							// Optimal Jug logic
							if (score > maxScore)
							{
								maxScore = score;
								optimalJug = jug;
								minDistanceForOptimal = distance;
							}
							else if (score == maxScore)
							{
								if (distance < minDistanceForOptimal)
								{
									optimalJug = jug;
									minDistanceForOptimal = distance;
								}
							}
						}
					}

					List<NPC> validJugs = new java.util.ArrayList<>();
					for (int i = 0; i < jugs.size(); i++)
					{
						if (scores[i] > 0)
						{
							validJugs.add(jugs.get(i));
						}
					}
					validJugs.sort((a, b) -> Integer.compare(a.getWorldLocation().distanceTo(playerLocation), b.getWorldLocation().distanceTo(playerLocation)));
					
					NPC nearestJug = null;
					if (!validJugs.isEmpty())
					{
						List<NPC> top3 = validJugs.subList(0, Math.min(3, validJugs.size()));
						top3.sort((a, b) -> {
							int scoreA = scores[jugs.indexOf(a)];
							int scoreB = scores[jugs.indexOf(b)];
							return Integer.compare(scoreB, scoreA);
						});
						nearestJug = top3.get(0);
					}

					NPC primaryJug = null;
					ZebakRoarConfig.JugHighlightMode jugMode = config.jugMode();
					
					if (jugMode == ZebakRoarConfig.JugHighlightMode.OPTIMAL)
					{
						primaryJug = optimalJug;
					}
					else // NEAREST or ALL
					{
						primaryJug = nearestJug;
					}

					// Fallback
					if (primaryJug == null)
					{
						int minDistance = Integer.MAX_VALUE;
						for (NPC jug : jugs)
						{
							int distance = jug.getWorldLocation().distanceTo(playerLocation);
							if (distance < minDistance)
							{
								primaryJug = jug;
								minDistance = distance;
							}
						}
					}

					for (int i = 0; i < jugs.size(); i++)
					{
						NPC jug = jugs.get(i);
						int score = scores[i];
						NPC targetRock = targetRocks[i];
						
						if (jugMode != ZebakRoarConfig.JugHighlightMode.ALL && jug != primaryJug)
						{
							continue;
						}

						WorldPoint jugLoc = jug.getWorldLocation();
						int dx;
						int dy;

						if (targetRock != null)
						{
							WorldPoint rockLoc = targetRock.getWorldLocation();
							dx = Integer.compare(rockLoc.getX(), jugLoc.getX());
							dy = Integer.compare(rockLoc.getY(), jugLoc.getY());
						}
						else
						{
							dx = -1;
							dy = 0;
						}

						WorldPoint stanceTile = jugLoc.dx(-dx).dy(-dy);
						boolean isPoisoned = plugin.getActiveAcid().contains(stanceTile);
						boolean isAttackMode = isPoisoned || targetRock == null;

						if (isAttackMode)
						{
							// Attack Mode: Highlight the 3D model
							java.awt.Shape hull = jug.getConvexHull();
							if (hull != null)
							{
								OverlayUtil.renderPolygon(graphics, hull, config.attackJugColor());
							}
						}
						else
						{
							// Push Mode: Draw the floor tile
							Color highlightColor;
							if (jug == primaryJug)
							{
								highlightColor = Color.GREEN;
							}
							else if (score == 0)
							{
								highlightColor = Color.RED;
							}
							else
							{
								highlightColor = Color.YELLOW;
							}

							LocalPoint jugLocal = jug.getLocalLocation();
							if (jugLocal != null)
							{
								Polygon tilePolygon = Perspective.getCanvasTilePoly(client, jugLocal);
								if (tilePolygon != null)
								{
									OverlayUtil.renderPolygon(graphics, tilePolygon, highlightColor);
								}
							}

							// Draw the stance tile
							LocalPoint stanceLocal = LocalPoint.fromWorld(client, stanceTile);
							if (stanceLocal != null)
							{
								Polygon stancePoly = Perspective.getCanvasTilePoly(client, stanceLocal);
								if (stancePoly != null)
								{
									OverlayUtil.renderPolygon(graphics, stancePoly, config.stanceTileColor());
								}
							}
						}

						// Draw trajectory line if valid rock exists
						if (targetRock != null)
						{
							WorldPoint rockLoc = targetRock.getWorldLocation();
							int distanceToRock = Math.max(Math.abs(rockLoc.getX() - jugLoc.getX()), Math.abs(rockLoc.getY() - jugLoc.getY()));
							for (int step = 1; step < distanceToRock; step++)
							{
								WorldPoint pathTile = jugLoc.dx(dx * step).dy(dy * step);
								LocalPoint pathLocal = LocalPoint.fromWorld(client, pathTile);
								if (pathLocal != null)
								{
									Polygon pathPoly = Perspective.getCanvasTilePoly(client, pathLocal);
									if (pathPoly != null)
									{
										OverlayUtil.renderPolygon(graphics, pathPoly, new Color(0, 255, 0, 30));
									}
								}
							}
						}
					}
				}
			}
		}

		if (!rocks.isEmpty() && config.showSafeZone())
		{
			// Render safe zone behind rocks
			for (NPC rock : rocks)
			{
				WorldPoint rockLocation = rock.getWorldLocation();
				
				// NOTE: Since Zebak is always on the West side of the arena, the safe tiles are 
				// expected to be directly East (positive X) of the rocks. 
				// You may still need to adjust these offsets based on exact in-game mechanics.
				renderSafeTile(graphics, rockLocation.dx(1));
				renderSafeTile(graphics, rockLocation.dx(2));
				renderSafeTile(graphics, rockLocation.dx(3));
			}
		}

		return null;
	}

	private void renderSafeTile(Graphics2D graphics, WorldPoint worldPoint)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint != null)
		{
			Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
			if (poly != null)
			{
				OverlayUtil.renderPolygon(graphics, poly, config.safeZoneColor());
			}
		}
	}
}

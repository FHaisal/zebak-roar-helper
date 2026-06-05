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
				for (NPC jug : jugs)
				{
					if (jug.getId() == 11736) // ROLLING_JUG_ID
					{
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

				int splashRadius = config.upsetStomach() ? 1 : 2;
				int[] cyanDx = new int[jugs.size()];
				int[] cyanDy = new int[jugs.size()];
				int[] cyanDist = new int[jugs.size()];
				
				for (int i = 0; i < jugs.size(); i++)
				{
					if (!config.showPushToHit()) continue;
					if (scores[i] > 0) continue; // Skip push-to-hit if a simple push tactic already exists
					NPC jug = jugs.get(i);
					WorldPoint jugLoc = jug.getWorldLocation();
					int bestCyanDist = Integer.MAX_VALUE;
					int bestCyanDx = 0;
					int bestCyanDy = 0;
					
					int[] dxs = {0, 0, -1, 1};
					int[] dys = {1, -1, 0, 0};
					
					for (int d = 0; d < 4; d++)
					{
						for (int step = 1; step <= 25; step++)
						{
							WorldPoint pathTile = jugLoc.dx(dxs[d] * step).dy(dys[d] * step);
							boolean hit = false;
							for (NPC rock : rocks)
							{
								WorldPoint rockLoc = rock.getWorldLocation();
								if (pathTile.distanceTo(rockLoc.dx(1)) <= splashRadius ||
									pathTile.distanceTo(rockLoc.dx(2)) <= splashRadius ||
									pathTile.distanceTo(rockLoc.dx(3)) <= splashRadius)
								{
									hit = true;
									break;
								}
							}
							if (hit)
							{
								if (step < bestCyanDist)
								{
									bestCyanDist = step;
									bestCyanDx = dxs[d];
									bestCyanDy = dys[d];
								}
								break;
							}
						}
					}
					if (bestCyanDist != Integer.MAX_VALUE)
					{
						cyanDx[i] = bestCyanDx;
						cyanDy[i] = bestCyanDy;
						cyanDist[i] = bestCyanDist;
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

				NPC primaryPushJug = null;
				ZebakRoarConfig.JugHighlightMode jugMode = config.jugMode();
				
				if (jugMode == ZebakRoarConfig.JugHighlightMode.OPTIMAL)
				{
					primaryPushJug = optimalJug;
				}
				else // NEAREST or ALL
				{
					primaryPushJug = nearestJug;
				}

				NPC primaryCyanJug = null;
				int minCyanDistToPlayer = Integer.MAX_VALUE;
				for (int i = 0; i < jugs.size(); i++)
				{
					if (cyanDist[i] > 0)
					{
						int pDist = jugs.get(i).getWorldLocation().distanceTo(playerLocation);
						if (pDist < minCyanDistToPlayer)
						{
							minCyanDistToPlayer = pDist;
							primaryCyanJug = jugs.get(i);
						}
					}
				}

				// Fallback ONLY if both tactics yield nothing
				if (primaryPushJug == null && primaryCyanJug == null)
				{
					int minDistance = Integer.MAX_VALUE;
					for (NPC jug : jugs)
					{
						int distance = jug.getWorldLocation().distanceTo(playerLocation);
						if (distance < minDistance)
						{
							primaryPushJug = jug;
							minDistance = distance;
						}
					}
				}

				for (int i = 0; i < jugs.size(); i++)
				{
					NPC jug = jugs.get(i);
					int score = scores[i];
					NPC targetRock = targetRocks[i];
					
					boolean isRolling = jug.getId() == 11736;
					
					WorldPoint jugLoc = jug.getWorldLocation();
					
					boolean isAttackMode = false;
					for (NPC rock : rocks)
					{
						WorldPoint rockLoc = rock.getWorldLocation();
						if (jugLoc.distanceTo(rockLoc.dx(1)) <= splashRadius ||
							jugLoc.distanceTo(rockLoc.dx(2)) <= splashRadius ||
							jugLoc.distanceTo(rockLoc.dx(3)) <= splashRadius)
						{
							isAttackMode = true;
							break;
						}
					}

					if (isAttackMode)
					{
						// Attack Mode: Highlight the 3D model
						java.awt.Shape hull = jug.getConvexHull();
						if (hull != null)
						{
							OverlayUtil.renderPolygon(graphics, hull, config.attackableJugColor());
						}
					}

					boolean isPrimaryPush = jug == primaryPushJug;
					boolean isPrimaryCyan = jug == primaryCyanJug;

					if (jugMode != ZebakRoarConfig.JugHighlightMode.ALL && !isPrimaryPush && !isPrimaryCyan)
					{
						continue;
					}

					if (isRolling)
					{
						continue;
					}

					// Render Cyan Push-to-Hit
					if ((jugMode == ZebakRoarConfig.JugHighlightMode.ALL || isPrimaryCyan) && cyanDist[i] > 0)
					{
						int cDx = cyanDx[i];
						int cDy = cyanDy[i];
						
						java.awt.Shape hull = jug.getConvexHull();
						if (hull != null)
						{
							OverlayUtil.renderPolygon(graphics, hull, config.attackableJugColor());
						}
						
						renderDirectionLine(graphics, jug, cDx, cDy, config.attackableJugColor());
					}

					// Render Standard Push
					if ((jugMode == ZebakRoarConfig.JugHighlightMode.ALL || isPrimaryPush) && targetRock != null)
					{
						WorldPoint rockLoc = targetRock.getWorldLocation();
						int dx = Integer.compare(rockLoc.getX(), jugLoc.getX());
						int dy = Integer.compare(rockLoc.getY(), jugLoc.getY());
						
						java.awt.Shape hull = jug.getConvexHull();
						if (hull != null)
						{
							OverlayUtil.renderPolygon(graphics, hull, config.pushableJugColor());
						}
						
						renderDirectionLine(graphics, jug, dx, dy, config.pushableJugColor());
					}
					else if ((jugMode == ZebakRoarConfig.JugHighlightMode.ALL || isPrimaryPush) && targetRock == null && primaryCyanJug == null)
					{
						java.awt.Shape hull = jug.getConvexHull();
						if (hull != null)
						{
							OverlayUtil.renderPolygon(graphics, hull, Color.RED);
						}
					}
				}
			}
		}

		return null;
	}

	private void renderDirectionLine(Graphics2D graphics, NPC jug, int dx, int dy, Color color)
	{
		WorldPoint center = jug.getWorldLocation();
		WorldPoint target = center.dx(dx).dy(dy);

		LocalPoint centerLocal = LocalPoint.fromWorld(client, center);
		LocalPoint targetLocal = LocalPoint.fromWorld(client, target);

		if (centerLocal != null && targetLocal != null)
		{
			net.runelite.api.Point p1 = Perspective.localToCanvas(client, centerLocal, client.getPlane(), jug.getLogicalHeight() / 2);
			net.runelite.api.Point p2 = Perspective.localToCanvas(client, targetLocal, client.getPlane(), jug.getLogicalHeight() / 2);

			if (p1 != null && p2 != null)
			{
				graphics.setColor(color);
				graphics.setStroke(new java.awt.BasicStroke(3));
				graphics.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
			}
		}
	}
}

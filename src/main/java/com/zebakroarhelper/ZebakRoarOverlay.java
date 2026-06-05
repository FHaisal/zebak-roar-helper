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

				int splashRadius = config.upsetStomach() ? 1 : 2;

				WorldPoint playerLocation = localPlayer.getWorldLocation();

				for (NPC jug : jugs)
				{
					if (jug.getId() == 11736) // ROLLING_JUG_ID
					{
						continue; // Handled earlier for true tile
					}

					WorldPoint jugLoc = jug.getWorldLocation();
					boolean isHitOnly = false;
					boolean isPush = false;
					boolean isPushToHit = false;

					// Evaluate Hit Only condition
					for (NPC rock : rocks)
					{
						WorldPoint rockLoc = rock.getWorldLocation();
						if (jugLoc.distanceTo(rockLoc.dx(1)) <= splashRadius ||
							jugLoc.distanceTo(rockLoc.dx(2)) <= splashRadius ||
							jugLoc.distanceTo(rockLoc.dx(3)) <= splashRadius)
						{
							isHitOnly = true;
							break;
						}
					}

					// Evaluate Push condition
					for (NPC rock : rocks)
					{
						WorldPoint rockLoc = rock.getWorldLocation();
						int dx = Math.abs(jugLoc.getX() - rockLoc.getX());
						int dy = Math.abs(jugLoc.getY() - rockLoc.getY());
						
						if (dx == 0 || dy == 0 || dx == dy)
						{
							isPush = true;
							break;
						}
					}

					boolean rendered = false;

					if (isHitOnly && isPush)
					{
						int distToPlayer = jugLoc.distanceTo(playerLocation);
						boolean hasAcid = false;
						
						if (distToPlayer <= 2)
						{
							int minX = Math.min(jugLoc.getX(), playerLocation.getX());
							int maxX = Math.max(jugLoc.getX(), playerLocation.getX());
							int minY = Math.min(jugLoc.getY(), playerLocation.getY());
							int maxY = Math.max(jugLoc.getY(), playerLocation.getY());
							
							for (int x = minX; x <= maxX; x++)
							{
								for (int y = minY; y <= maxY; y++)
								{
									if (x == playerLocation.getX() && y == playerLocation.getY()) continue;
									if (x == jugLoc.getX() && y == jugLoc.getY()) continue;
									
									WorldPoint p = new WorldPoint(x, y, jugLoc.getPlane());
									if (plugin.getActiveAcid().contains(p))
									{
										hasAcid = true;
										break;
									}
								}
								if (hasAcid) break;
							}
						}

						if (distToPlayer <= 2 && !hasAcid)
						{
							if (config.showPush())
							{
								java.awt.Shape hull = jug.getConvexHull();
								if (hull != null)
								{
									OverlayUtil.renderPolygon(graphics, hull, config.pushColor());
								}
								rendered = true;
							}
						}
						else
						{
							if (config.showHitOnly())
							{
								java.awt.Shape hull = jug.getConvexHull();
								if (hull != null)
								{
									OverlayUtil.renderPolygon(graphics, hull, config.hitOnlyColor());
								}
								rendered = true;
							}
						}
					}

					if (!rendered && isPush && config.showPush())
					{
						java.awt.Shape hull = jug.getConvexHull();
						if (hull != null)
						{
							OverlayUtil.renderPolygon(graphics, hull, config.pushColor());
						}
						rendered = true;
					}

					if (!rendered && isHitOnly && config.showHitOnly())
					{
						java.awt.Shape hull = jug.getConvexHull();
						if (hull != null)
						{
							OverlayUtil.renderPolygon(graphics, hull, config.hitOnlyColor());
						}
						rendered = true;
					}

					if (!rendered && config.showPushToHit())
					{
						int[] dxs = {0, 0, -1, 1, -1, 1, -1, 1};
						int[] dys = {1, -1, 0, 0, 1, 1, -1, -1};
						
						for (int d = 0; d < 8; d++)
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
									isPushToHit = true;
									break;
								}
							}
							if (isPushToHit) break;
						}

						if (isPushToHit)
						{
							java.awt.Shape hull = jug.getConvexHull();
							if (hull != null)
							{
								OverlayUtil.renderPolygon(graphics, hull, config.pushToHitColor());
							}
							rendered = true;
						}
					}
				}
			}
		}

		return null;
	}
}

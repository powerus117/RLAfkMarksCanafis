package com.marksofgracecooldown;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.time.Instant;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ColorUtil;

class MarksOfGraceCDClickBoxOverlay extends OverlayPanel
{
	private static final int MAX_DISTANCE_FROM_COURSE = 2350; // Max distance from course end to show clickbox highlights

	private final Client client;
	private final MarksOfGraceCDPlugin plugin;
	private final MarksOfGraceCDConfig config;

	@Inject
	public MarksOfGraceCDClickBoxOverlay(Client client, MarksOfGraceCDPlugin plugin, MarksOfGraceCDConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		MarksOfGraceCDConfig.HighlightClickBoxesMode mode = config.highlightClickBoxesMode();
		if (mode == MarksOfGraceCDConfig.HighlightClickBoxesMode.ON_COOLDOWN || mode == MarksOfGraceCDConfig.HighlightClickBoxesMode.XP)
		{
			// Only show clickbox highlights if we're on a course and the course is enabled in the config
			if (plugin.currentCourse == null)
			{
				return null;
			}

			if (!plugin.isCourseEnabled(plugin.currentCourse))
			{
				return null;
			}

			// Get player and mouse location. Get final obstacle.
			Point mousePosition = client.getMouseCanvasPosition();
			LocalPoint playerLocation = client.getLocalPlayer().getLocalLocation();
			int playerPlane = client.getTopLevelWorldView().getPlane();
			TileObject finalObstacle = plugin.getObstacles().get(plugin.currentCourse);

			if (finalObstacle.getPlane() == playerPlane
				&& finalObstacle.getLocalLocation().distanceTo(playerLocation) <= MAX_DISTANCE_FROM_COURSE)
			{

				// Set timer and threshold for clickbox highlight
				long currentMillis = Instant.now().toEpochMilli();
				long cooldownTimestamp = plugin.getCooldownTimestamp(false);
				long secondsLeft = plugin.getSecondsLeft(cooldownTimestamp, currentMillis);
				int thresholdSeconds = plugin.currentCourse != null
					? plugin.getLapThresholdSeconds(plugin.currentCourse) : 0;

				Color color;
				Shape clickbox = finalObstacle.getClickbox();
				boolean darkOnHover = false;
				if (clickbox != null)
				{
					if (secondsLeft >= thresholdSeconds && mode == MarksOfGraceCDConfig.HighlightClickBoxesMode.XP)
					{
						color = config.XpHighlightColor();
						if (clickbox.contains(mousePosition.getX(), mousePosition.getY()))
						{
							darkOnHover = true;
						}
						drawClickbox(graphics, clickbox, color, darkOnHover);
					}
					else if (plugin.isOnCooldown)
					{
						color = config.CooldownHighlightColor();
						drawClickbox(graphics, clickbox, color, darkOnHover);
					}
				}
			}
		}
		return null;
	}

	private void drawClickbox(Graphics2D graphics, Shape clickbox, Color color, boolean darkerOnHover)
	{
		Color borderColor = darkerOnHover ? color.darker() : color;
		graphics.setColor(borderColor);
		graphics.draw(clickbox);
		graphics.setColor(ColorUtil.colorWithAlpha(color, color.getAlpha() / 5));
		graphics.fill(clickbox);
	}
}
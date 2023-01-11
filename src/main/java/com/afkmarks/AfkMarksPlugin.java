package com.afkmarks;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static net.runelite.api.Skill.AGILITY;

@Slf4j
@PluginDescriptor(
	name = "Afk Marks"
)
public class AfkMarksPlugin extends Plugin
{
	private final int canafisLastObstacleXp = 175;
	private final int canafisRegionId = 13878;

	@Inject
	private Client client;

	@Inject
	private AfkMarksConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AfkMarksOverlay agilityOverlay;

	public ZonedDateTime markCooldownCompleteTime;
	public ZonedDateTime lastCompleteTime;
	public boolean shouldRun = true;

	private int lastAgilityXp;

	@Provides
	AfkMarksConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AfkMarksConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(agilityOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(agilityOverlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			markCooldownCompleteTime = null;
			lastCompleteTime = null;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (statChanged.getSkill() != AGILITY)
		{
			return;
		}

		// Determine how much EXP was actually gained
		int agilityXp = client.getSkillExperience(AGILITY);
		int skillGained = agilityXp - lastAgilityXp;
		lastAgilityXp = agilityXp;

		// Get course
		if (client.getLocalPlayer().getWorldLocation().getRegionID() != canafisRegionId ||
				Math.abs(canafisLastObstacleXp - skillGained) > 1)
		{
			return;
		}

		Instant now = Instant.now();
		ZonedDateTime zonedNow = now.atZone(ZoneOffset.UTC);
		lastCompleteTime = zonedNow.truncatedTo(ChronoUnit.MINUTES);
		if (zonedNow.getSecond() >= 58)
		{
			lastCompleteTime = lastCompleteTime.plusMinutes(1);
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (isInCanafisArea() && !shouldRun)
		{
			if (markCooldownCompleteTime == null)
			{
				shouldRun = true;
				return;
			}

			Instant now = Instant.now();
			ZonedDateTime zonedNow = now.atZone(ZoneOffset.UTC);

			if (zonedNow.isAfter(markCooldownCompleteTime))
			{
				shouldRun = true;
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Mark cooldown finished, run", null);
				notifier.notify("Mark of grace cooldown finished");
			}
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		if (!isInCanafisArea())
		{
			return;
		}

		final TileItem item = itemSpawned.getItem();

		if (item.getId() == ItemID.MARK_OF_GRACE)
		{
			markCooldownCompleteTime = lastCompleteTime.plusMinutes(3).plusSeconds(2);
			shouldRun = false;
		}
	}

	private boolean isInCanafisArea()
	{
		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return false;
		}

		WorldPoint location = local.getWorldLocation();
		return location.getRegionID() == canafisRegionId;
	}
}

package com.afkmarkscanafis;

import com.afkmarkscanafis.ntp.NtpClient;
import com.afkmarkscanafis.ntp.NtpSyncState;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.time.Instant;

import static net.runelite.api.Skill.AGILITY;

@Slf4j
@PluginDescriptor(
	name = "AFK Marks Canifis",
	description = "Allows you to AFK on the last roof of the Canifis Rooftop Agility Course for marks of grace.",
	tags = {"afk", "mark", "grace", "canifis"}
)
public class AfkMarksCanafisPlugin extends Plugin
{
	public static final long MILLIS_PER_MINUTE = 60_000;

	private static final int CANAFIS_LAST_OBSTACLE_XP = 175;
	private static final int CANAFIS_REGION_ID = 13878;
	private static final int MARK_COOLDOWN_MINUTES = 3;
	private static final int LAST_OBSTACLE_ID = 14897;

	@Inject
	private Client client;

	@Inject
	private AfkMarksCanafisConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AfkMarksCanafisOverlay agilityOverlay;

	public long lastCompleteMarkTimeMillis;
	public long lastCompleteTimeMillis;

	public boolean isOnCooldown = false;
	public boolean isInCanafisArea = false;

	private int lastAgilityXp;

	@Provides
	AfkMarksCanafisConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AfkMarksCanafisConfig.class);
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
		if (client.getLocalPlayer().getWorldLocation().getRegionID() != CANAFIS_REGION_ID ||
			skillGained != CANAFIS_LAST_OBSTACLE_XP)
		{
			return;
		}

		lastCompleteTimeMillis = Instant.now().toEpochMilli();
		CheckNtpSync();
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		boolean isCurrentlyInCanafis = isInCanafisArea();

		if (isInCanafisArea != isCurrentlyInCanafis)
		{
			isInCanafisArea = isCurrentlyInCanafis;
		}

		if (isCurrentlyInCanafis && isOnCooldown)
		{
			if (lastCompleteMarkTimeMillis == 0)
			{
				isOnCooldown = false;
				return;
			}

			if (Instant.now().toEpochMilli() >= getCooldownTimestamp())
			{
				isOnCooldown = false;
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Marks of grace cooldown has finished, run until you find your next mark.", null);
				notifier.notify("Marks of grace cooldown has finished.");
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
			lastCompleteMarkTimeMillis = lastCompleteTimeMillis;
			isOnCooldown = true;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (isInCanafisArea && isOnCooldown && config.swapLeftClickOnWait() && e.getIdentifier() == LAST_OBSTACLE_ID)
		{
			e.getMenuEntry().setDeprioritized(true);
		}
	}

	public long getCooldownTimestamp()
	{
		if (lastCompleteMarkTimeMillis == 0)
			return lastCompleteMarkTimeMillis;

		// First convert to server timestamp to get the correct minute
		long offsetMillis = lastCompleteMarkTimeMillis + NtpClient.SyncedOffsetMillis;
		long minuteTruncatedMillis = offsetMillis - (offsetMillis % MILLIS_PER_MINUTE);
		long localCooldownMillis = minuteTruncatedMillis + (MARK_COOLDOWN_MINUTES * MILLIS_PER_MINUTE);
		long leewayAdjusted = localCooldownMillis + ((long)config.leewaySeconds() * 1000);
		// We revert the ntp offset to get back to a local time that we locally wait for
		return leewayAdjusted - NtpClient.SyncedOffsetMillis;
	}

	private boolean isInCanafisArea()
	{
		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return false;
		}

		WorldPoint location = local.getWorldLocation();
		return location.getRegionID() == CANAFIS_REGION_ID;
	}

	private void CheckNtpSync()
	{
		if (NtpClient.SyncState == NtpSyncState.NOT_SYNCED)
			NtpClient.startSync();
	}
}

package com.marksofgracecooldown;

import com.marksofgracecooldown.ntp.NtpClient;
import com.marksofgracecooldown.ntp.NtpSyncState;
import com.google.inject.Provides;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
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
	name = "Marks of Grace Cooldown",
	description = "Tracks the cooldown for Marks of Grace on Agility Courses, allowing you to time your laps, AFK or do other activities",
	tags = {"mark", "of", "grace", "afk", "cooldown", "tracker", "canifis"}
)
public class MarksOfGraceCDPlugin extends Plugin
{
	public static final long MILLIS_PER_MINUTE = 60_000;
	private static final int MARK_COOLDOWN_MINUTES = 3;

	@Inject
	private Client client;

	@Inject
	private MarksOfGraceCDConfig config;

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MarksOfGraceCDOverlay agilityOverlay;

	public long lastCompleteMarkTimeMillis;
	public long lastCompleteTimeMillis;

	public boolean isOnCooldown = false;
	public Courses currentCourse;
	public boolean hasReducedCooldown = false;

	@Provides
	MarksOfGraceCDConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MarksOfGraceCDConfig.class);
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
			return;

		Courses course = Courses.getCourse(this.client.getLocalPlayer().getWorldLocation().getRegionID());

		if (course != null && Arrays.stream(course.getCourseEndWorldPoints()).anyMatch((wp) ->
			wp.equals(this.client.getLocalPlayer().getWorldLocation())))
		{
			currentCourse = course;
			lastCompleteTimeMillis = Instant.now().toEpochMilli();
			CheckNtpSync();

			hasReducedCooldown = currentCourse == Courses.ARDOUGNE &&
				client.getVarbitValue(net.runelite.api.Varbits.DIARY_ARDOUGNE_ELITE) == 1;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (isOnCooldown)
		{
			if (lastCompleteMarkTimeMillis == 0)
			{
				isOnCooldown = false;
				return;
			}

			long cooldownTimestamp = getCooldownTimestamp(true);

			if (Instant.now().toEpochMilli() >= cooldownTimestamp)
			{
				isOnCooldown = false;
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Marks of grace cooldown has finished, run until you find your next mark.", null);

				if (config.sendNotification())
				{
					notifier.notify("Marks of grace cooldown has finished.");
				}
			}
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		if (currentCourse == null)
			return;

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
		if (!isOnCooldown || currentCourse == null)
			return;

		if (config.swapLeftClickOnWait() && e.getIdentifier() == currentCourse.getLastObstacleId())
		{
			long millisLeft = getCooldownTimestamp(true) - Instant.now().toEpochMilli();
			if (millisLeft > 0 && millisLeft / 1000 < config.swapLeftClickTimeLeft())
			{
				e.getMenuEntry().setDeprioritized(true);
			}
		}
	}

	public long getCooldownTimestamp(boolean checkForReduced)
	{
		if (lastCompleteMarkTimeMillis == 0)
			return lastCompleteMarkTimeMillis;

		// First convert to server timestamp to get the correct minute
		long offsetMillis = lastCompleteMarkTimeMillis + NtpClient.SyncedOffsetMillis;
		long minuteTruncatedMillis = offsetMillis - (offsetMillis % MILLIS_PER_MINUTE);
		long localCooldownMillis = minuteTruncatedMillis + (MARK_COOLDOWN_MINUTES * MILLIS_PER_MINUTE);
		long leewayAdjusted = localCooldownMillis + ((long)config.leewaySeconds() * 1000);
		// We revert the ntp offset to get back to a local time that we locally wait for
		long ntpAdjusted = leewayAdjusted - NtpClient.SyncedOffsetMillis;

		if (checkForReduced && hasReducedCooldown && config.useShortArdougneTimer())
			ntpAdjusted -= MILLIS_PER_MINUTE;

		return ntpAdjusted;
	}

	private void CheckNtpSync()
	{
		if (NtpClient.SyncState == NtpSyncState.NOT_SYNCED)
			NtpClient.startSync();
	}
}

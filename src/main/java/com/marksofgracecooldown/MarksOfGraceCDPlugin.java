package com.marksofgracecooldown;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.TileItem;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.worldhopper.ping.Ping;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static net.runelite.api.Skill.AGILITY;

@Slf4j
@PluginDescriptor(
        name = "Marks of Grace Cooldown",
        description = "Tracks the cooldown for Marks of Grace on Agility Courses, allowing you to time your laps, AFK or do other activities",
        tags = {"mark", "of", "grace", "afk", "cooldown", "tracker", "canifis", "agility"},
        configName = "AfkMarksCanafisPlugin" // Old name from when it was canifis only
)
public class MarksOfGraceCDPlugin extends Plugin {
    public static final long MILLIS_PER_MINUTE = 60_000;
    private static final int MARK_COOLDOWN_MINUTES = 3;
    public long lastCompleteMarkTimeMillis;
    public long lastCompleteTimeMillis;
    public boolean isOnCooldown = false;
    public boolean hasReducedCooldown = false;
    Courses currentCourse;
    @Inject
    private Client client;
    @Inject
    private MarksOfGraceCDConfig config;
    @Inject
    private Notifier notifier;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private MarksOfGraceCDOverlay marksCooldownOverlay;
    @Inject
    private WorldService worldService;
    private ScheduledExecutorService pingExecutor;
    // last measured ping to the current RS world in ms; -1 == unknown
    @Setter
    @Getter
    private volatile int lastWorldPing = -1;

    @Provides
    MarksOfGraceCDConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MarksOfGraceCDConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(marksCooldownOverlay);

        // Start background executor to periodically refresh the ping to the current world if enabled
        if (config.enableWorldPing()) {
            pingExecutor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "mogcd-world-ping"));
            int interval = Math.max(1, config.pingRefreshInterval());
            pingExecutor.scheduleWithFixedDelay(this::refreshWorldPing, 5, interval, TimeUnit.SECONDS);
        } else {
            pingExecutor = null;
            lastWorldPing = -1;
        }
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(marksCooldownOverlay);
        if (pingExecutor != null) {
            pingExecutor.shutdownNow();
            pingExecutor = null;
        }
        lastWorldPing = -1;
    }

    private void refreshWorldPing() {
        WorldResult worldResult = worldService.getWorlds();
        // There is no reason to ping the current world if not logged in, as the overlay doesn't draw
        if (worldResult == null || client.getGameState() != GameState.LOGGED_IN) return;
        final World currentWorld = worldResult.findWorld(client.getWorld());
        if (currentWorld == null) return;

        try {
            // Ping.ping is a blocking call; run in this background thread
            int ping = Ping.ping(currentWorld);
            if (ping >= 0) {
                lastWorldPing = ping;
            } else {
                lastWorldPing = -1;
            }
        } catch (Throwable t) {
            log.debug("Failed to refresh world ping: {}", t.toString());
            lastWorldPing = -1;
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        if (statChanged.getSkill() != AGILITY)
            return;

        Courses course = Courses.getCourse(this.client.getLocalPlayer().getWorldLocation().getRegionID());

        if (course != null && Arrays.stream(course.getCourseEndWorldPoints()).anyMatch((wp) ->
                wp.equals(this.client.getLocalPlayer().getWorldLocation()))) {
            currentCourse = course;
            lastCompleteTimeMillis = Instant.now().toEpochMilli();

            hasReducedCooldown = currentCourse == Courses.ARDOUGNE &&
                    client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE) == 1;
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        if (isOnCooldown) {
            if (lastCompleteMarkTimeMillis == 0) {
                isOnCooldown = false;
                return;
            }

            long cooldownTimestamp = getCooldownTimestamp(true);

            if (Instant.now().toEpochMilli() >= cooldownTimestamp) {
                isOnCooldown = false;
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Marks of grace cooldown has finished, run until you find your next mark.", null);

                notifier.notify(config.notifyMarksOfGraceCD(), "Marks of grace cooldown has finished.");
            }
        }
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned itemSpawned) {
        if (currentCourse == null)
            return;

        final TileItem item = itemSpawned.getItem();

        if (item.getId() == ItemID.GRACE) {
            lastCompleteMarkTimeMillis = lastCompleteTimeMillis;
            isOnCooldown = true;
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded e) {
        if (!isOnCooldown || currentCourse == null)
            return;

        if (config.swapLeftClickOnWait() && e.getIdentifier() == currentCourse.getLastObstacleId()) {
            long millisLeft = getCooldownTimestamp(true) - Instant.now().toEpochMilli();
            if (millisLeft > 0 && millisLeft / 1000 < config.swapLeftClickTimeLeft()) {
                e.getMenuEntry().setDeprioritized(true);
            }
        }
    }

    public long getCooldownTimestamp(boolean checkForReduced) {
        if (lastCompleteMarkTimeMillis == 0)
            return lastCompleteMarkTimeMillis;

        // Use local clock minute-truncation and the user-configured leewaySeconds instead of NTP
        long minuteTruncatedMillis = lastCompleteMarkTimeMillis - (lastCompleteMarkTimeMillis % MILLIS_PER_MINUTE);
        long localCooldownMillis = minuteTruncatedMillis + (MARK_COOLDOWN_MINUTES * MILLIS_PER_MINUTE);
        long leewayAdjusted = localCooldownMillis + ((long) config.timerBufferSeconds() * 1000);

        if (checkForReduced && hasReducedCooldown && config.useShortArdougneTimer())
            leewayAdjusted -= MILLIS_PER_MINUTE;

        // If we have a recent ping to the world, approximate one-way delay and subtract it
        try {
            int ping = lastWorldPing;
            if (ping > 0) {
                long oneWay = ping / 2L;
                leewayAdjusted -= oneWay;
            }
        } catch (Throwable t) {
            // ignore and fall back to local-only timing
        }

        // Safety: don't return a timestamp earlier than the recorded completion time
        if (leewayAdjusted < lastCompleteMarkTimeMillis) {
            leewayAdjusted = lastCompleteMarkTimeMillis;
        }

        return leewayAdjusted;
    }
}

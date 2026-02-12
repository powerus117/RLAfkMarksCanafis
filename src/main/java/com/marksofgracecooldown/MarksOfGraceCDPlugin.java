package com.marksofgracecooldown;

import com.google.inject.Provides;
import com.marksofgracecooldown.ntp.NtpClient;
import com.marksofgracecooldown.ntp.NtpSyncState;
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
import net.runelite.api.events.WorldChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
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
    @Inject
    private ConfigManager configManager;
    @Inject
    private ClientThread clientThread;
    private ScheduledExecutorService pingExecutor;
    // last measured ping to the current RS world in ms; -1 == unknown
    @Setter
    @Getter
    private volatile int lastWorldPing = -1;

    // track whether we've updated the kandarin detection config for this login session
    private volatile boolean kandarinDetectionConfigUpdated = false;

    @Provides
    MarksOfGraceCDConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(MarksOfGraceCDConfig.class);
    }

    // Package-private setter used by tests to inject a fake config without reflection
    void setConfig(MarksOfGraceCDConfig config) {
        this.config = config;
    }

    @Override
    protected void startUp() {
        overlayManager.add(marksCooldownOverlay);
        kandarinDetectionConfigUpdated = false;
        // Attempt to update kandarin detection status (will only take effect if logged in)
        clientThread.invoke(() -> updateKandarinDetectedConfigIfNeeded());

        // Start NTP sync if enabled to correct for system clock drift
        if (config.enableNtpSync()) {
            NtpClient.startSync();
        }

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
    protected void shutDown() {
        overlayManager.remove(marksCooldownOverlay);
        if (pingExecutor != null) {
            pingExecutor.shutdownNow();
            pingExecutor = null;
        }
        lastWorldPing = -1;
        // reset the kandarin detection flag so we update next login
        kandarinDetectionConfigUpdated = false;
        // clear detection flag in config UI
        configManager.setConfiguration("AfkMarksCanafis", "kandarinDiaryDetected", false);
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

            // Ensure NTP is synced when user starts agility training
            checkNtpSync();

            hasReducedCooldown = currentCourse == Courses.ARDOUGNE &&
                    client.getVarbitValue(VarbitID.ARDOUGNE_DIARY_ELITE_COMPLETE) == 1;
        }
    }

    @Subscribe
    public void onWorldChanged(WorldChanged event) {
        // Reset ping when changing worlds to avoid using stale latency data
        lastWorldPing = -1;
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        if (!kandarinDetectionConfigUpdated) {
            updateKandarinDetectedConfigIfNeeded();
        }

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

        // Respect per-course toggle
        if (!isCourseEnabled(currentCourse)) return;

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

        // Respect per-course toggle
        if (!isCourseEnabled(currentCourse)) return;

        // Determine configured behaviour
        MarksOfGraceCDConfig.SwapLeftClickMode mode = config.swapLeftClickMode();

        if (mode == MarksOfGraceCDConfig.SwapLeftClickMode.OFF) return;

        if (!currentCourse.containsLastObstacle(e.getIdentifier())) return;

        long millisLeft = getCooldownTimestamp(true) - Instant.now().toEpochMilli();
        if (millisLeft <= 0) {
            return;
        }

        if (mode == MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_NOT_EXPIRED) {
            e.getMenuEntry().setDeprioritized(true);
            return;
        }

        // WHEN_CANNOT_COMPLETE_LAP (previous behaviour)
        int thresholdSeconds = getLapThresholdSeconds(currentCourse);
        if (millisLeft / 1000 < thresholdSeconds) {
            e.getMenuEntry().setDeprioritized(true);
        }
    }

    // Package-private helper to determine whether a menu entry for a given obstacle id should be deprioritized.
    // This mirrors the logic in onMenuEntryAdded, but accepts an obstacle id and is easier to unit-test.
    boolean shouldDeprioritizeMenuEntry(int obstacleId) {
        if (!isOnCooldown || currentCourse == null) return false;

        // Respect per-course toggle
        if (!isCourseEnabled(currentCourse)) return false;

        MarksOfGraceCDConfig.SwapLeftClickMode mode = config.swapLeftClickMode();
        if (mode == MarksOfGraceCDConfig.SwapLeftClickMode.OFF) return false;

        if (!currentCourse.containsLastObstacle(obstacleId)) return false;

        long millisLeft = getCooldownTimestamp(true) - Instant.now().toEpochMilli();
        if (millisLeft <= 0) return false;

        if (mode == MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_NOT_EXPIRED) return true;

        int thresholdSeconds = getLapThresholdSeconds(currentCourse);
        return (millisLeft / 1000) < thresholdSeconds;
    }

    /**
     * Helper to check whether the plugin is enabled for a given course based on config toggles.
     */
    boolean isCourseEnabled(Courses course) {
        return course == null || course.isEnabled(config);
    }

    /**
     * Compute the lap-time threshold in seconds used for swapping/deprioritizing the last obstacle.
     * If the user enabled the custom-lap-time override, return the configured custom seconds.
     * Otherwise, compute the course optimal time (considering course-specific toggles) and add the
     * user's optimalTimeBufferSeconds to allow forgiving timing.
     */
    int getLapThresholdSeconds(Courses course) {
        if (config.useCustomLapTime()) {
            return Math.max(0, config.customLapTimeSeconds());
        }

        int baseOptimal = course.getOptimalTime(key ->
                ("useSeersTeleport".equals(key) && config.useSeersTeleport() &&
                        (config.assumeHardKandarinDiary() || hasHardKandarinDiary()))
        );

        int combined = baseOptimal + config.lapTimeBuffer();
        return Math.max(0, combined);
    }

    public long getCooldownTimestamp(boolean checkForReduced) {
        if (lastCompleteMarkTimeMillis == 0)
            return lastCompleteMarkTimeMillis;

        // Apply NTP offset to get the correct server time for minute-truncation
        // This corrects for users whose system clocks are out of sync
        long ntpOffset = config.enableNtpSync() ? NtpClient.getSyncedOffsetMillis() : 0;
        long serverTimeMillis = lastCompleteMarkTimeMillis + ntpOffset;

        // Minute-truncate using server time to match OSRS game tick timing
        long minuteTruncatedMillis = serverTimeMillis - (serverTimeMillis % MILLIS_PER_MINUTE);
        long serverCooldownMillis = minuteTruncatedMillis + (MARK_COOLDOWN_MINUTES * MILLIS_PER_MINUTE);

        // Add user-configured buffer for safety margin
        long leewayAdjusted = serverCooldownMillis + ((long) config.timerBufferSeconds() * 1000);

        // Apply Ardougne elite diary reduced cooldown only when user opted into using the short Ardougne timer
        if (checkForReduced && hasReducedCooldown && config.useShortArdougneTimer())
            leewayAdjusted -= MILLIS_PER_MINUTE;

        // Convert back to local time by removing the NTP offset
        long localCooldownMillis = leewayAdjusted - ntpOffset;

        // If we have a recent ping to the world, approximate one-way delay and subtract it
        int ping = lastWorldPing;
        if (ping > 0) {
            long oneWay = ping / 2L;
            localCooldownMillis -= oneWay;
        }

        // Safety: don't return a timestamp earlier than the recorded completion time
        if (localCooldownMillis < lastCompleteMarkTimeMillis) {
            localCooldownMillis = lastCompleteMarkTimeMillis;
        }

        return localCooldownMillis;
    }

    /**
     * Detect whether the player has completed the Hard Kandarin diary using a direct VarbitID lookup.
     */
    public boolean hasHardKandarinDiary() {
        if (client == null) {
            return false;
        }
        return client.getVarbitValue(VarbitID.KANDARIN_DIARY_HARD_COMPLETE) == 1;
    }

    private void updateKandarinDetectedConfigIfNeeded() {
        if (kandarinDetectionConfigUpdated) return;
        if (client == null || client.getGameState() != GameState.LOGGED_IN) return;

        boolean detected = hasHardKandarinDiary();
        // Respect the override - don't change the UI value if user manually set override true
        // We still want the UI to show true when detected or when user has assume override
        if (config.assumeHardKandarinDiary()) {
            // if user has set the assume override, show detected as true in UI as well
            configManager.setConfiguration("AfkMarksCanafis", "kandarinDiaryDetected", true);
        } else {
            configManager.setConfiguration("AfkMarksCanafis", "kandarinDiaryDetected", detected);
        }

        kandarinDetectionConfigUpdated = true;
    }

    /**
     * Starts NTP sync if enabled and not already synced.
     */
    private void checkNtpSync() {
        if (config.enableNtpSync() && NtpClient.getSyncState() == NtpSyncState.NOT_SYNCED) {
            NtpClient.startSync();
        }
    }
}

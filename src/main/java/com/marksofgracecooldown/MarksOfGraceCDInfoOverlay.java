package com.marksofgracecooldown;

import com.marksofgracecooldown.ntp.NtpClient;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;

class MarksOfGraceCDInfoOverlay extends OverlayPanel {

    private final MarksOfGraceCDPlugin plugin;
    private final MarksOfGraceCDConfig config;

    @Inject
    public MarksOfGraceCDInfoOverlay(MarksOfGraceCDPlugin plugin, MarksOfGraceCDConfig config) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.currentCourse == null) {
            return null;
        }

        if (!plugin.isCourseEnabled(plugin.currentCourse)) {
            return null;
        }

        long currentMillis = Instant.now().toEpochMilli();

        // Only time out if there's been no lap activity recently AND no active cooldown.
        long lastActivity = Math.max(plugin.courseStartTimeMillis, plugin.lastCompleteTimeMillis);
        long timeoutMillis = Math.max(1, config.inactivityTimeoutMinutes()) * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE;
        if (!plugin.isOnCooldown && currentMillis - lastActivity > timeoutMillis) {
            resetPluginState();
            return null;
        }

        long cooldownTimestamp = plugin.getCooldownTimestamp(false);
        long secondsLeft = plugin.getSecondsLeft(cooldownTimestamp, currentMillis);

        renderStatusTitle(secondsLeft);
        addLine("Time until run:", formatTime(secondsLeft));

        if (plugin.hasReducedCooldown) {
            addLine("Reduced time:", formatTime(Math.max(secondsLeft - 60, 0)));
        }

        if (config.showDebugValues() && plugin.currentCourse != null) {
            renderDebugInfo();
        }

        return super.render(graphics);
    }

    private void resetPluginState() {
        plugin.lastCompleteMarkTimeMillis = 0;
        plugin.lastCompleteTimeMillis = 0;
        plugin.courseStartTimeMillis = 0;
        plugin.currentCourse = null;
    }

    private void renderStatusTitle(long secondsLeft) {
        if (!plugin.isOnCooldown) {
            addTitle("Run", Color.GREEN);
            return;
        }

        int thresholdSeconds = plugin.currentCourse != null ? plugin.getLapThresholdSeconds(plugin.currentCourse) : 0;

        if (secondsLeft >= thresholdSeconds) {
            addTitle("XP", config.XpHighlightColor());
        } else {
            addTitle("Wait", config.CooldownHighlightColor());
        }
    }

    private void renderDebugInfo() {
        int baseOptimal = plugin.currentCourse.getOptimalTime(key -> "useSeersTeleport".equals(key) && config.useSeersTeleport() && (config.assumeHardKandarinDiary() || plugin.hasHardKandarinDiary()));
        int combined = Math.max(0, baseOptimal + config.lapTimeBuffer());

        addLine("Base lap time:", formatTime(baseOptimal));
        addLine("Combined lap time:", formatTime(combined));

        int ping = plugin.getLastWorldPing();
        addLine("World ping:", ping >= 0 ? ping + "ms" : "N/A");

        if (config.enableNtpSync()) {
            addLine("NTP status:", NtpClient.getSyncState().toString());
            long offset = NtpClient.getSyncedOffsetMillis();
            addLine("Clock offset:", (offset >= 0 ? "+" : "") + offset + "ms");
        }
    }

    private void addTitle(String text, Color color) {
        panelComponent.getChildren().add(TitleComponent.builder().text(text).color(color).build());
    }

    private void addLine(String left, String right) {
        panelComponent.getChildren().add(LineComponent.builder().left(left).right(right).build());
    }

    private String formatTime(long totalSeconds) {
        return String.format("%d:%02d", (totalSeconds % 3600) / 60, totalSeconds % 60);
    }
}

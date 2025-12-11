package com.marksofgracecooldown;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Instant;

class MarksOfGraceCDOverlay extends OverlayPanel {
    private static final int TIMEOUT_MINUTES = 5;
    private static final long TIMEOUT_MILLIS = TIMEOUT_MINUTES * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE;

    private final MarksOfGraceCDPlugin plugin;

    @Inject
    private MarksOfGraceCDConfig config;

    @Inject
    public MarksOfGraceCDOverlay(MarksOfGraceCDPlugin plugin) {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);
        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.lastCompleteMarkTimeMillis == 0) {
            return null;
        }

        // Don't show overlay if the current course is disabled in settings
        if (plugin.currentCourse != null && !plugin.isCourseEnabled(plugin.currentCourse)) {
            return null;
        }

        long currentMillis = Instant.now().toEpochMilli();
        long millisSinceLastComplete = currentMillis - plugin.lastCompleteTimeMillis;

        if (millisSinceLastComplete > TIMEOUT_MILLIS) {
            plugin.lastCompleteMarkTimeMillis = 0;
            plugin.lastCompleteTimeMillis = 0;
            plugin.currentCourse = null;
            return null;
        }

        // Determine cooldown state: Run (not on cooldown), XP (on cooldown but safe to complete laps), Wait (on cooldown and near end)
        if (!plugin.isOnCooldown) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Run")
                    .color(Color.GREEN)
                    .build());
        } else {
            long millisLeft = Math.max(plugin.getCooldownTimestamp(false) - currentMillis, 0);
            int secondsLeft = (int) Math.ceil((double) millisLeft / 1000);
            int thresholdSeconds = plugin.currentCourse != null ? plugin.getLapThresholdSeconds(plugin.currentCourse) : 0;

            if (secondsLeft >= thresholdSeconds) {
                // You can safely complete laps for XP
                panelComponent.getChildren().add(TitleComponent.builder()
                        .text("XP")
                        .color(Color.ORANGE)
                        .build());
            } else {
                // Too close to cooldown end - wait
                panelComponent.getChildren().add(TitleComponent.builder()
                        .text("Wait")
                        .color(Color.RED)
                        .build());
            }
        }

        long millisLeft = Math.max(plugin.getCooldownTimestamp(false) - currentMillis, 0);
        long secondsLeft = (long) Math.ceil((double) millisLeft / 1000);
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Time until run:")
                .right(String.format("%d:%02d", (secondsLeft % 3600) / 60, (secondsLeft % 60)))
                .build());

        // Show reduced Ardougne timer right after the main timer (user-facing info)
        if (plugin.hasReducedCooldown) {
            long shortTimeSecondsLeft = Math.max(secondsLeft - 60, 0);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Reduced time:")
                    .right(String.format("%d:%02d", (shortTimeSecondsLeft % 3600) / 60, (shortTimeSecondsLeft % 60)))
                    .build());
        }

        // Debug-only: show optimal lap times, buffer effects, and ping
        if (config.showDebugValues() && plugin.currentCourse != null) {
            int baseOptimal = plugin.currentCourse.getOptimalTime(key ->
                    ("useSeersTeleport".equals(key) && config.useSeersTeleport() &&
                            (config.assumeHardKandarinDiary() || plugin.hasHardKandarinDiary()))
            );
            int combined = Math.max(0, baseOptimal + config.lapTimeBuffer());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Base lap time:")
                    .right(String.format("%d:%02d", (baseOptimal % 3600) / 60, (baseOptimal % 60)))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Combined lap time:")
                    .right(String.format("%d:%02d", (combined % 3600) / 60, (combined % 60)))
                    .build());

            int ping = plugin.getLastWorldPing();
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("World ping:")
                    .right(ping >= 0 ? ping + "ms" : "N/A")
                    .build());
        }

        return super.render(graphics);
    }
}

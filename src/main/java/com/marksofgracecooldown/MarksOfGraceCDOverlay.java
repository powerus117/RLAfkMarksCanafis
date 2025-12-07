package com.marksofgracecooldown;

import com.marksofgracecooldown.ntp.NtpClient;
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

        long currentMillis = Instant.now().toEpochMilli();
        long millisSinceLastComplete = currentMillis - plugin.lastCompleteTimeMillis;

        if (millisSinceLastComplete > TIMEOUT_MILLIS) {
            plugin.lastCompleteMarkTimeMillis = 0;
            plugin.lastCompleteTimeMillis = 0;
            plugin.currentCourse = null;
            return null;
        }

        if (plugin.isOnCooldown) {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Wait")
                    .color(Color.RED)
                    .build());
        } else {
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Run")
                    .color(Color.GREEN)
                    .build());
        }

        long millisLeft = Math.max(plugin.getCooldownTimestamp(false) - currentMillis, 0);
        long secondsLeft = (long) Math.ceil((double) millisLeft / 1000);
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Time until run:")
                .right(String.format("%d:%02d", (secondsLeft % 3600) / 60, (secondsLeft % 60)))
                .build());

        if (plugin.hasReducedCooldown) {
            long shortTimeSecondsLeft = Math.max(secondsLeft - 60, 0);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Reduced time:")
                    .right(String.format("%d:%02d", (shortTimeSecondsLeft % 3600) / 60, (shortTimeSecondsLeft % 60)))
                    .build());
        }

        if (config.showDebugValues()) {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("NTP State:")
                    .right(String.valueOf(NtpClient.SyncState))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time offset:")
                    .right(getReadableOffset(NtpClient.SyncedOffsetMillis))
                    .build());
        }

        return super.render(graphics);
    }

    private String getReadableOffset(long offset) {
        if (Math.abs(offset) < 1000)
            return offset + "ms";

        offset /= 1000; // Seconds

        if (Math.abs(offset) < 1000)
            return offset + "s";

        offset /= 60; // Minutes

        if (Math.abs(offset) < 1000)
            return offset + "m";

        offset /= 60; // Hours

        if (Math.abs(offset) < 1000)
            return offset + "h";

        return "LOTS";
    }
}

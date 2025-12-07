package com.marksofgracecooldown;

import net.runelite.client.config.Notification;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.*;

public class MarksOfGraceCDPluginTest {
    private MarksOfGraceCDPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new MarksOfGraceCDPlugin();

        // Provide a minimal config implementation (use defaults for unused methods)
        MarksOfGraceCDConfig cfg = new MarksOfGraceCDConfig() {
            @Override
            public Notification notifyMarksOfGraceCD() {
                return Notification.OFF;
            }

            @Override
            public boolean swapLeftClickOnWait() {
                return false;
            }

            @Override
            public int customLapTimeSeconds() {
                return 180;
            }

            @Override
            public int timerBufferSeconds() {
                return 0;
            }

            @Override
            public boolean useShortArdougneTimer() {
                return true;
            }

            @Override
            public boolean useSeersTeleport() {
                return false;
            }

            @Override
            public boolean enableWorldPing() {
                return false;
            }

            @Override
            public int pingRefreshInterval() {
                return 15;
            }

            @Override
            public boolean showDebugValues() {
                return false;
            }
        };

        // Inject the test config without reflection
        plugin.setConfig(cfg);

        // default values
        plugin.hasReducedCooldown = false;
        plugin.lastCompleteMarkTimeMillis = 0;
        plugin.lastCompleteTimeMillis = 0;
        plugin.isOnCooldown = false;
    }

    @Test
    public void testCooldownTimestamp_noPing_noBuffer() {
        long base = Instant.parse("2020-09-13T12:34:56Z").toEpochMilli();
        plugin.lastCompleteMarkTimeMillis = base;

        // No ping, buffer 0
        plugin.setLastWorldPing(-1);

        long minuteTruncated = base - (base % MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);
        long expected = minuteTruncated + (3 * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);

        long ts = plugin.getCooldownTimestamp(false);
        assertEquals(expected, ts);
    }

    @Test
    public void testCooldownTimestamp_withPing_andClamp() {
        long base = Instant.parse("2020-09-13T12:59:10Z").toEpochMilli();
        plugin.lastCompleteMarkTimeMillis = base;

        // Simulate large RTT (120s) -> one-way 60s
        plugin.setLastWorldPing(120_000);

        long minuteTruncated = base - (base % MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);
        long expected = minuteTruncated + (3 * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE) - (plugin.getLastWorldPing() / 2L);
        // clamp: cannot be earlier than lastCompleteMarkTimeMillis
        if (expected < plugin.lastCompleteMarkTimeMillis) expected = plugin.lastCompleteMarkTimeMillis;

        long ts = plugin.getCooldownTimestamp(false);
        assertEquals(expected, ts);
    }

    @Test
    public void testCooldownTimestamp_reducedArdougne() {
        long base = Instant.parse("2020-09-13T12:00:30Z").toEpochMilli();
        plugin.lastCompleteMarkTimeMillis = base;
        plugin.hasReducedCooldown = true;

        // No ping
        plugin.setLastWorldPing(-1);

        long minuteTruncated = base - (base % MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);
        long expected = minuteTruncated + (3 * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);
        // reduced cooldown subtract one minute
        expected -= MarksOfGraceCDPlugin.MILLIS_PER_MINUTE;

        long ts = plugin.getCooldownTimestamp(true);
        assertEquals(expected, ts);
    }
}

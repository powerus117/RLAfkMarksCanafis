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
            public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() {
                return MarksOfGraceCDConfig.SwapLeftClickMode.OFF;
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

            // Per-course toggles default to true in the real config; tests can override as needed
            @Override
            public boolean enableDraynor() {
                return true;
            }

            @Override
            public boolean enableAlKharid() {
                return true;
            }

            @Override
            public boolean enableVarrock() {
                return true;
            }

            @Override
            public boolean enableCanifis() {
                return true;
            }

            @Override
            public boolean enableFalador() {
                return true;
            }

            @Override
            public boolean enableSeers() {
                return true;
            }

            @Override
            public boolean enablePollnivneach() {
                return true;
            }

            @Override
            public boolean enableRelleka() {
                return true;
            }

            @Override
            public boolean enableArdougne() {
                return true;
            }

            @Override
            public boolean enableGnome() {
                return true;
            }

            @Override
            public boolean enableShayzienBasic() {
                return true;
            }

            @Override
            public boolean enableBarbarian() {
                return true;
            }

            @Override
            public boolean enableShayzienAdvanced() {
                return true;
            }

            @Override
            public boolean enableApeAtoll() {
                return true;
            }

            @Override
            public boolean enableWerewolf() {
                return true;
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

    @Test
    public void testDeprioritizeAllFinalObstacles_gnome() {
        // Create plugin that simulates a short time-left by overriding getCooldownTimestamp
        MarksOfGraceCDPlugin p = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 1000; // 1s left
            }
        };

        p.setConfig(new MarksOfGraceCDConfig() {
            @Override public net.runelite.client.config.Notification notifyMarksOfGraceCD() { return net.runelite.client.config.Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_CANNOT_COMPLETE_LAP; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return true; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            // explicitly disable GNOME for this test
            @Override public boolean enableGnome() { return false; }
        });

        p.currentCourse = Courses.GNOME;
        p.isOnCooldown = true;

        // Because we disabled GNOME in config, deprioritization should be suppressed
        assertFalse(p.shouldDeprioritizeMenuEntry(23138));
        assertFalse(p.shouldDeprioritizeMenuEntry(23139));
    }

    @Test
    public void testDeprioritizeAllFinalObstacles_werewolf() {
        MarksOfGraceCDPlugin p = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 1000; // 1s left
            }
        };

        p.setConfig(new MarksOfGraceCDConfig() {
            @Override public net.runelite.client.config.Notification notifyMarksOfGraceCD() { return net.runelite.client.config.Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_CANNOT_COMPLETE_LAP; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return true; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            // explicitly disable WEREWOLF for this test
            @Override public boolean enableWerewolf() { return false; }
        });

        p.currentCourse = Courses.WEREWOLF;
        p.isOnCooldown = true;

        // Because we disabled Werewolf in config, deprioritization should be suppressed
        assertFalse(p.shouldDeprioritizeMenuEntry(11644));
        assertFalse(p.shouldDeprioritizeMenuEntry(11645));
        assertFalse(p.shouldDeprioritizeMenuEntry(11646));
    }
}

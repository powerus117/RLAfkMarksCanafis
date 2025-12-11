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
        // Per-course toggles default to true in the real config; tests can override as needed
        MarksOfGraceCDConfig defaultConfig = new MarksOfGraceCDConfig() {
            @Override
            public Notification notifyMarksOfGraceCD() {
                return Notification.OFF;
            }

            @Override
            public SwapLeftClickMode swapLeftClickMode() {
                return SwapLeftClickMode.OFF;
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
        plugin.setConfig(defaultConfig);

        // default values
        plugin.hasReducedCooldown = false;
        plugin.lastCompleteMarkTimeMillis = 0;
        plugin.lastCompleteTimeMillis = 0;
        plugin.isOnCooldown = false;
    }

    // ========== Cooldown Timestamp Tests ==========

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
    public void testCooldownTimestamp_withTimerBuffer() {
        // Test with a timer buffer configured
        MarksOfGraceCDConfig cfgWithBuffer = new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.OFF; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 5; } // 5 second buffer
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
        };

        plugin.setConfig(cfgWithBuffer);

        long base = Instant.parse("2020-09-13T12:00:00Z").toEpochMilli();
        plugin.lastCompleteMarkTimeMillis = base;
        plugin.setLastWorldPing(-1);

        long minuteTruncated = base - (base % MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);
        long expected = minuteTruncated + (3 * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE) + (5 * 1000); // + 5 seconds buffer

        long ts = plugin.getCooldownTimestamp(false);
        assertEquals(expected, ts);
    }

    @Test
    public void testCooldownTimestamp_reducedNotAppliedWhenNotChecked() {
        long base = Instant.parse("2020-09-13T12:00:30Z").toEpochMilli();
        plugin.lastCompleteMarkTimeMillis = base;
        plugin.hasReducedCooldown = true;

        plugin.setLastWorldPing(-1);

        long minuteTruncated = base - (base % MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);
        long expected = minuteTruncated + (3 * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);
        // checkForReduced = false, so no reduction applied

        long ts = plugin.getCooldownTimestamp(false);
        assertEquals(expected, ts);
    }

    @Test
    public void testCooldownTimestamp_zeroReturnsZero() {
        plugin.lastCompleteMarkTimeMillis = 0;
        assertEquals(0, plugin.getCooldownTimestamp(false));
        assertEquals(0, plugin.getCooldownTimestamp(true));
    }

    @Test
    public void testCooldownTimestamp_withSmallPing() {
        long base = Instant.parse("2020-09-13T12:00:00Z").toEpochMilli();
        plugin.lastCompleteMarkTimeMillis = base;

        // 100ms ping -> 50ms one-way
        plugin.setLastWorldPing(100);

        long minuteTruncated = base - (base % MarksOfGraceCDPlugin.MILLIS_PER_MINUTE);
        long expected = minuteTruncated + (3 * MarksOfGraceCDPlugin.MILLIS_PER_MINUTE) - 50;

        long ts = plugin.getCooldownTimestamp(false);
        assertEquals(expected, ts);
    }

    // ========== Course Enabled Tests ==========

    @Test
    public void testIsCourseEnabled_allEnabledByDefault() {
        for (Courses course : Courses.values()) {
            assertTrue(course.name() + " should be enabled by default",
                    plugin.isCourseEnabled(course));
        }
    }

    @Test
    public void testIsCourseEnabled_nullReturnsTrue() {
        assertTrue(plugin.isCourseEnabled(null));
    }

    // ========== Swap Mode Tests ==========

    @Test
    public void testDeprioritize_swapModeOff_neverDeprioritizes() {
        MarksOfGraceCDPlugin p = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 1000; // 1s left
            }
        };

        p.setConfig(new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.OFF; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            @Override public boolean enableCanifis() { return true; }
        });

        p.currentCourse = Courses.CANIFIS;
        p.isOnCooldown = true;

        // Even with cooldown and correct obstacle, OFF mode should never deprioritize
        assertFalse(p.shouldDeprioritizeMenuEntry(14897));
    }

    @Test
    public void testDeprioritize_swapModeAlways_deprioritizesWhenOnCooldown() {
        MarksOfGraceCDPlugin p = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 60000; // 60s left
            }
        };

        p.setConfig(new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_NOT_EXPIRED; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            @Override public boolean enableCanifis() { return true; }
        });

        p.currentCourse = Courses.CANIFIS;
        p.isOnCooldown = true;

        // WHEN_NOT_EXPIRED mode should deprioritize as long as cooldown is active
        assertTrue(p.shouldDeprioritizeMenuEntry(14897));
    }

    @Test
    public void testDeprioritize_swapModeNearEnd_deprioritizesOnlyWhenBelowThreshold() {
        // Test WHEN_CANNOT_COMPLETE_LAP mode - should only deprioritize when time < threshold
        MarksOfGraceCDPlugin pLongTime = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 120000; // 120s left
            }
        };

        pLongTime.setConfig(new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_CANNOT_COMPLETE_LAP; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            @Override public boolean enableCanifis() { return true; }
            @Override public boolean useCustomLapTime() { return false; }
            @Override public int lapTimeBuffer() { return 0; }
            @Override public boolean assumeHardKandarinDiary() { return false; }
        });

        pLongTime.currentCourse = Courses.CANIFIS;
        pLongTime.isOnCooldown = true;

        // 120s > 44s threshold, should NOT deprioritize
        assertFalse(pLongTime.shouldDeprioritizeMenuEntry(14897));

        // Now test with short time left
        MarksOfGraceCDPlugin pShortTime = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 10000; // 10s left
            }
        };

        pShortTime.setConfig(new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_CANNOT_COMPLETE_LAP; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            @Override public boolean enableCanifis() { return true; }
            @Override public boolean useCustomLapTime() { return false; }
            @Override public int lapTimeBuffer() { return 0; }
            @Override public boolean assumeHardKandarinDiary() { return false; }
        });

        pShortTime.currentCourse = Courses.CANIFIS;
        pShortTime.isOnCooldown = true;

        // 10s < 44s threshold, SHOULD deprioritize
        assertTrue(pShortTime.shouldDeprioritizeMenuEntry(14897));
    }

    @Test
    public void testDeprioritize_wrongObstacle_neverDeprioritizes() {
        MarksOfGraceCDPlugin p = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 1000;
            }
        };

        p.setConfig(new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_NOT_EXPIRED; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            @Override public boolean enableCanifis() { return true; }
        });

        p.currentCourse = Courses.CANIFIS;
        p.isOnCooldown = true;

        // Wrong obstacle ID - should not deprioritize
        assertFalse(p.shouldDeprioritizeMenuEntry(99999));
    }

    @Test
    public void testDeprioritize_notOnCooldown_neverDeprioritizes() {
        MarksOfGraceCDPlugin p = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 1000;
            }
        };

        p.setConfig(new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_NOT_EXPIRED; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            @Override public boolean enableCanifis() { return true; }
        });

        p.currentCourse = Courses.CANIFIS;
        p.isOnCooldown = false; // Not on cooldown

        // Not on cooldown - should not deprioritize
        assertFalse(p.shouldDeprioritizeMenuEntry(14897));
    }

    @Test
    public void testDeprioritize_cooldownExpired_neverDeprioritizes() {
        MarksOfGraceCDPlugin p = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() - 1000; // Expired 1s ago
            }
        };

        p.setConfig(new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_NOT_EXPIRED; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            @Override public boolean enableCanifis() { return true; }
        });

        p.currentCourse = Courses.CANIFIS;
        p.isOnCooldown = true;

        // Cooldown timestamp in the past - should not deprioritize
        assertFalse(p.shouldDeprioritizeMenuEntry(14897));
    }

    // ========== Per-Course Toggle Deprioritization Tests ==========

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

    @Test
    public void testDeprioritize_gnomeEnabled_deprioritizesAllObstacles() {
        MarksOfGraceCDPlugin p = new MarksOfGraceCDPlugin() {
            @Override
            public long getCooldownTimestamp(boolean checkForReduced) {
                return Instant.now().toEpochMilli() + 1000; // 1s left
            }
        };

        p.setConfig(new MarksOfGraceCDConfig() {
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.WHEN_CANNOT_COMPLETE_LAP; }
            @Override public int customLapTimeSeconds() { return 180; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public boolean showDebugValues() { return false; }
            @Override public boolean enableGnome() { return true; }
            @Override public boolean useCustomLapTime() { return false; }
            @Override public int lapTimeBuffer() { return 0; }
            @Override public boolean assumeHardKandarinDiary() { return false; }
        });

        p.currentCourse = Courses.GNOME;
        p.isOnCooldown = true;

        // Gnome is enabled and time left (1s) < threshold (34s), should deprioritize both obstacles
        assertTrue(p.shouldDeprioritizeMenuEntry(23138));
        assertTrue(p.shouldDeprioritizeMenuEntry(23139));
    }
}

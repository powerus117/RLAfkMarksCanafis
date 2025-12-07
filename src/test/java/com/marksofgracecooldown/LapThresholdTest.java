package com.marksofgracecooldown;

import net.runelite.client.config.Notification;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LapThresholdTest {
    private MarksOfGraceCDPlugin plugin;

    @Before
    public void setUp() throws Exception {
        plugin = new MarksOfGraceCDPlugin();

        // Minimal config: defaults
        MarksOfGraceCDConfig cfg = new MarksOfGraceCDConfig() {
            @Override public int customLapTimeSeconds() { return 200; }
            @Override public boolean useCustomLapTime() { return false; }
            @Override public int lapTimeBuffer() { return 2; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean assumeHardKandarinDiary() { return false; }
            // Unused defaults
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.OFF; }
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean showDebugValues() { return false; }
        };

        // Inject the test config without reflection
        plugin.setConfig(cfg);
    }

    private int callLapThreshold(Courses course) {
        return plugin.getLapThresholdSeconds(course);
    }

    @Test
    public void testAutomaticSeersThreshold() {
        // SEERS default optimal 44 + buffer 2 = 46
        int t = callLapThreshold(Courses.SEERS);
        assertEquals(46, t);
    }

    @Test
    public void testCustomOverride() throws Exception {
        // Replace with a config that returns useCustomLapTime = true
        MarksOfGraceCDConfig cfg2 = new MarksOfGraceCDConfig() {
            @Override public int customLapTimeSeconds() { return 123; }
            @Override public boolean useCustomLapTime() { return true; }
            @Override public int lapTimeBuffer() { return 0; }
            @Override public boolean useSeersTeleport() { return false; }
            @Override public boolean assumeHardKandarinDiary() { return false; }
            @Override public MarksOfGraceCDConfig.SwapLeftClickMode swapLeftClickMode() { return MarksOfGraceCDConfig.SwapLeftClickMode.OFF; }
            @Override public Notification notifyMarksOfGraceCD() { return Notification.OFF; }
            @Override public boolean useShortArdougneTimer() { return false; }
            @Override public boolean enableWorldPing() { return false; }
            @Override public int pingRefreshInterval() { return 15; }
            @Override public int timerBufferSeconds() { return 0; }
            @Override public boolean showDebugValues() { return false; }
        };

        // Inject the test config without reflection
        plugin.setConfig(cfg2);

        int t = callLapThreshold(Courses.SEERS);
        assertEquals(123, t);
    }
}

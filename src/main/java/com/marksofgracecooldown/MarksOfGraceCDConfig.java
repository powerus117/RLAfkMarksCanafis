package com.marksofgracecooldown;

import net.runelite.client.config.*;

@ConfigGroup("AfkMarksCanafis")  // Old name from when it was canifis only
public interface MarksOfGraceCDConfig extends Config {
    @ConfigItem(
            keyName = "cooldownNotifier",
            name = "Cooldown notifier",
            description = "Notify when the cooldown has expired.",
            position = 0
    )
    default Notification notifyMarksOfGraceCD() {
        return Notification.ON;
    }

    @ConfigItem(
            keyName = "optimalTimeBufferSeconds",
            name = "Optimal time buffer (seconds)",
            description =
                    "Adds extra seconds to the optimal time so users<br>" +
                    "have some room for mistakes (for example, set 2-4).<br>" +
                    "This is added on top of the computed optimal time.",
            position = 1
    )
    @Units(Units.SECONDS)
    default int optimalTimeBufferSeconds() { return 2; }

    @ConfigItem(
            keyName = "swapLeftClickOnWait",
            name = "Swap left click on wait",
            description =
                    "Swaps left click of the last obstacle while wait is on<br>" +
                    "to prevent accidental lap completion.",
            position = 2
    )
    default boolean swapLeftClickOnWait() {
        return false;
    }

    @ConfigItem(
            keyName = "customLapTimeSeconds",
            name = "Custom lap time (seconds)",
            description =
                    "When 'Use custom lap time' is enabled, this value will be used<br>" +
                    "as the lap-time threshold (in seconds) to disable the final obstacle.<br>" +
                    "Leave disabled to use the course-specific optimal times (recommended).",
            position = 3
    )
    @Units(Units.SECONDS)
    default int customLapTimeSeconds() {
        return 180;
    }

    @ConfigItem(
            keyName = "useCustomLapTime",
            name = "Use custom lap time (override optimal)",
            description =
                    "When enabled, the plugin will use the 'Custom lap time (seconds)' value<br>" +
                    "as the lap-time threshold instead of the course-specific optimal time.<br>" +
                    "Disable to use the automatic optimal times (recommended).",
            position = 4
    )
    default boolean useCustomLapTime() { return false; }

    @ConfigItem(
            keyName = "useShortArdougneTimer",
            name = "Use short Ardougne timer",
            description =
                    "If you have the elite Ardougne diary, there is a<br>" +
                    "50% chance the Ardougne cooldown is reduced to 2 min.<br>" +
                    "Choose whether to notify after the reduced or normal time.",
            position = 5
    )
    default boolean useShortArdougneTimer() {
        return false;
    }

    @ConfigItem(
            keyName = "useSeersTeleport",
            name = "Use Seers bank teleport",
            description =
                    "Whether to use the Seers bank teleport shortcut.<br>" +
                    "Using the Camelot teleport requires the Hard Kandarin diary.<br>" +
                    "The plugin will attempt to auto-detect the diary and will<br>" +
                    "show the detected status below; if detection fails, use the<br>" +
                    "override in Advanced.",
            position = 6
    )
    default boolean useSeersTeleport() {return false;}

    @ConfigItem(
            keyName = "kandarinDiaryDetected",
            name = "Hard Kandarin diary detected",
            description =
                    "Indicates whether the plugin detected the Hard Kandarin diary<br>" +
                    "as completed for your account. This is updated automatically<br>" +
                    "on login; if it is incorrect, use the override in Advanced.",
            position = 7
    )
    default boolean kandarinDiaryDetected() { return false; }

    @ConfigSection(
            name = "Advanced",
            description =
                    "Advanced settings (opt-in). These affect network<br>" +
                    "checks and are normally left at defaults.",
            position = 8,
            closedByDefault = true
    )
    String advanced = "advanced";

    @ConfigItem(
            keyName = "assumeHardKandarinDiary",
            name = "Assume Hard Kandarin diary (absolute override)",
            description =
                    "ABSOLUTE OVERRIDE: If enabled, the plugin will assume you have<br>" +
                    "completed the Hard Kandarin diary even if automatic detection<br>" +
                    "fails. Only use this if you are certain you have the diary.<br>" +
                    "Using this incorrectly may result in incorrect optimal times.",
            position = 9,
            section = advanced
    )
    default boolean assumeHardKandarinDiary() { return false; }

    @ConfigItem(
            keyName = "enableWorldPing",
            name = "Enable world ping",
            description =
                    "When enabled, the plugin will periodically measure<br>" +
                    "latency to your current RuneScape world using<br>" +
                    "RuneLite's ping implementation. Disable to avoid<br>" +
                    "any additional network probes.",
            position = 10,
            section = advanced
    )
    default boolean enableWorldPing() {
        return true;
    }

    @ConfigItem(
            keyName = "pingRefreshInterval",
            name = "Ping refresh interval",
            description =
                    "How often (seconds) to refresh the world ping<br>" +
                    "when enabled. Larger values reduce network<br>" +
                    "activity. Minimum 1 second.",
            position = 11,
            section = advanced
    )
    @Units(Units.SECONDS)
    default int pingRefreshInterval() {
        return 15;
    }

    @ConfigItem(
            keyName = "timerBufferSeconds",
            name = "Timer buffer (seconds)",
            description =
                    "Extra buffer added when checking whether the<br>" +
                    "Marks of Grace cooldown has expired. Default is 0<br>" +
                    "because world-ping is used; increase only if the<br>" +
                    "overlay reports cooldown finished before a mark<br>" +
                    "can actually spawn (try 1-3 seconds).",
            position = 12,
            section = advanced
    )
    @Units(Units.SECONDS)
    default int timerBufferSeconds() {
        return 0;
    }

    @ConfigItem(
            keyName = "showDebugValues",
            name = "Show debug values",
            description =
                    "Displays plugin debug values such as measured<br>" +
                    "world ping and internal timings for troubleshooting.<br>" +
                    "Enabling this also shows ping in the overlay.",
            position = 13,
            section = advanced
    )
    default boolean showDebugValues() {
        return false;
    }

}

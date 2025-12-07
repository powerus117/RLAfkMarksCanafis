package com.marksofgracecooldown;

import net.runelite.client.config.*;

@ConfigGroup("AfkMarksCanafis")  // Old name from when it was canifis only
public interface MarksOfGraceCDConfig extends Config {
    @ConfigItem(
            keyName = "cooldownNotifier",
            name = "Cooldown notification",
            description = "Notify when the cooldown has expired.",
            position = 0
    )
    default Notification notifyMarksOfGraceCD() {
        return Notification.ON;
    }

    @ConfigItem(
            keyName = "lapTimeBuffer",
            name = "Lap time buffer",
            description =
                    "Adds extra seconds to the optimal lap time so you<br>" +
                    "have some room for mistakes (for example, set 2-4).<br>",
            position = 1
    )
    @Units(Units.SECONDS)
    default int lapTimeBuffer() { return 2; }

    // Replaced the previous boolean setting `swapLeftClickOnWait` with an enum dropdown
    // to allow three modes: OFF, SWAP_WHEN_CANNOT_COMPLETE_LAP (previous behaviour),
    // and SWAP_WHEN_NOT_EXPIRED (always swap while cooldown active).
    enum SwapLeftClickMode {
        OFF("Off"),
        WHEN_CANNOT_COMPLETE_LAP("Near end"),
        WHEN_NOT_EXPIRED("Always");

        private final String label;

        SwapLeftClickMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @ConfigItem(
            keyName = "swapLeftClickMode",
            name = "Swap mode",
            description =
                    "When to swap/deprioritize left-click on the final obstacle.<br>" +
                    "Off - never swap.<br>" +
                    "Near end - swap only if remaining time is less than the lap threshold.<br>" +
                    "Always - swap whenever the cooldown is active.",
            position = 2
    )
    default SwapLeftClickMode swapLeftClickMode() {
        return SwapLeftClickMode.OFF; // keep previous default (disabled)
    }

    @ConfigItem(
            keyName = "useCustomLapTime",
            name = "Use custom lap time",
            description =
                    "When enabled, the plugin will use the 'Custom lap time' value<br>" +
                    "as the lap-time threshold instead of the course-specific optimal time.",
            position = 3
    )
    default boolean useCustomLapTime() { return false; }

    @ConfigItem(
            keyName = "customLapTimeSeconds",
            name = "Custom lap time",
            description =
                    "When 'Use custom lap time' is enabled, this value will be used<br>" +
                    "as the lap-time threshold  to disable the final obstacle.<br>" +
                    "Leave disabled to use the course-specific optimal times (recommended).",
            position = 4
    )
    @Units(Units.SECONDS)
    default int customLapTimeSeconds() {
        return 180;
    }

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
            // Keep the core description focused; move diary-detection guidance to the warning field per request
            description =
                    "Whether to use the Seers bank teleport shortcut.<br>" +
                    "Using the Camelot teleport requires the Hard Kandarin diary.",
            position = 6
    )
    default boolean useSeersTeleport() {return false;}

    @ConfigSection(
            name = "Per-course",
            description = "Enable or disable the plugin per agility course.",
            position = 7,
            closedByDefault = true
    )
    String perCourse = "perCourse";

    @ConfigItem(
            keyName = "enableDraynor",
            name = "Draynor Rooftop",
            description = "Enable plugin on the Draynor rooftop course.",
            position = 8,
            section = perCourse
    )
    default boolean enableDraynor() { return true; }

    @ConfigItem(
            keyName = "enableAlKharid",
            name = "Al Kharid Rooftop",
            description = "Enable plugin on the Al Kharid rooftop course.",
            position = 9,
            section = perCourse
    )
    default boolean enableAlKharid() { return true; }

    @ConfigItem(
            keyName = "enableVarrock",
            name = "Varrock Rooftop",
            description = "Enable plugin on the Varrock rooftop course.",
            position = 10,
            section = perCourse
    )
    default boolean enableVarrock() { return true; }

    @ConfigItem(
            keyName = "enableCanifis",
            name = "Canifis Rooftop",
            description = "Enable plugin on the Canifis rooftop course.",
            position = 11,
            section = perCourse
    )
    default boolean enableCanifis() { return true; }

    @ConfigItem(
            keyName = "enableFalador",
            name = "Falador Rooftop",
            description = "Enable plugin on the Falador rooftop course.",
            position = 12,
            section = perCourse
    )
    default boolean enableFalador() { return true; }

    @ConfigItem(
            keyName = "enableSeers",
            name = "Seers Rooftop",
            description = "Enable plugin on the Seers' Village rooftop course.",
            position = 13,
            section = perCourse
    )
    default boolean enableSeers() { return true; }

    @ConfigItem(
            keyName = "enablePollnivneach",
            name = "Pollnivneach Rooftop",
            description = "Enable plugin on the Pollnivneach rooftop course.",
            position = 14,
            section = perCourse
    )
    default boolean enablePollnivneach() { return true; }

    @ConfigItem(
            keyName = "enableRelleka",
            name = "Rellekka Rooftop",
            description = "Enable plugin on the Rellekka rooftop course.",
            position = 15,
            section = perCourse
    )
    default boolean enableRelleka() { return true; }

    @ConfigItem(
            keyName = "enableArdougne",
            name = "Ardougne Rooftop",
            description = "Enable plugin on the Ardougne rooftop course.",
            position = 16,
            section = perCourse
    )
    default boolean enableArdougne() { return true; }

    @ConfigItem(
            keyName = "enableGnome",
            name = "Gnome Course",
            description = "Enable plugin on the Gnome agility course.",
            position = 17,
            section = perCourse
    )
    default boolean enableGnome() { return true; }

    @ConfigItem(
            keyName = "enableShayzienBasic",
            name = "Shayzien (Basic)",
            description = "Enable plugin on the Shayzien basic course.",
            position = 18,
            section = perCourse
    )
    default boolean enableShayzienBasic() { return true; }

    @ConfigItem(
            keyName = "enableBarbarian",
            name = "Barbarian Outpost",
            description = "Enable plugin on the Barbarian Outpost course.",
            position = 19,
            section = perCourse
    )
    default boolean enableBarbarian() { return true; }

    @ConfigItem(
            keyName = "enableShayzienAdvanced",
            name = "Shayzien (Advanced)",
            description = "Enable plugin on the Shayzien advanced course.",
            position = 20,
            section = perCourse
    )
    default boolean enableShayzienAdvanced() { return true; }

    @ConfigItem(
            keyName = "enableApeAtoll",
            name = "Ape Atoll",
            description = "Enable plugin on the Ape Atoll course.",
            position = 21,
            section = perCourse
    )
    default boolean enableApeAtoll() { return true; }

    @ConfigItem(
            keyName = "enableWerewolf",
            name = "Werewolf Agility",
            description = "Enable plugin on the Werewolf agility course.",
            position = 22,
            section = perCourse
    )
    default boolean enableWerewolf() { return true; }


    @ConfigSection(
            name = "Advanced",
            description =
                    "Advanced settings (opt-in). These affect network<br>" +
                    "checks and are normally left at defaults.",
            position = 23,
            closedByDefault = true
    )
    String advanced = "advanced";

    @ConfigItem(
            keyName = "assumeHardKandarinDiary",
            name = "Assume Hard Kandarin diary",
            description =
                    "If enabled, the plugin will assume you have completed<br>" +
                    "the Hard Kandarin diary even if automatic detection<br>" +
                    "fails. Only use this if you are certain you have the diary.<br>" +
                    "Using this incorrectly may result in incorrect optimal times.",
            position = 24,
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
            position = 25,
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
            position = 26,
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
            position = 27,
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
            position = 28,
            section = advanced
    )
    default boolean showDebugValues() {
        return false;
    }

}

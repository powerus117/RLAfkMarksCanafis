package com.marksofgracecooldown;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("AfkMarksCanafis")  // Old name from when it was canifis only
public interface MarksOfGraceCDConfig extends Config {
    @ConfigItem(
            keyName = "cooldownNotifier",
            name = "Cooldown notification",
            description = "Send a notification when the cooldown expires<br>and you can collect a new mark.",
            position = 0
    )
    default Notification notifyMarksOfGraceCD() {
        return Notification.ON;
    }

    @ConfigItem(
            keyName = "lapTimeBuffer",
            name = "Lap time buffer",
            description =
                    "Extra seconds added to the optimal lap time to<br>" +
                            "give you more room for slower or imperfect laps.<br>" +
                            "Recommended: 2-4 seconds.",
            position = 1
    )
    @Units(Units.SECONDS)
    default int lapTimeBuffer() {
        return 2;
    }

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
                    "Deprioritize left-click on the final obstacle to<br>" +
                            "help prevent accidentally finishing a lap too early.<br>" +
                            "Off: never deprioritize.<br>" +
                            "Near end: only when the overlay shows 'Wait'.<br>" +
                            "Always: whenever the cooldown is active.",
            position = 2
    )
    default SwapLeftClickMode swapLeftClickMode() {
        return SwapLeftClickMode.OFF; // keep previous default (disabled)
    }

    enum HighlightClickBoxesMode {
        OFF("Off"),
        ON_COOLDOWN("Cooldown"),
        XP("XP");

        private final String label;

        HighlightClickBoxesMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @ConfigItem(
            keyName = "HighLightClickBoxesMode",
            name = "Highlight click boxes",
            description =
                    "Update highlight of obstacle click boxes.<br>" +
                            "Off: no change.<br>" +
                            "Cooldown Active: highlight while cooldown active.<br>" +
                            "XP Highlight: additional highlight when in XP mode.<br>" +
                            "Note: Not influenced by 'Swap mode' above.<br>",
            position = 3
    )

    default HighlightClickBoxesMode highlightClickBoxesMode() {
        return HighlightClickBoxesMode.OFF;
    }

    @ConfigItem(
            keyName = "CooldownHighlightColor",
            name = "Cooldown highlight",
            description = "Color used to highlight obstacles when cooldown is active.<br>" +
                    "Only applies if 'Highlight click boxes' is set to 'Cooldown Active' or 'XP Highlight'.<br>" +
                    "Also colors the overlay title when cooldown is active.",
            position = 4
    )
    default Color CooldownHighlightColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "XpHighlightColor",
            name = "XP highlight",
            description = "Color used to highlight obstacles when you can earn XP (i.e. not on cooldown).<br>" +
                    "Only applies if 'Highlight click boxes' is set to 'XP Highlight'.<br>" +
                    "Also colors the overlay title when you can earn XP.",
            position = 5
    )

    default Color XpHighlightColor() {
        return Color.ORANGE;
    }

    @ConfigItem(
            keyName = "useCustomLapTime",
            name = "Use custom lap time",
            description =
                    "Replace the built-in optimal lap time with your<br>" +
                            "own custom value. Leave disabled to use the<br>" +
                            "course-specific lap times (recommended).",
            position = 6
    )
    default boolean useCustomLapTime() {
        return false;
    }

    @ConfigItem(
            keyName = "customLapTimeSeconds",
            name = "Custom lap time",
            description =
                    "Your custom lap time in seconds.<br>" +
                            "Only used when 'Use custom lap time' is enabled.",
            position = 7
    )
    @Units(Units.SECONDS)
    default int customLapTimeSeconds() {
        return 180;
    }

    @ConfigItem(
            keyName = "useShortArdougneTimer",
            name = "Use short Ardougne timer",
            description =
                    "With the Ardougne Elite Diary, the cooldown has a<br>" +
                            "50% chance to be reduced to 2 minutes. Enable this<br>" +
                            "to use the shorter timer instead of the full 3 minutes.",
            position = 8
    )
    default boolean useShortArdougneTimer() {
        return false;
    }

    @ConfigItem(
            keyName = "useSeersTeleport",
            name = "Use Seers bank teleport",
            description =
                    "Enable if you use the Camelot teleport to bank<br>" +
                            "after each lap. This shortens the optimal lap time.<br>" +
                            "Requires the Hard Kandarin Diary.",
            position = 9
    )
    default boolean useSeersTeleport() {
        return false;
    }

    @ConfigSection(
            name = "Per-course",
            description = "Choose which courses the plugin is active on.",
            position = 10,
            closedByDefault = true
    )
    String perCourse = "perCourse";

    @ConfigItem(
            keyName = "enableDraynor",
            name = "Draynor Rooftop",
            description = "Enable plugin on the Draynor rooftop course.",
            position = 11,
            section = perCourse
    )
    default boolean enableDraynor() {
        return true;
    }

    @ConfigItem(
            keyName = "enableAlKharid",
            name = "Al Kharid Rooftop",
            description = "Enable plugin on the Al Kharid rooftop course.",
            position = 12,
            section = perCourse
    )
    default boolean enableAlKharid() {
        return true;
    }

    @ConfigItem(
            keyName = "enableVarrock",
            name = "Varrock Rooftop",
            description = "Enable plugin on the Varrock rooftop course.",
            position = 13,
            section = perCourse
    )
    default boolean enableVarrock() {
        return true;
    }

    @ConfigItem(
            keyName = "enableCanifis",
            name = "Canifis Rooftop",
            description = "Enable plugin on the Canifis rooftop course.",
            position = 14,
            section = perCourse
    )
    default boolean enableCanifis() {
        return true;
    }

    @ConfigItem(
            keyName = "enableFalador",
            name = "Falador Rooftop",
            description = "Enable plugin on the Falador rooftop course.",
            position = 15,
            section = perCourse
    )
    default boolean enableFalador() {
        return true;
    }

    @ConfigItem(
            keyName = "enableSeers",
            name = "Seers Rooftop",
            description = "Enable plugin on the Seers' Village rooftop course.",
            position = 16,
            section = perCourse
    )
    default boolean enableSeers() {
        return true;
    }

    @ConfigItem(
            keyName = "enablePollnivneach",
            name = "Pollnivneach Rooftop",
            description = "Enable plugin on the Pollnivneach rooftop course.",
            position = 17,
            section = perCourse
    )
    default boolean enablePollnivneach() {
        return true;
    }

    @ConfigItem(
            keyName = "enableRelleka",
            name = "Rellekka Rooftop",
            description = "Enable plugin on the Rellekka rooftop course.",
            position = 18,
            section = perCourse
    )
    default boolean enableRelleka() {
        return true;
    }

    @ConfigItem(
            keyName = "enableArdougne",
            name = "Ardougne Rooftop",
            description = "Enable plugin on the Ardougne rooftop course.",
            position = 19,
            section = perCourse
    )
    default boolean enableArdougne() {
        return true;
    }

    @ConfigItem(
            keyName = "enableGnome",
            name = "Gnome Course",
            description = "Enable plugin on the Gnome agility course.",
            position = 20,
            section = perCourse
    )
    default boolean enableGnome() {
        return true;
    }

    @ConfigItem(
            keyName = "enableShayzienBasic",
            name = "Shayzien (Basic)",
            description = "Enable plugin on the Shayzien basic course.",
            position = 21,
            section = perCourse
    )
    default boolean enableShayzienBasic() {
        return true;
    }

    @ConfigItem(
            keyName = "enableBarbarian",
            name = "Barbarian Outpost",
            description = "Enable plugin on the Barbarian Outpost course.",
            position = 22,
            section = perCourse
    )
    default boolean enableBarbarian() {
        return true;
    }

    @ConfigItem(
            keyName = "enableShayzienAdvanced",
            name = "Shayzien (Advanced)",
            description = "Enable plugin on the Shayzien advanced course.",
            position = 23,
            section = perCourse
    )
    default boolean enableShayzienAdvanced() {
        return true;
    }

    @ConfigItem(
            keyName = "enableApeAtoll",
            name = "Ape Atoll",
            description = "Enable plugin on the Ape Atoll course.",
            position = 24,
            section = perCourse
    )
    default boolean enableApeAtoll() {
        return true;
    }

    @ConfigItem(
            keyName = "enableWerewolf",
            name = "Werewolf Agility",
            description = "Enable plugin on the Werewolf agility course.",
            position = 25,
            section = perCourse
    )
    default boolean enableWerewolf() {
        return true;
    }


    @ConfigSection(
            name = "Advanced",
            description =
                    "Fine-tuning options. Most users can leave<br>" +
                            "these at their default values.",
            position = 26,
            closedByDefault = true
    )
    String advanced = "advanced";

    @ConfigItem(
            keyName = "assumeHardKandarinDiary",
            name = "Assume Hard Kandarin diary",
            description =
                    "Force the plugin to treat you as having the Hard<br>" +
                            "Kandarin Diary. Only enable this if you have the<br>" +
                            "diary but automatic detection isn't working.",
            position = 27,
            section = advanced
    )
    default boolean assumeHardKandarinDiary() {
        return false;
    }

    @ConfigItem(
            keyName = "enableNtpSync",
            name = "Enable NTP time sync",
            description =
                    "Sync with an internet time server to correct for<br>" +
                            "system clock drift. Recommended if your computer's<br>" +
                            "clock is not accurate.",
            position = 28,
            section = advanced
    )
    default boolean enableNtpSync() {
        return true;
    }

    @ConfigItem(
            keyName = "enableWorldPing",
            name = "Enable world ping",
            description =
                    "Measure your connection latency and use it to<br>" +
                            "improve timer accuracy. Disable if you don't want<br>" +
                            "the plugin making network requests.",
            position = 29,
            section = advanced
    )
    default boolean enableWorldPing() {
        return true;
    }

    @ConfigItem(
            keyName = "pingRefreshInterval",
            name = "Ping refresh interval",
            description =
                    "How often to re-measure your connection latency.<br>" +
                            "Higher values reduce network activity.",
            position = 30,
            section = advanced
    )
    @Units(Units.SECONDS)
    default int pingRefreshInterval() {
        return 15;
    }

    @ConfigItem(
            keyName = "timerBufferSeconds",
            name = "Timer buffer",
            description =
                    "Extra seconds added to the cooldown timer. Increase<br>" +
                            "this (try 1-3) if the overlay says 'Run' but marks<br>" +
                            "don't spawn yet.",
            position = 31,
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
                    "Display extra information in the overlay such as<br>" +
                            "lap times, NTP sync status, and connection latency.<br>" +
                            "Useful for troubleshooting timing issues.",
            position = 32,
            section = advanced
    )
    default boolean showDebugValues() {
        return false;
    }

}

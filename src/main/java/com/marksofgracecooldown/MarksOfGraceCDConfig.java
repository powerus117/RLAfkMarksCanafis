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
            keyName = "swapLeftClickOnWait",
            name = "Swap left click on wait",
            description =
                    "Swaps left click of the last obstacle while wait is on<br>" +
                    "to prevent accidental lap completion.",
            position = 1
    )
    default boolean swapLeftClickOnWait() {
        return false;
    }

    @ConfigItem(
            keyName = "swapLeftClickTimeLeft",
            name = "Swap left click on time left",
            description =
                    "Only swap the left click when the cooldown time<br>" +
                    "remaining is below this number.",
            position = 2
    )
    @Units(Units.SECONDS)
    default int swapLeftClickTimeLeft() {
        return 180;
    }

    @ConfigItem(
            keyName = "useShortArdougneTimer",
            name = "Use short Ardougne timer",
            description =
                    "If you have the elite Ardougne diary, there is a<br>" +
                    "50% chance the Ardougne cooldown is reduced to 2 min.<br>" +
                    "Choose whether to notify after the reduced or normal time.",
            position = 3
    )
    default boolean useShortArdougneTimer() {
        return true;
    }

    @ConfigSection(
            name = "Advanced",
            description =
                    "Advanced settings (opt-in). These affect network<br>" +
                    "checks and are normally left at defaults.",
            position = 4,
            closedByDefault = true
    )
    String advanced = "advanced";

    @ConfigItem(
            keyName = "enableWorldPing",
            name = "Enable world ping",
            description =
                    "When enabled, the plugin will periodically measure<br>" +
                    "latency to your current RuneScape world using<br>" +
                    "RuneLite's ping implementation. Disable to avoid<br>" +
                    "any additional network probes.",
            position = 5,
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
            position = 6,
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
            position = 7,
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
            position = 8,
            section = advanced
    )
    default boolean showDebugValues() {
        return false;
    }

}

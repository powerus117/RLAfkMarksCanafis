package com.marksofgracecooldown;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;

@ConfigGroup("MarksOfGraceCooldown")
public interface MarksOfGraceCDConfig extends Config
{
    @ConfigItem(
            keyName = "swapLeftClickOnWait",
            name = "Swap left click on wait",
            description = "Swaps left click of last obstacle while wait is on to prevent accidental lap completion",
            position = 0
    )
    default boolean swapLeftClickOnWait()
    {
        return false;
    }

    @ConfigItem(
            keyName = "leewaySeconds",
            name = "Seconds of leeway",
            description = "Grace period for when timer is triggered, increase if timings are off.",
            position = 1
    )
    @Units(Units.SECONDS)
    default int leewaySeconds()
    {
        return 2;
    }

	@ConfigItem(
		keyName = "useShortArdougneTimer",
		name = "Use short Ardougne timer",
		description = "When having the elite Ardougne diary, there is a 50% chance to reduce the Ardougne cooldown to 2 min. Would you want to be notified after the reduced time or normal time?",
		position = 2
	)
	default boolean useShortArdougneTimer()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showDebugValues",
		name = "Show debug values",
		description = "Displays plugin debug values like ntp offset and state",
		position = 3
	)
	default boolean showDebugValues()
	{
		return false;
	}
}

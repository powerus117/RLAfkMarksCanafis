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
		keyName = "swapLeftClickTimeLeft",
		name = "Swap left click on time left",
		description = "Only swap left click when the cooldown time remaining is below this number.",
		position = 1
	)
	@Units(Units.SECONDS)
	default int swapLeftClickTimeLeft()
	{
		return 180;
	}

    @ConfigItem(
            keyName = "leewaySeconds",
            name = "Seconds of leeway",
            description = "Grace period for when timer is triggered, increase if timings are off.",
            position = 2
    )
    @Units(Units.SECONDS)
    default int leewaySeconds()
    {
        return 1;
    }

	@ConfigItem(
		keyName = "useShortArdougneTimer",
		name = "Use short Ardougne timer",
		description = "When having the elite Ardougne diary, there is a 50% chance to reduce the Ardougne cooldown to 2 min. Would you want to be notified after the reduced time or normal time?",
		position = 3
	)
	default boolean useShortArdougneTimer()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showDebugValues",
		name = "Show debug values",
		description = "Displays plugin debug values like ntp offset and state",
		position = 4
	)
	default boolean showDebugValues()
	{
		return false;
	}
}

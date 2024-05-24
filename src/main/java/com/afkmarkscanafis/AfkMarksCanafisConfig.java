package com.afkmarkscanafis;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;

@ConfigGroup("AfkMarksCanafis")
public interface AfkMarksCanafisConfig extends Config
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
            description = "Grace period for when timer is triggered, increase if timings feel off. And sync computer clock",
            position = 1
    )
    @Units(Units.SECONDS)
    default int leewaySeconds()
    {
        return 2;
    }
}

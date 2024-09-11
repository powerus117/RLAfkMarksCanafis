package com.marksofgracecooldown;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ArkMarksPluginCanafisTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MarksOfGraceCDPlugin.class);
		RuneLite.main(args);
	}
}
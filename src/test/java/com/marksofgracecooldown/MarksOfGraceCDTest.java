package com.marksofgracecooldown;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MarksOfGraceCDTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MarksOfGraceCDPlugin.class);
		RuneLite.main(args);
	}
}
package com.afkmarkscanafis;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ArkMarksPluginCanafisTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AfkMarksCanafisPlugin.class);
		RuneLite.main(args);
	}
}
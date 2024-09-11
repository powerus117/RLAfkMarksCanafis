/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.afkmarkscanafis;

import com.afkmarkscanafis.ntp.NtpClient;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

class AfkMarksCanafisOverlay extends OverlayPanel
{
	private static final int TIMEOUT_MINUTES = 5;
	private static final long TIMEOUT_MILLIS = TIMEOUT_MINUTES * AfkMarksCanafisPlugin.MILLIS_PER_MINUTE;

	private final AfkMarksCanafisPlugin plugin;

	@Inject
	private AfkMarksCanafisConfig config;

	@Inject
	public AfkMarksCanafisOverlay(AfkMarksCanafisPlugin plugin) {
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.lastCompleteMarkTimeMillis == 0)
		{
			return null;
		}

		long currentMillis = Instant.now().toEpochMilli();
		long millisSinceLastComplete = currentMillis - plugin.lastCompleteTimeMillis;

		if (millisSinceLastComplete > TIMEOUT_MILLIS)
		{
			plugin.lastCompleteMarkTimeMillis = 0;
			plugin.lastCompleteTimeMillis = 0;
			return null;
		}

		if (plugin.isOnCooldown)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Wait")
				.color(Color.RED)
				.build());
		}
		else
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Run")
				.color(Color.GREEN)
				.build());
		}

		long millisLeft = plugin.isOnCooldown ? plugin.getCooldownTimestamp() - currentMillis : 0;
		long secondsLeft = (long)Math.ceil((double)millisLeft / 1000);
		panelComponent.getChildren().add(LineComponent.builder()
				.left("Time until run:")
				.right(String.format("%d:%02d", (secondsLeft % 3600) / 60, (secondsLeft % 60)))
				.build());

		if (config.showDebugValues())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("NTP State:")
				.right(String.valueOf(NtpClient.SyncState))
				.build());

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Time offset:")
				.right(getReadableOffset(NtpClient.SyncedOffsetMillis))
				.build());
		}

		return super.render(graphics);
	}

	private String getReadableOffset(long offset)
	{
		if (Math.abs(offset) < 1000)
			return offset + "ms";

		offset /= 1000; // Seconds

		if (Math.abs(offset) < 1000)
			return offset + "s";

		offset /= 60; // Minutes

		if (Math.abs(offset) < 1000)
			return offset + "m";

		offset /= 60; // Hours

		if (Math.abs(offset) < 1000)
			return offset + "h";

		return "LOTS";
	}
}

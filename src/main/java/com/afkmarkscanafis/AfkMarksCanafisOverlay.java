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
	private static final int TIMEOUT = 10;

	private final AfkMarksCanafisPlugin plugin;

	@Inject
	public AfkMarksCanafisOverlay(AfkMarksCanafisPlugin plugin) {
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.markCooldownCompleteTime == null ||
			!plugin.isInCanafisArea)
		{
			return null;
		}

		Duration markTimeout = Duration.ofMinutes(TIMEOUT);
		Duration sinceLastComplete = Duration.between(plugin.lastCompleteTime, Instant.now().atZone(ZoneOffset.UTC));

		if (sinceLastComplete.compareTo(markTimeout) >= 0)
		{
			plugin.markCooldownCompleteTime = null;
			plugin.lastCompleteTime = null;
			return null;
		}

		if (plugin.shouldRun)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Run")
				.color(Color.GREEN)
				.build());
		}
		else
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Wait")
				.color(Color.RED)
				.build());
		}

		long s = Math.max(Duration.between(Instant.now().atZone(ZoneOffset.UTC), plugin.markCooldownCompleteTime).getSeconds(), 0);
		panelComponent.getChildren().add(LineComponent.builder()
				.left("Time until run:")
				.right(String.format("%d:%02d", (s % 3600) / 60, (s % 60)))
				.build());

		return super.render(graphics);
	}
}

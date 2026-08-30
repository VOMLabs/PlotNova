/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.paper.placeholder;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.query.PlotQuery;
import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MiniPlaceholder expansion for PlotNova.
 * Registers global and audience placeholders compatible with MiniPlaceholder API v2.
 */
public final class MiniPlaceholderExpansion {

    private MiniPlaceholderExpansion() {
        // Utility class
    }

    /**
     * Register the PlotNova expansion with MiniPlaceholder.
     * Should be called during plugin enable if MiniPlaceholder is present.
     */
    public static void register() {
        final Expansion.Builder builder = Expansion.builder("plotnova");

        // === Global placeholders (no audience required) ===

        // <plotnova_server_plot_count> — total plots on the server
        builder.globalPlaceholder("server_plot_count", (queue, ctx) -> {
            final int count = PlotQuery.newQuery().allPlots().count();
            return Tag.selfClosingInserting(Component.text(count));
        });

        // === Audience placeholders (require a player) ===

        // <plotnova_has_plot> — whether the player owns any plot
        builder.audiencePlaceholder("has_plot", (audience, queue, ctx) -> {
            final PlotPlayer<?> plotPlayer = getPlayer(audience);
            if (plotPlayer == null) {
                return Tag.selfClosingInserting(Component.text("false"));
            }
            final boolean hasPlot = plotPlayer.getPlotCount() > 0;
            return Tag.selfClosingInserting(Component.text(hasPlot));
        });

        // <plotnova_plot_count> — number of plots the player owns
        builder.audiencePlaceholder("plot_count", (audience, queue, ctx) -> {
            final PlotPlayer<?> plotPlayer = getPlayer(audience);
            if (plotPlayer == null) {
                return Tag.selfClosingInserting(Component.text("0"));
            }
            return Tag.selfClosingInserting(Component.text(plotPlayer.getPlotCount()));
        });

        // <plotnova_plot_count_world> — plot count in the player's current world
        builder.audiencePlaceholder("plot_count_world", (audience, queue, ctx) -> {
            final PlotPlayer<?> plotPlayer = getPlayer(audience);
            if (plotPlayer == null) {
                return Tag.selfClosingInserting(Component.text("0"));
            }
            final String worldName = plotPlayer.getLocation().getWorldName();
            return Tag.selfClosingInserting(Component.text(plotPlayer.getPlotCount(worldName)));
        });

        // <plotnova_plot_area> — current plot area name
        builder.audiencePlaceholder("plot_area", (audience, queue, ctx) -> {
            final PlotPlayer<?> plotPlayer = getPlayer(audience);
            if (plotPlayer == null) {
                return Tag.selfClosingInserting(Component.empty());
            }
            final var plotArea = plotPlayer.getLocation().getPlotArea();
            if (plotArea == null) {
                return Tag.selfClosingInserting(Component.empty());
            }
            return Tag.selfClosingInserting(Component.text(plotArea.getWorldName()));
        });

        // <plotnova_plot_id> — current plot ID (or empty if not in a plot)
        builder.audiencePlaceholder("plot_id", (audience, queue, ctx) -> {
            final PlotPlayer<?> plotPlayer = getPlayer(audience);
            if (plotPlayer == null) {
                return Tag.selfClosingInserting(Component.empty());
            }
            final var plot = plotPlayer.getCurrentPlot();
            if (plot == null) {
                return Tag.selfClosingInserting(Component.empty());
            }
            return Tag.selfClosingInserting(Component.text(plot.getId().toString()));
        });

        // <plotnova_plot_owner> — current plot owner (or empty if unowned)
        builder.audiencePlaceholder("plot_owner", (audience, queue, ctx) -> {
            final PlotPlayer<?> plotPlayer = getPlayer(audience);
            if (plotPlayer == null) {
                return Tag.selfClosingInserting(Component.empty());
            }
            final var plot = plotPlayer.getCurrentPlot();
            if (plot == null || !plot.hasOwner()) {
                return Tag.selfClosingInserting(Component.empty());
            }
            final UUID ownerUuid = plot.getOwnerAbs();
            if (ownerUuid == null) {
                return Tag.selfClosingInserting(Component.empty());
            }
            try {
                final String ownerName = PlotSquared.platform().playerManager()
                        .getUsernameCaption(ownerUuid)
                        .get(Settings.UUID.BLOCKING_TIMEOUT, TimeUnit.MILLISECONDS)
                        .toString();
                return Tag.selfClosingInserting(Component.text(ownerName));
            } catch (final Exception e) {
                return Tag.selfClosingInserting(Component.empty());
            }
        });

        // <plotnova_plot_count_not_done> — count of player's plots not marked as done
        builder.audiencePlaceholder("plot_count_not_done", (audience, queue, ctx) -> {
            final PlotPlayer<?> plotPlayer = getPlayer(audience);
            if (plotPlayer == null) {
                return Tag.selfClosingInserting(Component.text("0"));
            }
            final int count = PlotQuery.newQuery()
                    .ownedBy(plotPlayer)
                    .whereBasePlot()
                    .thatPasses(plot -> !DoneFlag.isDone(plot))
                    .count();
            return Tag.selfClosingInserting(Component.text(count));
        });

        // Build and register
        final Expansion expansion = builder.build();
        expansion.register();
    }

    private static PlotPlayer<?> getPlayer(final Audience audience) {
        if (audience instanceof final Player player) {
            return PlotSquared.platform().playerManager().getPlayerIfExists(player.getUniqueId());
        }
        return null;
    }

}

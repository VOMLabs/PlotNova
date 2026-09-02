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
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;

@CommandDeclaration(command = "music",
        permission = "plots.music",
        usage = "/plot music",
        category = CommandCategory.APPEARANCE,
        requiredType = RequiredType.PLAYER)
public class Music extends SubCommand {

    private static final Collection<String> DISCS = Arrays
            .asList("music_disc_13", "music_disc_cat", "music_disc_blocks", "music_disc_chirp",
                    "music_disc_far", "music_disc_mall", "music_disc_mellohi", "music_disc_stal",
                    "music_disc_strad", "music_disc_ward", "music_disc_11", "music_disc_wait", "music_disc_otherside",
                    "music_disc_pigstep", "music_disc_5", "music_disc_relic", "music_disc_creator",
                    "music_disc_creator_music_box", "music_disc_precipice", "music_disc_tears", "music_disc_lava_chicken"
            );

    // make sure all discs and the bedrock ("cancel") fit into the inventory
    private static final int INVENTORY_ROWS = (int) Math.ceil((DISCS.size() + 1) / 9.0);

    private final EventDispatcher eventDispatcher;

    @Inject
    public Music(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (!plot.isAdded(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_MUSIC_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ADMIN_MUSIC_OTHER)
                    )
            );
            return true;
        }

        // Try dialog first
        final DialogFactory dialogFactory = DialogFactoryHolder.getDialogFactory();
        if (dialogFactory != null && dialogFactory.showMusicDialog(player, plot)) {
            return true;
        }

        // Dialog not available — send error message
        player.sendMessage(TranslatableCaption.of("errors.error"));
        return true;
    }

}

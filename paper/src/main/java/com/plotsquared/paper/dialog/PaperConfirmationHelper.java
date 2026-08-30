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
package com.plotsquared.paper.dialog;

import com.plotsquared.core.command.CmdConfirm;
import com.plotsquared.core.command.CmdInstance;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

/**
 * Paper-specific confirmation handler that uses the Dialog API
 * when available, falling back to the chat-based confirmation system.
 */
public final class PaperConfirmationHelper {

    private PaperConfirmationHelper() {
        // Utility class
    }

    /**
     * Show a confirmation to the player using Dialog API if available.
     * Falls back to the chat-based confirmation system.
     *
     * @param player     The player to show the confirmation to
     * @param commandStr The command string for the confirmation message
     * @param runnable   The action to execute on confirmation
     * @param title      The dialog title (used for Dialog API)
     * @param message    The dialog message (used for Dialog API)
     */
    public static void addPending(
            final PlotPlayer<?> player,
            final String commandStr,
            final Runnable runnable,
            final Component title,
            final Component message
    ) {
        // Remove any existing pending confirmation
        CmdConfirm.removePending(player);

        // Try to show Dialog API confirmation
        if (player.getPlatformPlayer() instanceof Player bukkitPlayer) {
            try {
                PaperDialogUtil.showConfirmation(
                        bukkitPlayer,
                        title,
                        message,
                        () -> {
                            // On confirm: execute the command
                            TaskManager.runTask(runnable);
                        },
                        () -> {
                            // On cancel: send cancellation message
                            player.sendMessage(TranslatableCaption.of("confirm.failed_confirm"));
                        }
                );
                return;
            } catch (final Exception ignored) {
                // Fall through to chat-based confirmation
            }
        }

        // Fallback: chat-based confirmation
        if (commandStr != null) {
            player.sendMessage(
                    TranslatableCaption.of("confirm.requires_confirm"),
                    TagResolver.builder()
                            .tag("command", Tag.inserting(Component.text(commandStr)))
                            .tag("timeout", Tag.inserting(Component.text(Settings.Confirmation.CONFIRMATION_TIMEOUT_SECONDS)))
                            .tag("value", Tag.inserting(Component.text("/plot confirm")))
                            .build()
            );
        }
        TaskManager.runTaskLater(() -> {
            CmdInstance cmd = new CmdInstance(runnable);
            try (final MetaDataAccess<CmdInstance> metaDataAccess = player.accessTemporaryMetaData(
                    PlayerMetaDataKeys.TEMPORARY_CONFIRM)) {
                metaDataAccess.set(cmd);
            }
        }, TaskTime.ticks(1L));
    }

    /**
     * Convenience overload using default title/message.
     */
    public static void addPending(
            final PlotPlayer<?> player,
            final String commandStr,
            final Runnable runnable
    ) {
        addPending(
                player,
                commandStr,
                runnable,
                Component.text("Confirm Action"),
                Component.text("Are you sure you want to proceed?")
        );
    }

}

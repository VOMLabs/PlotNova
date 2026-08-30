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

import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CmdConfirm {

    private static @Nullable ConfirmationHandler confirmationHandler;

    /**
     * Set the confirmation handler for showing confirmation UIs.
     * Called by platform-specific modules (e.g., Paper) during initialization.
     *
     * @param handler The confirmation handler to use
     */
    public static void setConfirmationHandler(final @Nullable ConfirmationHandler handler) {
        CmdConfirm.confirmationHandler = handler;
    }

    /**
     * Get the current confirmation handler.
     *
     * @return The current handler, or null if none is set
     */
    public static @Nullable ConfirmationHandler getConfirmationHandler() {
        return confirmationHandler;
    }

    public static @Nullable CmdInstance getPending(PlotPlayer<?> player) {
        try (final MetaDataAccess<CmdInstance> metaDataAccess = player.accessTemporaryMetaData(
                PlayerMetaDataKeys.TEMPORARY_CONFIRM)) {
            return metaDataAccess.get().orElse(null);
        }
    }

    public static void removePending(PlotPlayer<?> player) {
        try (final MetaDataAccess<CmdInstance> metaDataAccess = player.accessTemporaryMetaData(
                PlayerMetaDataKeys.TEMPORARY_CONFIRM)) {
            metaDataAccess.remove();
        }
    }

    public static void addPending(
            final PlotPlayer<?> player, String commandStr,
            final Runnable runnable
    ) {
        removePending(player);
        if (commandStr != null) {
            final Component title = Component.text("Confirm Action");
            final Component message = Component.text(commandStr);
            final Runnable onConfirm = () -> {
                // Store the pending command so Confirm command can execute it
                TaskManager.runTaskLater(() -> {
                    CmdInstance cmd = new CmdInstance(runnable);
                    try (final MetaDataAccess<CmdInstance> metaDataAccess = player.accessTemporaryMetaData(
                            PlayerMetaDataKeys.TEMPORARY_CONFIRM)) {
                        metaDataAccess.set(cmd);
                    }
                }, TaskTime.ticks(1L));
            };
            final Runnable onCancel = () -> {
                player.sendMessage(TranslatableCaption.of("confirm.failed_confirm"));
            };

            // Use the platform-specific handler if available
            final ConfirmationHandler handler = confirmationHandler;
            if (handler != null && player instanceof Audience audience) {
                handler.showConfirmation(audience, title, message, onConfirm, onCancel);
                // Store immediately for handler-based flow
                TaskManager.runTaskLater(() -> {
                    CmdInstance cmd = new CmdInstance(runnable);
                    try (final MetaDataAccess<CmdInstance> metaDataAccess = player.accessTemporaryMetaData(
                            PlayerMetaDataKeys.TEMPORARY_CONFIRM)) {
                        metaDataAccess.set(cmd);
                    }
                }, TaskTime.ticks(1L));
            } else {
                // Fallback: text-based confirmation (legacy behavior)
                player.sendMessage(
                        TranslatableCaption.of("confirm.requires_confirm"),
                        TagResolver.builder()
                                .tag("command", Tag.inserting(Component.text(commandStr)))
                                .tag("timeout", Tag.inserting(Component.text(Settings.Confirmation.CONFIRMATION_TIMEOUT_SECONDS)))
                                .tag("value", Tag.inserting(Component.text("/plot confirm")))
                                .build()
                );
                TaskManager.runTaskLater(() -> {
                    CmdInstance cmd = new CmdInstance(runnable);
                    try (final MetaDataAccess<CmdInstance> metaDataAccess = player.accessTemporaryMetaData(
                            PlayerMetaDataKeys.TEMPORARY_CONFIRM)) {
                        metaDataAccess.set(cmd);
                    }
                }, TaskTime.ticks(1L));
            }
        }
    }

}

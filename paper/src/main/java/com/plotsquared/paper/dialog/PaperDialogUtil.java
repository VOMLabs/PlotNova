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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for creating and showing Paper Dialog API dialogs.
 * Provides convenience methods for common dialog patterns used by PlotNova.
 */
public final class PaperDialogUtil {

    private static final Map<UUID, CompletableFuture<Boolean>> pendingConfirmations = new ConcurrentHashMap<>();

    private PaperDialogUtil() {
        // Utility class
    }

    /**
     * Show a confirmation dialog to a player.
     *
     * @param audience  The audience to show the dialog to
     * @param title     The dialog title
     * @param message   The confirmation message
     * @param onConfirm Callback when the player confirms (may be null)
     * @param onCancel  Callback when the player cancels (may be null)
     */
    public static void showConfirmation(
            final @NonNull Audience audience,
            final @NonNull Component title,
            final @NonNull Component message,
            final @Nullable Runnable onConfirm,
            final @Nullable Runnable onCancel
    ) {
        final DialogActionCallback confirmCallback = (responseView, responseAudience) -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
        };
        final DialogActionCallback cancelCallback = (responseView, responseAudience) -> {
            if (onCancel != null) {
                onCancel.run();
            }
        };

        final Dialog dialog = Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(message)))
                        .build())
                .type(DialogType.confirmation(
                        ActionButton.builder(Component.text("Yes"))
                                .action(DialogAction.customClick(confirmCallback, ClickCallback.Options.builder().build()))
                                .build(),
                        ActionButton.builder(Component.text("No"))
                                .action(DialogAction.customClick(cancelCallback, ClickCallback.Options.builder().build()))
                                .build()
                )));

        audience.showDialog(dialog);
    }

    /**
     * Show a blocking confirmation dialog that returns a CompletableFuture.
     *
     * @param audience The audience to show the dialog to
     * @param title    The dialog title
     * @param message  The confirmation message
     * @param playerId The player's UUID for tracking
     * @return A CompletableFuture that completes with true (confirmed) or false (cancelled/timed out)
     */
    public static CompletableFuture<Boolean> showBlockingConfirmation(
            final @NonNull Audience audience,
            final @NonNull Component title,
            final @NonNull Component message,
            final @NonNull UUID playerId
    ) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.completeOnTimeout(false, 1, TimeUnit.MINUTES);
        pendingConfirmations.put(playerId, future);

        showConfirmation(audience, title, message,
                () -> completeConfirmation(playerId, true),
                () -> completeConfirmation(playerId, false)
        );

        return future;
    }

    /**
     * Complete a pending blocking confirmation for a player.
     */
    public static void completeConfirmation(
            final @NonNull UUID playerId,
            final boolean result
    ) {
        final CompletableFuture<Boolean> future = pendingConfirmations.remove(playerId);
        if (future != null) {
            future.complete(result);
        }
    }

    /**
     * Show a notice dialog (single button).
     *
     * @param audience The audience to show the dialog to
     * @param title    The dialog title
     * @param message  The notice message
     */
    public static void showNotice(
            final @NonNull Audience audience,
            final @NonNull Component title,
            final @NonNull Component message
    ) {
        final Dialog dialog = Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(message)))
                        .build())
                .type(DialogType.notice()));

        audience.showDialog(dialog);
    }

    /**
     * Show a multi-action dialog with custom buttons.
     *
     * @param audience The audience to show the dialog to
     * @param title    The dialog title
     * @param message  The dialog message
     * @param actions  The list of action buttons
     */
    public static void showMultiAction(
            final @NonNull Audience audience,
            final @NonNull Component title,
            final @NonNull Component message,
            final @NonNull List<ActionButton> actions
    ) {
        final Dialog dialog = Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(message)))
                        .build())
                .type(DialogType.multiAction(actions).build()));

        audience.showDialog(dialog);
    }

    /**
     * Create an ActionButton with a custom click handler.
     *
     * @param label    The button label
     * @param key      The action key
     * @param callback The click callback
     * @return The created ActionButton
     */
    public static @NonNull ActionButton createActionButton(
            final @NonNull Component label,
            final @NonNull Key key,
            final @NonNull DialogActionCallback callback
    ) {
        return ActionButton.builder(label)
                .action(DialogAction.customClick(callback, ClickCallback.Options.builder().build()))
                .build();
    }

}

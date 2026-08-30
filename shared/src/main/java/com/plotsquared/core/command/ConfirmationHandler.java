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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Functional interface for showing confirmation UIs.
 * Implementations can provide different UIs (chat text, Dialog API, etc.)
 */
@FunctionalInterface
public interface ConfirmationHandler {

    /**
     * Show a confirmation prompt to the audience.
     *
     * @param audience    The audience to show the confirmation to
     * @param title       The confirmation title
     * @param message     The confirmation message
     * @param onConfirm   Callback when the user confirms
     * @param onCancel    Callback when the user cancels (may be null)
     */
    void showConfirmation(
            @NonNull Audience audience,
            @NonNull Component title,
            @NonNull Component message,
            @NonNull Runnable onConfirm,
            @NonNull Runnable onCancel
    );

}

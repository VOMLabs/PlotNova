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

import com.plotsquared.core.command.ConfirmationHandler;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Paper-specific confirmation handler that uses the Paper Dialog API
 * to show native confirmation dialogs to players.
 */
public class PaperConfirmationHandler implements ConfirmationHandler {

    @Override
    public void showConfirmation(
            final @NonNull Audience audience,
            final @NonNull Component title,
            final @NonNull Component message,
            final @NonNull Runnable onConfirm,
            final @NonNull Runnable onCancel
    ) {
        PaperDialogUtil.showConfirmation(audience, title, message, onConfirm, onCancel);
    }

}

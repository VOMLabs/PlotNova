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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Holder for the platform-specific dialog factory.
 * Set during platform initialization (e.g., Paper module).
 */
public final class DialogFactoryHolder {

    private static @Nullable DialogFactory dialogFactory;

    private DialogFactoryHolder() {
    }

    /**
     * Set the dialog factory for the current platform.
     *
     * @param factory The dialog factory to use
     */
    public static void setDialogFactory(final @Nullable DialogFactory factory) {
        DialogFactoryHolder.dialogFactory = factory;
    }

    /**
     * Get the current dialog factory.
     *
     * @return The dialog factory, or null if none is set
     */
    public static @Nullable DialogFactory getDialogFactory() {
        return dialogFactory;
    }

}

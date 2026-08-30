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

import com.plotsquared.core.components.ComponentPreset;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.setup.SetupProcess;

import java.util.List;

/**
 * Factory interface for creating platform-specific dialogs.
 * When set, commands will use dialogs instead of chest GUIs.
 */
public interface DialogFactory {

    /**
     * Show the component preset dialog to a player.
     *
     * @param player  The player to show the dialog to
     * @param plot    The plot the player is in
     * @param presets The list of allowed presets
     * @return true if the dialog was shown, false to fall back to PlotInventory
     */
    boolean showComponentDialog(PlotPlayer<?> player, Plot plot, List<ComponentPreset> presets);

    /**
     * Show the music/jukebox dialog to a player.
     *
     * @param player The player to show the dialog to
     * @param plot   The plot to modify
     * @return true if the dialog was shown, false to fall back to PlotInventory
     */
    boolean showMusicDialog(PlotPlayer<?> player, Plot plot);

    /**
     * Show the rating dialog to a player.
     *
     * @param player     The player to show the dialog to
     * @param plot       The plot to rate
     * @param categories The list of rating categories
     * @param onComplete Callback when all categories are rated
     * @return true if the dialog was shown, false to fall back to PlotInventory
     */
    boolean showRatingDialog(PlotPlayer<?> player, Plot plot, List<String> categories, Runnable onComplete);

    /**
     * Show the setup dialog to a player.
     *
     * @param player  The player to show the dialog to
     * @param process The setup process
     * @return true if the dialog was shown, false to fall back to chat
     */
    boolean showSetupDialog(PlotPlayer<?> player, SetupProcess process);

}

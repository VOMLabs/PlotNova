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
package com.plotsquared.core.components;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.configuration.serialization.ConfigurationSerialization;
import com.plotsquared.core.generator.ClassicPlotManagerComponent;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EconHandler;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ComponentPresetManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + ComponentPresetManager.class.getSimpleName());

    private final List<ComponentPreset> presets;
    private final EconHandler econHandler;
    private File componentsFile;

    @SuppressWarnings("unchecked")
    @Inject
    public ComponentPresetManager(final @NonNull EconHandler econHandler) throws
            IOException {
        this.econHandler = econHandler;
        final File oldLocation = new File(Objects.requireNonNull(PlotSquared.platform()).getDirectory(), "components.yml");
        final File folder = new File(Objects.requireNonNull(PlotSquared.platform()).getDirectory(), "config");
        if (!folder.exists() && !folder.mkdirs()) {
            LOGGER.error("Failed to create the /plugins/PlotSquared/config folder. Please create it manually");
        }
        if (oldLocation.exists()) {
            Path oldLoc = Paths.get(PlotSquared.platform().getDirectory() + "/components.yml");
            Path newLoc = Paths.get(PlotSquared.platform().getDirectory() + "/config" + "/components.yml");
            Files.move(oldLoc, newLoc);
        }
        try {
            this.componentsFile = new File(folder, "components.yml");
            if (!this.componentsFile.exists() && !this.componentsFile.createNewFile()) {
                LOGGER.error("Could not create the components.yml file. Please create 'components.yml' manually.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ConfigurationSerialization.registerClass(ComponentPreset.class, "ComponentPreset");

        final YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(this.componentsFile);

        if (yamlConfiguration.contains("title")) {
            yamlConfiguration.set("title", "#Now in /lang/messages_%.json, preset.title");
            try {
                yamlConfiguration.save(this.componentsFile);
            } catch (IOException e) {
                LOGGER.error("Failed to save default values to components.yml", e);
            }
        }

        if (yamlConfiguration.contains("presets")) {
            this.presets = yamlConfiguration
                    .getMapList("presets")
                    .stream()
                    .map(o -> (Map<String, Object>) o)
                    .map(ComponentPreset::deserialize)
                    .collect(Collectors.toList());
        } else {
            final List<ComponentPreset> defaultPreset = Collections.singletonList(
                    new ComponentPreset(
                            ClassicPlotManagerComponent.FLOOR,
                            "##wool",
                            0,
                            "",
                            "<rainbow:2>Disco Floor</rainbow>",
                            List.of("<gold>Spice up your plot floor</gold>"),
                            ItemTypes.YELLOW_WOOL
                    ));
            yamlConfiguration.set("presets", defaultPreset.stream().map(ComponentPreset::serialize).collect(Collectors.toList()));
            try {
                yamlConfiguration.save(this.componentsFile);
            } catch (final IOException e) {
                LOGGER.error("Failed to save default values to components.yml", e);
            }
            this.presets = defaultPreset;
        }

        MainCommand.getInstance().register(new ComponentCommand(this));
    }

    /**
     * Get the list of presets allowed for a player.
     * Used by the dialog factory to build dialog buttons.
     *
     * @param player the player
     * @return the list of allowed presets, or null if validation fails
     */
    public @Nullable List<ComponentPreset> getAllowedPresets(final PlotPlayer<?> player) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return null;
        } else if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return null;
        } else if (!plot.isOwner(player.getUUID()) && !plot.getTrusted().contains(player.getUUID()) && !player.hasPermission(
                Permission.PERMISSION_ADMIN_COMPONENTS_OTHER
        )) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return null;
        } else if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
            return null;
        }

        final List<ComponentPreset> allowedPresets = new ArrayList<>(this.presets.size());
        for (final ComponentPreset componentPreset : this.presets) {
            if (!componentPreset.permission().isEmpty() && !player.hasPermission(
                    componentPreset.permission()
            )) {
                continue;
            }
            allowedPresets.add(componentPreset);
        }
        if (allowedPresets.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("preset.empty"));
            return null;
        }
        return allowedPresets;
    }

}

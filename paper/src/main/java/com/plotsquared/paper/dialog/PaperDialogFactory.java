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

import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.command.DialogFactory;
import com.plotsquared.core.components.ComponentPreset;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.PlotRateEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.Rating;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.MusicFlag;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.setup.CommonSetupSteps;
import com.plotsquared.core.setup.SetupProcess;
import com.plotsquared.core.setup.SetupStep;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PatternUtil;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Paper-specific dialog factory using the Paper Dialog API.
 */
public class PaperDialogFactory implements DialogFactory {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private final EconHandler econHandler;
    private final EventDispatcher eventDispatcher;

    public PaperDialogFactory(final @NonNull EconHandler econHandler, final @NonNull EventDispatcher eventDispatcher) {
        this.econHandler = econHandler;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean showComponentDialog(
            final PlotPlayer<?> player,
            final Plot plot,
            final List<ComponentPreset> presets
    ) {
        if (!(player instanceof Audience audience)) {
            return false;
        }

        final Component title = TranslatableCaption.of("preset.title").toComponent(player);
        final List<ActionButton> actions = new ArrayList<>();

        for (int i = 0; i < presets.size(); i++) {
            final ComponentPreset preset = presets.get(i);
            final int index = i;

            // Build button label from preset display name
            final Component label = MINI_MESSAGE.deserialize(preset.displayName());

            // Build tooltip with description and cost
            final List<Component> tooltipLines = new ArrayList<>();
            if (preset.cost() > 0) {
                tooltipLines.add(Component.text(String.format("Cost: %.2f", preset.cost())));
            }
            tooltipLines.add(Component.text("Component: " + preset.component().name().toLowerCase()));
            tooltipLines.addAll(preset.description().stream()
                    .map(MINI_MESSAGE::deserialize)
                    .toList());
            Component tooltip = Component.empty();
            for (int j = 0; j < tooltipLines.size(); j++) {
                if (j > 0) {
                    tooltip = tooltip.append(Component.newline());
                }
                tooltip = tooltip.append(tooltipLines.get(j));
            }

            final DialogActionCallback callback = (responseView, responseAudience) -> {
                handleComponentPresetClick(player, plot, preset);
            };

            actions.add(ActionButton.builder(label)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick(callback, ClickCallback.Options.builder().build()))
                    .build());
        }

        final Dialog dialog = Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(
                                Component.text("Select a component preset to apply to your plot."))))
                        .build())
                .type(DialogType.multiAction(actions).build()));

        audience.showDialog(dialog);
        return true;
    }

    private void handleComponentPresetClick(
            final PlotPlayer<?> player,
            final Plot plot,
            final ComponentPreset preset
    ) {
        if (!plot.getOwnerAbs().equals(player.getUUID())
                && !plot.getTrusted().contains(player.getUUID())
                && !player.hasPermission(Permission.PERMISSION_ADMIN_COMPONENTS_OTHER)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return;
        }

        if (plot.getRunning() > 0) {
            player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            return;
        }

        final Pattern pattern = PatternUtil.parse(null, preset.pattern(), false);
        if (pattern == null) {
            player.sendMessage(TranslatableCaption.of("preset.preset_invalid"));
            return;
        }

        if (preset.cost() > 0.0D && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON)) {
            if (!econHandler.isEnabled(plot.getArea())) {
                player.sendMessage(
                        TranslatableCaption.of("preset.economy_disabled"),
                        TagResolver.resolver("preset", Tag.inserting(Component.text(preset.displayName())))
                );
                return;
            }
            if (econHandler.getMoney(player) < preset.cost()) {
                player.sendMessage(TranslatableCaption.of("preset.preset_cannot_afford"));
                return;
            } else {
                econHandler.withdrawMoney(player, preset.cost());
                player.sendMessage(
                        TranslatableCaption.of("economy.removed_balance"),
                        TagResolver.resolver(
                                "money",
                                Tag.inserting(Component.text(econHandler.format(preset.cost())))
                        )
                );
            }
        }

        BackupManager.backup(player, plot, () -> {
            plot.addRunning();
            QueueCoordinator queue = plot.getArea().getQueue();
            queue.setCompleteTask(plot::removeRunning);
            for (Plot current : plot.getConnectedPlots()) {
                current.getPlotModificationManager().setComponent(
                        preset.component().name(),
                        pattern,
                        player,
                        queue
                );
            }
            queue.enqueue();
            player.sendMessage(TranslatableCaption.of("working.generating_component"));
        });
    }

    @Override
    public boolean showMusicDialog(final PlotPlayer<?> player, final Plot plot) {
        if (!(player instanceof Audience audience)) {
            return false;
        }

        final Component title = TranslatableCaption.of("plotjukebox.jukebox_header").toComponent(player);
        final List<ActionButton> actions = new ArrayList<>();

        // Add music disc buttons
        final String[] discs = {
                "music_disc_13", "music_disc_cat", "music_disc_blocks", "music_disc_chirp",
                "music_disc_far", "music_disc_mall", "music_disc_mellohi", "music_disc_stal",
                "music_disc_strad", "music_disc_ward", "music_disc_11", "music_disc_wait",
                "music_disc_otherside", "music_disc_pigstep", "music_disc_5", "music_disc_relic",
                "music_disc_creator", "music_disc_creator_music_box", "music_disc_precipice",
                "music_disc_tears", "music_disc_lava_chicken"
        };

        for (final String disc : discs) {
            final ItemType type = ItemTypes.get(disc);
            if (type == null) {
                continue;
            }

            final Component label = Component.text(disc);
            final Component tooltip = TranslatableCaption.of("plotjukebox.click_to_play").toComponent(player);

            final DialogActionCallback callback = (responseView, responseAudience) -> {
                handleMusicClick(player, plot, type, disc);
            };

            actions.add(ActionButton.builder(label)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick(callback, ClickCallback.Options.builder().build()))
                    .build());
        }

        // Add cancel/remove button
        final Component cancelLabel = TranslatableCaption.of("plotjukebox.cancel_music").toComponent(player);
        final Component cancelTooltip = TranslatableCaption.of("plotjukebox.reset_music").toComponent(player);

        final DialogActionCallback cancelCallback = (responseView, responseAudience) -> {
            handleMusicRemove(player, plot);
        };

        actions.add(ActionButton.builder(cancelLabel)
                .tooltip(cancelTooltip)
                .action(DialogAction.customClick(cancelCallback, ClickCallback.Options.builder().build()))
                .build());

        final Dialog dialog = Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(
                                Component.text("Select a music disc to play on your plot."))))
                        .build())
                .type(DialogType.multiAction(actions).build()));

        audience.showDialog(dialog);
        return true;
    }

    private void handleMusicClick(
            final PlotPlayer<?> player,
            final Plot plot,
            final ItemType discType,
            final String discName
    ) {
        PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(MusicFlag.class)
                .createFlagInstance(discType);
        PlotFlagAddEvent event = eventDispatcher.callFlagAdd(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Music addition")))
            );
            return;
        }
        plot.setFlag(event.getFlag());
        player.sendMessage(
                TranslatableCaption.of("flag.flag_added"),
                TagResolver.builder()
                        .tag("flag", Tag.inserting(Component.text("music")))
                        .tag("value", Tag.inserting(Component.text(event.getFlag().getValue().toString())))
                        .build()
        );
    }

    private void handleMusicRemove(final PlotPlayer<?> player, final Plot plot) {
        PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(MusicFlag.class)
                .createFlagInstance(ItemTypes.BEDROCK);
        PlotFlagRemoveEvent event = eventDispatcher.callFlagRemove(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Music removal")))
            );
            return;
        }
        plot.removeFlag(event.getFlag());
        player.sendMessage(
                TranslatableCaption.of("flag.flag_removed"),
                TagResolver.builder()
                        .tag("flag", Tag.inserting(Component.text("music")))
                        .tag("value", Tag.inserting(Component.text("music_disc")))
                        .build()
        );
    }

    @Override
    public boolean showRatingDialog(
            final PlotPlayer<?> player,
            final Plot plot,
            final List<String> categories,
            final Runnable onComplete
    ) {
        if (!(player instanceof Audience audience)) {
            return false;
        }

        // Use a CompletableFuture chain for multi-category rating
        showRatingCategoryDialog(audience, player, plot, categories, 0, 0, onComplete);
        return true;
    }

    private void showRatingCategoryDialog(
            final Audience audience,
            final PlotPlayer<?> player,
            final Plot plot,
            final List<String> categories,
            final int categoryIndex,
            final int accumulatedRating,
            final Runnable onComplete
    ) {
        if (categoryIndex >= categories.size()) {
            // All categories rated, apply the rating
            PlotRateEvent event = eventDispatcher.callRating(player, plot, new Rating(accumulatedRating));
            if (event.getRating() != null) {
                plot.addRating(player.getUUID(), event.getRating());
                player.sendMessage(
                        TranslatableCaption.of("ratings.rating_applied"),
                        TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                );
            }
            onComplete.run();
            return;
        }

        final String categoryName = categories.get(categoryIndex);
        final Component title = Component.text(categoryName);
        final List<ActionButton> actions = new ArrayList<>();

        // Rating buttons 1-9
        for (int i = 0; i < 9; i++) {
            final int ratingValue = i + 1;
            final Component label = Component.text(ratingValue);
            final Component tooltip = Component.text("Rate " + ratingValue + "/9 for " + categoryName);

            final DialogActionCallback callback = (responseView, responseAudience) -> {
                int newAccumulated = accumulatedRating + ratingValue * (int) Math.pow(10, categoryIndex);
                showRatingCategoryDialog(audience, player, plot, categories, categoryIndex + 1, newAccumulated, onComplete);
            };

            actions.add(ActionButton.builder(label)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick(callback, ClickCallback.Options.builder().build()))
                    .build());
        }

        final Dialog dialog = Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(
                                Component.text("Rate category " + (categoryIndex + 1) + "/" + categories.size() + ": " + categoryName))))
                        .build())
                .type(DialogType.multiAction(actions).build()));

        audience.showDialog(dialog);
    }

    @Override
    public boolean showSetupDialog(final PlotPlayer<?> player, final SetupProcess process) {
        if (!(player instanceof Audience audience)) {
            return false;
        }

        final SetupStep currentStep = process.getCurrentStep();
        if (currentStep == null) {
            return false;
        }

        final Component title = Component.text("Plot Setup");
        final Component message = TranslatableCaption.of("setup.setup_init").toComponent(player);

        // Get suggestions for the current step
        final var suggestions = new ArrayList<String>();
        for (final var cmd : currentStep.createSuggestions(player, "")) {
            suggestions.add(cmd.toString());
        }

        final List<ActionButton> actions = new ArrayList<>();

        // Add suggestion buttons
        for (final String suggestion : suggestions) {
            final Component label = Component.text(suggestion);
            final Component tooltip = Component.text("Select: " + suggestion);

            final DialogActionCallback callback = (responseView, responseAudience) -> {
                process.handleInput(player, suggestion);
                if (process.getCurrentStep() != null) {
                    showSetupDialog(player, process);
                }
            };

            actions.add(ActionButton.builder(label)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick(callback, ClickCallback.Options.builder().build()))
                    .build());
        }

        // Add back button if there's history
        if (process.getCurrentStep() != CommonSetupSteps.CHOOSE_GENERATOR) {
            final Component backLabel = Component.text("Back");
            final Component backTooltip = Component.text("Go back to the previous step");

            final DialogActionCallback backCallback = (responseView, responseAudience) -> {
                process.back();
                showSetupDialog(player, process);
            };

            actions.add(ActionButton.builder(backLabel)
                    .tooltip(backTooltip)
                    .action(DialogAction.customClick(backCallback, ClickCallback.Options.builder().build()))
                    .build());
        }

        // Add cancel button
        final Component cancelLabel = Component.text("Cancel");
        final Component cancelTooltip = Component.text("Cancel the setup process");

        final DialogActionCallback cancelCallback = (responseView, responseAudience) -> {
            player.sendMessage(TranslatableCaption.of("setup.setup_cancelled"));
        };

        actions.add(ActionButton.builder(cancelLabel)
                .tooltip(cancelTooltip)
                .action(DialogAction.customClick(cancelCallback, ClickCallback.Options.builder().build()))
                .build());

        final Dialog dialog = Dialog.create(builder -> builder
                .empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(message)))
                        .build())
                .type(DialogType.multiAction(actions).build()));

        audience.showDialog(dialog);
        return true;
    }

}

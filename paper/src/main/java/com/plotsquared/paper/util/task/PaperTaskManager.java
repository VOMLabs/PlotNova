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
package com.plotsquared.paper.util.task;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.paper.PaperPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.util.task.PlotSquaredTask;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Bukkit implementation of {@link TaskManager} using
 * by {@link org.bukkit.scheduler.BukkitScheduler} and {@link PaperPlotSquaredTask}
 */
@Singleton
public class PaperTaskManager extends TaskManager {

    private final PaperPlatform paperMain;
    private final TaskTime.TimeConverter timeConverter;

    @Inject
    public PaperTaskManager(
            final @NonNull PaperPlatform paperMain,
            final TaskTime.@NonNull TimeConverter timeConverter
    ) {
        this.paperMain = paperMain;
        this.timeConverter = timeConverter;
    }

    @Override
    public PlotSquaredTask taskRepeat(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final PaperPlotSquaredTask paperPlotSquaredTask = new PaperPlotSquaredTask(runnable);
        paperPlotSquaredTask.runTaskTimer(this.paperMain, ticks, ticks);
        return paperPlotSquaredTask;
    }

    @Override
    public PlotSquaredTask taskRepeatAsync(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long ticks = this.timeConverter.toTicks(taskTime);
        final PaperPlotSquaredTask paperPlotSquaredTask = new PaperPlotSquaredTask(runnable);
        paperPlotSquaredTask.runTaskTimerAsynchronously(this.paperMain, ticks, ticks);
        return paperPlotSquaredTask;
    }

    @Override
    public void taskAsync(final @NonNull Runnable runnable) {
        if (this.paperMain.isEnabled()) {
            new PaperPlotSquaredTask(runnable).runTaskAsynchronously(this.paperMain);
        } else {
            runnable.run();
        }
    }

    @Override
    public <T> T sync(final @NonNull Callable<T> function, final int timeout) throws Exception {
        if (PlotSquared.get().isMainThread(Thread.currentThread())) {
            return function.call();
        }
        return this.callMethodSync(function).get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T> Future<T> callMethodSync(final @NonNull Callable<T> method) {
        return Bukkit.getScheduler().callSyncMethod(this.paperMain, method);
    }

    @Override
    public void task(final @NonNull Runnable runnable) {
        new PaperPlotSquaredTask(runnable).runTask(this.paperMain);
    }

    @Override
    public void taskLater(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long delay = this.timeConverter.toTicks(taskTime);
        new PaperPlotSquaredTask(runnable).runTaskLater(this.paperMain, delay);
    }

    @Override
    public void taskLaterAsync(
            final @NonNull Runnable runnable,
            final @NonNull TaskTime taskTime
    ) {
        final long delay = this.timeConverter.toTicks(taskTime);
        new PaperPlotSquaredTask(runnable).runTaskLaterAsynchronously(this.paperMain, delay);
    }

}

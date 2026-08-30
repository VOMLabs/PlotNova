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
package com.plotsquared.paper.util;

import com.google.inject.Singleton;
import com.plotsquared.paper.player.PaperPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.PlotItemStack;
import com.plotsquared.core.util.InventoryUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Singleton
public class PaperInventoryUtil extends InventoryUtil {

    @SuppressWarnings("deprecation") // Paper deprecation
    private static @Nullable ItemStack getItem(PlotItemStack item) {
        if (item == null) {
            return null;
        }
        Material material = BukkitAdapter.adapt(item.getType());
        if (material == null) {
            return null;
        }
        ItemStack stack = new ItemStack(material, item.getAmount());
        ItemMeta meta = null;
        if (item.getName() != null) {
            meta = stack.getItemMeta();
            Component nameComponent = PaperUtil.MINI_MESSAGE.deserialize(item.getName());
            meta.setDisplayName(PaperUtil.LEGACY_COMPONENT_SERIALIZER.serialize(nameComponent));
        }
        if (item.getLore() != null) {
            if (meta == null) {
                meta = stack.getItemMeta();
            }
            List<String> lore = new ArrayList<>();
            for (String entry : item.getLore()) {
                lore.add(PaperUtil.LEGACY_COMPONENT_SERIALIZER.serialize(PaperUtil.MINI_MESSAGE.deserialize(entry)));
            }
            meta.setLore(lore);
        }
        if (meta != null) {
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @Override
    public void open(PlotInventory inv) {
        PaperPlayer bp = (PaperPlayer) inv.getPlayer();
        Component titleComponent = PaperUtil.LEGACY_COMPONENT_SERIALIZER_AMPERSAND.deserialize(inv.getTitle());
        Inventory inventory = Bukkit.createInventory(null, inv.getLines() * 9, titleComponent);
        PlotItemStack[] items = inv.getItems();
        for (int i = 0; i < inv.getLines() * 9; i++) {
            PlotItemStack item = items[i];
            if (item != null) {
                inventory.setItem(i, getItem(item));
            }
        }
        bp.player.openInventory(inventory);
    }

    @Override
    public void close(PlotInventory inv) {
        if (!inv.isOpen()) {
            return;
        }
        PaperPlayer bp = (PaperPlayer) inv.getPlayer();
        bp.player.closeInventory();
    }

    @Override
    public boolean setItemChecked(PlotInventory inv, int index, PlotItemStack item) {
        PaperPlayer bp = (PaperPlayer) inv.getPlayer();
        InventoryView opened = bp.player.getOpenInventory();
        ItemStack stack = getItem(item);
        if (stack == null) {
            return false;
        }
        if (!inv.isOpen()) {
            return true;
        }
        opened.setItem(index, stack);
        // Paper auto-syncs inventory changes; updateInventory() is a no-op
        return true;
    }

    @SuppressWarnings("deprecation") // Paper deprecation
    public PlotItemStack getItem(ItemStack item) {
        if (item == null) {
            return null;
        }
        // int id = item.getTypeId();
        Material id = item.getType();
        ItemMeta meta = item.getItemMeta();
        int amount = item.getAmount();
        String name = null;
        String[] lore = null;
        if (item.hasItemMeta()) {
            assert meta != null;
            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            }
            if (meta.hasLore()) {
                List<String> itemLore = meta.getLore();
                assert itemLore != null;
                lore = itemLore.toArray(new String[0]);
            }
        }
        return new PlotItemStack(id.name(), amount, name, lore);
    }

    @Override
    public PlotItemStack[] getItems(PlotPlayer<?> player) {
        PaperPlayer bp = (PaperPlayer) player;
        PlayerInventory inv = bp.player.getInventory();
        return IntStream.range(0, 36).mapToObj(i -> getItem(inv.getItem(i)))
                .toArray(PlotItemStack[]::new);
    }

    @SuppressWarnings("deprecation") // #getTitle is needed for Spigot compatibility
    @Override
    public boolean isOpen(PlotInventory plotInventory) {
        if (!plotInventory.isOpen()) {
            return false;
        }
        PaperPlayer bp = (PaperPlayer) plotInventory.getPlayer();
        InventoryView opened = bp.player.getOpenInventory();
        if (plotInventory.isOpen()) {
            if (opened.getType() == InventoryType.CRAFTING) {
                opened.getTitle();
            }
        }
        return false;
    }

}

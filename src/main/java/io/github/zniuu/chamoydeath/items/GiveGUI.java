package io.github.zniuu.chamoydeath.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.ArrayList;
import java.util.List;

public class GiveGUI {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    public static void abrir(Player player) {
        List<GiveItem> items = GiveItemsRegistry.getAll();

        int filas = (int) Math.ceil(items.size() / 9.0);
        GiveGUIHolder holder = new GiveGUIHolder(items);

        Inventory inv = Bukkit.createInventory(holder, filas * 9, Component.text("Dar Ítems"));
        holder.setInventory(inv);

        for (int i = 0; i < items.size(); i++) {
            inv.setItem(i, construirItem(items.get(i)));
        }

        player.openInventory(inv);
    }

    public static ItemStack construirItem(GiveItem giveItem) {
        ItemStack item = new ItemStack(giveItem.getMaterial());
        ItemMeta meta = item.getItemMeta();

        Component nombre = LEGACY.deserialize(giveItem.getRawDisplayName()).decoration(TextDecoration.ITALIC, false);
        meta.displayName(nombre);

        if (!giveItem.getRawLore().isEmpty()) {
            List<Component> lore = new ArrayList<>();
            for (String linea : giveItem.getRawLore()) {
                lore.add(LEGACY.deserialize(linea).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }

        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of(giveItem.getCustomModelData()));
        meta.setCustomModelDataComponent(cmd);

        item.setItemMeta(meta);
        return item;
    }
}
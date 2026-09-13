package io.github.zniuu.chamoydeath.listeners;

import io.github.zniuu.chamoydeath.items.GiveGUI;
import io.github.zniuu.chamoydeath.items.GiveGUIHolder;
import io.github.zniuu.chamoydeath.items.GiveItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GiveGUIListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;
        if (!(clicked.getHolder() instanceof GiveGUIHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        GiveItem giveItem = holder.getItemAt(event.getSlot());
        if (giveItem == null) return;

        ItemStack itemAEntregar = GiveGUI.construirItem(giveItem);
        Map<Integer, ItemStack> sobrante = player.getInventory().addItem(itemAEntregar);
        sobrante.values().forEach(restante -> player.getWorld().dropItemNaturally(player.getLocation(), restante));

        player.sendMessage(Component.text("Recibiste 1x ", NamedTextColor.GRAY)
                .append(itemAEntregar.getItemMeta().displayName()));
    }
}
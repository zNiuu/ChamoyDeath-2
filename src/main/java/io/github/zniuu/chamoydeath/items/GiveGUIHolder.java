package io.github.zniuu.chamoydeath.items;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class GiveGUIHolder implements InventoryHolder {

    private Inventory inventory;
    private final List<GiveItem> items;

    public GiveGUIHolder(List<GiveItem> items) {
        this.items = items;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public GiveItem getItemAt(int slot) {
        if (slot < 0 || slot >= items.size()) return null;
        return items.get(slot);
    }
}
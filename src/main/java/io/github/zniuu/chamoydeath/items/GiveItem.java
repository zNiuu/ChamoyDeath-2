package io.github.zniuu.chamoydeath.items;

import org.bukkit.Material;

import java.util.List;

public class GiveItem {
    private final Material material;
    private final String customModelData;
    private final String rawDisplayName;
    private final List<String> rawLore;

    public GiveItem(Material material, String customModelData, String rawDisplayName, List<String> rawLore) {
        this.material = material;
        this.customModelData = customModelData;
        this.rawDisplayName = rawDisplayName;
        this.rawLore = rawLore;
    }

    public Material getMaterial() { return material; }
    public String getCustomModelData() { return customModelData; }
    public String getRawDisplayName() { return rawDisplayName; }
    public List<String> getRawLore() { return rawLore; }
}

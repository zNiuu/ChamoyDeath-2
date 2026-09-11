package io.github.zniuu.chamoydeath.ranks;

import net.kyori.adventure.text.format.TextColor;

/**
 * Jerarquía de rangos del servidor. El "weight" define el orden:
 * 0 = más alto (aparece primero en el tab list).
 */
public enum Rank {

    OWNER(0, "Owner", "#EE5252", "\uD83C\uDD94"), // 🆔
    STAFF(1, "Staff", "#FCD05C", "\uD83C\uDD95"), // 🆕
    VIP(2, "Vip", "#ADD9FC", "\uD83C\uDD99"),     // 🆙
    VIVO(3, "Vivo", "#34DE70", "\uD83C\uDD9A");   // 🆚

    private final int weight;
    private final String displayName;
    private final String colorHex;
    private final String icon;

    Rank(int weight, String displayName, String colorHex, String icon) {
        this.weight = weight;
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.icon = icon;
    }

    public int getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TextColor getColor() {
        return TextColor.fromHexString(colorHex);
    }

    public String getIcon() {
        return icon;
    }

    /** Busca un rango por su nombre de enum o por su displayName (ej. "vip" o "Vip"). */
    public static Rank fromString(String input) {
        if (input == null) return null;
        for (Rank rank : values()) {
            if (rank.name().equalsIgnoreCase(input) || rank.displayName.equalsIgnoreCase(input)) {
                return rank;
            }
        }
        return null;
    }
}
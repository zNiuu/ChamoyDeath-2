package io.github.zniuu.chamoydeath.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class TotemManager {

    private static final Map<String, Double> BONUS_VIDA = Map.of(
            "diamond_totem", 4.0,   // +2 corazones = +4 puntos de vida
            "netherite_totem", 8.0, // +4 corazones
            "evoker_totem", 10.0,   // +5 corazones
            "demonic_totem", 12.0   // +6 corazones
    );

    private static final String CMD_DEMONIC = "demonic_totem";

    private final NamespacedKey llaveVida;

    public TotemManager(JavaPlugin plugin) {
        this.llaveVida = new NamespacedKey(plugin, "totem_heart_bonus");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    actualizarJugador(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void actualizarJugador(Player player) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        String cmd = obtenerCustomModelData(offhand);

        Double bonus = (offhand.getType() == Material.TOTEM_OF_UNDYING && cmd != null)
                ? BONUS_VIDA.get(cmd)
                : null;

        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        AttributeModifier existente = attr.getModifiers().stream()
                .filter(m -> m.getKey().equals(llaveVida))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            attr.removeModifier(existente);
        }

        if (bonus != null) {
            attr.addModifier(new AttributeModifier(llaveVida, bonus, AttributeModifier.Operation.ADD_NUMBER));

            double max = attr.getValue();
            if (player.getHealth() > max) {
                player.setHealth(max);
            }
        }

        // Solo el Demonic Totem da velocidad e inmunidad al fuego, como pasivo propio
        if (CMD_DEMONIC.equals(cmd) && offhand.getType() == Material.TOTEM_OF_UNDYING) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15, 0, false, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 15, 0, false, false, false));
        }
    }

    private String obtenerCustomModelData(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        var strings = meta.getCustomModelDataComponent().getStrings();
        if (strings.isEmpty()) return null;

        return strings.get(0);
    }
}
package io.github.zniuu.chamoydeath.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class RelicListener implements Listener {

    private static final int DURACION_COOLDOWN_SEGUNDOS = 5 * 60;

    private final RelicCooldownManager cooldownManager;

    public RelicListener(RelicCooldownManager cooldownManager) {
        this.cooldownManager = cooldownManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ECHO_SHARD) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> strings = meta.getCustomModelDataComponent().getStrings();
        if (strings.isEmpty() || !strings.get(0).equals("ancest_sculk")) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        if (cooldownManager.enCooldown(player.getUniqueId())) {
            int segundos = cooldownManager.segundosRestantes(player.getUniqueId());
            player.sendActionBar(Component.text("Reliquia del Warden en cooldown: " + segundos + "s", NamedTextColor.RED));
            return;
        }

        aplicarEfectos(player);
        reproducirSonido();

        cooldownManager.activarCooldown(player.getUniqueId(), DURACION_COOLDOWN_SEGUNDOS);
        player.setCooldown(Material.ECHO_SHARD, DURACION_COOLDOWN_SEGUNDOS * 20);

        player.sendMessage(Component.text("Activaste la Reliquia del Warden.", NamedTextColor.LIGHT_PURPLE));
    }

    /** Si el jugador entra con cooldown pendiente, se lo volvemos a mostrar visualmente en el ítem. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int segundosRestantes = cooldownManager.segundosRestantes(player.getUniqueId());
        if (segundosRestantes > 0) {
            player.setCooldown(Material.ECHO_SHARD, segundosRestantes * 20);
        }
    }

    private void aplicarEfectos(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 15 * 20, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 30 * 20, 2, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 1, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1 * 20, 99, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 15 * 20, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 20, 1, false, true, true));
    }

    private void reproducirSonido() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(online.getLocation(), "minecraft:entity.guardian.death", SoundCategory.MASTER, 100f, 0.5f);
        }
    }
}
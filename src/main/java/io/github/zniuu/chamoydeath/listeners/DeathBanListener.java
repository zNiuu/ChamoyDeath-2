package io.github.zniuu.chamoydeath.listeners;

import io.github.zniuu.chamoydeath.StormManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class DeathBanListener implements Listener {

    private final JavaPlugin plugin;
    private final StormManager stormManager;

    public DeathBanListener(JavaPlugin plugin, StormManager stormManager) {
        this.plugin = plugin;
        this.stormManager = stormManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        Component emoji = Component.text("🅰");

        Title title = Title.title(
                emoji,
                Component.empty(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(500))
        );

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showTitle(title);
            online.playSound(deathLocation, "minecraft:custom.chamoydead", SoundCategory.AMBIENT, 3.0f, 1.0f);
        }

        player.getWorld().spawnParticle(Particle.FLAME, deathLocation, 50, 0.5, 1, 0.5, 0.05);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOp()) {
                    String razon = "Baneado automáticamente al morir";
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), razon, null, "ChamoyDeath");

                    if (player.isOnline()) {
                        player.kick(Component.text("§c" + razon));
                    }
                }

                int minSegundos = 30 * 60;
                int maxSegundos = 60 * 60;
                int duracion = ThreadLocalRandom.current().nextInt(minSegundos, maxSegundos + 1);
                stormManager.iniciarOSumarTormenta(player.getWorld(), duracion);
            }
        }.runTaskLater(plugin, 100L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        stormManager.mostrarBossBarSiHay(event.getPlayer());
    }
}
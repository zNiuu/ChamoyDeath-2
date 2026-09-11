package io.github.zniuu.chamoydeath;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class StormManager {

    private final JavaPlugin plugin;
    private final Map<World, StormState> tormentasActivas = new HashMap<>();

    public StormManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static class StormState {
        BossBar bossBar;
        int restanteSegundos;
        int totalSegundos;
        BukkitTask tarea;
    }

    public boolean hayTormentaActiva(World world) {
        return tormentasActivas.containsKey(world);
    }

    public void iniciarOSumarTormenta(World world, int nuevaDuracionSegundos) {
        StormState estado = tormentasActivas.get(world);

        reproducirSonidoInicioTormenta();

        if (estado != null) {
            estado.restanteSegundos += nuevaDuracionSegundos;
            estado.totalSegundos = estado.restanteSegundos;

            int duracionTicks = estado.restanteSegundos * 20;
            world.setWeatherDuration(duracionTicks);
            world.setThunderDuration(duracionTicks);

            Title stormTitle = Title.title(
                    Component.text("Cʜᴀᴍᴏʏ Rᴀɪɴ"),
                    Component.text("+" + formatearTiempo(nuevaDuracionSegundos) + " (" + formatearTiempo(estado.restanteSegundos) + ")"),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            );
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showTitle(stormTitle);
            }
        } else {
            world.setStorm(true);
            world.setThundering(true);
            int duracionTicks = nuevaDuracionSegundos * 20;
            world.setWeatherDuration(duracionTicks);
            world.setThunderDuration(duracionTicks);

            Title stormTitle = Title.title(
                    Component.text("Cʜᴀᴍᴏʏ Rᴀɪɴ"),
                    Component.text(formatearTiempo(nuevaDuracionSegundos)),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            );
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showTitle(stormTitle);
            }

            StormState nuevoEstado = new StormState();
            nuevoEstado.restanteSegundos = nuevaDuracionSegundos;
            nuevoEstado.totalSegundos = nuevaDuracionSegundos;
            nuevoEstado.bossBar = BossBar.bossBar(
                    Component.text("Cʜᴀᴍᴏʏ Rᴀɪɴ - " + formatearTiempo(nuevaDuracionSegundos)),
                    1.0f,
                    BossBar.Color.BLUE,
                    BossBar.Overlay.PROGRESS
            );

            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showBossBar(nuevoEstado.bossBar);
            }

            tormentasActivas.put(world, nuevoEstado);

            nuevoEstado.tarea = new BukkitRunnable() {
                @Override
                public void run() {
                    StormState actual = tormentasActivas.get(world);
                    if (actual == null) {
                        this.cancel();
                        return;
                    }

                    if (actual.restanteSegundos <= 0 || !world.hasStorm()) {
                        finalizarTormenta(world);
                        this.cancel();
                        return;
                    }

                    actual.bossBar.name(Component.text("Cʜᴀᴍᴏʏ Rᴀɪɴ - " + formatearTiempo(actual.restanteSegundos)));
                    actual.bossBar.progress(Math.max(0f, Math.min(1f, (float) actual.restanteSegundos / actual.totalSegundos)));

                    for (Monster mob : world.getEntitiesByClass(Monster.class)) {
                        if (mob instanceof Creeper) {
                            continue;
                        }

                        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 0, false, false));
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, false, false));
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false));
                    }

                    actual.restanteSegundos--;
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    public void finalizarTormenta(World world) {
        StormState estado = tormentasActivas.get(world);
        if (estado == null) return;

        world.setStorm(false);
        world.setThundering(false);

        if (estado.tarea != null) {
            estado.tarea.cancel();
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.hideBossBar(estado.bossBar);
            online.sendMessage(Component.text("[ChamoyGod] El tormento del chamoy a finalizado", NamedTextColor.GRAY));
        }

        reproducirSonidoFinTormenta();

        tormentasActivas.remove(world);
    }

    private void reproducirSonidoInicioTormenta() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(online.getLocation(), "minecraft:entity.breeze.jump", SoundCategory.MASTER, 1000f, 0.3f);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), "minecraft:entity.breeze.jump", SoundCategory.MASTER, 1000f, 0.3f);
                online.playSound(online.getLocation(), "minecraft:entity.breeze.death", SoundCategory.MASTER, 1000f, 0.6f);
                online.playSound(online.getLocation(), "minecraft:entity.warden.death", SoundCategory.AMBIENT, 3f, 0.6f);
            }
        }, 10L);
    }

    private void reproducirSonidoFinTormenta() {
        for (int i = 0; i < 3; i++) {
            long delay = i * 10L;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.playSound(online.getLocation(), "minecraft:block.bell.use", SoundCategory.AMBIENT, 100f, 1.3f);
                }
            }, delay);
        }
    }

    public void mostrarBossBarSiHay(Player player) {
        StormState estado = tormentasActivas.get(player.getWorld());
        if (estado != null) {
            player.showBossBar(estado.bossBar);
        }
    }

    public String formatearTiempo(int segundos) {
        int min = segundos / 60;
        int seg = segundos % 60;
        return String.format("%02d:%02d", min, seg);
    }
}
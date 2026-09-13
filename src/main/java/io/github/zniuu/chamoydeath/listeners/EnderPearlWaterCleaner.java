package io.github.zniuu.chamoydeath.listeners;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EnderPearl;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Las perlas de ender arrojadas normalmente teletransportan al golpear un bloque
 * sólido, pero si caen en agua profunda pueden quedar flotando/atascadas sin
 * llegar nunca a tocar fondo, acumulándose como entidades fantasma. Esta clase
 * revisa cada segundo todas las perlas en vuelo y elimina las que llevan más de
 * 5 segundos seguidos dentro del agua.
 */
public class EnderPearlWaterCleaner {

    private static final int SEGUNDOS_LIMITE = 5;

    private final Map<UUID, Integer> segundosEnAgua = new HashMap<>();

    public EnderPearlWaterCleaner(JavaPlugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, this::revisar, 20L, 20L);
    }

    private void revisar() {
        Map<UUID, Integer> actualizado = new HashMap<>();

        for (World world : Bukkit.getWorlds()) {
            for (EnderPearl perla : world.getEntitiesByClass(EnderPearl.class)) {
                if (!perla.isInWater()) continue;

                int segundos = segundosEnAgua.getOrDefault(perla.getUniqueId(), 0) + 1;

                if (segundos >= SEGUNDOS_LIMITE) {
                    perla.remove();
                    continue;
                }

                actualizado.put(perla.getUniqueId(), segundos);
            }
        }

        // Solo se conservan las perlas que siguen en agua; las demás se olvidan solas.
        segundosEnAgua.clear();
        segundosEnAgua.putAll(actualizado);
    }
}
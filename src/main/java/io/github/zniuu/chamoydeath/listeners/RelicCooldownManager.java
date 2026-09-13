package io.github.zniuu.chamoydeath.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cooldown persistente de la Reliquia del Warden. A diferencia de
 * Player#setCooldown (que es solo visual y se pierde al desconectar),
 * esto guarda la fecha de fin en un archivo para que sobreviva
 * relogs y reinicios del servidor.
 */
public class RelicCooldownManager {

    private final Map<UUID, Long> finDeCooldown = new HashMap<>();
    private File archivo;

    public boolean enCooldown(UUID uuid) {
        Long fin = finDeCooldown.get(uuid);
        return fin != null && fin > System.currentTimeMillis();
    }

    /** Segundos restantes de cooldown (0 si ya puede usarla). */
    public int segundosRestantes(UUID uuid) {
        Long fin = finDeCooldown.get(uuid);
        if (fin == null) return 0;
        long restanteMs = fin - System.currentTimeMillis();
        return restanteMs <= 0 ? 0 : (int) Math.ceil(restanteMs / 1000.0);
    }

    public void activarCooldown(UUID uuid, int segundos) {
        finDeCooldown.put(uuid, System.currentTimeMillis() + segundos * 1000L);
        // Se guarda al toque para no depender de que el server se apague bien.
        if (archivo != null) {
            save(archivo);
        }
    }

    public void load(File file) {
        this.archivo = file;
        finDeCooldown.clear();
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("cooldowns")) return;

        for (String uuidStr : config.getConfigurationSection("cooldowns").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                long fin = config.getLong("cooldowns." + uuidStr);
                finDeCooldown.put(uuid, fin);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save(File file) {
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Long> entry : finDeCooldown.entrySet()) {
            config.set("cooldowns." + entry.getKey(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

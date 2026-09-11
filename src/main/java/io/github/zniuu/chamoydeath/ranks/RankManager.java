package io.github.zniuu.chamoydeath.ranks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RankManager {

    public static final Rank RANGO_POR_DEFECTO = Rank.VIVO;

    private final Map<UUID, Rank> rangos = new HashMap<>();

    public Rank getRank(UUID uuid) {
        return rangos.getOrDefault(uuid, RANGO_POR_DEFECTO);
    }

    public boolean tieneRangoAsignado(UUID uuid) {
        return rangos.containsKey(uuid);
    }

    public void setRank(UUID uuid, Rank rank) {
        rangos.put(uuid, rank);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            actualizarVisual(player);
        }
    }

    public Component nombreConRango(Player player) {
        Rank rank = getRank(player.getUniqueId());
        return Component.text("[", rank.getColor())
                .append(Component.text(rank.getIcon(), NamedTextColor.WHITE))
                .append(Component.text("] ", rank.getColor()))
                .append(Component.text(player.getName(), rank.getColor()));
    }

    public void actualizarVisual(Player player) {
        Rank rank = getRank(player.getUniqueId());
        player.playerListName(nombreConRango(player));

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Rank r : Rank.values()) {
            Team viejo = scoreboard.getTeam(teamKey(r));
            if (viejo != null) viejo.removeEntry(player.getName());
        }

        Team team = scoreboard.getTeam(teamKey(rank));
        if (team == null) {
            team = scoreboard.registerNewTeam(teamKey(rank));
        }
        team.addEntry(player.getName());
    }

    private String teamKey(Rank rank) {
        return rank.getWeight() + "_" + rank.name();
    }

    public void load(File file) {
        rangos.clear();
        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("ranks")) return;

        for (String uuidStr : config.getConfigurationSection("ranks").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Rank rank = Rank.fromString(config.getString("ranks." + uuidStr));
                if (rank != null) rangos.put(uuid, rank);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void save(File file) {
        FileConfiguration config = new YamlConfiguration();
        for (Map.Entry<UUID, Rank> entry : rangos.entrySet()) {
            config.set("ranks." + entry.getKey(), entry.getValue().name());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
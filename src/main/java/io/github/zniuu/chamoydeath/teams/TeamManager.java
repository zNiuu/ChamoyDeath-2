package io.github.zniuu.chamoydeath.teams;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TeamManager {

    private final Map<String, PlayerTeam> teams = new LinkedHashMap<>();
    private final Map<UUID, PlayerTeam> playerTeamMap = new HashMap<>();

    public PlayerTeam createTeam(String name, TextColor color) {
        PlayerTeam team = new PlayerTeam(name, color);
        teams.put(name.toLowerCase(), team);
        return team;
    }

    public boolean deleteTeam(String name) {
        PlayerTeam removed = teams.remove(name.toLowerCase());
        if (removed == null) return false;
        removed.getMembers().forEach(playerTeamMap::remove);
        return true;
    }

    public Optional<PlayerTeam> getTeam(String name) {
        return Optional.ofNullable(teams.get(name.toLowerCase()));
    }

    public Collection<PlayerTeam> getAllTeams() {
        return teams.values();
    }

    public void addPlayerToTeam(UUID uuid, PlayerTeam team) {
        removePlayerFromCurrentTeam(uuid);
        team.addMember(uuid);
        playerTeamMap.put(uuid, team);
    }

    public void removePlayerFromCurrentTeam(UUID uuid) {
        PlayerTeam current = playerTeamMap.remove(uuid);
        if (current != null) current.removeMember(uuid);
    }

    public Optional<PlayerTeam> getPlayerTeam(UUID uuid) {
        return Optional.ofNullable(playerTeamMap.get(uuid));
    }

    public static TextColor parseColor(String input) {
        if (input.startsWith("#")) {
            return TextColor.fromHexString(input);
        }
        NamedTextColor named = NamedTextColor.NAMES.value(input.toLowerCase());
        if (named != null) return named;
        return TextColor.fromHexString("#" + input);
    }

    public void load(File file) {
        teams.clear();
        playerTeamMap.clear();

        if (!file.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("teams")) return;

        for (String key : config.getConfigurationSection("teams").getKeys(false)) {
            String path = "teams." + key;
            String displayName = config.getString(path + ".displayName", key);
            String colorHex = config.getString(path + ".color", "#FFFFFF");

            TextColor color = TextColor.fromHexString(colorHex);
            if (color == null) color = NamedTextColor.WHITE;

            PlayerTeam team = createTeam(displayName, color);

            for (String uuidStr : config.getStringList(path + ".members")) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    team.addMember(uuid);
                    playerTeamMap.put(uuid, team);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public void save(File file) {
        FileConfiguration config = new YamlConfiguration();

        for (PlayerTeam team : teams.values()) {
            String path = "teams." + team.getName().toLowerCase();
            config.set(path + ".displayName", team.getName());
            config.set(path + ".color", team.getColor().asHexString());

            List<String> memberUuids = new ArrayList<>();
            for (UUID uuid : team.getMembers()) memberUuids.add(uuid.toString());
            config.set(path + ".members", memberUuids);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
package io.github.zniuu.chamoydeath;

import io.github.zniuu.chamoydeath.chat.ChatModeManager;
import io.github.zniuu.chamoydeath.listeners.ChatListener;
import io.github.zniuu.chamoydeath.listeners.DeathBanListener;
import io.github.zniuu.chamoydeath.listeners.PlayerListener;
import io.github.zniuu.chamoydeath.ranks.RankManager;
import io.github.zniuu.chamoydeath.teams.TeamManager;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChamoyDeath extends JavaPlugin {

    private TeamManager teamManager;
    private ChatModeManager chatModeManager;
    private RankManager rankManager;

    @Override
    public void onEnable() {
        System.out.println("ChamoyDeath activado");

        saveDefaultConfig();

        getCommand("Ejemplo").setExecutor(new EjemploCMD());

        StormManager stormManager = new StormManager(this);
        DiscordNotifier discordNotifier = new DiscordNotifier(this);

        getServer().getPluginManager().registerEvents(new DeathBanListener(this, stormManager, discordNotifier), this);

        getDataFolder().mkdirs();

        teamManager = new TeamManager();
        teamManager.load(new File(getDataFolder(), "teams.yml"));

        if (teamManager.getAllTeams().isEmpty()) {
            teamManager.createTeam("Azules", TextColor.fromHexString("#4498DB"));
        }

        chatModeManager = new ChatModeManager();

        // --- Rangos (ahora depende de teamManager para mostrar el team al lado del nombre) ---
        rankManager = new RankManager(teamManager);
        rankManager.load(new File(getDataFolder(), "ranks.yml"));

        getServer().getPluginManager().registerEvents(new PlayerListener(rankManager), this);

        getServer().getOnlinePlayers().forEach(rankManager::actualizarVisual);

        getServer().getPluginManager().registerEvents(new ChatListener(teamManager, chatModeManager, rankManager), this);

        StaffCommand staffCommand = new StaffCommand(stormManager, teamManager, rankManager);
        getCommand("staff").setExecutor(staffCommand);
        getCommand("staff").setTabCompleter(staffCommand);

        CDCommand cdCommand = new CDCommand(chatModeManager);
        getCommand("cd").setExecutor(cdCommand);
        getCommand("cd").setTabCompleter(cdCommand);

        TeamCommand teamCommand = new TeamCommand(teamManager, rankManager);
        getCommand("cteams").setExecutor(teamCommand);
        getCommand("cteams").setTabCompleter(teamCommand);
    }

    @Override
    public void onDisable() {
        System.out.println("ChamoyDeath desactivado");

        if (teamManager != null) {
            teamManager.save(new File(getDataFolder(), "teams.yml"));
        }
        if (rankManager != null) {
            rankManager.save(new File(getDataFolder(), "ranks.yml"));
        }
    }
}
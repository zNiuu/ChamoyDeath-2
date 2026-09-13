package io.github.zniuu.chamoydeath;

import io.github.zniuu.chamoydeath.chat.ChatModeManager;
import io.github.zniuu.chamoydeath.items.TotemManager;
import io.github.zniuu.chamoydeath.listeners.AnimalRavagerListener;
import io.github.zniuu.chamoydeath.listeners.ChatListener;
import io.github.zniuu.chamoydeath.listeners.DeathBanListener;
import io.github.zniuu.chamoydeath.listeners.EnderPearlWaterCleaner;
import io.github.zniuu.chamoydeath.listeners.GiveGUIListener;
import io.github.zniuu.chamoydeath.listeners.PlayerListener;
import io.github.zniuu.chamoydeath.listeners.RelicCooldownManager;
import io.github.zniuu.chamoydeath.listeners.RelicListener;
import io.github.zniuu.chamoydeath.missions.MisionGUI;
import io.github.zniuu.chamoydeath.missions.MisionManager;
import io.github.zniuu.chamoydeath.mobs.SculkMobListener;
import io.github.zniuu.chamoydeath.mobs.SculkMobManager;
import io.github.zniuu.chamoydeath.ranks.RankManager;
import io.github.zniuu.chamoydeath.teams.TeamManager;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChamoyDeath extends JavaPlugin {

    private TeamManager teamManager;
    private ChatModeManager chatModeManager;
    private RankManager rankManager;
    private RelicCooldownManager relicCooldownManager;

    @Override
    public void onEnable() {
        System.out.println("ChamoyDeath activado");

        saveDefaultConfig();

        getCommand("Ejemplo").setExecutor(new EjemploCMD());

        StormManager stormManager = new StormManager(this);
        DiscordNotifier discordNotifier = new DiscordNotifier(this);

        getDataFolder().mkdirs();

        relicCooldownManager = new RelicCooldownManager();
        relicCooldownManager.load(new File(getDataFolder(), "relic_cooldowns.yml"));

        getServer().getPluginManager().registerEvents(new DeathBanListener(this, stormManager, discordNotifier), this);
        getServer().getPluginManager().registerEvents(new GiveGUIListener(), this);
        getServer().getPluginManager().registerEvents(new RelicListener(relicCooldownManager), this);

        SculkMobManager sculkMobManager = new SculkMobManager(this);
        getServer().getPluginManager().registerEvents(new SculkMobListener(this, sculkMobManager), this);

        new TotemManager(this);

        teamManager = new TeamManager();
        teamManager.load(new File(getDataFolder(), "teams.yml"));

        if (teamManager.getAllTeams().isEmpty()) {
            teamManager.createTeam("Azules", TextColor.fromHexString("#4498DB"));
        }

        chatModeManager = new ChatModeManager();

        // --- Rangos ---
        rankManager = new RankManager(teamManager);
        rankManager.load(new File(getDataFolder(), "ranks.yml"));

        getServer().getPluginManager().registerEvents(new PlayerListener(rankManager), this);

        getServer().getOnlinePlayers().forEach(rankManager::actualizarVisual);

        getServer().getPluginManager().registerEvents(new ChatListener(teamManager, chatModeManager, rankManager), this);

        StaffCommand staffCommand = new StaffCommand(stormManager, teamManager, rankManager, sculkMobManager);
        getCommand("staff").setExecutor(staffCommand);
        getCommand("staff").setTabCompleter(staffCommand);

        CDCommand cdCommand = new CDCommand(chatModeManager);
        getCommand("cd").setExecutor(cdCommand);
        getCommand("cd").setTabCompleter(cdCommand);

        TeamCommand teamCommand = new TeamCommand(teamManager, rankManager);
        getCommand("cteams").setExecutor(teamCommand);
        getCommand("cteams").setTabCompleter(teamCommand);

        // --- Misiones ---
        MisionManager misionManager = new MisionManager(this);
        MisionGUI misionGUI = new MisionGUI(misionManager);
        getServer().getPluginManager().registerEvents(misionGUI, this);
        getCommand("misiones").setExecutor(new MisionCommand(misionGUI));

        // --- Animales -> Ravager salvaje (10%) ---
        getServer().getPluginManager().registerEvents(new AnimalRavagerListener(), this);

        // --- Limpieza de perlas de ender atascadas en agua ---
        new EnderPearlWaterCleaner(this);
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
        if (relicCooldownManager != null) {
            relicCooldownManager.save(new File(getDataFolder(), "relic_cooldowns.yml"));
        }
    }
}
package io.github.zniuu.chamoydeath;

import io.github.zniuu.chamoydeath.listeners.DeathBanListener;
import org.bukkit.plugin.java.JavaPlugin;

public class ChamoyDeath extends JavaPlugin {

    @Override
    public void onEnable() {
        System.out.println("ChamoyDeath activado");

        getCommand("Ejemplo").setExecutor(new EjemploCMD());

        StormManager stormManager = new StormManager(this);

        getServer().getPluginManager().registerEvents(new DeathBanListener(this, stormManager), this);
        getCommand("staff").setExecutor(new StaffCommand(stormManager));
    }

    @Override
    public void onDisable() {
        System.out.println("ChamoyDeath desactivado");
    }
}


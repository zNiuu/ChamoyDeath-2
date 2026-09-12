package io.github.zniuu.chamoydeath;

import io.github.zniuu.chamoydeath.missions.MisionGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MisionCommand implements CommandExecutor {

    private final MisionGUI misionGUI;

    public MisionCommand(MisionGUI misionGUI) {
        this.misionGUI = misionGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse en el juego.");
            return true;
        }

        misionGUI.abrir(player);
        return true;
    }
}
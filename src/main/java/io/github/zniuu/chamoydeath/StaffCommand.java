package io.github.zniuu.chamoydeath;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffCommand implements CommandExecutor {

    private final StormManager stormManager;

    public StaffCommand(StormManager stormManager) {
        this.stormManager = stormManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("No tenés permiso para usar este comando.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("chamoyrain")) {
            sender.sendMessage(Component.text("Uso: /staff chamoyrain <on <minutos>|off>", NamedTextColor.GRAY));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso: /staff chamoyrain <on <minutos>|off>", NamedTextColor.GRAY));
            return true;
        }

        World world = (sender instanceof Player p) ? p.getWorld() : Bukkit.getWorlds().get(0);

        if (args[1].equalsIgnoreCase("on")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Especificá el tiempo en minutos. Ej: /staff chamoyrain on 30", NamedTextColor.GRAY));
                return true;
            }

            int minutos;
            try {
                minutos = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("El tiempo tiene que ser un número entero de minutos.", NamedTextColor.RED));
                return true;
            }

            if (minutos <= 0) {
                sender.sendMessage(Component.text("El tiempo tiene que ser mayor a 0.", NamedTextColor.RED));
                return true;
            }

            stormManager.iniciarOSumarTormenta(world, minutos * 60);
            sender.sendMessage(Component.text("Tormenta activada/extendida por " + minutos + " minutos.", NamedTextColor.GRAY));
            return true;
        }

        if (args[1].equalsIgnoreCase("off")) {
            if (!stormManager.hayTormentaActiva(world)) {
                sender.sendMessage(Component.text("No hay ninguna tormenta activa en este mundo.", NamedTextColor.GRAY));
                return true;
            }
            stormManager.finalizarTormenta(world);
            return true;
        }

        sender.sendMessage(Component.text("Uso: /staff chamoyrain <on <minutos>|off>", NamedTextColor.GRAY));
        return true;
    }
}
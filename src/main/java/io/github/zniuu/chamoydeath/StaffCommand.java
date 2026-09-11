package io.github.zniuu.chamoydeath;

import io.github.zniuu.chamoydeath.ranks.Rank;
import io.github.zniuu.chamoydeath.ranks.RankManager;
import io.github.zniuu.chamoydeath.teams.PlayerTeam;
import io.github.zniuu.chamoydeath.teams.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class StaffCommand implements CommandExecutor, TabCompleter {

    private final StormManager stormManager;
    private final TeamManager teamManager;
    private final RankManager rankManager;

    public StaffCommand(StormManager stormManager, TeamManager teamManager, RankManager rankManager) {
        this.stormManager = stormManager;
        this.teamManager = teamManager;
        this.rankManager = rankManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("No tienes permisos para usar este comando.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "chamoyrain" -> handleChamoyRain(sender, args);
            case "team" -> handleTeam(sender, args);
            case "rank" -> handleRank(sender, args);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== Comandos de /staff ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/staff chamoyrain on <minutos>", NamedTextColor.YELLOW)
                .append(Component.text(" - Inicia o extiende la tormenta", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/staff chamoyrain off", NamedTextColor.YELLOW)
                .append(Component.text(" - Detiene la tormenta activa", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/staff team join <team> <jugador>", NamedTextColor.YELLOW)
                .append(Component.text(" - Añade un jugador a un team", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/staff team list", NamedTextColor.YELLOW)
                .append(Component.text(" - Muestra los teams existentes", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/staff rank give <rango> <jugador>", NamedTextColor.YELLOW)
                .append(Component.text(" - Asigna un rango a un jugador", NamedTextColor.GRAY)));
    }

    private void handleChamoyRain(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso: /staff chamoyrain <on <minutos>|off>", NamedTextColor.GRAY));
            return;
        }

        World world = (sender instanceof Player p) ? p.getWorld() : Bukkit.getWorlds().get(0);

        if (args[1].equalsIgnoreCase("on")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Especificá el tiempo en minutos. Ej: /staff chamoyrain on 30", NamedTextColor.GRAY));
                return;
            }

            int minutos;
            try {
                minutos = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("El tiempo tiene que ser un número entero de minutos.", NamedTextColor.RED));
                return;
            }

            if (minutos <= 0) {
                sender.sendMessage(Component.text("El tiempo tiene que ser mayor a 0.", NamedTextColor.RED));
                return;
            }

            stormManager.iniciarOSumarTormenta(world, minutos * 60);
            sender.sendMessage(Component.text("Tormenta activada/extendida por " + minutos + " minutos.", NamedTextColor.GRAY));
            return;
        }

        if (args[1].equalsIgnoreCase("off")) {
            if (!stormManager.hayTormentaActiva(world)) {
                sender.sendMessage(Component.text("No hay ninguna tormenta activa en este mundo.", NamedTextColor.GRAY));
                return;
            }
            stormManager.finalizarTormenta(world);
            return;
        }

        sender.sendMessage(Component.text("Uso: /staff chamoyrain <on <minutos>|off>", NamedTextColor.GRAY));
    }

    private void handleTeam(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Uso: /staff team <join <team> <jugador>|list>", NamedTextColor.GRAY));
            return;
        }

        if (args[1].equalsIgnoreCase("list")) {
            if (teamManager.getAllTeams().isEmpty()) {
                sender.sendMessage(Component.text("No hay teams creados.", NamedTextColor.GRAY));
                return;
            }
            sender.sendMessage(Component.text("Teams:", NamedTextColor.YELLOW));
            for (PlayerTeam team : teamManager.getAllTeams()) {
                sender.sendMessage(Component.text(
                        "- " + team.getName() + " (" + team.getMembers().size() + " miembros)", team.getColor()));
            }
            return;
        }

        if (args[1].equalsIgnoreCase("join")) {
            if (args.length < 4) {
                sender.sendMessage(Component.text("Uso: /staff team join <team> <jugador>", NamedTextColor.GRAY));
                return;
            }

            Optional<PlayerTeam> teamOpt = teamManager.getTeam(args[2]);
            if (teamOpt.isEmpty()) {
                sender.sendMessage(Component.text("No existe ese team.", NamedTextColor.RED));
                return;
            }

            Player target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage(Component.text("Ese jugador no está online.", NamedTextColor.RED));
                return;
            }

            PlayerTeam team = teamOpt.get();
            teamManager.addPlayerToTeam(target.getUniqueId(), team);

            sender.sendMessage(Component.text(
                    target.getName() + " fue añadido al team " + team.getName() + ".", team.getColor()));
            target.sendMessage(Component.text(
                    "Fuiste añadido al team " + team.getName() + ".", team.getColor()));
            return;
        }

        sender.sendMessage(Component.text("Uso: /staff team <join <team> <jugador>|list>", NamedTextColor.GRAY));
    }

    private void handleRank(CommandSender sender, String[] args) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("give")) {
            sender.sendMessage(Component.text("Uso: /staff rank give <rango> <jugador>", NamedTextColor.GRAY));
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(Component.text("Uso: /staff rank give <rango> <jugador>", NamedTextColor.GRAY));
            return;
        }

        Rank rank = Rank.fromString(args[2]);
        if (rank == null) {
            sender.sendMessage(Component.text("Rango invalido. Usa: Owner, Staff, Vip o Vivo.", NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[3]);
        if (target == null) {
            sender.sendMessage(Component.text("Ese jugador no está online.", NamedTextColor.RED));
            return;
        }

        rankManager.setRank(target.getUniqueId(), rank);

        sender.sendMessage(Component.text(
                target.getName() + " ahora tiene el rango " + rank.getIcon() + " " + rank.getDisplayName() + ".",
                rank.getColor()));
        target.sendMessage(Component.text(
                "Tu rango ahora es " + rank.getIcon() + " " + rank.getDisplayName() + ".", rank.getColor()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("chamoyrain", "team", "rank"));
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("chamoyrain")) {
                completions.addAll(Arrays.asList("on", "off"));
            } else if (args[0].equalsIgnoreCase("team")) {
                completions.addAll(Arrays.asList("join", "list"));
            } else if (args[0].equalsIgnoreCase("rank")) {
                completions.add("give");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("team") && args[1].equalsIgnoreCase("join")) {
            completions.addAll(teamManager.getAllTeams().stream()
                    .map(PlayerTeam::getName)
                    .collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("rank") && args[1].equalsIgnoreCase("give")) {
            for (Rank rank : Rank.values()) completions.add(rank.getDisplayName());
        } else if (args.length == 4 && args[0].equalsIgnoreCase("team") && args[1].equalsIgnoreCase("join")) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("rank") && args[1].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        }

        String actual = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(c -> c.toLowerCase().startsWith(actual))
                .collect(Collectors.toList());
    }
}
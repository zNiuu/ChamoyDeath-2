package io.github.zniuu.chamoydeath;

import io.github.zniuu.chamoydeath.teams.PlayerTeam;
import io.github.zniuu.chamoydeath.teams.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TeamCommand implements CommandExecutor, TabCompleter {

    private final TeamManager teamManager;

    public TeamCommand(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse en el juego.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Uso: /cteams <create|delete|join|leave|list>", NamedTextColor.YELLOW));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (!player.isOp()) {
                    player.sendMessage(Component.text("No tienes permisos para crear teams.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(Component.text("Uso: /cteams create <nombre> <color>", NamedTextColor.YELLOW));
                    return true;
                }
                String nombre = args[1];
                if (teamManager.getTeam(nombre).isPresent()) {
                    player.sendMessage(Component.text("Ya existe un team con ese nombre.", NamedTextColor.RED));
                    return true;
                }
                TextColor color = TeamManager.parseColor(args[2]);
                if (color == null) {
                    player.sendMessage(Component.text(
                            "Color inválido. Ej: red, blue, aqua, gold, light_purple, green.", NamedTextColor.RED));
                    return true;
                }
                teamManager.createTeam(nombre, color);
                player.sendMessage(Component.text("Team '" + nombre + "' creado.", NamedTextColor.GREEN));
            }

            case "delete" -> {
                if (!player.isOp()) {
                    player.sendMessage(Component.text("No tienes permisos para borrar teams.", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(Component.text("Uso: /cteams delete <nombre>", NamedTextColor.YELLOW));
                    return true;
                }
                boolean borrado = teamManager.deleteTeam(args[1]);
                player.sendMessage(borrado
                        ? Component.text("Team eliminado.", NamedTextColor.GREEN)
                        : Component.text("No existe ese team.", NamedTextColor.RED));
            }

            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("Uso: /cteams join <nombre>", NamedTextColor.YELLOW));
                    return true;
                }
                Optional<PlayerTeam> teamOpt = teamManager.getTeam(args[1]);
                if (teamOpt.isEmpty()) {
                    player.sendMessage(Component.text("No existe ese team.", NamedTextColor.RED));
                    return true;
                }
                teamManager.addPlayerToTeam(player.getUniqueId(), teamOpt.get());
                player.sendMessage(Component.text(
                        "Te uniste al team " + teamOpt.get().getName() + ".", teamOpt.get().getColor()));
            }

            case "leave" -> {
                if (teamManager.getPlayerTeam(player.getUniqueId()).isEmpty()) {
                    player.sendMessage(Component.text("No perteneces a ningún team.", NamedTextColor.RED));
                    return true;
                }
                teamManager.removePlayerFromCurrentTeam(player.getUniqueId());
                player.sendMessage(Component.text("Saliste de tu team.", NamedTextColor.GRAY));
            }

            case "list" -> {
                if (teamManager.getAllTeams().isEmpty()) {
                    player.sendMessage(Component.text("No hay teams creados.", NamedTextColor.GRAY));
                    return true;
                }
                player.sendMessage(Component.text("Teams:", NamedTextColor.YELLOW));
                for (PlayerTeam team : teamManager.getAllTeams()) {
                    player.sendMessage(Component.text(
                            "- " + team.getName() + " (" + team.getMembers().size() + " miembros)", team.getColor()));
                }
            }

            default -> player.sendMessage(Component.text("Uso: /cteams <create|delete|join|leave|list>", NamedTextColor.YELLOW));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "delete", "join", "leave", "list"));
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("delete"))) {
            for (PlayerTeam team : teamManager.getAllTeams()) completions.add(team.getName());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            completions.addAll(Arrays.asList(
                    "red", "blue", "green", "yellow", "aqua", "gold", "light_purple", "dark_purple", "white", "black", "gray"));
        }
        return completions;
    }
}
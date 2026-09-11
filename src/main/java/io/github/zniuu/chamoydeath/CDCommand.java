package io.github.zniuu.chamoydeath;

import io.github.zniuu.chamoydeath.chat.ChatMode;
import io.github.zniuu.chamoydeath.chat.ChatModeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CDCommand implements CommandExecutor, TabCompleter {

    private final ChatModeManager chatModeManager;

    public CDCommand(ChatModeManager chatModeManager) {
        this.chatModeManager = chatModeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse en el juego.");
            return true;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("chat")) {
            player.sendMessage(Component.text("Uso: /cd chat <global|team>", NamedTextColor.YELLOW));
            return true;
        }

        switch (args[1].toLowerCase()) {
            case "global" -> {
                chatModeManager.setMode(player.getUniqueId(), ChatMode.GLOBAL);
                player.sendMessage(Component.text("Ahora estás hablando en el chat global.", NamedTextColor.GREEN));
            }
            case "team" -> {
                chatModeManager.setMode(player.getUniqueId(), ChatMode.TEAM);
                player.sendMessage(Component.text("Ahora estás hablando solo con tu team.", NamedTextColor.GREEN));
            }
            default -> player.sendMessage(Component.text("Opción inválida. Usa 'global' o 'team'.", NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) completions.add("chat");
        else if (args.length == 2 && args[0].equalsIgnoreCase("chat"))
            completions.addAll(Arrays.asList("global", "team"));
        return completions;
    }
}
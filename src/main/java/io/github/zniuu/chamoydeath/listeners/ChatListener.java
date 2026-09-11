package io.github.zniuu.chamoydeath.listeners;

import io.github.zniuu.chamoydeath.chat.ChatMode;
import io.github.zniuu.chamoydeath.chat.ChatModeManager;
import io.github.zniuu.chamoydeath.ranks.RankManager;
import io.github.zniuu.chamoydeath.teams.PlayerTeam;
import io.github.zniuu.chamoydeath.teams.TeamManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Optional;
import java.util.UUID;

public class ChatListener implements Listener {

    private final TeamManager teamManager;
    private final ChatModeManager chatModeManager;
    private final RankManager rankManager;

    public ChatListener(TeamManager teamManager, ChatModeManager chatModeManager, RankManager rankManager) {
        this.teamManager = teamManager;
        this.chatModeManager = chatModeManager;
        this.rankManager = rankManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Optional<PlayerTeam> teamOpt = teamManager.getPlayerTeam(uuid);
        ChatMode modo = chatModeManager.getMode(uuid);

        Component mensajeOriginal = event.message();

        Component nombreColoreado = rankManager.nombreConRango(player);

        if (modo == ChatMode.TEAM) {
            if (teamOpt.isEmpty()) {
                event.setCancelled(true);
                player.sendMessage(Component.text(
                        "No perteneces a ningún team. Usa /cd chat global.", NamedTextColor.RED));
                return;
            }

            PlayerTeam team = teamOpt.get();
            event.setCancelled(true);

            Component formateado = Component.text("[" + team.getName() + "] ")
                    .append(nombreColoreado)
                    .append(Component.text(": "))
                    .append(mensajeOriginal.color(NamedTextColor.WHITE));

            team.getMembers().forEach(memberUuid -> {
                Player member = player.getServer().getPlayer(memberUuid);
                if (member != null) member.sendMessage(formateado);
            });

        } else {
            event.renderer((source, sourceDisplayName, message, viewer) ->
                    nombreColoreado.append(Component.text(": ")).append(message.color(NamedTextColor.WHITE)));
        }
    }
}
package io.github.zniuu.chamoydeath.listeners;

import io.github.zniuu.chamoydeath.ranks.Rank;
import io.github.zniuu.chamoydeath.ranks.RankManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final RankManager rankManager;

    public PlayerListener(RankManager rankManager) {
        this.rankManager = rankManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!rankManager.tieneRangoAsignado(player.getUniqueId())) {
            rankManager.setRank(player.getUniqueId(), Rank.VIVO);
        } else {
            rankManager.actualizarVisual(player);
        }
    }
}
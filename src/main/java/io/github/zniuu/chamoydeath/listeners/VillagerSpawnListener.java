package io.github.zniuu.chamoydeath.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.EnumSet;
import java.util.Set;

public class VillagerSpawnListener implements Listener {

    private static final Set<CreatureSpawnEvent.SpawnReason> RAZONES_PERMITIDAS = EnumSet.of(
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
            CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
            CreatureSpawnEvent.SpawnReason.COMMAND
    );

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVillagerSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.VILLAGER) {
            return;
        }

        if (!RAZONES_PERMITIDAS.contains(event.getSpawnReason())) {
            event.setCancelled(true);
        }
    }
}

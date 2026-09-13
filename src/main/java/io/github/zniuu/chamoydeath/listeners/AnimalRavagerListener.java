package io.github.zniuu.chamoydeath.listeners;

import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 10% de probabilidad de que un animal que spawnea naturalmente sea reemplazado
 * por un Ravager "salvaje" con Fuerza 2 y Velocidad 2 (una emboscada ocasional
 * en vez del animal pasivo normal).
 */
public class AnimalRavagerListener implements Listener {

    private static final double CHANCE_REEMPLAZO = 0.20;

    @EventHandler
    public void onAnimalSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;
        if (!(event.getEntity() instanceof Animals)) return;

        if (ThreadLocalRandom.current().nextDouble() >= CHANCE_REEMPLAZO) return;

        event.setCancelled(true);

        LivingEntity ravager = (LivingEntity) event.getLocation().getWorld()
                .spawnEntity(event.getLocation(), EntityType.RAVAGER);

        ravager.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1, false, false, false));
        ravager.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1, false, false, false));
    }
}
package io.github.zniuu.chamoydeath.mobs;

import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Optional;

/**
 * Los 4 mobs "sculk" del set. Cada uno reutiliza una entidad vanilla como base
 * (se distinguen por PersistentDataContainer + nombre, no son entidades nuevas).
 */
public enum SculkMobType {

    SCULK_SPIDER(EntityType.SPIDER, "Sculk Spider", "sculk_spider", "haunted_echo_shard", 0.50),
    SCULK_ZOMBIE(EntityType.ZOMBIE, "Sculk Zombie", "sculk_zombie", "haunted_echo_shard", 0.50),
    SCULK_CREEPER(EntityType.CREEPER, "Sculk Creeper", "sculk_creeper", "echo_shard_new", 0.75),
    SCULK_SKELETON(EntityType.SKELETON, "Sculk Skeleton", "sculk_skeleton", "haunted_echo_shard", 0.50);

    private final EntityType baseType;
    private final String displayName;
    private final String comandoNombre;
    private final String dropItemId;
    private final double dropChance;

    SculkMobType(EntityType baseType, String displayName, String comandoNombre, String dropItemId, double dropChance) {
        this.baseType = baseType;
        this.displayName = displayName;
        this.comandoNombre = comandoNombre;
        this.dropItemId = dropItemId;
        this.dropChance = dropChance;
    }

    public EntityType getBaseType() {
        return baseType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getComandoNombre() {
        return comandoNombre;
    }

    /** id del GiveItem (en GiveItemsRegistry) que dropea este mob. */
    public String getDropItemId() {
        return dropItemId;
    }

    public double getDropChance() {
        return dropChance;
    }

    /** Para saber si un EntityType vanilla tiene variante sculk (spawn natural). */
    public static Optional<SculkMobType> fromEntityType(EntityType tipo) {
        return Arrays.stream(values()).filter(t -> t.baseType == tipo).findFirst();
    }

    /** Para /staff summon <nombre>. */
    public static Optional<SculkMobType> fromComandoNombre(String nombre) {
        return Arrays.stream(values()).filter(t -> t.comandoNombre.equalsIgnoreCase(nombre)).findFirst();
    }
}
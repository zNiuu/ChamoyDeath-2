package io.github.zniuu.chamoydeath.mobs;

import io.github.zniuu.chamoydeath.items.GiveGUI;
import io.github.zniuu.chamoydeath.items.GiveItem;
import io.github.zniuu.chamoydeath.items.GiveItemsRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

public class SculkMobManager {

    /** Probabilidad de que un mob vanilla nazca como su variante sculk al spawnear naturalmente. */
    public static final double CHANCE_REEMPLAZO_NATURAL = 0.25;

    private final NamespacedKey keyTipo;
    private final NamespacedKey keyVelocidadCreeper;

    public SculkMobManager(JavaPlugin plugin) {
        this.keyTipo = new NamespacedKey(plugin, "sculk_mob_type");
        this.keyVelocidadCreeper = new NamespacedKey(plugin, "sculk_creeper_speed_boost");
    }

    public boolean esSculk(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(keyTipo, PersistentDataType.STRING);
    }

    public Optional<SculkMobType> getTipo(LivingEntity entity) {
        String valor = entity.getPersistentDataContainer().get(keyTipo, PersistentDataType.STRING);
        if (valor == null) return Optional.empty();
        try {
            return Optional.of(SculkMobType.valueOf(valor));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Etiqueta una entidad ya spawneada como variante sculk (le pone nombre visible también). */
    public void marcarComoSculk(LivingEntity entity, SculkMobType tipo) {
        entity.getPersistentDataContainer().set(keyTipo, PersistentDataType.STRING, tipo.name());
        entity.customName(Component.text(tipo.getDisplayName(), NamedTextColor.DARK_GRAY));
        entity.setCustomNameVisible(true);
        aplicarEstadisticas(entity, tipo);
    }

    // ---------- Vida, daño, equipo y buffs de cada variante ----------

    private void aplicarEstadisticas(LivingEntity entity, SculkMobType tipo) {
        switch (tipo) {
            case SCULK_ZOMBIE -> aplicarZombie(entity);
            case SCULK_SKELETON -> aplicarSkeleton(entity);
            case SCULK_SPIDER -> aplicarSpider(entity);
            case SCULK_CREEPER -> aplicarCreeper(entity);
        }
    }

    private void setVida(LivingEntity entity, double vida) {
        AttributeInstance attr = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(vida);
        }
        entity.setHealth(vida);
    }

    /** Sculk Zombie: 60 HP (30 corazones), espada de netherite Sharpness 5, 5 de daño base. */
    private void aplicarZombie(LivingEntity entity) {
        setVida(entity, 60.0);

        AttributeInstance dano = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (dano != null) {
            dano.setBaseValue(5.0);
        }

        EntityEquipment equipo = entity.getEquipment();
        if (equipo != null) {
            equipo.clear();

            ItemStack espada = new ItemStack(Material.NETHERITE_SWORD);
            espada.addUnsafeEnchantment(Enchantment.SHARPNESS, 5);
            equipo.setItemInMainHand(espada);
            equipo.setItemInMainHandDropChance(0f);
        }

        aplicarResistencia1(entity);
    }

    /** Sculk Skeleton: 40 HP (20 corazones), arco Power 10 (sin armadura). */
    private void aplicarSkeleton(LivingEntity entity) {
        setVida(entity, 40.0);

        EntityEquipment equipo = entity.getEquipment();
        if (equipo != null) {
            equipo.clear();

            ItemStack arco = new ItemStack(Material.BOW);
            arco.addUnsafeEnchantment(Enchantment.POWER, 10);
            equipo.setItemInMainHand(arco);
            equipo.setItemInMainHandDropChance(0f);
        }

        aplicarResistencia1(entity);
    }

    /** Sculk Spider: 45 HP, Speed 2 + Strength 2 permanentes, 10 de daño base (antes del bonus de Strength). */
    private void aplicarSpider(LivingEntity entity) {
        setVida(entity, 45.0);

        AttributeInstance dano = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (dano != null) {
            dano.setBaseValue(10.0);
        }

        entity.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 1, false, false, false));
        entity.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 1, false, false, false));

        aplicarResistencia1(entity);
    }

    /**
     * Resistencia I permanente. NO se usa en el Creeper a propósito: al ser un potion
     * effect real, si se le diera al creeper podría "contagiarse" a quien reciba la
     * explosión (como pasa con otros efectos), y eso rompería el balance de la explosión.
     */
    private void aplicarResistencia1(LivingEntity entity) {
        entity.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 0, false, false, false));
    }

    /**
     * Sculk Creeper: 50 HP, se mueve como con Speed 2 pero SIN el potion effect real
     * (usa un modificador de atributo directo) para que no tenga icono/partículas
     * ni se pueda "contagiar" por la explosión.
     */
    private void aplicarCreeper(LivingEntity entity) {
        setVida(entity, 50.0);

        AttributeInstance velocidad = entity.getAttribute(Attribute.MOVEMENT_SPEED);
        if (velocidad != null) {
            velocidad.getModifiers().stream()
                    .filter(m -> m.getKey().equals(keyVelocidadCreeper))
                    .findFirst()
                    .ifPresent(velocidad::removeModifier);

            // Speed II vanilla equivale a +40% de velocidad (multiplicativo sobre el total).
            velocidad.addModifier(new AttributeModifier(
                    keyVelocidadCreeper, 0.40, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
        }
    }

    /** Invoca un mob sculk nuevo en una ubicación (usado por /staff summon). */
    public LivingEntity invocar(Location location, SculkMobType tipo) {
        LivingEntity entidad = (LivingEntity) location.getWorld().spawnEntity(location, tipo.getBaseType());
        marcarComoSculk(entidad, tipo);
        return entidad;
    }

    /** Arma el ItemStack de drop (echo fragment/powder) para un tipo de mob sculk. */
    public ItemStack construirDropItem(SculkMobType tipo) {
        Optional<GiveItem> item = GiveItemsRegistry.getById(tipo.getDropItemId());
        return item.map(GiveGUI::construirItem).orElse(null);
    }
}
package io.github.zniuu.chamoydeath.mobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("ALL")
public class SculkMobListener implements Listener {

    private static final int PODER_EXPLOSION_MAXIMO = 8;
    private static final int RADIO_RESONANCIA = 20;
    private static final int RADIO_CONVERSION_SCULK = 3;
    private static final double DAÑO_SONIC_BOOM = 12.0; // 6 corazones

    private final JavaPlugin plugin;
    private final SculkMobManager sculkMobManager;

    public SculkMobListener(JavaPlugin plugin, SculkMobManager sculkMobManager) {
        this.plugin = plugin;
        this.sculkMobManager = sculkMobManager;
    }

    // ---------- Reemplazo natural (25%) ----------

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        SculkMobType.fromEntityType(event.getEntityType()).ifPresent(tipo -> {
            if (ThreadLocalRandom.current().nextDouble() < SculkMobManager.CHANCE_REEMPLAZO_NATURAL) {
                sculkMobManager.marcarComoSculk(event.getEntity(), tipo);
            }
        });
    }

    // ---------- Resonancia: si uno detecta al jugador, avisa a los demás sculk cerca ----------

    @EventHandler
    public void onResonancia(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Mob origen)) return;
        if (sculkMobManager.getTipo(origen).isEmpty()) return;
        if (!(event.getTarget() instanceof Player jugador)) return;

        for (Entity cercano : origen.getNearbyEntities(RADIO_RESONANCIA, RADIO_RESONANCIA, RADIO_RESONANCIA)) {
            if (!(cercano instanceof Mob otroMob)) continue;
            if (sculkMobManager.getTipo(otroMob).isEmpty()) continue;
            if (otroMob.getTarget() instanceof Player) continue; // ya tiene objetivo

            otroMob.setTarget(jugador); // sin sonido, es una llamada directa a la API
        }
    }

    // ---------- Muerte: drops de echo + esporas del zombie ----------

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entidad = event.getEntity();
        Optional<SculkMobType> tipoOpt = sculkMobManager.getTipo(entidad);
        if (tipoOpt.isEmpty()) return;

        SculkMobType tipo = tipoOpt.get();

        if (ThreadLocalRandom.current().nextDouble() < tipo.getDropChance()) {
            ItemStack drop = sculkMobManager.construirDropItem(tipo);
            if (drop != null) event.getDrops().add(drop);
        }

        if (tipo == SculkMobType.SCULK_ZOMBIE) {
            liberarEsporas(entidad);
        }
    }

    private void liberarEsporas(LivingEntity muerto) {
        Location loc = muerto.getLocation();
        World world = loc.getWorld();
        world.spawnParticle(Particle.SCULK_SOUL, loc, 25, 1, 1, 1, 0.02);
        world.playSound(loc, Sound.BLOCK_SCULK_CATALYST_BLOOM, 1f, 0.7f);

        for (Entity cercano : world.getNearbyEntities(loc, 3, 3, 3)) {
            if (cercano.equals(muerto)) continue;
            if (cercano instanceof LivingEntity victima) {
                victima.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0, false, false));
                victima.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1, false, false));
            }
        }
    }

    // ---------- Explosión del Sculk Creeper: contamina en vez de destruir ----------

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;
        if (sculkMobManager.getTipo(creeper).filter(t -> t == SculkMobType.SCULK_CREEPER).isEmpty()) return;

        event.blockList().clear(); // no destruye nada

        Location centro = event.getLocation();
        World world = centro.getWorld();

        for (int dx = -RADIO_CONVERSION_SCULK; dx <= RADIO_CONVERSION_SCULK; dx++) {
            for (int dy = -RADIO_CONVERSION_SCULK; dy <= RADIO_CONVERSION_SCULK; dy++) {
                for (int dz = -RADIO_CONVERSION_SCULK; dz <= RADIO_CONVERSION_SCULK; dz++) {
                    Block bloque = world.getBlockAt(
                            centro.getBlockX() + dx, centro.getBlockY() + dy, centro.getBlockZ() + dz);
                    if (esConvertibleASculk(bloque.getType())) {
                        bloque.setType(Material.SCULK);
                    }
                }
            }
        }
    }

    private boolean esConvertibleASculk(Material tipo) {
        return switch (tipo) {
            case STONE, DIRT, GRASS_BLOCK, COBBLESTONE, ANDESITE, DIORITE, GRANITE -> true;
            default -> false;
        };
    }

    // ---------- Darkness duplica TODO el daño recibido mientras esté activo ----------

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDañoConDarkness(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victima)) return;
        if (!victima.hasPotionEffect(PotionEffectType.DARKNESS)) return;

        event.setDamage(event.getDamage() * 2.0);
    }

    // ---------- Daño entre entidades: acá vive la mayoría de las habilidades ----------

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damagerRaw = event.getDamager();
        Entity victimRaw = event.getEntity();

        // Inmunidad de mobs sculk al Sonic Boom "real" (del Warden u otro).
        if (event.getCause() == EntityDamageEvent.DamageCause.SONIC_BOOM
                && victimRaw instanceof LivingEntity victimSonic
                && sculkMobManager.esSculk(victimSonic)) {
            event.setCancelled(true);
            return;
        }

        // Mordida del Sculk Spider: darkness + chance de telarañas.
        if (damagerRaw instanceof LivingEntity spider
                && victimRaw instanceof LivingEntity mordido
                && sculkMobManager.getTipo(spider).filter(t -> t == SculkMobType.SCULK_SPIDER).isPresent()) {
            mordido.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 70, 0, false, false));
            if (ThreadLocalRandom.current().nextDouble() < 0.10) {
                colocarCuboTelaranas(mordido.getLocation());
            }
        }

        // Sculk Spider recibe golpe crítico: se teletransporta y deja mancha de sculk.
        if (victimRaw instanceof LivingEntity spiderGolpeada
                && event.isCritical()
                && sculkMobManager.getTipo(spiderGolpeada).filter(t -> t == SculkMobType.SCULK_SPIDER).isPresent()) {
            Bukkit.getScheduler().runTask(plugin, () -> teletransportarSpider(spiderGolpeada));
        }

        // Sculk Zombie golpea: darkness 3s garantizado + 15% de chance de un eco
        // (segundo golpe fantasma 1s después).
        if (damagerRaw instanceof LivingEntity zombie
                && victimRaw instanceof LivingEntity victimaZombie
                && sculkMobManager.getTipo(zombie).filter(t -> t == SculkMobType.SCULK_ZOMBIE).isPresent()) {

            victimaZombie.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, false));

            if (ThreadLocalRandom.current().nextDouble() < 0.15) {
                double dañoEco = event.getFinalDamage();
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (victimaZombie.isValid() && !victimaZombie.isDead()) {
                        victimaZombie.damage(dañoEco, zombie);
                    }
                }, 20L);
            }
        }

        // Flechas de Sculk Skeleton: 17% de aplicar darkness.
        if (damagerRaw instanceof Arrow flecha
                && flecha.getShooter() instanceof LivingEntity tirador
                && victimRaw instanceof LivingEntity victimaFlecha
                && sculkMobManager.getTipo(tirador).filter(t -> t == SculkMobType.SCULK_SKELETON).isPresent()
                && ThreadLocalRandom.current().nextDouble() < 0.17) {
            victimaFlecha.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
        }

        // Explosión de CUALQUIER creeper: Instant Damage IV a quien la reciba.
        if (damagerRaw instanceof Creeper creeperExplota
                && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                && victimRaw instanceof LivingEntity victimaExplosion) {

            victimaExplosion.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 3, false, false));

            // Si además es Sculk Creeper: efecto de "aturdimiento" (darkness + lentitud).
            if (sculkMobManager.getTipo(creeperExplota).filter(t -> t == SculkMobType.SCULK_CREEPER).isPresent()) {
                victimaExplosion.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
                victimaExplosion.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, false));
            }
        }

        // Sculk Creeper "cargándose" con cada golpe recibido (menos por su propia explosión).
        if (victimRaw instanceof Creeper creeperCargando
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                && sculkMobManager.getTipo(creeperCargando).filter(t -> t == SculkMobType.SCULK_CREEPER).isPresent()) {
            cargarCreeper(creeperCargando);
        }
    }

    private void cargarCreeper(Creeper creeper) {
        int actual = creeper.getExplosionRadius();
        if (actual < PODER_EXPLOSION_MAXIMO) {
            creeper.setExplosionRadius(actual + 1);
        }
        creeper.setMaxFuseTicks(Math.min(creeper.getMaxFuseTicks() + 5, 60));
    }

    private void colocarCuboTelaranas(Location centro) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Block bloque = centro.clone().add(dx, dy, dz).getBlock();
                    if (bloque.getType() == Material.AIR) {
                        bloque.setType(Material.COBWEB);
                    }
                }
            }
        }
    }

    private void teletransportarSpider(LivingEntity spider) {
        if (!spider.isValid() || spider.isDead()) return;

        Location origen = spider.getLocation();
        Location destino = buscarTeletransporteSeguro(origen, 5);
        if (destino == null) return;

        Block sueloOrigen = origen.getBlock().getRelative(BlockFace.DOWN);
        if (sueloOrigen.getType().isSolid()) {
            sueloOrigen.setType(Material.SCULK);
        }

        spider.teleport(destino);
        spider.getWorld().spawnParticle(Particle.SCULK_SOUL, origen, 15, 0.3, 0.3, 0.3, 0.02);
        spider.getWorld().playSound(origen, Sound.ENTITY_SHULKER_TELEPORT, 1f, 1.2f);
    }

    private Location buscarTeletransporteSeguro(Location origen, int radio) {
        for (int intento = 0; intento < 8; intento++) {
            double dx = ThreadLocalRandom.current().nextDouble(-radio, radio);
            double dz = ThreadLocalRandom.current().nextDouble(-radio, radio);
            Location candidata = origen.clone().add(dx, 0, dz);

            Block pies = candidata.getBlock();
            Block cabeza = pies.getRelative(BlockFace.UP);
            Block suelo = pies.getRelative(BlockFace.DOWN);

            if (!pies.getType().isSolid() && !cabeza.getType().isSolid() && suelo.getType().isSolid()) {
                candidata.setY(pies.getY());
                return candidata;
            }
        }
        return null;
    }

    // ---------- Sonic Boom del Sculk Skeleton ----------

    @EventHandler
    public void onSculkSkeletonShoot(EntityShootBowEvent event) {
        LivingEntity tirador = event.getEntity();
        if (sculkMobManager.getTipo(tirador).filter(t -> t == SculkMobType.SCULK_SKELETON).isEmpty()) return;

        LivingEntity objetivo = null;
        if (tirador instanceof Mob mobTirador && mobTirador.getTarget() instanceof LivingEntity target) {
            objetivo = target;
        }
        if (objetivo == null) return;

        double distancia = tirador.getLocation().distance(objetivo.getLocation());
        if (distancia < 5) return; // cuerpo a cuerpo: se deja la flecha normal, no es justo ahí

        if (ThreadLocalRandom.current().nextDouble() < 0.25) {
            event.setCancelled(true);
            dispararSonicBoom(tirador, objetivo);
        }
    }

    private void dispararSonicBoom(LivingEntity origen, LivingEntity objetivo) {
        if (sculkMobManager.esSculk(objetivo)) return; // inmunidad entre mobs sculk

        World world = objetivo.getWorld();
        world.playSound(objetivo.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 3f, 1f);
        world.spawnParticle(Particle.SONIC_BOOM, objetivo.getLocation(), 1);

        aplicarDañoVerdadero(objetivo, origen, DAÑO_SONIC_BOOM);

        Vector direccion = objetivo.getLocation().toVector().subtract(origen.getLocation().toVector());
        if (direccion.lengthSquared() > 0) {
            direccion.normalize();
            objetivo.setVelocity(direccion.multiply(1.2).setY(0.4));
        }
    }

    /**
     * Daño "verdadero" que ignora armadura y sus encantamientos, igual que el Sonic
     * Boom real del Warden (solo lo reduce la Resistencia, nunca la armadura).
     * Usamos el DamageType.SONIC_BOOM vanilla a través de DamageSource en vez de
     * armar el EntityDamageByEntityEvent a mano: el constructor viejo basado en
     * Map<DamageModifier,...> está deprecado y en Paper 1.21.5 tira NullPointerException
     * al construirse (bug conocido de esa API vieja). Con DamageSource, el propio juego
     * ya ignora la armadura y aplica la reducción de Resistencia como corresponde,
     * y el evento se dispara normal para que lo vean otros plugins y el indicador de daño.
     */
    private void aplicarDañoVerdadero(LivingEntity victima, Entity causante, double daño) {
        DamageSource fuente = DamageSource.builder(DamageType.SONIC_BOOM)
                .withCausingEntity(causante)
                .withDirectEntity(causante)
                .build();
        victima.damage(daño, fuente);
    }
}
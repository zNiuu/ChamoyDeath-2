package io.github.zniuu.chamoydeath.listeners;

import io.github.zniuu.chamoydeath.DiscordNotifier;
import io.github.zniuu.chamoydeath.StormManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class DeathBanListener implements Listener {

    private final JavaPlugin plugin;
    private final StormManager stormManager;
    private final DiscordNotifier discordNotifier;

    private final Map<UUID, Location> ubicacionesMuerte = new HashMap<>();

    public DeathBanListener(JavaPlugin plugin, StormManager stormManager, DiscordNotifier discordNotifier) {
        this.plugin = plugin;
        this.stormManager = stormManager;
        this.discordNotifier = discordNotifier;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();

        List<ItemStack> loot = new ArrayList<>(event.getDrops());
        event.getDrops().clear();

        colocarCofreConLoot(deathLocation, loot, player);

        ubicacionesMuerte.put(player.getUniqueId(), deathLocation.clone());

        String razon = obtenerRazonMuerte(player);
        anunciarMuerte(player, deathLocation, razon);
        discordNotifier.enviarMuerte(player, deathLocation, razon);

        Component emoji = Component.text("🅰");

        Title title = Title.title(
                emoji,
                Component.empty(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(4), Duration.ofMillis(500))
        );

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showTitle(title);
            online.playSound(deathLocation, "minecraft:custom.chamoydead", SoundCategory.AMBIENT, 3.0f, 1.0f);
        }

        player.getWorld().spawnParticle(Particle.FLAME, deathLocation, 50, 0.5, 1, 0.5, 0.05);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOp()) {
                    String banRazon = "Baneado automáticamente al morir";
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), banRazon, null, "ChamoyDeath");

                    if (player.isOnline()) {
                        player.kick(Component.text("§c¡GG!" + banRazon));
                    }
                }

                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, false));
                    online.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, false));
                    online.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1, false, false));
                }

                int minSegundos = 30 * 60;
                int maxSegundos = 60 * 60;
                int duracion = ThreadLocalRandom.current().nextInt(minSegundos, maxSegundos + 1);
                stormManager.iniciarOSumarTormenta(player.getWorld(), duracion);
            }
        }.runTaskLater(plugin, 100L);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location muerte = ubicacionesMuerte.remove(player.getUniqueId());

        if (muerte != null) {
            event.setRespawnLocation(muerte);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    player.setGameMode(GameMode.SPECTATOR);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        stormManager.mostrarBossBarSiHay(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTotemUse(EntityResurrectEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack totem = obtenerTotemUsado(player);
        String nombreTotem = obtenerNombreItem(totem);

        EntityDamageEvent daño = player.getLastDamageCause();
        String causa = (daño != null) ? razonDesdeEvento(daño) : "causas desconocidas";

        String texto = "[ChamoyGod] El jugador " + player.getName()
                + " a gastado un " + nombreTotem
                + " por " + causa;

        Component mensaje = Component.text(texto, TextColor.fromHexString("#FFE7A9"));

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(mensaje);
        }
    }

    private ItemStack obtenerTotemUsado(Player player) {
        ItemStack mano = player.getInventory().getItemInMainHand();
        if (mano.getType() == Material.TOTEM_OF_UNDYING) return mano;

        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType() == Material.TOTEM_OF_UNDYING) return offhand;

        return null;
    }

    private String obtenerNombreItem(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
        }
        return "Totem de la Inmortalidad";
    }

    private void anunciarMuerte(Player player, Location loc, String razon) {
        String texto = "[ChamoyGod] El jugador " + player.getName() + " ha muerto por " + razon
                + " en las coordenadas X: " + loc.getBlockX()
                + " Y: " + loc.getBlockY()
                + " Z: " + loc.getBlockZ();

        Component mensaje = Component.text(texto, TextColor.fromHexString("#FF505E"));

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(mensaje);
        }
    }

    private String obtenerRazonMuerte(Player player) {
        if (player.getKiller() != null) {
            return "el jugador " + player.getKiller().getName();
        }

        EntityDamageEvent ultimoDaño = player.getLastDamageCause();
        if (ultimoDaño == null) {
            return "causas desconocidas";
        }

        return razonDesdeEvento(ultimoDaño);
    }

    private String razonDesdeEvento(EntityDamageEvent evento) {
        if (evento instanceof EntityDamageByEntityEvent porEntidad) {
            Entity atacante = porEntidad.getDamager();
            if (atacante instanceof Player jugadorAtacante) {
                return "el jugador " + jugadorAtacante.getName();
            }
            if (atacante instanceof LivingEntity) {
                return atacante.getName();
            }
            return "un ataque";
        }

        return switch (evento.getCause()) {
            case FALL -> "caida";
            case DROWNING -> "ahogado";
            case FIRE, FIRE_TICK -> "fuego";
            case LAVA -> "lava";
            case VOID -> "vacio";
            case STARVATION -> "hambre";
            case SUFFOCATION -> "asfixia";
            case LIGHTNING -> "rayo";
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> "explosion";
            case FREEZE -> "congelado";
            case POISON -> "veneno";
            case MAGIC -> "magia";
            default -> "???";
        };
    }

    private void colocarCofresDobles(Location loc) {
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] offset : offsets) {
            Location segundo = loc.clone().add(offset[0], 0, offset[1]);
            if (segundo.getBlock().getType() == Material.AIR) {
                aplicarCofreDoble(loc, segundo, offset[0], offset[1]);
                return;
            }
        }

        loc.getBlock().setType(Material.CHEST);
    }

    private void aplicarCofreDoble(Location original, Location segundo, int dx, int dz) {
        BlockFace facing;
        Chest.Type tipoOriginal;
        Chest.Type tipoSegundo;

        if (dx != 0) {
            facing = BlockFace.SOUTH;
            if (dx > 0) {
                tipoOriginal = Chest.Type.RIGHT;
                tipoSegundo = Chest.Type.LEFT;
            } else {
                tipoOriginal = Chest.Type.LEFT;
                tipoSegundo = Chest.Type.RIGHT;
            }
        } else {
            facing = BlockFace.WEST;
            if (dz > 0) {
                tipoOriginal = Chest.Type.RIGHT;
                tipoSegundo = Chest.Type.LEFT;
            } else {
                tipoOriginal = Chest.Type.LEFT;
                tipoSegundo = Chest.Type.RIGHT;
            }
        }

        Chest dataOriginal = (Chest) Material.CHEST.createBlockData();
        dataOriginal.setFacing(facing);
        dataOriginal.setType(tipoOriginal);

        Chest dataSegundo = (Chest) Material.CHEST.createBlockData();
        dataSegundo.setFacing(facing);
        dataSegundo.setType(tipoSegundo);

        original.getBlock().setBlockData(dataOriginal, false);
        segundo.getBlock().setBlockData(dataSegundo, false);
    }

    private void colocarCofreConLoot(Location loc, List<ItemStack> loot, Player player) {
        colocarCofresDobles(loc);

        if (loc.getBlock().getState() instanceof org.bukkit.block.Chest chest) {
            Inventory inv = chest.getInventory();
            for (ItemStack item : loot) {
                Map<Integer, ItemStack> sobrante = inv.addItem(item);
                for (ItemStack restante : sobrante.values()) {
                    loc.getWorld().dropItemNaturally(loc, restante);
                }
            }
        }

        Location cabezaLoc = loc.clone().add(0, 1, 0);
        if (cabezaLoc.getBlock().getType() == Material.AIR) {
            cabezaLoc.getBlock().setType(Material.PLAYER_HEAD);
            if (cabezaLoc.getBlock().getState() instanceof Skull skull) {
                skull.setOwningPlayer(player);
                skull.update();
            }
        }
    }
}
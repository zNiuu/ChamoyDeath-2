package io.github.zniuu.chamoydeath.missions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Maneja el progreso, los requisitos y las recompensas de las dos misiones
 * del servidor: la obligatoria (set de diamante) y la opcional (caza y
 * coleccionables). Los kills se verifican con las estadísticas propias del
 * juego (no un contador nuestro), así no se pueden falsear.
 */
public class MisionManager {

    private final JavaPlugin plugin;
    private final File archivo;
    private final File archivoProgreso;
    private final Set<UUID> obligatoriaReclamada = new HashSet<>();
    private final Set<UUID> opcionalReclamada = new HashSet<>();

    private static final Material[] SET_DIAMANTE = {
            Material.DIAMOND_HELMET,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_BOOTS,
            Material.DIAMOND_SWORD,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_AXE
    };

    public static final int RAVAGERS_REQUERIDOS = 5;
    public static final int PIGLIN_BRUTES_REQUERIDOS = 5;
    public static final int BREEZES_REQUERIDOS = 10;
    public static final int TOTEMS_REQUERIDOS = 7;
    public static final int LLAVES_REQUERIDAS = 5;

    public MisionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.archivo = new File(plugin.getDataFolder(), "misiones.yml");
        this.archivoProgreso = new File(plugin.getDataFolder(), "progreso.yml");
        cargar();

        // Refresca el progreso de todos los jugadores conectados cada 5 minutos,
        // aunque no hayan abierto el menú de misiones.
        Bukkit.getScheduler().runTaskTimer(plugin, this::actualizarProgresoTodosOnline, 20L * 30, 20L * 60 * 5);
    }

    // ---------- Misión obligatoria: set de diamante ----------

    public int contarPiezasDiamante(Player player) {
        int encontradas = 0;
        for (Material pieza : SET_DIAMANTE) {
            if (contarMaterial(player, pieza) >= 1) encontradas++;
        }
        return encontradas;
    }

    public boolean cumpleObligatoria(Player player) {
        return contarPiezasDiamante(player) == SET_DIAMANTE.length;
    }

    public boolean yaReclamoObligatoria(Player player) {
        return obligatoriaReclamada.contains(player.getUniqueId());
    }

    public ResultadoReclamo reclamarObligatoria(Player player) {
        if (yaReclamoObligatoria(player)) return ResultadoReclamo.YA_RECLAMADA;
        if (!cumpleObligatoria(player)) return ResultadoReclamo.INCOMPLETA;

        List<ItemStack> recompensa = new ArrayList<>();
        recompensa.add(libro("Protección IV / Unbreaking III / Mending",
                enc(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)));
        recompensa.add(libro("Protección IV / Unbreaking III / Mending",
                enc(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)));
        recompensa.add(libro("Prot. IV / Unb. III / Mending / Aqua Affinity / Resp. III",
                enc(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1,
                        Enchantment.AQUA_AFFINITY, 1, Enchantment.RESPIRATION, 3)));
        recompensa.add(libro("Prot. IV / Unb. III / Mending / Feather Falling IV",
                enc(Enchantment.PROTECTION, 4, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1,
                        Enchantment.FEATHER_FALLING, 4)));

        entregar(player, recompensa);
        obligatoriaReclamada.add(player.getUniqueId());
        guardar();
        return ResultadoReclamo.OK;
    }

    // ---------- Misión opcional: cazador experto ----------

    public int killsRavager(Player player) {
        return player.getStatistic(Statistic.KILL_ENTITY, EntityType.RAVAGER);
    }

    public int killsPiglinBrute(Player player) {
        return player.getStatistic(Statistic.KILL_ENTITY, EntityType.PIGLIN_BRUTE);
    }

    public int killsBreeze(Player player) {
        return player.getStatistic(Statistic.KILL_ENTITY, EntityType.BREEZE);
    }

    public int totemsEnInventario(Player player) {
        return contarMaterial(player, Material.TOTEM_OF_UNDYING);
    }

    public int llavesEnInventario(Player player) {
        return contarMaterial(player, Material.OMINOUS_TRIAL_KEY);
    }

    public boolean cumpleOpcional(Player player) {
        return killsRavager(player) >= RAVAGERS_REQUERIDOS
                && killsPiglinBrute(player) >= PIGLIN_BRUTES_REQUERIDOS
                && killsBreeze(player) >= BREEZES_REQUERIDOS
                && totemsEnInventario(player) >= TOTEMS_REQUERIDOS
                && llavesEnInventario(player) >= LLAVES_REQUERIDAS;
    }

    public boolean yaReclamoOpcional(Player player) {
        return opcionalReclamada.contains(player.getUniqueId());
    }

    public ResultadoReclamo reclamarOpcional(Player player) {
        if (yaReclamoOpcional(player)) return ResultadoReclamo.YA_RECLAMADA;
        if (!cumpleOpcional(player)) return ResultadoReclamo.INCOMPLETA;

        List<ItemStack> recompensa = new ArrayList<>();
        recompensa.add(libro("Eficiencia V / Unbreaking III / Mending / Silk Touch",
                enc(Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1,
                        Enchantment.SILK_TOUCH, 1)));
        recompensa.add(libro("Eficiencia V / Unbreaking III / Mending / Fortuna III",
                enc(Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1,
                        Enchantment.FORTUNE, 3)));
        recompensa.add(libro("Eficiencia V / Unbreaking III / Mending",
                enc(Enchantment.EFFICIENCY, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)));
        recompensa.add(libro("Poder V / Infinidad / Unbreaking III / Fuego",
                enc(Enchantment.POWER, 5, Enchantment.INFINITY, 1, Enchantment.UNBREAKING, 3,
                        Enchantment.FLAME, 1)));
        recompensa.add(libro("Looting III / Sharpness V / Unbreaking III / Mending / Sweeping Edge III",
                enc(Enchantment.LOOTING, 3, Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3,
                        Enchantment.MENDING, 1, Enchantment.SWEEPING_EDGE, 3)));
        recompensa.add(new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        recompensa.add(new ItemStack(Material.GOLDEN_APPLE, 10));

        entregar(player, recompensa);
        opcionalReclamada.add(player.getUniqueId());
        guardar();
        return ResultadoReclamo.OK;
    }

    // ---------- Utilidades de inventario ----------

    private int contarMaterial(Player player, Material material) {
        int total = 0;
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == material) total += item.getAmount();
        }
        ItemStack offhand = inv.getItemInOffHand();
        if (offhand.getType() == material) total += offhand.getAmount();
        return total;
    }

    private void entregar(Player player, List<ItemStack> items) {
        Map<Integer, ItemStack> sobrante = player.getInventory().addItem(items.toArray(new ItemStack[0]));
        for (ItemStack item : sobrante.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        player.sendMessage(Component.text("¡Recompensa entregada! Revisá tu inventario.", NamedTextColor.GREEN));
    }

    private Map<Enchantment, Integer> enc(Object... paresEncantoNivel) {
        Map<Enchantment, Integer> mapa = new LinkedHashMap<>();
        for (int i = 0; i < paresEncantoNivel.length; i += 2) {
            mapa.put((Enchantment) paresEncantoNivel[i], (Integer) paresEncantoNivel[i + 1]);
        }
        return mapa;
    }

    private ItemStack libro(String nombre, Map<Enchantment, Integer> encantos) {
        ItemStack libro = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) libro.getItemMeta();
        for (Map.Entry<Enchantment, Integer> entry : encantos.entrySet()) {
            meta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
        }
        meta.displayName(Component.text(nombre, NamedTextColor.AQUA));
        libro.setItemMeta(meta);
        return libro;
    }

    // ---------- progreso.yml: para que el staff pueda ver el avance de cada jugador ----------

    /** Actualiza el progreso de un solo jugador en progreso.yml (se llama al abrir el menú). */
    public void actualizarProgreso(Player player) {
        FileConfiguration config = cargarProgreso();
        escribirProgreso(config, player);
        guardarProgreso(config);
    }

    /** Actualiza el progreso de todos los jugadores conectados de una sola vez. */
    private void actualizarProgresoTodosOnline() {
        FileConfiguration config = cargarProgreso();
        for (Player online : Bukkit.getOnlinePlayers()) {
            escribirProgreso(config, online);
        }
        guardarProgreso(config);
    }

    private FileConfiguration cargarProgreso() {
        return archivoProgreso.exists() ? YamlConfiguration.loadConfiguration(archivoProgreso) : new YamlConfiguration();
    }

    private void escribirProgreso(FileConfiguration config, Player player) {
        String base = "jugadores." + player.getUniqueId();

        config.set(base + ".nombre", player.getName());

        config.set(base + ".obligatoria.piezas_actuales", contarPiezasDiamante(player));
        config.set(base + ".obligatoria.piezas_requeridas", SET_DIAMANTE.length);
        config.set(base + ".obligatoria.completa", cumpleObligatoria(player));
        config.set(base + ".obligatoria.reclamada", yaReclamoObligatoria(player));

        config.set(base + ".opcional.ravagers", killsRavager(player));
        config.set(base + ".opcional.ravagers_requeridos", RAVAGERS_REQUERIDOS);
        config.set(base + ".opcional.piglin_brutes", killsPiglinBrute(player));
        config.set(base + ".opcional.piglin_brutes_requeridos", PIGLIN_BRUTES_REQUERIDOS);
        config.set(base + ".opcional.breezes", killsBreeze(player));
        config.set(base + ".opcional.breezes_requeridos", BREEZES_REQUERIDOS);
        config.set(base + ".opcional.totems", totemsEnInventario(player));
        config.set(base + ".opcional.totems_requeridos", TOTEMS_REQUERIDOS);
        config.set(base + ".opcional.llaves", llavesEnInventario(player));
        config.set(base + ".opcional.llaves_requeridas", LLAVES_REQUERIDAS);
        config.set(base + ".opcional.completa", cumpleOpcional(player));
        config.set(base + ".opcional.reclamada", yaReclamoOpcional(player));
    }

    private void guardarProgreso(FileConfiguration config) {
        try {
            plugin.getDataFolder().mkdirs();
            config.save(archivoProgreso);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ---------- Persistencia (quién ya reclamó cada misión) ----------

    private void cargar() {
        if (!archivo.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(archivo);

        for (String uuidStr : config.getStringList("obligatoria")) {
            try {
                obligatoriaReclamada.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
        for (String uuidStr : config.getStringList("opcional")) {
            try {
                opcionalReclamada.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void guardar() {
        FileConfiguration config = new YamlConfiguration();

        List<String> obligatoriaList = new ArrayList<>();
        for (UUID uuid : obligatoriaReclamada) obligatoriaList.add(uuid.toString());

        List<String> opcionalList = new ArrayList<>();
        for (UUID uuid : opcionalReclamada) opcionalList.add(uuid.toString());

        config.set("obligatoria", obligatoriaList);
        config.set("opcional", opcionalList);

        try {
            plugin.getDataFolder().mkdirs();
            config.save(archivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum ResultadoReclamo {
        OK, INCOMPLETA, YA_RECLAMADA
    }

    // ---------- Reseteo manual (para testing / soporte de staff) ----------

    /** Permite volver a reclamar la obligatoria (no toca el inventario ni el progreso actual). */
    public void resetearObligatoria(UUID uuid) {
        obligatoriaReclamada.remove(uuid);
        guardar();
    }

    /** Permite volver a reclamar la opcional. */
    public void resetearOpcional(UUID uuid) {
        opcionalReclamada.remove(uuid);
        guardar();
    }

    public void resetearTodo(UUID uuid) {
        resetearObligatoria(uuid);
        resetearOpcional(uuid);
    }
}
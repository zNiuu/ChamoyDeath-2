package io.github.zniuu.chamoydeath.missions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Menú de misiones: muestra el progreso de cada una y permite reclamar la recompensa con un click. */
public class MisionGUI implements Listener {

    private static final int SLOT_OBLIGATORIA = 11;
    private static final int SLOT_OPCIONAL = 15;

    private final MisionManager misionManager;

    public MisionGUI(MisionManager misionManager) {
        this.misionManager = misionManager;
    }

    public void abrir(Player player) {
        MisionGUIHolder holder = new MisionGUIHolder();
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Misiones - ChamoyDeath", NamedTextColor.DARK_AQUA));
        holder.setInventory(inv);

        ItemStack relleno = relleno();
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, relleno);
        }

        inv.setItem(SLOT_OBLIGATORIA, itemObligatoria(player));
        inv.setItem(SLOT_OPCIONAL, itemOpcional(player));

        player.openInventory(inv);
    }

    private ItemStack itemObligatoria(Player player) {
        boolean reclamada = misionManager.yaReclamoObligatoria(player);
        boolean completa = misionManager.cumpleObligatoria(player);
        int piezas = misionManager.contarPiezasDiamante(player);

        ItemStack item = new ItemStack(reclamada ? Material.GRAY_DYE : Material.DIAMOND_CHESTPLATE);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Misión Obligatoria: Set de Diamante", NamedTextColor.YELLOW, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(sinItalica("Conseguí un set completo de diamante:", NamedTextColor.GRAY));
        lore.add(sinItalica("• Casco, Pechera, Pantalón y Botas", NamedTextColor.WHITE));
        lore.add(sinItalica("• Espada, Pico y Hacha", NamedTextColor.WHITE));
        lore.add(Component.empty());
        lore.add(sinItalica("Progreso: " + piezas + "/7", NamedTextColor.AQUA));
        lore.add(Component.empty());
        agregarEstado(lore, reclamada, completa);

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack itemOpcional(Player player) {
        boolean reclamada = misionManager.yaReclamoOpcional(player);
        boolean completa = misionManager.cumpleOpcional(player);

        ItemStack item = new ItemStack(reclamada ? Material.GRAY_DYE : Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Misión Opcional: Cazador Experto", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(sinItalica("Objetivos:", NamedTextColor.GRAY));
        lore.add(progreso("Ravagers", misionManager.killsRavager(player), MisionManager.RAVAGERS_REQUERIDOS));
        lore.add(progreso("Piglin Brutes", misionManager.killsPiglinBrute(player), MisionManager.PIGLIN_BRUTES_REQUERIDOS));
        lore.add(progreso("Breezes", misionManager.killsBreeze(player), MisionManager.BREEZES_REQUERIDOS));
        lore.add(progreso("Totems", misionManager.totemsEnInventario(player), MisionManager.TOTEMS_REQUERIDOS));
        lore.add(progreso("Ominous Keys", misionManager.llavesEnInventario(player), MisionManager.LLAVES_REQUERIDAS));
        lore.add(Component.empty());
        agregarEstado(lore, reclamada, completa);

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void agregarEstado(List<Component> lore, boolean reclamada, boolean completa) {
        if (reclamada) {
            lore.add(sinItalica("Recompensa ya reclamada", NamedTextColor.DARK_GRAY));
        } else if (completa) {
            lore.add(sinItalica("¡Click para reclamar tu recompensa!", NamedTextColor.GREEN));
        } else {
            lore.add(sinItalica("Todavía te faltan objetivos.", NamedTextColor.RED));
        }
    }

    private Component progreso(String nombre, int actual, int requerido) {
        NamedTextColor color = actual >= requerido ? NamedTextColor.GREEN : NamedTextColor.RED;
        return sinItalica("• " + nombre + ": " + Math.min(actual, requerido) + "/" + requerido, color);
    }

    private Component sinItalica(String texto, NamedTextColor color) {
        return Component.text(texto, color).decoration(TextDecoration.ITALIC, false);
    }

    private ItemStack relleno() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(" "));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MisionGUIHolder)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        if (slot == SLOT_OBLIGATORIA) {
            manejarReclamo(player, misionManager.reclamarObligatoria(player));
            abrir(player);
        } else if (slot == SLOT_OPCIONAL) {
            manejarReclamo(player, misionManager.reclamarOpcional(player));
            abrir(player);
        }
    }

    private void manejarReclamo(Player player, MisionManager.ResultadoReclamo resultado) {
        switch (resultado) {
            case YA_RECLAMADA -> player.sendMessage(Component.text("Ya reclamaste esta recompensa.", NamedTextColor.GRAY));
            case INCOMPLETA -> player.sendMessage(Component.text("Todavía no cumplís los requisitos de esta misión.", NamedTextColor.RED));
            case OK -> { /* el mensaje de éxito ya lo manda MisionManager al entregar los items */ }
        }
    }
}

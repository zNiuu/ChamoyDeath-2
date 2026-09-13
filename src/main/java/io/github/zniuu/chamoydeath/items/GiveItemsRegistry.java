package io.github.zniuu.chamoydeath.items;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GiveItemsRegistry {

    private static final List<GiveItem> ITEMS = new ArrayList<>();

    static {
        ITEMS.add(new GiveItem(Material.TOTEM_OF_UNDYING, "diamond_totem",
                "&x&4&C&4&C&4&Cᴅɪᴀᴍᴏɴᴅ ᴛᴏᴛᴇᴍ",
                List.of(
                        "§7\"ꜰᴏʀᴊᴀᴅᴏ ᴘᴏʀ ᴍɪɴᴇʀᴏs ǫᴜᴇ sᴇ ɴᴇɢᴀʀᴏɴ",
                        "§7ᴀ ᴍᴏʀɪʀ ᴇɴ ʟᴀ ᴏsᴄᴜʀɪᴅᴀᴅ.\"",
                        "§f",
                        "§b+2 §c❤ §fᴄᴏʀᴀᴢᴏɴᴇs ᴇxᴛʀᴀ",
                        "§fᴀʟ ᴀᴄᴛɪᴠᴀʀsᴇ: §fʀᴇsɪsᴛᴇɴᴄɪᴀ ɪ §7(15s) §f· ᴠᴇʟᴏᴄɪᴅᴀᴅ ɪ §7(15s)"
                )));

        ITEMS.add(new GiveItem(Material.TOTEM_OF_UNDYING, "netherite_totem",
                "&x&4&C&4&C&4&Cɴᴇᴛʜᴇʀɪᴛᴇ ᴛᴏᴛᴇᴍ",
                List.of(
                        "§7\"ɴɪ ʟᴀ ʟᴀᴠᴀ ɴɪ ᴇʟ ᴠᴀᴄɪᴏ ᴘᴜᴅɪᴇʀᴏɴ",
                        "§7ʀᴇᴄʟᴀᴍᴀʀ ᴀ sᴜ ᴘᴏʀᴛᴀᴅᴏʀ.\"",
                        "§f",
                        "§8+4 §c❤ §fᴄᴏʀᴀᴢᴏɴᴇs ᴇxᴛʀᴀ",
                        "§fᴀʟ ᴀᴄᴛɪᴠᴀʀsᴇ: §fʀᴇsɪsᴛᴇɴᴄɪᴀ ɪ §7(25s) §f· ᴠᴇʟᴏᴄɪᴅᴀᴅ ɪɪ §7(15s) §f· ʀᴇɢᴇɴᴇʀᴀᴄɪᴏɴ ɪɪɪ §7(10s)"
                )));

        ITEMS.add(new GiveItem(Material.TOTEM_OF_UNDYING, "evoker_totem",
                "&x&F&C&D&D&8&Fᴇ&x&E&0&E&2&8&9ᴠ&x&C&3&E&6&8&3ᴏ&x&A&7&E&B&7&Dᴋ&x&8&A&E&F&7&6ᴇ&x&6&E&F&4&7&0ʀ &x&5&2&D&8&6&6ᴛ&x&5&4&B&8&6&3ᴏ&x&5&5&9&8&5&Fᴛ&x&5&7&7&8&5&Cᴇ&x&5&8&5&8&5&8ᴍ",
                List.of(
                        "§7\"ᴜɴ ꜰʀᴀɢᴍᴇɴᴛᴏ ᴅᴇʟ ʀɪᴛᴜᴀʟ ᴘʀᴏʜɪʙɪᴅᴏ",
                        "§7ᴅᴇ ʟᴏs ᴇᴠᴏᴄᴀᴅᴏʀᴇs, ʀᴏʙᴀᴅᴏ ᴀ ᴅᴜʀᴀs ᴘᴇɴᴀs.\"",
                        "§f",
                        "§a+5 §c❤ §fᴄᴏʀᴀᴢᴏɴᴇs ᴇxᴛʀᴀ",
                        "§fᴀʟ ᴀᴄᴛɪᴠᴀʀsᴇ: §fʀᴇsɪsᴛᴇɴᴄɪᴀ ɪɪ §7(20s)",
                        "§8sᴏʟᴏ sᴇ ᴏʙᴛɪᴇɴᴇ ᴄᴏᴍᴘʟᴇᴛᴀɴᴅᴏ ᴍɪsɪᴏɴᴇs"
                )));

        ITEMS.add(new GiveItem(Material.TOTEM_OF_UNDYING, "demonic_totem",
                "&x&8&8&3&2&3&2ᴅᴇᴍᴏɴɪᴄ ᴛᴏᴛᴇᴍ",
                List.of(
                        "§7\"sᴜ ᴄᴀʟᴏʀ ɴᴏ ǫᴜᴇᴍᴀ ᴀ ǫᴜɪᴇɴ ʟᴏ ᴘᴏʀᴛᴀ,",
                        "§7sᴏʟᴏ ᴀ ǫᴜɪᴇɴ sᴇ ᴀᴛʀᴇᴠᴀ ᴀ ᴀᴄᴇʀᴄᴀʀsᴇ.\"",
                        "§f",
                        "§6+6 §c❤ §fᴄᴏʀᴀᴢᴏɴᴇs ᴇxᴛʀᴀ",
                        "§fᴘᴀsɪᴠᴏ: §fɪɴᴍᴜɴɪᴅᴀᴅ ᴀʟ ꜰᴜᴇɢᴏ §f· §f+10% ᴠᴇʟᴏᴄɪᴅᴀᴅ",
                        "§8sᴏʟᴏ sᴇ ᴏʙᴛɪᴇɴᴇ ᴇɴ ᴅᴜɴɢᴇᴏɴs ᴅᴇʟ ɴᴇᴛʜᴇʀ"
                )));

        ITEMS.add(new GiveItem(Material.SADDLE, "enderbudle",
                "&x&9&8&6&3&E&9ᴇɴᴅᴇʀ ʙᴀɢ", List.of()));

        ITEMS.add(new GiveItem(Material.ECHO_SHARD, "ancest_sculk",
                "&x&0&F&4&4&6&Eʀᴇʟɪǫᴜɪᴀ ᴅᴇʟ ᴡᴀʀᴅᴇɴ",
                List.of(
                        "§7\"ᴇʟ ᴄᴏʀᴀᴢᴏɴ ᴅᴇ ᴜɴᴀ ᴄʀɪᴀᴛᴜʀᴀ ǫᴜᴇ ɴᴜɴᴄᴀ ᴅᴇʙɪᴏ ᴛᴇɴᴇʀ ᴜɴᴏ.\"",
                        "§7\"ʟᴀᴛᴇ ᴇɴ ʟᴀ ᴏsᴄᴜʀɪᴅᴀᴅ, ᴀᴜɴ ᴅᴇsᴘᴜᴇs ᴅᴇ sᴇʀ ᴀʀʀᴀɴᴄᴀᴅᴏ.\"",
                        "§f",
                        "§dᴀʟ ᴄᴏɴsᴜᴍɪʀsᴇ:",
                        "§fꜰᴜᴇʀᴢᴀ ɪɪ §7(15s)",
                        "§fʜᴇᴀʟᴛʜ ʙᴏᴏsᴛ ɪɪɪ §7(30s)",
                        "§fʀᴇɢᴇɴᴇʀᴀᴄɪᴏɴ ɪɪ §7(20s)",
                        "§fsᴀᴛᴜʀᴀᴄɪᴏɴ 100 §7(1s)",
                        "§fsʟᴏᴡ ꜰᴀʟʟɪɴɢ ɪ §7(15s)",
                        "§fsᴘᴇᴇᴅ ɪɪ §7(20s)",
                        "§f",
                        "§cᴄᴏᴏʟᴅᴏᴡɴ: 5 ᴍɪɴᴜᴛᴏs"
                )));

        ITEMS.add(new GiveItem(Material.NETHERITE_SWORD, "watergodsword",
                "&x&8&B&C&B&F&Fᴡᴀᴛᴇʀ'ѕ ɢᴏᴅ ѕᴡᴏʀᴅ", List.of()));

        ITEMS.add(new GiveItem(Material.TURTLE_SCUTE, "warden_heart",
                "&x&0&0&3&0&5&6ᴡᴀʀᴅᴇɴ'ѕ ʜᴇᴀʀᴛ", List.of()));

        ITEMS.add(new GiveItem(Material.QUARTZ, "haunted_echo_shard",
                "&x&1&5&5&E&9&8ᴇᴄʜᴏ ꜰʀᴀɢᴍᴇɴᴛ", List.of()));

        ITEMS.add(new GiveItem(Material.QUARTZ, "echo_shard_new",
                "&x&1&D&3&F&5&Aᴇᴄʜᴏ ᴘᴏᴡᴅᴇʀ", List.of()));

        ITEMS.add(new GiveItem(Material.AMETHYST_SHARD, "vex_angry_totem",
                "&x&F&5&9&9&9&9ᴇ&x&E&5&9&4&9&4ɴ&x&D&4&8&F&8&Fʀ&x&C&4&8&A&8&Aᴀ&x&B&4&8&6&8&6ɢ&x&A&3&8&1&8&1ᴇ&x&9&3&7&C&7&Cᴅ &x&7&2&7&2&7&2ᴠ&x&6&D&6&D&6&Dᴇ&x&6&8&6&8&6&8x &x&5&D&5&D&5&Dѕ&x&5&8&5&8&5&8ᴏ&x&5&3&5&3&5&3ᴜ&x&4&E&4&E&4&Eʟ", List.of()));

        ITEMS.add(new GiveItem(Material.AMETHYST_SHARD, "vex_totem",
                "&x&7&2&7&2&7&2ᴠᴇx ѕ&x&6&6&6&6&6&6ᴏ&x&5&A&5&A&5&Aᴜ&x&4&E&4&E&4&Eʟ", List.of()));

        ITEMS.add(new GiveItem(Material.AMETHYST_SHARD, "allay_totem",
                "&x&6&D&B&6&F&Cᴀʟʟᴀʏ &x&9&2&C&8&F&Dʟ&x&B&6&D&B&F&Eɪ&x&D&B&E&D&F&Eɴ&x&F&F&F&F&F&Fᴋ", List.of()));
    }

    public static List<GiveItem> getAll() {
        return ITEMS;
    }

    /** Busca un item por su id (el "customModelData" que se usa como identificador interno). */
    public static Optional<GiveItem> getById(String id) {
        return ITEMS.stream().filter(item -> item.getCustomModelData().equalsIgnoreCase(id)).findFirst();
    }
}
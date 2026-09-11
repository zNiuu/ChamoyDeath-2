package io.github.zniuu.chamoydeath;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class DiscordNotifier {

    private final JavaPlugin plugin;
    private final HttpClient httpClient;

    public DiscordNotifier(JavaPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void enviarMuerte(Player player, Location loc, String razon) {
        String webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        String roleId = plugin.getConfig().getString("discord.role-id", "");

        plugin.getLogger().info("[Discord Debug] webhookUrl=[" + webhookUrl + "] roleId=[" + roleId + "] longitud roleId=" + roleId.length());

        if (webhookUrl.isBlank()) {
            plugin.getLogger().warning("No hay webhook-url configurada en config.yml, no se envió el aviso a Discord.");
            return;
        }

        String uuid = player.getUniqueId().toString();
        String nombre = player.getName();
        String mundo = (loc.getWorld() != null) ? loc.getWorld().getName() : "desconocido";
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        String avatarUrl = "https://mc-heads.net/avatar/" + uuid + "/128";

        String contenido = roleId.isBlank() ? "" : "<@&" + roleId + ">";

        String json = "{"
                + "\"content\": \"" + escapar(contenido) + "\","
                + "\"embeds\": [{"
                + "\"title\": \"" + escapar("💀 " + nombre + " ha muerto") + "\","
                + "\"color\": 14624563,"
                + "\"thumbnail\": {\"url\": \"" + avatarUrl + "\"},"
                + "\"fields\": ["
                + "{\"name\": \"Jugador\", \"value\": \"" + escapar(nombre) + "\", \"inline\": true},"
                + "{\"name\": \"Mundo\", \"value\": \"" + escapar(mundo) + "\", \"inline\": true},"
                + "{\"name\": \"Coordenadas\", \"value\": \"" + escapar("X: " + x + " Y: " + y + " Z: " + z) + "\", \"inline\": false},"
                + "{\"name\": \"Causa\", \"value\": \"" + escapar(razon) + "\", \"inline\": false}"
                + "]"
                + "}]"
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 300) {
                        plugin.getLogger().warning("Discord webhook respondio con código " + response.statusCode() + ": " + response.body());
                    }
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Error enviando aviso a Discord: " + ex.getMessage());
                    return null;
                });
    }

    private String escapar(String texto) {
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
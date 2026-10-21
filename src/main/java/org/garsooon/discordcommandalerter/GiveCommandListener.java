package org.garsooon.discordcommandalerter;

import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.awt.Color;

public class GiveCommandListener implements Listener {
    private static final Color EMBED_COLOR = new Color(0x1E8449);

    private final DiscordCommandAlerter plugin;

    public GiveCommandListener(DiscordCommandAlerter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!isGiveCommand(message)) return;

        Player player = event.getPlayer();

        String world = player.getWorld().getName();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        String desc = player.getName() + " Executed: " + message
                + "\nCommand: " + message
                + "\nPosition: " + world + " `/tppos " + x + " " + y + " " + z + "`";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Give Command Logger")
                .setDescription(desc)
                .setColor(EMBED_COLOR);

        plugin.sendEmbed(plugin.getGiveChannelId(), embed);
    }

    private boolean isGiveCommand(String message) {
        String lower = message.toLowerCase();
        return lower.equals("/i") || lower.startsWith("/i ")
                || lower.equals("/give") || lower.startsWith("/give ");
    }
}

package org.garsooon.discordcommandalerter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
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
        if (!playerCanRunCommand(player, message)) return;

        TextChannel channel = plugin.getDiscordBot().getJda().getTextChannelById(plugin.getGiveChannelId());
        if (channel == null) {
            System.out.println("[DiscordCommandAlerter] Could not find give channel " + plugin.getGiveChannelId());
            return;
        }

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

        channel.sendMessage(embed.build()).queue();
    }

    private boolean playerCanRunCommand(Player player, String message) {
        try {
            String name = message.trim().split("\\s+")[0];
            if (name.startsWith("/")) name = name.substring(1);

            PluginCommand command = Bukkit.getPluginCommand(name);
            String permission = command != null ? command.getPermission() : null;

            if (permission != null && !permission.isEmpty()) {
                return player.hasPermission(permission);
            }
            return player.isOp();
        } catch (Throwable t) {
            return true;
        }
    }

    private boolean isGiveCommand(String message) {
        String lower = message.toLowerCase();
        return lower.equals("/i") || lower.startsWith("/i ")
                || lower.equals("/give") || lower.startsWith("/give ");
    }
}

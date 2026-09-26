package org.garsooon.discordcommandalerter;

import com.johnymuffin.beta.discordauth.DiscordAuthentication;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.awt.Color;
import java.util.UUID;

public class RegisterCommandListener implements Listener {
    private static final Color EMBED_COLOR = new Color(0x5865F2);

    private final DiscordCommandAlerter plugin;

    public RegisterCommandListener(DiscordCommandAlerter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!isLinkCommand(event.getMessage())) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        System.out.println("[DiscordCommandAlerter] Link command caught for " + player.getName() + " UUID=" + uuid);

        DiscordAuthentication discordAuth = (DiscordAuthentication) Bukkit.getPluginManager().getPlugin("DiscordAuthentication");
        if (discordAuth == null) {
            System.out.println("[DiscordCommandAlerter] DiscordAuthentication plugin not found, posting IGN only");
            postEmbed(player.getName(), null);
            return;
        }

        boolean pending = discordAuth.getCache().isUserPending(uuid.toString());
        System.out.println("[DiscordCommandAlerter] isUserPending=" + pending);

        if (!pending) {
            System.out.println("[DiscordCommandAlerter] No pending session, posting IGN only");
            postEmbed(player.getName(), null);
            return;
        }

        String rawDiscordId = discordAuth.getCache().getUUIDDiscordID(uuid.toString());
        System.out.println("[DiscordCommandAlerter] Raw Discord ID from cache: " + rawDiscordId);

        if (rawDiscordId == null || rawDiscordId.equals("0")) {
            System.out.println("[DiscordCommandAlerter] Discord ID invalid, posting IGN only");
            postEmbed(player.getName(), null);
            return;
        }

        long discordId;
        try {
            discordId = Long.parseLong(rawDiscordId);
        } catch (NumberFormatException e) {
            System.out.println("[DiscordCommandAlerter] Could not parse Discord ID: " + rawDiscordId);
            postEmbed(player.getName(), null);
            return;
        }

        plugin.getJDA().retrieveUserById(discordId).queue(
                user -> {
                    System.out.println("[DiscordCommandAlerter] Resolved Discord user: " + user.getName());
                    postEmbed(player.getName(), "`" + user.getName() + " (" + discordId + ")`");
                },
                error -> {
                    System.out.println("[DiscordCommandAlerter] Could not resolve Discord user for ID " + discordId + ": " + error.getMessage());
                    postEmbed(player.getName(), "`" + discordId + "`");
                }
        );
    }

    private void postEmbed(String ign, String discordPart) {
        String desc = discordPart != null
                ? discordPart + " is attempting to link their Discord to Minecraft IGN `" + ign + "`"
                : "`" + ign + "` is attempting to link their Discord account.";
        plugin.sendEmbed(plugin.getRegisterChannelId(), new EmbedBuilder().setDescription(desc).setColor(EMBED_COLOR));
    }

    private boolean isLinkCommand(String message) {
        String[] parts = message.split(" ");
        if (parts.length < 2) return false;
        if (parts[0].equalsIgnoreCase("/link")) return true;
        if (parts.length >= 3
                && parts[0].equalsIgnoreCase("/discordauth")
                && parts[1].equalsIgnoreCase("link")) return true;
        return false;
    }
}

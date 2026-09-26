package org.garsooon.discordcommandalerter;

import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class WorldEditCommandListener implements Listener {
    private static final Color EMBED_COLOR = new Color(0xAA0000);

    private final DiscordCommandAlerter plugin;

    public WorldEditCommandListener(DiscordCommandAlerter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (!isWorldEditCommand(message)) return;

        Player player = event.getPlayer();
        if (!playerCanRunCommand(player, message)) return;

        TextChannel channel = plugin.getDiscordBot().getJda().getTextChannelById(plugin.getWorldEditChannelId());
        if (channel == null) {
            System.out.println("[DiscordCommandAlerter] Could not find WorldEdit channel " + plugin.getWorldEditChannelId());
            return;
        }

        String world = player.getWorld().getName();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        StringBuilder desc = new StringBuilder()
                .append(player.getName()).append(" Executed: ").append(message)
                .append("\nCommand: ").append(message)
                .append("\nPosition: ").append(world).append(" `/tppos ").append(x).append(" ").append(y).append(" ").append(z).append("`");

        String selection = getSelectionString(player);
        if (selection != null) {
            desc.append("\nSelection: ").append(selection);
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("World Edit Logger")
                .setDescription(desc.toString())
                .setColor(EMBED_COLOR);

        channel.sendMessage(embed.build()).queue();
    }

    private String getSelectionString(Player player) {
        try {
            WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
            if (we == null) return null;

            LocalSession session = we.getSession(player);
            Region region = session.getSelection(new BukkitWorld(player.getWorld()));

            com.sk89q.worldedit.Vector min = region.getMinimumPoint();
            com.sk89q.worldedit.Vector max = region.getMaximumPoint();

            return String.format("(%d, %d, %d) -> (%d, %d, %d)",
                    min.getBlockX(), min.getBlockY(), min.getBlockZ(),
                    max.getBlockX(), max.getBlockY(), max.getBlockZ());
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean playerCanRunCommand(Player player, String message) {
        try {
            WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
            if (we == null) return true;

            WorldEdit worldEdit = we.getWorldEdit();

            Field commandsField = WorldEdit.class.getDeclaredField("commands");
            commandsField.setAccessible(true);
            CommandsManager<?> commandsManager = (CommandsManager<?>) commandsField.get(worldEdit);

            Field mapField = CommandsManager.class.getDeclaredField("commands");
            mapField.setAccessible(true);
            Map<Method, Map<String, Method>> commandMap =
                    (Map<Method, Map<String, Method>>) mapField.get(commandsManager);

            LocalPlayer localPlayer = we.wrapPlayer(player);

            String[] tokens = message.trim().split("\\s+");
            tokens[0] = tokens[0].substring(1);

            return isPermitted(commandMap, null, tokens, 0, localPlayer);
        } catch (Throwable t) {
            return true;
        }
    }

    private boolean isPermitted(Map<Method, Map<String, Method>> commandMap, Method parent,
                                String[] tokens, int level, LocalPlayer player) {
        if (level >= tokens.length) return true;

        Map<String, Method> levelCommands = commandMap.get(parent);
        if (levelCommands == null) return true;

        String name = tokens[level].toLowerCase();
        Method method = levelCommands.get(name);
        if (method == null) method = levelCommands.get("/" + name);
        if (method == null) return true;

        CommandPermissions perms = method.getAnnotation(CommandPermissions.class);
        if (perms != null) {
            boolean allowed = false;
            for (String node : perms.value()) {
                if (player.hasPermission(node)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) return false;
        }

        if (method.isAnnotationPresent(NestedCommand.class)) {
            return isPermitted(commandMap, method, tokens, level + 1, player);
        }
        return true;
    }

    private boolean isWorldEditCommand(String message) {
        if (message.startsWith("//")) return true;
        String lower = message.toLowerCase();
        return lower.equals("/worldedit") || lower.startsWith("/worldedit ")
                || lower.equals("/we") || lower.startsWith("/we ")
                || lower.equals("/wand");
    }
}

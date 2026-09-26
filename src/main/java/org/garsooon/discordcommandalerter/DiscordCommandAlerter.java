package org.garsooon.discordcommandalerter;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.retromc.discordcore.api.DiscordCoreAPI;
import org.retromc.discordcore.v6.DiscordCorePlugin;

public class DiscordCommandAlerter extends JavaPlugin {
    private static DiscordCommandAlerter instance;
    private DiscordCorePlugin discordCore;
    private String worldEditChannelId;
    private String giveChannelId;
    private String registerChannelId;
    private String dogKillChannelId;

    @Override
    public void onEnable() {
        instance = this;
        getDataFolder().mkdirs();

        Configuration config = getConfiguration();
        config.load();

        if (config.getString("worldedit-channel-id") == null) {
            config.setProperty("worldedit-channel-id", "CHANNEL_ID_HERE");
            config.setProperty("give-channel-id", "CHANNEL_ID_HERE");
            config.setProperty("register-channel-id", "CHANNEL_ID_HERE");
            config.setProperty("dog-kill-channel-id", "CHANNEL_ID_HERE");
            config.save();
        }

        worldEditChannelId = config.getString("worldedit-channel-id", "CHANNEL_ID_HERE");
        giveChannelId = config.getString("give-channel-id", "CHANNEL_ID_HERE");
        registerChannelId = config.getString("register-channel-id", "CHANNEL_ID_HERE");
        dogKillChannelId = config.getString("dog-kill-channel-id", "CHANNEL_ID_HERE");

        discordCore = (DiscordCorePlugin) Bukkit.getPluginManager().getPlugin("DiscordCore-6");
        if (discordCore == null) {
            System.out.println("[DiscordCommandAlerter] DiscordCore-6 not found, disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new WorldEditCommandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GiveCommandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new RegisterCommandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DogKillListener(this), this);
        System.out.println("[DiscordCommandAlerter] Enabled.");
    }

    @Override
    public void onDisable() {
        System.out.println("[DiscordCommandAlerter] Disabled.");
    }

    public static DiscordCommandAlerter getInstance() {
        return instance;
    }

    public JDA getJDA() {
        return discordCore.getDiscordBot().getJDA();
    }

    public void sendEmbed(String channelId, EmbedBuilder embed) {
        try {
            DiscordCoreAPI.sendEmbed(Long.parseLong(channelId), embed);
        } catch (NumberFormatException e) {
            System.out.println("[DiscordCommandAlerter] Invalid channel id: " + channelId);
        }
    }

    public String getWorldEditChannelId() {
        return worldEditChannelId;
    }

    public String getGiveChannelId() {
        return giveChannelId;
    }

    public String getRegisterChannelId() {
        return registerChannelId;
    }

    public String getDogKillChannelId() {
        return dogKillChannelId;
    }
}

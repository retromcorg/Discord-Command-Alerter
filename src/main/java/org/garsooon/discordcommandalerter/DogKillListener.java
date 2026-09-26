package org.garsooon.discordcommandalerter;

import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.awt.Color;

public class DogKillListener implements Listener {
    private static final Color EMBED_COLOR = new Color(0xE67E22);

    private final DiscordCommandAlerter plugin;

    public DogKillListener(DiscordCommandAlerter plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Wolf)) return;

        Wolf wolf = (Wolf) event.getEntity();
        if (!wolf.isTamed()) return;

        EntityDamageEvent lastDamage = wolf.getLastDamageCause();
        if (!(lastDamage instanceof EntityDamageByEntityEvent)) return;

        Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();
        if (!(damager instanceof Player)) return;

        Player killer = (Player) damager;

        AnimalTamer owner = wolf.getOwner();
        String ownerName = ownerPlayerName(owner);
        if (owner instanceof Player && ((Player) owner).getName().equals(killer.getName())) return;

        String world = killer.getWorld().getName();
        int x = killer.getLocation().getBlockX();
        int y = killer.getLocation().getBlockY();
        int z = killer.getLocation().getBlockZ();

        String desc = killer.getName() + " killed a dog owned by " + ownerName
                + "\nPosition: " + world + " `/tppos " + x + " " + y + " " + z + "`";

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Dog Kill Logger")
                .setDescription(desc)
                .setColor(EMBED_COLOR);

        plugin.sendEmbed(plugin.getDogKillChannelId(), embed);
    }

    private String ownerPlayerName(AnimalTamer owner) {
        if (owner == null) return "unknown";
        if (owner instanceof Player) return ((Player) owner).getName();
        return "unknown";
    }
}

package com.minecraftserver.pvptoolkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerTracker implements Listener {
    private PVPToolkit pvptoolkit;

    private int        trackingdistance;
    public final String MODULVERSION = "1.1";

    public PlayerTracker(PVPToolkit toolkit) {
        pvptoolkit = toolkit;
        this.trackingdistance = pvptoolkit.getTrackingdistance();
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.isCancelled()) return;
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && event.getMaterial() == Material.COMPASS
                && event.getPlayer().hasPermission("pvptoolkit.playertracker.cantrack")
                && !(event.getClickedBlock().getType() == Material.CHEST
                        || event.getClickedBlock().getType() == Material.FURNACE
                        || event.getClickedBlock().getType() == Material.WORKBENCH
                        || event.getClickedBlock().getType() == Material.DISPENSER
                        || event.getClickedBlock().getType() == Material.ANVIL || event
                        .getClickedBlock().getType() == Material.BREWING_STAND)) {
            Location playerLoc = event.getPlayer().getLocation();
            Location targetLoc = null;
            double finaldistance = Double.MAX_VALUE, distance = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.isOp() && p.hasPermission("pvptoolkit.playertracker.trackable") &&!p.hasPermission("pvptoolkit.admin")
                        && p != event.getPlayer() && p.getWorld() == event.getPlayer().getWorld()) {
                    distance = playerLoc.distance(p.getLocation());
                    if (distance < finaldistance) {
                        finaldistance = distance;
                        targetLoc = p.getLocation();
                    }
                }
            }
            if (targetLoc != null && finaldistance < trackingdistance) {
                event.getPlayer().sendMessage(
                        ChatColor.BLUE + "The next player is " + ChatColor.GOLD
                                + (int) finaldistance + ChatColor.BLUE + " blocks away from you.");
                event.getPlayer().setCompassTarget(targetLoc);
            } else event.getPlayer().sendMessage(ChatColor.BLUE + "There is no player near you!");

        }
    }
}

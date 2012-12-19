package com.minecraftserver.pvptoolkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class PVPFly implements Listener {
    private PVPToolkit   pvptoolkit;

    private final String MODULVERSION = "0.4";

    public PVPFly(PVPToolkit toolkit) {
        pvptoolkit = toolkit;
    }

    public void togglefly(CommandSender sender, String[] args) {
        if ((sender instanceof Player)) {
            Player player = (Player) sender;
            if (args.length == 0) {
                if (player.hasPermission("pvptoolkit.fly.fly")) if (player.getAllowFlight()) {
                    player.setFlying(false);
                    player.setAllowFlight(false);
                    player.sendMessage(ChatColor.GREEN + "Disabled fly mode!");
                } else {
                    player.setAllowFlight(true);
                    player.setFlySpeed(0.1F);
                    player.sendMessage(ChatColor.GREEN + "Enabled fly mode!");
                }
            } else if (args.length == 1) {
                if (args[0].equals("help")) {
                    player.sendMessage(ChatColor.RED
                            + "PVP, Flint and Steel and placing of Lava is turned off while flying.");
                    player.sendMessage(ChatColor.GREEN + "/fly - Toggles your fly mode!");
                    player.sendMessage(ChatColor.BLACK + "/fly version - Don't try it, its evil!");
                } else if (args[0].equals("version")) {
                    player.sendMessage("version " + MODULVERSION);
                    player.sendMessage("author: stolen and improved by " + ChatColor.GOLD + "M0P"
                            + ChatColor.RESET + ChatColor.BLACK + " (original by jorisk322)");
                } else if (player.hasPermission("pvptoolkit.fly.other")) {
                    Player otherplayer = Bukkit.getPlayer(args[0]);
                    if (otherplayer != null) {
                        if (otherplayer.getAllowFlight()) {
                            otherplayer.setFlying(false);
                            otherplayer.setAllowFlight(false);
                            otherplayer.sendMessage("Disabled fly mode!");
                            player.sendMessage(args[0] + " was put out of fly mode!");
                        } else {
                            otherplayer.setAllowFlight(true);
                            otherplayer.setFlySpeed(0.1F);
                            otherplayer.sendMessage("Enabled fly mode!");
                            player.sendMessage(args[0] + " was sent to sky!");
                        }
                    } else player.sendMessage(args[0] + " is not online or doesn't exist!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;
        if ((event.getBucket().compareTo(Material.LAVA_BUCKET) == 0)) {
            Player player = (Player) event.getPlayer();
            if ((player.getAllowFlight()) && (!player.hasPermission("fly.damage"))
                    && (player.getGameMode() != GameMode.CREATIVE)) {
                player.sendMessage(ChatColor.RED + "You can't place lava while in fly mode!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled()) return;
        if ((event.getCause().compareTo(IgniteCause.FLINT_AND_STEEL) == 0)) {
            Player player = (Player) event.getPlayer();
            if ((player.getAllowFlight()) && (!player.hasPermission("fly.damage"))
                    && (player.getGameMode() != GameMode.CREATIVE)) {
                player.sendMessage(ChatColor.RED
                        + "You can't use flint and steel while in fly mode!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if ((event.getDamager() instanceof Player) && !(event.getEntity() instanceof Monster)) {
            Player damager = (Player) event.getDamager();
            if ((damager.getAllowFlight()) && (!damager.hasPermission("fly.damage"))
                    && (damager.getGameMode() != GameMode.CREATIVE)) {
                damager.sendMessage(ChatColor.RED + "You can't attack while in fly mode!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if ((event.getEntity() instanceof Player)) {
            Player receiver = (Player) event.getEntity();
            if ((receiver.getAllowFlight()) && (receiver.hasPermission("fly.invincible")))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onShoot(EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        if ((event.getEntity() instanceof Player)) {
            Player archer = (Player) event.getEntity();
            if ((archer.getAllowFlight()) && (!archer.hasPermission("fly.damage"))
                    && (archer.getGameMode() != GameMode.CREATIVE)) {
                archer.sendMessage(ChatColor.RED + "You can't attack while in fly mode!");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.isCancelled()) return;
        if ((event.getCaught() instanceof Player)) {
            Player damager = (Player) event.getPlayer();
            if ((damager.getAllowFlight()) && (!damager.hasPermission("fly.damage"))
                    && (damager.getGameMode() != GameMode.CREATIVE)) {
                damager.sendMessage(ChatColor.RED + "You can't attack while in fly mode!");
                event.setCancelled(true);
            }
        }
    }

}
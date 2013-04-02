package com.minecraftserver.pvptoolkit;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;

public class PVPSpawnCampProtection implements Listener {
    private final PVPToolkit                pvptoolkit;
    private final IEssentials               ess;
    private int                             radius;

    private final HashMap<String, Location> protectedPlayers = new HashMap<>();

    public final String                     MODULVERSION     = "1.02";

    public PVPSpawnCampProtection(PVPToolkit toolkit) {
        this.pvptoolkit = toolkit;
        this.radius = this.pvptoolkit.getspawnprotectradius();
        this.ess = (IEssentials) toolkit.getServer().getPluginManager()
                .getPlugin("Essentials");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getDamage() == 0 || event.isCancelled()) return;
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            Entity dmgr = e.getDamager();
            if (dmgr instanceof Projectile) {
                dmgr = ((Projectile) dmgr).getShooter();
            }
            if ((dmgr instanceof Player) && (e.getEntity() instanceof Player)) {
                Player damager = (Player) dmgr;
                Player receiver = (Player) e.getEntity();
                if (this.protectedPlayers.containsKey(damager.getName()))
                    this.protectedPlayers.remove(damager.getName());
                if (this.protectedPlayers.containsKey((receiver.getName())))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        if (this.protectedPlayers.containsKey(event.getPlayer().getName())) {
            try {
                if ((int) event.getTo().distance(
                        this.protectedPlayers.get(event.getPlayer().getName())) > this.radius) {
                    this.protectedPlayers.remove(event.getPlayer().getName());
                }
            } catch (Exception e) {
                this.protectedPlayers.remove(event.getPlayer().getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (this.protectedPlayers.containsKey(event.getPlayer().getName()))
            this.protectedPlayers.remove(event.getPlayer().getName());
        if (event.getPlayer().hasPermission("pvptoolkit.spawnprot")
                && event.getPlayer().getWorld() == this.pvptoolkit.getServer()
                        .getWorld("world")) {
            Location home = event.getRespawnLocation();
            final User user = this.ess.getUser(event.getPlayer());
            final Location bed = user.getBedSpawnLocation();
            if (bed != null && bed.getBlock().getType() == Material.BED_BLOCK) {
                home = bed;
            } else {
                try {
                    home = user.getHome();
                } catch (Exception e) {
                    home = event.getRespawnLocation();
                }
            }
            event.setRespawnLocation(home);
            this.protectedPlayers.put(event.getPlayer().getName(), home);
        }
    }

    public void reloadcfg() {
        this.radius = this.pvptoolkit.getspawnprotectradius();
    }

}

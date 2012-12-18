package com.minecraftserver.pvptoolkit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class PVPBlocker implements Listener {
    PVPToolkit pvptoolkit;

    boolean    isBlockAttackAllowed;

    public PVPBlocker(PVPToolkit toolkit) {
        pvptoolkit = toolkit;
        isBlockAttackAllowed = pvptoolkit.isPvpBlockAttackAllowed();
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.isCancelled() || (event.getDamage() == 0)) {
            return;
        }
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            Entity dmgr = e.getDamager();
            if (dmgr instanceof Projectile) {
                dmgr = ((Projectile) dmgr).getShooter();
            }
            if ((dmgr instanceof Player) && (e.getEntity() instanceof Player)) {
                Player damager = (Player) dmgr;
                Player victim = (Player) e.getEntity();
                if (victim.hasPermission("pvptoolkit.blocker.block")
                        && !damager.hasPermission("pvptoolkit.blocker.bypassblock")) {
                    damager.sendMessage(ChatColor.RED + "You cant attack "
                            + victim.getDisplayName());
                    event.setCancelled(true);
                }
                if (damager.hasPermission("pvptoolkit.blocker.block") && !isBlockAttackAllowed) {
                    damager.sendMessage(ChatColor.RED + "You cant attack "
                            + victim.getDisplayName());
                    event.setCancelled(true);
                }

            }
        }
    }
}

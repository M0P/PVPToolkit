package com.minecraftserver.pvptoolkit;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PVPBlockerPassword implements Listener {
    private PVPToolkit   pvptoolkit;
    private PVPIOManager datamanager;

    private List<String> blockedPlayers;

    private String       password;

    public PVPBlockerPassword(PVPToolkit toolkit) {
        pvptoolkit = toolkit;
        datamanager = new PVPIOManager();
        datamanager.init(pvptoolkit);
        password = datamanager.getPVPPassword();
        if (password == null) {
            pvptoolkit.getServer().getLogger()
                    .log(Level.WARNING, "PVP Password isnt set, using default config password");
            pvptoolkit.getConfig().getString("pvpblockpassword.default_password");
        }
        blockedPlayers = datamanager.getBlockedPlayers();
        if (blockedPlayers == null) blockedPlayers = new Vector<String>();
    }

    public void checkPassword(String password, Player player) {
        if (blockedPlayers.contains(player.getName()) && this.password == password)
            blockedPlayers.remove(player.getName());
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if ((event.getPlayer().getFirstPlayed() == 0 || (event.getPlayer().getFirstPlayed() + 30000) <= System
                .currentTimeMillis())
                && event.getPlayer().hasPermission("pvptoolkit.blockerpassword.protection")) {
            blockedPlayers.add(event.getPlayer().getName());
        }
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
                if (blockedPlayers.contains(victim.getName())
                        || blockedPlayers.contains(damager.getName())) {
                    damager.sendMessage(ChatColor.RED + "You cant attack "
                            + victim.getDisplayName());
//                    damager.sendMessage(message)
                    event.setCancelled(true);
                }

            }
        }
    }

    public void saveData() {
        if (datamanager != null) datamanager.saveBlockerPassword(password, blockedPlayers);
    }
}

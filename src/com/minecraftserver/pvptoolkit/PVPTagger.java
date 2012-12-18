package com.minecraftserver.pvptoolkit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PVPTagger implements Listener {
    private PVPToolkit            pvptoolkit;
    private int                   pvpTagDuration;

    private HashMap<String, Long> taggedPlayers = new HashMap<>();

    private List<String>          pvpTagBlockedCmds;

    public PVPTagger(PVPToolkit toolkit) {
        pvptoolkit = toolkit;
        pvpTagDuration = pvptoolkit.getPvpTagDuration();
        pvpTagBlockedCmds = pvptoolkit.getPvpTagBlockedCmds();

    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(event.isCancelled())return;
        Player player = event.getPlayer();
        boolean notallowed = false;
        String command = event.getMessage().toLowerCase().substring(1, event.getMessage().length());
        stopTagging(player);
        if (isTagged(player)) {
            for (String cmd : pvpTagBlockedCmds)
                if (command.toLowerCase().startsWith(cmd)) {
                    notallowed = true;
                    break;
                }
            if (notallowed) {
                if (command.toLowerCase().startsWith("fly") && player.isFlying()) return;
                player.sendMessage(ChatColor.DARK_RED + "/" + command + " is disabled in combat");
                event.setCancelled(true);
                return;
            }
        }
    }

    private void startTagging(final Player player) {
        taggedPlayers.put(player.getName(), System.currentTimeMillis());
    }

    private void stopTagging(Player player) {
        long millis = System.currentTimeMillis();
        if (taggedPlayers.containsKey(player.getName())) {
            if (millis - taggedPlayers.get(player.getName()).longValue() >= (pvpTagDuration * 1000)) {
                taggedPlayers.remove(player.getName());
            }
        }
    }

    private void resetTagging(Player player) {
        if (taggedPlayers.containsKey(player.getName())) {
            stopTagging(player);
            startTagging(player);
        }

    }

    public boolean isTagged(Player player) {
        stopTagging(player);
        if (taggedPlayers.containsKey(player.getName())) return true;
        return false;

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
                Player tagged = (Player) e.getEntity();
                if (!damager.hasPermission("pvptoolkit.blocker.nottagable"))
                    if (isTagged(damager)) {
                        resetTagging(damager);
                    } else startTagging(damager);
                if (!tagged.hasPermission("pvptoolkit.blocker.nottagable")) if (isTagged(tagged)) {
                    resetTagging(tagged);
                } else startTagging(tagged);
            }
        }

    }
}

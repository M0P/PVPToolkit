package com.minecraftserver.pvptoolkit;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVPTagger implements Listener {

    private final PVPToolkit pvptoolkit;
    private       int        pvpTagDuration;

    private final HashMap<String, Long> taggedPlayers = new HashMap<>();

    private List<String> pvpTagBlockedCmds;
    public final String MODULVERSION = "1.2.12";
    private boolean enabled;

    public PVPTagger(PVPToolkit toolkit) {
        this.pvptoolkit = toolkit;
        this.pvpTagDuration = this.pvptoolkit.getPvpTagDuration();
        this.pvpTagBlockedCmds = this.pvptoolkit.getPvpTagBlockedCmds();
        this.enabled = true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void checkTaggedPlayers() {
        HashMap<String, Long> tempMap = (HashMap<String, Long>) this.taggedPlayers.clone();
        for(Map.Entry<String, Long> stringLongEntry : tempMap.entrySet()) {
            Map.Entry pairs = (Map.Entry) stringLongEntry;
            String key = (String) pairs.getKey();
            Player player = this.pvptoolkit.getServer().getPlayer(key);
            if(stopTagging(key) && player != null) player.sendMessage(ChatColor.GOLD + "You are no longer tagged");
        }

    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isTagged(Player player) {
        stopTagging(player.getName());
        return taggedPlayers.containsKey(player.getName());
    }

    @EventHandler()
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(event.isCancelled() || !this.enabled) return;
        Player player = event.getPlayer();
        boolean notallowed = false;
        String command = event.getMessage().toLowerCase().substring(1, event.getMessage().length());
        stopTagging(player.getName());
        if(isTagged(player)) {
            for(String cmd : this.pvpTagBlockedCmds)
                if(command.toLowerCase().startsWith(cmd)) {
                    notallowed = true;
                    break;
                }
            if(notallowed) {
                if((command.toLowerCase().startsWith("fly") || command.toLowerCase().startsWith("c windwalk")) &&
                           player.getAllowFlight())
                    return;
                player.sendMessage(ChatColor.DARK_RED + "/" + command + " is disabled in combat");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if(event.isCancelled() || (event.getDamage() == 0) || !this.enabled) {
            return;
        }
        if(event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            Entity dmgr = e.getDamager();
            if(dmgr instanceof Projectile) {
                dmgr = ((Projectile) dmgr).getShooter();
            }
            if((dmgr instanceof Player) && (e.getEntity() instanceof Player)) {
                Player damager = (Player) dmgr;
                Player receiver = (Player) e.getEntity();
                if(!damager.getAllowFlight()) {
                    if(!damager.hasPermission("pvptoolkit.blocker.nottagable") && !damager.isOp() && !damager
                                                                                                              .hasPermission("pvptoolkit.admin"))
                        if(isTagged(damager)) {
                            resetTagging(damager);
                        }
                        else startTagging(damager);
                    if(!receiver.hasPermission("pvptoolkit.blocker.nottagable") && !receiver.isOp() && !receiver
                                                                                                                .hasPermission("pvptoolkit.admin"))
                        if(isTagged(receiver)) {
                            resetTagging(receiver);
                        }
                        else startTagging(receiver);
                }
            }
        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(isTagged(event.getEntity())) this.taggedPlayers.remove(event.getEntity().getName());
    }

    public void reloadcfg() {
        this.pvpTagDuration = this.pvptoolkit.getPvpTagDuration();
        this.pvpTagBlockedCmds = this.pvptoolkit.getPvpTagBlockedCmds();

    }

    public void startTagging(final Player player) {
        this.taggedPlayers.put(player.getName(), System.currentTimeMillis());
    }

    private void resetTagging(Player player) {
        if(this.taggedPlayers.containsKey(player.getName())) {
            stopTagging(player.getName());
            startTagging(player);
        }

    }

    private boolean stopTagging(String playername) {
        long millis = System.currentTimeMillis();
        if(taggedPlayers.containsKey(playername)) {
            if(millis - taggedPlayers.get(playername) >= (pvpTagDuration * 1000)) {
                taggedPlayers.remove(playername);
                return true;
            }
        }
        return false;
    }
}

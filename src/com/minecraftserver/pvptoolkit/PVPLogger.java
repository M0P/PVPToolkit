package com.minecraftserver.pvptoolkit;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PVPLogger {
    PVPToolkit            pvptoolkit;
    PVPTagger             pvpTagger;
    HashMap<String, Long> loggedPlayers = new HashMap<>(); ;
    private int           runningTask;

    public PVPLogger(PVPToolkit toolkit) {
        pvptoolkit = toolkit;
        pvpTagger = pvptoolkit.getPvptagger();
        runningTask = pvptoolkit.getServer().getScheduler()
                .scheduleAsyncRepeatingTask(pvptoolkit, new Runnable() {
                    @Override
                    public void run() {
                        long currentmillis = System.currentTimeMillis();
                        Iterator iterator = loggedPlayers.entrySet().iterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next().toString();
                            Long val = loggedPlayers.get(key).longValue();
                            int deathtime = pvptoolkit.getPvpLogDuration();
                            OfflinePlayer player;
                            if (currentmillis >= (val + deathtime * 1000)) {
                                player = pvptoolkit.getServer().getOfflinePlayer(key);
                                Player deadplayer = player.getPlayer();
                                deadplayer.loadData();
                                Inventory inv = deadplayer.getInventory();
                                for (ItemStack is : inv) {
                                    deadplayer.getWorld().dropItemNaturally(
                                            deadplayer.getLocation(), is);
                                }
                                deadplayer.setHealth(0);
                                deadplayer.saveData();
                                loggedPlayers.remove(key);
                            }
                        }
                    }
                }, 60L, 20L);
    }

    @EventHandler
    public void PlayerLogOut(PlayerQuitEvent event) {
        if (pvpTagger.isTagged(event.getPlayer())
                && !event.getPlayer().hasPermission("pvptoolkit.logger.notlogable")
                && !event.getPlayer().isOp())
            loggedPlayers.put(event.getPlayer().getName(), System.currentTimeMillis());
    }

    @EventHandler
    public void PlayerLogIn(PlayerJoinEvent event) {
        if (loggedPlayers.containsKey(event.getPlayer().getName()))
            loggedPlayers.remove(event.getPlayer().getName());
    }

}

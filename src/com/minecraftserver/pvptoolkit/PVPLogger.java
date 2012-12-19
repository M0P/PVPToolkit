package com.minecraftserver.pvptoolkit;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PVPLogger implements Listener {
    PVPToolkit            pvptoolkit;
    PVPTagger             pvpTagger;
    PVPIOManager          datamanager;
    List<String>          markedtoremoval = new Vector<>();
    HashMap<String, Long> loggedPlayers   = new HashMap<>(); ;
    private int           runningTask;
    List<Player>          offlinePlayers  = new Vector<>();
    List<String>          deadplayers;

    public PVPLogger(PVPToolkit toolkit) {
        pvptoolkit = toolkit;
        pvpTagger = pvptoolkit.getPvptagger();
        datamanager = new PVPIOManager();
        datamanager.init(pvptoolkit);
        deadplayers = datamanager.loadLoggerData();
        if (deadplayers == null) deadplayers = new Vector<>();
        runningTask = pvptoolkit.getServer().getScheduler()
                .scheduleAsyncRepeatingTask(pvptoolkit, new Runnable() {
                    @Override
                    public void run() {
                        long currentmillis = System.currentTimeMillis();
                        Iterator iterator = loggedPlayers.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry pairs = (Map.Entry) iterator.next();
                            Bukkit.broadcastMessage(pairs.getKey() + "=" + pairs.getValue());
                            String key = (String) pairs.getKey();
                            Long val = (Long) pairs.getValue();
                            Bukkit.broadcastMessage("Found " + key + " as pvp logged player");
                            int logduration = pvptoolkit.getPvpLogDuration();
                            Bukkit.broadcastMessage("Deathtime:" + val + " || current time:"
                                    + currentmillis + " || difference:" + (currentmillis - val));
                            if (currentmillis >= (val + logduration * 1000)) {
                                Player deadplayer = null;
                                for (Player player : offlinePlayers)
                                    if (player.getName().equals(key)) deadplayer = player;
                                if (deadplayer != null) {
                                    Bukkit.broadcastMessage("Loaded Player data, clearing inv");
                                    Inventory inv = deadplayer.getInventory();
                                    for (ItemStack is : inv) {
                                        if (is != null)
                                            deadplayer.getWorld().dropItemNaturally(
                                                    deadplayer.getLocation(), is);
                                    }
                                    Bukkit.broadcastMessage("cleared inv, kill him");
                                    markedtoremoval.add(key);
                                    offlinePlayers.remove(deadplayer);
                                    deadplayers.add(deadplayer.getName());
                                    Bukkit.broadcastMessage("all done");
                                } else Bukkit
                                        .broadcastMessage("Player not found, something went wrong");// TODO
                            }
                        }
                        for (String s : markedtoremoval)
                            loggedPlayers.remove(s);
                        markedtoremoval.clear();
                    }
                }, 60L, 100L);
        pvptoolkit.getLogger().log(Level.INFO, "PVP Logger really loaded, cycle started...");
    }

    @EventHandler
    public void PlayerLogOut(PlayerQuitEvent event) {
        Bukkit.broadcastMessage(event.getPlayer().getName() + " logged out and is tagged? :"
                + pvpTagger.isTagged(event.getPlayer()));
        if (pvpTagger.isTagged(event.getPlayer())
                && !event.getPlayer().hasPermission("pvptoolkit.logger.notlogable")
                && !event.getPlayer().isOp()) {
            Bukkit.broadcastMessage(event.getPlayer().getName() + " logged out while tagged");
            loggedPlayers.put(event.getPlayer().getName(), System.currentTimeMillis());
            offlinePlayers.add(event.getPlayer());
        }
    }

    @EventHandler
    public void PlayerLogIn(PlayerJoinEvent event) {
        if (loggedPlayers.containsKey(event.getPlayer().getName())) {
            markedtoremoval.add(event.getPlayer().getName());
            for (Player player : offlinePlayers)
                if (player.getName().equals(event.getPlayer().getName())) {
                    offlinePlayers.remove(player);
                    break;
                }
        }
        if (deadplayers.contains(event.getPlayer().getName())) {
            event.getPlayer().getInventory().clear();
            event.getPlayer().setHealth(0);
            deadplayers.remove(event.getPlayer().getName());
        }
    }

    public void saveData() {
        datamanager.saveLoggerData(deadplayers);
    }

}

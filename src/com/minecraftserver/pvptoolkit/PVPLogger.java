package com.minecraftserver.pvptoolkit;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    public final String   MODULVERSION    = "1.0";
    private boolean       enabled;

    public PVPLogger(PVPToolkit toolkit) {
        pvptoolkit = toolkit;
        enabled = true;
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
                            String key = (String) pairs.getKey();
                            Long val = (Long) pairs.getValue();
                            int logduration = pvptoolkit.getPvpLogDuration();
                            if (currentmillis >= (val + logduration * 1000)) {
                                Player deadplayer = null;
                                for (Player player : offlinePlayers)
                                    if (player.getName().equals(key)) deadplayer = player;
                                if (deadplayer != null) {
                                    Inventory inv = deadplayer.getInventory();
                                    for (ItemStack is : inv) {
                                        if (is != null)
                                            deadplayer.getWorld().dropItemNaturally(
                                                    deadplayer.getLocation(), is);
                                    }
                                    markedtoremoval.add(key);
                                    offlinePlayers.remove(deadplayer);
                                    Bukkit.broadcastMessage(ChatColor.AQUA + deadplayer.getName()
                                            + ChatColor.RED + " died because he PvP logged");
                                    deadplayers.add(deadplayer.getName());
                                } else pvptoolkit.getLogger().log(Level.WARNING,
                                        "PVP Logger: Couldnt find Playerdata of logged player");
                            }
                        }
                        for (String s : markedtoremoval)
                            loggedPlayers.remove(s);
                        markedtoremoval.clear();
                        pvpTagger.checkTaggedPlayers();
                    }
                }, 60L, 20L);
    }

    @EventHandler
    public void PlayerLogOut(PlayerQuitEvent event) {
        if (enabled && pvpTagger.isTagged(event.getPlayer())
                && !event.getPlayer().hasPermission("pvptoolkit.logger.notlogable")
                && !event.getPlayer().isOp()
                && !event.getPlayer().hasPermission("pvptoolkit.admin")) {
            loggedPlayers.put(event.getPlayer().getName(), System.currentTimeMillis());
            offlinePlayers.add(event.getPlayer());
        }
    }

    @EventHandler
    public void PlayerLogIn(PlayerJoinEvent event) {
        if (enabled && loggedPlayers.containsKey(event.getPlayer().getName())) {
            pvpTagger.startTagging(event.getPlayer());
            markedtoremoval.add(event.getPlayer().getName());
            for (Player player : offlinePlayers)
                if (player.getName().equals(event.getPlayer().getName())) {
                    offlinePlayers.remove(player);
                    break;
                }
        }
        if (enabled && deadplayers.contains(event.getPlayer().getName())) {
            event.getPlayer().getInventory().clear();
            event.getPlayer().setHealth(0);
            deadplayers.remove(event.getPlayer().getName());
        }
    }

    public void saveData() {
        datamanager.saveLoggerData(deadplayers);
    }

    public void disable() {
        enabled = false;
        pvptoolkit.getServer().getScheduler().cancelTask(runningTask);

    }

}

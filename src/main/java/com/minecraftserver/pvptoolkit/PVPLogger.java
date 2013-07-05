package com.minecraftserver.pvptoolkit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PVPLogger implements Listener {
    PVPToolkit            pvptoolkit;
    PVPTagger             pvpTagger;
    PVPIOManager          datamanager;
    List<String>          markedtoremoval = new Vector<>();
    HashMap<String, Long> loggedPlayers   = new HashMap<>();
    private final int     runningTask;
    List<Player>          offlinePlayers  = new Vector<>();
    List<String>          deadplayers;

    public final String   MODULVERSION    = "1.12";
    private boolean       enabled;

    public PVPLogger(PVPToolkit toolkit) {
        this.pvptoolkit = toolkit;
        this.enabled = true;
        this.pvpTagger = this.pvptoolkit.getPvptagger();
        this.datamanager = new PVPIOManager();
        PVPIOManager.init(this.pvptoolkit);
        this.deadplayers = PVPIOManager.loadLoggerData();
        if (this.deadplayers == null) this.deadplayers = new Vector<>();
        this.runningTask = this.pvptoolkit.getServer().getScheduler()
                .scheduleSyncRepeatingTask(this.pvptoolkit, new Runnable() {
                    @Override
                    public void run() {
                        long currentmillis = System.currentTimeMillis();
                        Iterator<?> iterator = PVPLogger.this.loggedPlayers
                                .entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry pairs = (Map.Entry) iterator.next();
                            String key = (String) pairs.getKey();
                            Long val = (Long) pairs.getValue();
                            int logduration = PVPLogger.this.pvptoolkit
                                    .getPvpLogDuration();
                            if (currentmillis >= (val + logduration * 1000)) {
                                Player deadplayer = null;
                                for (Player player : PVPLogger.this.offlinePlayers)
                                    if (player.getName().equals(key))
                                        deadplayer = player;
                                if (deadplayer != null) {
                                    Inventory inv = deadplayer.getInventory();
                                    for (ItemStack is : inv) {
                                        if (is != null && is.getTypeId() != 00)
                                            deadplayer
                                                    .getWorld()
                                                    .dropItemNaturally(
                                                            deadplayer
                                                                    .getLocation(),
                                                            is);
                                    }
                                    ItemStack[] armor = deadplayer
                                            .getInventory().getArmorContents();
                                    for (ItemStack is : armor) {
                                        if (is != null && is.getTypeId() != 00)
                                            deadplayer
                                                    .getWorld()
                                                    .dropItemNaturally(
                                                            deadplayer
                                                                    .getLocation(),
                                                            is);
                                    }
                                    PVPLogger.this.markedtoremoval.add(key);
                                    PVPLogger.this.offlinePlayers
                                            .remove(deadplayer);
                                    Bukkit.broadcastMessage(ChatColor.AQUA
                                            + deadplayer.getName()
                                            + ChatColor.RED
                                            + " died because he PvP logged");
                                    PVPLogger.this.deadplayers.add(deadplayer
                                            .getName());
                                } else PVPLogger.this.pvptoolkit
                                        .getLogger()
                                        .log(Level.WARNING,
                                                "PVP Logger: Couldnt find Playerdata of logged player");
                            }
                        }
                        for (String s : PVPLogger.this.markedtoremoval)
                            PVPLogger.this.loggedPlayers.remove(s);
                        PVPLogger.this.markedtoremoval.clear();
                        PVPLogger.this.pvpTagger.checkTaggedPlayers();
                    }
                }, 60L, 20L);
    }

    public void disable() {
        this.enabled = false;
        this.pvptoolkit.getServer().getScheduler().cancelTask(this.runningTask);

    }

    @EventHandler
    public void PlayerLogIn(PlayerJoinEvent event) {
        if (this.enabled
                && this.loggedPlayers.containsKey(event.getPlayer().getName())) {
            this.pvpTagger.startTagging(event.getPlayer());
            this.markedtoremoval.add(event.getPlayer().getName());
            for (Player player : this.offlinePlayers)
                if (player.getName().equals(event.getPlayer().getName())) {
                    this.offlinePlayers.remove(player);
                    break;
                }
        }
        if (this.enabled
                && this.deadplayers.contains(event.getPlayer().getName())) {
            event.getPlayer().getInventory().clear();
            event.getPlayer().getInventory().setArmorContents(null);
            event.getPlayer().setHealth(0);
            this.deadplayers.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void PlayerLogOut(PlayerQuitEvent event) {
        if (this.enabled
                && this.pvpTagger.isTagged(event.getPlayer())
                && !event.getPlayer().hasPermission(
                        "pvptoolkit.logger.notlogable")
                && !event.getPlayer().isOp()
                && !event.getPlayer().hasPermission("pvptoolkit.admin")) {
            this.loggedPlayers.put(event.getPlayer().getName(),
                    System.currentTimeMillis());
            this.offlinePlayers.add(event.getPlayer());
        }
    }

    public void saveData() {
        PVPIOManager.saveLoggerData(this.deadplayers);
    }

}

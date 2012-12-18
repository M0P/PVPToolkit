package com.minecraftserver.pvptoolkit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PVPToolkit extends JavaPlugin {
    protected final static Logger logger = Logger.getLogger("Minecraft");
    public static final String    name   = "PVPToolkit";

    private int                   pvpTagDuration;
    private int                   pvpLogDuration;
    private int                   trackingdistance;

    private boolean               pvpBlockAttackAllowed;

    private List<String>          pvpTagBlockedCmds;

    private boolean               pvpTagEnabled;
    private boolean               pvpBlockerEnabled;
    private boolean               pvpLoggerEnabled;
    private boolean               playerTrackerEnabled;
    private boolean               pvpFlyEnabled;

    private PVPTagger             pvptagger;
    private PVPLogger             pvplogger;
    private PVPBlocker            pvpblocker;
    private PVPBlockerPassword    pvpblockerPassword;
    private PVPFly                pvpfly;

    private PlayerTracker         playertracker;

    public void loadConfiguration() {
        FileConfiguration cfg = this.getConfig();

        pvpTagEnabled = cfg.getBoolean("modules.pvptag.enabled");
        pvpBlockerEnabled = cfg.getBoolean("modules.pvpblock.enabled");
        pvpLoggerEnabled = cfg.getBoolean("modules.pvplog.enabled");
        playerTrackerEnabled = cfg.getBoolean("modules.playertracker.enabled");
        pvpFlyEnabled = cfg.getBoolean("modules.pvpfly.enabled");

        if (pvpTagEnabled) {
            pvpTagDuration = cfg.getInt("modules.pvptag.duration", 20);
            pvpTagBlockedCmds = cfg.getStringList("modules.pvptag.blockedcmds");
            if (pvpLoggerEnabled) pvpLogDuration = cfg.getInt("modules.pvplog.duration");
        }
        if (pvpBlockerEnabled)
            pvpBlockAttackAllowed = cfg.getBoolean("modules.pvpblock.allow_attack");
        if (playerTrackerEnabled)
            trackingdistance = cfg.getInt("modules.playertracker.trackdistance", 350);
    }

    public void saveConfiguration() {
        this.saveConfig();
    }

    private void enableModules() {
        PluginManager pm = getServer().getPluginManager();
        if (pvpTagEnabled) {
            pvptagger = new PVPTagger(this);
            pm.registerEvents(pvptagger, this);
            if (pvpBlockerEnabled) {
                pvpblocker = new PVPBlocker(this);
                pm.registerEvents(pvpblocker, this);
            }
        }
        if (pvpBlockerEnabled) {
            pvpblocker = new PVPBlocker(this);
            pm.registerEvents(pvpblocker, this);
        }
        if (playerTrackerEnabled) {
            playertracker = new PlayerTracker(this);
            pm.registerEvents(playertracker, this);
        }
        if (pvpFlyEnabled) {
            pvpfly = new PVPFly(this);
            pm.registerEvents(pvpfly, this);
        }

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("pvp")) {
                if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                    if (pvpTagEnabled && !player.hasPermission("pvptoolkit.blocker.nottagable"))
                        player.sendMessage(ChatColor.BLUE + "Type " + ChatColor.GOLD + "/pvp tag"
                                + ChatColor.BLUE + " to see if you are tagged");
                } else {
                    if (args[0].equalsIgnoreCase("tag") && pvpTagEnabled
                            && !player.hasPermission("pvptoolkit.blocker.nottagable")) {
                        if (pvptagger.isTagged(player))
                            player.sendMessage(ChatColor.BLUE + "You are currently "
                                    + ChatColor.RED + "tagged");
                        else player.sendMessage(ChatColor.BLUE + "You are currently "
                                + ChatColor.RED + "not tagged");

                    }
                }
                return false;
            } else {
                if (args[0].equalsIgnoreCase("fly") && pvpFlyEnabled) {
                    pvpfly.togglefly(sender, args);
                }
                return true;
            }
        }
        return false;
    }

    public boolean isPvpBlockAttackAllowed() {
        return pvpBlockAttackAllowed;
    }

    public int getPvpTagDuration() {
        return pvpTagDuration;
    }

    public PVPTagger getPvptagger() {
        return pvptagger;
    }

    public int getPvpLogDuration() {
        return pvpLogDuration;
    }

    public List<String> getPvpTagBlockedCmds() {
        return pvpTagBlockedCmds;
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile pdfFile = this.getDescription();

        loadConfiguration();
        saveConfiguration();

        enableModules();

        log(name + " - Version " + pdfFile.getVersion() + " is enabled");

    }

    @Override
    public void onDisable() {
        if (pvpblockerPassword != null) pvpblockerPassword.saveData();
        log(name + " is Disabled");
    }

    public static void log(String txt) {
        logger.log(Level.INFO, String.format("[%s] %s", name, txt));
    }

    public int getTrackingdistance() {
        return trackingdistance;
    }
}

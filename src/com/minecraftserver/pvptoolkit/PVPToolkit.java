package com.minecraftserver.pvptoolkit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PVPToolkit extends JavaPlugin {
    protected final static Logger logger = Logger.getLogger("Minecraft");
    public static final String    name   = "PVPToolkit";

    public static void log(String txt) {
        logger.log(Level.INFO, String.format("[%s] %s", name, txt));
    }

    private int                    pvpTagDuration;
    private int                    pvpLogDuration;

    private int                    smalltrackingdistance;
    private int                    advancedtrackingdistance;

    private int                    spawnprotectradius;

    private boolean                pvpBlockAttackAllowed;

    private List<String>           pvpTagBlockedCmds;
    private boolean                pvpTagEnabled;
    private boolean                pvpBlockerEnabled;
    private boolean                pvpLoggerEnabled;
    private boolean                playerTrackerEnabled;

    private boolean                pvpFlyEnabled;
    private PVPTagger              pvptagger;
    private PVPLogger              pvplogger;

    private PVPFly                 pvpfly;
    private PlayerTracker          playertracker;
    private PVPSpawnCampProtection spawnProtect;

    private PluginManager          pm;
    private boolean                spawnProtectEnabled;

    public int getadvancedTrackingdistance() {
        return advancedtrackingdistance;
    }

    public int getPvpLogDuration() {
        return pvpLogDuration;
    }

    public List<String> getPvpTagBlockedCmds() {
        return pvpTagBlockedCmds;
    }

    public int getPvpTagDuration() {
        return pvpTagDuration;
    }

    public PVPTagger getPvptagger() {
        return pvptagger;
    }

    public int getsmallTrackingdistance() {
        return smalltrackingdistance;
    }

    public int getspawnprotectradius() {
        return spawnprotectradius;
    }

    public boolean isPvpBlockAttackAllowed() {
        return pvpBlockAttackAllowed;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("pvp")) {
                if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                    if (pvpTagEnabled
                            && !player
                                    .hasPermission("pvptoolkit.blocker.nottagable"))
                        player.sendMessage(ChatColor.BLUE + "Type "
                                + ChatColor.GOLD + "/pvp tag" + ChatColor.BLUE
                                + " to see if you are tagged");
                    if (pvpFlyEnabled
                            && (player.hasPermission("pvptoolkit.fly.fly") || player
                                    .hasPermission("pvptoolkit.admin")))
                        player.sendMessage(ChatColor.BLUE + "Type "
                                + ChatColor.GOLD + "/fly help" + ChatColor.BLUE
                                + " for information about the fly command");
                    if (playerTrackerEnabled
                            && (player
                                    .hasPermission("pvptoolkit.playertracker.cantrack") || player
                                    .hasPermission("pvptoolkit.admin")))
                        player.sendMessage(ChatColor.BLUE
                                + "Right click a block using a "
                                + ChatColor.GOLD + " compass " + ChatColor.BLUE
                                + " to track the nearest player around you");
                } else {
                    if (args[0].equalsIgnoreCase("tag") && pvpTagEnabled) {
                        if (pvptagger.isTagged(player))
                            player.sendMessage(ChatColor.BLUE
                                    + "You are currently " + ChatColor.RED
                                    + "tagged");
                        else player.sendMessage(ChatColor.BLUE
                                + "You are currently " + ChatColor.RED
                                + "not tagged");
                    } else if (args[0].equalsIgnoreCase("reload-cfg")
                            && player.hasPermission("pvptoolkit.admin")) {
                        reloadcfg();
                    } else if (args[0].equalsIgnoreCase("version")) {
                        player.sendMessage(ChatColor.BLUE + "Version: "
                                + ChatColor.GOLD + this.getVersion() + " \n"
                                + ChatColor.BLUE + "Made by " + ChatColor.GOLD
                                + "M0P\n" + ChatColor.BLUE + "Thanks to "
                                + ChatColor.GOLD + "AquaXV" + ChatColor.BLUE
                                + " for helping and testing alot.");
                        if (pvpFlyEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "Fly module version: " + ChatColor.GOLD
                                    + pvpfly.MODULVERSION);
                        if (pvpLoggerEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "PVPlogger module version: "
                                    + ChatColor.GOLD + pvplogger.MODULVERSION);
                        if (pvpTagEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "PVPtagger module version: "
                                    + ChatColor.GOLD + pvptagger.MODULVERSION);
                        if (playerTrackerEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "Playertracker module version: "
                                    + ChatColor.GOLD
                                    + playertracker.MODULVERSION);
                        if (spawnProtectEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "SpawnCampProtection module version: "
                                    + ChatColor.GOLD
                                    + spawnProtect.MODULVERSION);
                    }
                }
                return false;
            } else {
                if (cmd.getName().equalsIgnoreCase("fly") && pvpFlyEnabled) {
                    pvpfly.togglefly(sender, args);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        // if (pvpblockerPassword != null) pvpblockerPassword.saveData();
        if (pvpLoggerEnabled) pvplogger.saveData();
        log(name + " is Disabled");

    }

    @Override
    public void onEnable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        PVPIOManager.init(this);
        loadConfiguration();
        enableModules();

        log(name + " - Version " + pdfFile.getVersion() + " is enabled");

    }

    public void reloadcfg() {
        loadConfiguration();
        if (spawnProtectEnabled) spawnProtect.reloadcfg();
        if (pvpTagEnabled) pvptagger.reloadcfg();
        if (playerTrackerEnabled) playertracker.reloadcfg();
    }

    private void enableModules() {
        pm = getServer().getPluginManager();
        if (pvpTagEnabled) {
            if (pvptagger == null) pvptagger = new PVPTagger(this);
            pm.registerEvents(pvptagger, this);
            this.getLogger().log(Level.INFO, "PVP Tagger loaded");

            if (pvpLoggerEnabled) {
                if (pvplogger == null) pvplogger = new PVPLogger(this);
                pm.registerEvents(pvplogger, this);
                this.getLogger().log(Level.INFO, "PVP Logger loaded");

            } else if (pvplogger != null) {
                pvplogger.disable();
                pvplogger = null;
            }
        } else if (pvptagger != null) {
            pvptagger.disable();
            pvptagger = null;
        }

        if (playerTrackerEnabled) {
            if (playertracker == null) playertracker = new PlayerTracker(this);
            pm.registerEvents(playertracker, this);
            this.getLogger().log(Level.INFO, "Player Tracker loaded");
        } else if (playertracker != null) {
            playertracker.disable();
            playertracker = null;
        }
        if (pvpFlyEnabled) {
            if (pvpfly == null) pvpfly = new PVPFly(this);
            pm.registerEvents(pvpfly, this);
            this.getLogger().log(Level.INFO, "PVP Fly loaded");
        } else if (pvpfly != null) {
            pvpfly.disable();
            pvpfly = null;
        }
        if (spawnProtectEnabled) {
            if (spawnProtect == null)
                spawnProtect = new PVPSpawnCampProtection(this);
            pm.registerEvents(spawnProtect, this);
            this.getLogger().log(Level.INFO, "Respawn Protection loaded");
        }

    }

    private String getVersion() {
        return this.getDescription().getVersion();
    }

    private void loadConfiguration() {
        YamlConfiguration cfg = PVPIOManager.loadConfig();
        if (cfg == null) {
            PVPIOManager.firstRun();
            cfg = PVPIOManager.loadConfig();
            this.getLogger()
                    .log(Level.WARNING,
                            "PVPToolkit: Config could not be loaded, using default config");
        }

        pvpTagEnabled = cfg.getBoolean("modules.pvptag.enabled");
        pvpLoggerEnabled = cfg.getBoolean("modules.pvplog.enabled");
        playerTrackerEnabled = cfg.getBoolean("modules.playertracker.enabled");
        spawnProtectEnabled = cfg.getBoolean("modules.spawnprotect.enabled");
        pvpFlyEnabled = cfg.getBoolean("modules.pvpfly.enabled");

        if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") == null) {
            spawnProtectEnabled = false;
            PVPToolkit.log("Cant run RespawnProtection: Essentials is missing");
        }

        if (pvpTagEnabled) {
            pvpTagDuration = cfg.getInt("modules.pvptag.duration", 20);
            pvpTagBlockedCmds = cfg.getStringList("modules.pvptag.blockedcmds");
            if (pvpLoggerEnabled)
                pvpLogDuration = cfg.getInt("modules.pvplog.duration");
        }
        if (playerTrackerEnabled) {
            smalltrackingdistance = cfg.getInt(
                    "modules.playertracker.smalltrackdistance", 350);
            advancedtrackingdistance = cfg.getInt(
                    "modules.playertracker.advancedtrackdistance", 600);
        }
        if (spawnProtectEnabled)
            spawnprotectradius = cfg.getInt("modules.spawnprotect.radius", 2);
    }
}

package com.minecraftserver.pvptoolkit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private boolean                pvpLoggerEnabled;
    private boolean                playerTrackerEnabled;

    private boolean                pvpFlyEnabled;
    private static PVPTagger       pvptagger;
    private PVPLogger              pvplogger;

    private PVPFly                 pvpfly;
    private PlayerTracker          playertracker;
    private PVPSpawnCampProtection spawnProtect;

    private PluginManager          pm;
    private boolean                spawnProtectEnabled;

    public int getadvancedTrackingdistance() {
        return this.advancedtrackingdistance;
    }

    public int getPvpLogDuration() {
        return this.pvpLogDuration;
    }

    public List<String> getPvpTagBlockedCmds() {
        return this.pvpTagBlockedCmds;
    }

    public int getPvpTagDuration() {
        return this.pvpTagDuration;
    }

    public PVPTagger getPvptagger() {
        return this.pvptagger;
    }

    public int getsmallTrackingdistance() {
        return this.smalltrackingdistance;
    }

    public int getspawnprotectradius() {
        return this.spawnprotectradius;
    }

    public boolean isPvpBlockAttackAllowed() {
        return this.pvpBlockAttackAllowed;
    }

    
    
    public static PVPTagger getPVPTagger() {
        return pvptagger;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
            String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("pvp")) {
                if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                    if (this.pvpTagEnabled
                            && !player
                                    .hasPermission("pvptoolkit.blocker.nottagable"))
                        player.sendMessage(ChatColor.BLUE + "Type "
                                + ChatColor.GOLD + "/pvp tag" + ChatColor.BLUE
                                + " to see if you are tagged");
                    if (this.pvpFlyEnabled
                            && (player.hasPermission("pvptoolkit.fly.fly") || player
                                    .hasPermission("pvptoolkit.admin")))
                        player.sendMessage(ChatColor.BLUE + "Type "
                                + ChatColor.GOLD + "/fly help" + ChatColor.BLUE
                                + " for information about the fly command");
                    if (this.playerTrackerEnabled
                            && (player
                                    .hasPermission("pvptoolkit.playertracker.cantrack") || player
                                    .hasPermission("pvptoolkit.admin")))
                        player.sendMessage(ChatColor.BLUE
                                + "Right click a block using a "
                                + ChatColor.GOLD + " compass " + ChatColor.BLUE
                                + " to track the nearest player around you");
                } else {
                    if (args[0].equalsIgnoreCase("tag") && this.pvpTagEnabled) {
                        if (this.pvptagger.isTagged(player))
                            player.sendMessage(ChatColor.BLUE
                                    + "You are currently " + ChatColor.RED
                                    + "tagged");
                        else player.sendMessage(ChatColor.BLUE
                                + "You are currently " + ChatColor.RED
                                + "not tagged");
                    } else if (args[0].equalsIgnoreCase("reload")
                            && player.hasPermission("pvptoolkit.admin")) {
                        reload();
                    } else if (args[0].equalsIgnoreCase("version")) {
                        player.sendMessage(ChatColor.BLUE + "Version: "
                                + ChatColor.GOLD + this.getVersion() + " \n"
                                + ChatColor.BLUE + "Made by " + ChatColor.GOLD
                                + "M0P\n" + ChatColor.BLUE + "Thanks to "
                                + ChatColor.GOLD + "AquaXV" + ChatColor.BLUE
                                + " for helping and testing alot.");
                        if (this.pvpFlyEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "Fly module version: " + ChatColor.GOLD
                                    + this.pvpfly.MODULVERSION);
                        if (this.pvpLoggerEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "PVPlogger module version: "
                                    + ChatColor.GOLD
                                    + this.pvplogger.MODULVERSION);
                        if (this.pvpTagEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "PVPtagger module version: "
                                    + ChatColor.GOLD
                                    + this.pvptagger.MODULVERSION);
                        if (this.playerTrackerEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "Playertracker module version: "
                                    + ChatColor.GOLD
                                    + this.playertracker.MODULVERSION);
                        if (this.spawnProtectEnabled)
                            player.sendMessage(ChatColor.BLUE
                                    + "SpawnCampProtection module version: "
                                    + ChatColor.GOLD
                                    + this.spawnProtect.MODULVERSION);
                    }
                }
                return false;
            }
            if (cmd.getName().equalsIgnoreCase("fly") && this.pvpFlyEnabled) {
                this.pvpfly.togglefly(sender, args);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        // if (pvpblockerPassword != null) pvpblockerPassword.saveData();
        if (this.pvpLoggerEnabled) this.pvplogger.saveData();
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

    public void reload() {
        loadConfiguration();
        enableModules();
        if (this.spawnProtectEnabled) this.spawnProtect.reloadcfg();
        if (this.pvpTagEnabled) this.pvptagger.reloadcfg();
        if (this.playerTrackerEnabled) this.playertracker.reloadcfg();
    }

    private void enableModules() {
        this.pm = getServer().getPluginManager();
        if (this.pvpTagEnabled) {
            if (this.pvptagger == null) {
                this.pvptagger = new PVPTagger(this);
                this.pm.registerEvents(this.pvptagger, this);
                this.getLogger().log(Level.INFO, "PVP Tagger loaded");
            }
        }
        if (this.pvpLoggerEnabled && this.pvpTagEnabled) {
            if (this.pvplogger == null) {
                this.pvplogger = new PVPLogger(this);
                this.pm.registerEvents(this.pvplogger, this);
                this.getLogger().log(Level.INFO, "PVP Logger loaded");
            }
        }
        if (this.playerTrackerEnabled) {
            if (this.playertracker == null) {
                this.playertracker = new PlayerTracker(this);
                this.pm.registerEvents(this.playertracker, this);
                this.getLogger().log(Level.INFO, "Player Tracker loaded");
            }
        }
        if (this.pvpFlyEnabled) {
            if (this.pvpfly == null) {
                this.pvpfly = new PVPFly(this);
                this.pm.registerEvents(this.pvpfly, this);
                this.getLogger().log(Level.INFO, "PVP Fly loaded");
            }
        }
        if (this.spawnProtectEnabled) {
            if (this.spawnProtect == null) {
                this.spawnProtect = new PVPSpawnCampProtection(this);
                this.pm.registerEvents(this.spawnProtect, this);
                this.getLogger().log(Level.INFO, "Respawn Protection loaded");
            }
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

        this.pvpTagEnabled = cfg.getBoolean("modules.pvptag.enabled", false);
        this.pvpLoggerEnabled = cfg.getBoolean("modules.pvplog.enabled", false);
        this.playerTrackerEnabled = cfg.getBoolean(
                "modules.playertracker.enabled", false);
        this.spawnProtectEnabled = cfg.getBoolean(
                "modules.spawnprotect.enabled", false);
        this.pvpFlyEnabled = cfg.getBoolean("modules.pvpfly.enabled", false);

        if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") == null) {
            this.spawnProtectEnabled = false;
            PVPToolkit.log("Cant run RespawnProtection: Essentials is missing");
        }

        if (this.pvpTagEnabled) {
            this.pvpTagDuration = cfg.getInt("modules.pvptag.duration", 20);
            this.pvpTagBlockedCmds = cfg
                    .getStringList("modules.pvptag.blockedcmds");
            if (this.pvpLoggerEnabled)
                this.pvpLogDuration = cfg.getInt("modules.pvplog.duration");
        }
        if (this.playerTrackerEnabled) {
            this.smalltrackingdistance = cfg.getInt(
                    "modules.playertracker.smalltrackdistance", 350);
            this.advancedtrackingdistance = cfg.getInt(
                    "modules.playertracker.advancedtrackdistance", 700);
        }
        if (this.spawnProtectEnabled)
            this.spawnprotectradius = cfg.getInt("modules.spawnprotect.radius",
                    2);
    }
}

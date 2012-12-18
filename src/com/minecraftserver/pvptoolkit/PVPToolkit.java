package com.minecraftserver.pvptoolkit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PVPToolkit extends JavaPlugin {
    protected final static Logger logger = Logger.getLogger("Minecraft");
    public static final String    name   = "PVPToolkit";

    private int                   pvpTagDuration;

    private boolean               pvpBlockAttackAllowed;

    private List<String>          pvpTagBlockedCmds;

    private Boolean               pvpTagEnabled;
    private Boolean               pvpBlockerEnabled;
    private Boolean               pvpLoggerEnabled;

    private PVPTagger             pvptagger;
    private PVPLogger             pvplogger;
    private PVPBlocker            pvpblocker;
    private PVPBlockerPassword    pvpblockerPassword;

    public void loadConfiguration() {
        FileConfiguration cfg = this.getConfig();

        pvpTagEnabled = cfg.getBoolean("modules.pvptag.enabled");
        pvpBlockerEnabled = cfg.getBoolean("modules.pvpblock.enabled");
        pvpLoggerEnabled = cfg.getBoolean("modules.pvplog.enabled");

        pvpTagDuration = cfg.getInt("modules.pvptag.duration", 20);
        pvpTagBlockedCmds = cfg.getStringList("modules.pvptag.blockedcmds");

        pvpBlockAttackAllowed = cfg.getBoolean("modules.pvpblock.allow_attack");

    }

    public void saveConfiguration() {
        this.saveConfig();
    }

    private void enableModules() {
        PluginManager pm = getServer().getPluginManager();
        if (pvpTagEnabled) {
            pvptagger = new PVPTagger(this);
            pm.registerEvents(pvptagger, this);
        }
        if (pvpBlockerEnabled) {
            pvpblocker = new PVPBlocker(this);
            pm.registerEvents(pvpblocker, this);
        }

    }

    public boolean isPvpBlockAttackAllowed() {
        return pvpBlockAttackAllowed;
    }

    public int getPvpTagDuration() {
        return pvpTagDuration;
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
}

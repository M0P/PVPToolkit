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

    private List<String>          pvpTagBlockedCmds;

    private Boolean               pvpTagEnabled;
    private Boolean               pvpLoggerEnabled;

    private PVPTagger             pvptagger;
    private PVPLogger             pvplogger;

    public void loadConfiguration() {
        FileConfiguration cfg = this.getConfig();

        pvpTagEnabled = cfg.getBoolean("modules.pvptag.enabled");
        pvpLoggerEnabled = cfg.getBoolean("modules.pvplog.enabled");

        pvpTagDuration = cfg.getInt("modules.pvptag.duration", 20);

        pvpTagBlockedCmds = cfg.getStringList("modules.pvptag.blockedcmds");

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
        log(name + " is Disabled");
    }

    public static void log(String txt) {
        logger.log(Level.INFO, String.format("[%s] %s", name, txt));
    }
}

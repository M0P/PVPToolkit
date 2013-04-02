package com.minecraftserver.pvptoolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public class PVPIOManager {
    static private PVPToolkit        plugin;
    static private YamlConfiguration config = new YamlConfiguration();
    static private File              configFile;

    public static void firstRun() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            copy(plugin.getResource("config.yml"), configFile);
        }
    }

    public static List<String> getBlockedPlayers() {
        PVPData data = loadData();
        if (data != null) return data.getBlockedPlayers();
        return null;
    }

    public static String getPVPPassword() {
        PVPData data = loadData();
        if (data != null) return data.getPassword();
        return null;
    }

    public static void init(PVPToolkit parent) {
        plugin = parent;
        configFile = new File(plugin.getDataFolder(), "config.yml");

    }

    public static YamlConfiguration loadConfig() {
        try {
            firstRun();
            config.load(configFile);
            return config;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> loadLoggerData() {
        PVPData data = loadData();
        if (data != null) return data.getDeadPlayers();
        return null;
    }

    public static void saveBlockerPassword(String password, List<String> list) {
        saveData(new PVPData(password, list));
    }

    // not needed yet, future use
    public static void saveConfig(YamlConfiguration newconfig) {
        try {
            newconfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveLoggerData(List<String> list) {
        saveData(new PVPData(list));
    }

    private static void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File getDir() {
        return new File(plugin.getDataFolder() + File.separator + "data");
    }

    private static PVPData loadData() {
        File dir = getDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File ownerFile = new File(dir + File.separator + "data.bin");
        try {
            if (!ownerFile.exists()) {
                saveData(new PVPData());
                return null;
            }
            FileInputStream fis = new FileInputStream(ownerFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            PVPData data = (PVPData) ois.readObject();
            ois.close();
            fis.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void saveData(PVPData data) {
        File dir = getDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File ownerFile = new File(dir + File.separator + "data.bin");
        try {
            if (!ownerFile.exists()) ownerFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(ownerFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.flush();
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.minecraftserver.pvptoolkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.security.acl.Owner;
import java.util.List;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class PVPIOManager {
    static private PVPToolkit plugin;

    public static void init(PVPToolkit parent) {
        plugin = parent;

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

    private static PVPData loadData() {
        File dir = getDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File ownerFile = new File(dir + File.separator + "data.bin");
        try {
            if (!ownerFile.exists()) {
                ownerFile.createNewFile();
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

    private static File getDir() {
        return new File(plugin.getDataFolder() + File.separator + "data");
    }

    public String getPVPPassword() {
        PVPData data = loadData();
        if (data != null) return data.getPassword();
        return null;
    }

    public List<String> getBlockedPlayers() {
        PVPData data = loadData();
        if (data != null) return data.getBlockedPlayers();
        return null;
    }

    public void saveBlockerPassword(String password, List<String> list) {
        saveData(new PVPData(password, list));
    }
    
    public void saveLoggerData(List<String>list){
        saveData(new PVPData(list));
    }
    public List<String> loadLoggerData(){
        PVPData data=loadData();
        return data.getDeadPlayers();
    }
}

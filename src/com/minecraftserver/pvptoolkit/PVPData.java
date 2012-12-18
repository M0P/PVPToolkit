package com.minecraftserver.pvptoolkit;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PVPData implements Serializable {

    final String                password;
    final List<String>          blockedPlayers;
    final HashMap<String, Date> joinedPlayers;

    public PVPData(String password, List<String> blockedPlayers) {
        this.password = password;
        this.blockedPlayers = blockedPlayers;
        joinedPlayers = null;
    }

    public PVPData(HashMap<String, Date> joinedPlayers) {
        this.password = null;
        this.blockedPlayers = null;
        this.joinedPlayers = joinedPlayers;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getBlockedPlayers() {
        return blockedPlayers;
    }

}

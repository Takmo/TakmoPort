// See license section in README.
package com.bitwisehero.takmoport;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;

public class TakmoTeleporterChunk {


    private int mX;
    private int mZ;
    private ArrayList<TakmoTeleporter> mTeleporters;


    public TakmoTeleporterChunk(int x, int z) {
        mX = x;
        mZ = z;
        mTeleporters = new ArrayList<TakmoTeleporter>();
    }


    public void add(TakmoTeleporter t) {
        mTeleporters.add(t);
    }


    public TakmoTeleporter checkPlayer(Player p) {
        for(TakmoTeleporter t : mTeleporters) {
            if(t.checkTeleportLocation(p))
                return t;
        }
        return null;
    }


    public ArrayList<TakmoTeleporter> getAllTeleporters() {
        return mTeleporters;
    }


    public int getX() {
        return mX;
    }


    public int getZ() {
        return mZ;
    }


    public boolean isEmpty() {
        return mTeleporters.isEmpty();
    }


    public void remove(TakmoTeleporter t) {
        mTeleporters.remove(t);
    }


}

// See license section in README.
package com.bitwisehero.takmoport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;

public class TakmoPortManager {


    private int mBaseBlockTypeId;
    private boolean mShowKeyInfo;
    private ArrayList<TakmoTeleporterChunk> mChunks;
    private HashMap<String,TakmoWaypoint> mWaypoints;


    public TakmoPortManager(int baseBlockTypeId, boolean showKeyInfo) {
        mBaseBlockTypeId = baseBlockTypeId;
        mShowKeyInfo = showKeyInfo;
        mChunks = new ArrayList<TakmoTeleporterChunk>();
        mWaypoints = new HashMap<String,TakmoWaypoint>();
    }


    public int addTeleporter(Location l, String n) {
        if(getWaypointAtLocation(l) != null || getTeleporterAtLocation(l) != null)
            return 2; // Waypoint or teleporter already exists here.
        TakmoWaypoint w = null;
        if(n != null) {
            w = getWaypointByName(n);
            if(w == null)
                return 3; // Not valid waypoint name.
        }
        TakmoTeleporterChunk c = getChunkFromLocation(l);
        if(c == null) {
            c = new TakmoTeleporterChunk(l.getChunk().getX(), l.getChunk().getZ());
            mChunks.add(c);
        }
        TakmoTeleporter t = new TakmoTeleporter(c, l, w);
        if(t.verify(mBaseBlockTypeId)) // When loading, make sure teleporter exists.
            c.add(new TakmoTeleporter(c, l, w));
        else
            return 4; // Teleporter did not verify.
        if(w == null)
            return 1; // Created temporary teleporter.
        return 0; // Created normal teleporter.
    }


    public int addWaypoint(String name, Location l, Material key, String permission) {
        if(mWaypoints.get(name) != null)
            return 1; // Waypoint name taken.
        if(getWaypointAtLocation(l) != null || getTeleporterAtLocation(l) != null)
            return 2; // Waypoint or teleporter already exists here.
        TakmoWaypoint w = new TakmoWaypoint(l, name, key, permission);
        if(w.verify(mBaseBlockTypeId)) // When loading, make sure waypoint exists.
            mWaypoints.put(name, w);
        else
            return 3; // Waypoint did not verify.
        return 0; // No error.
    }

    
    public int baseBroken(Location l) {
        TakmoWaypoint w = getWaypointAtLocation(l);
        if(w != null) {
            removeWaypoint(w);
            return 1; // Waypoint destroyed.
        }
        TakmoTeleporter t = getTeleporterAtLocation(l);
        if(t != null) {
            removeTeleporter(t);
            return 2; // Teleporter destroyed.
        }
        return 0; // Wasn't a teleporter or waypoint.
    }


    public int focusTeleporter(Location l, String n) {
        TakmoTeleporter t = getTeleporterAtLocation(l);
        if(t == null)
            return 2; // Location is not a teleporter.
        TakmoWaypoint w = null;
        if(n != null)
            w = getWaypointByName(n);
        if(w == null && n != null)
            return 3; // Not a valid desination waypoint.
        t.focus(w);
        if(w == null)
            return 1; // Removed focus, still working.
        return 0; // Updated focus with new destination.
    }


    public ArrayList<TakmoTeleporter> getAllTeleporters() {
        ArrayList<TakmoTeleporter> tl = new ArrayList<TakmoTeleporter>();
        for(TakmoTeleporterChunk c : mChunks)
            tl.addAll(c.getAllTeleporters());
        return tl;
    }


    private TakmoTeleporterChunk getChunkFromLocation(Location l) {
        for(TakmoTeleporterChunk t : mChunks) {
            if(l.getChunk().getX() == t.getX() && l.getChunk().getZ() == t.getZ())
                return t;
        }
        return null; // No chunk available.
    }


    public ArrayList<TakmoWaypoint> getAllWaypoints() {
        return new ArrayList<TakmoWaypoint>(mWaypoints.values());
    }


    public TakmoTeleporter getTeleporterAtLocation(Location l) {
        TakmoTeleporterChunk c = getChunkFromLocation(l);
        if(c == null)
            return null;
        for(TakmoTeleporter t : c.getAllTeleporters()) {
            if(t.getLocation().equals(l))
                return t;
        }
        return null; // No teleporter at location.
    }

    
    public TakmoWaypoint getWaypointAtLocation(Location l) {
        for(TakmoWaypoint w : mWaypoints.values()) {
            if(w.getLocation().equals(l))
                return w;
        }
        return null;
    }


    public TakmoWaypoint getWaypointByName(String n) {
        return mWaypoints.get(n);
    }


    public void removeTeleporter(TakmoTeleporter t) {
        t.getChunk().remove(t);
    }


    public void removeWaypoint(TakmoWaypoint w) {
        for(TakmoTeleporter t : getAllTeleporters()) {
            if(t.getWaypoint() == w)
                t.focus(null); // Remove broken focus.
        }
        mWaypoints.remove(w.getName());
    }


    public boolean sendInfo(Player p, Location l) {
        TakmoTeleporter t = getTeleporterAtLocation(l);
        TakmoWaypoint w = getWaypointAtLocation(l);
        if(t == null && w == null)
            return false; // Not a wyapoint/teleporter.
        if(t != null)
            t.sendInfo(p);
        if(w != null)
            w.sendInfo(p, mShowKeyInfo);
        return true; // Sent player info.
    }


    public void teleportPlayers(Player[] players) {
        for(Player p : players) {
            TakmoTeleporterChunk c = getChunkFromLocation(p.getLocation());
            if(c == null)
                continue; // Player not near teleporter.
            TakmoTeleporter t = c.checkPlayer(p);
            if(t == null)
                continue; // Player not on teleporter.
            if(!t.verify(mBaseBlockTypeId)) { // Teleporter doesn't verify.
                removeTeleporter(t); // Remove it.
                continue;
            }
            if(t.getWaypoint() != null && !t.getWaypoint().verify(mBaseBlockTypeId)) { // Waypoint doesn't verify.
                removeWaypoint(t.getWaypoint()); // Remove it.
                continue;
            }
            t.teleport(p);
        }
    }


}

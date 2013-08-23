package com.bitwisehero.takmoport;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class TakmoTeleporter {


    private Location location; // Location of the teleporter
    private boolean temporary; // True if temporary, false if not.
    private TakmoWaypoint waypoint; // The linked waypoint, if any.


    public TakmoTeleporter(Location l, TakmoWaypoint w) {

        if(w == null) // Null waypoint denotes temporary teleporter.
            temporary = true;
        else
            temporary = false;
        waypoint = w;
        location = l;

    }


    public Location getLocation() {
        return location.clone();
    }


    public void sendInfo(Player p) {

        if(temporary && waypoint == null)
            p.sendMessage(ChatColor.LIGHT_PURPLE + "This temporary teleporter lacks a focus.");
        else if(temporary)
            p.sendMessage(ChatColor.LIGHT_PURPLE + "This temporary teleporter is focused upon " + waypoint.getName());
        else if(waypoint == null)
            p.sendMessage(ChatColor.LIGHT_PURPLE + "This teleporter lacks a focus.");
        else
            p.sendMessage(ChatColor.LIGHT_PURPLE + "This teleporter is focused upon " + waypoint.getName());
    
    }


    public boolean checkTeleportLocation(Player p) {

        // Get block location.
        Location pl = p.getLocation();
        pl.setX((int)pl.getX());
        pl.setY((int)pl.getY());
        pl.setZ((int)pl.getZ());

        // Check player location vs teleporter location.
        if(pl.getX() == location.getX() &&
                (pl.getY() > location.getY() && pl.getY() < location.getY() + 4) &&
                pl.getZ() == location.getZ())
            return true;
        return false;

    }


    public void focus(Player p, TakmoWaypoint w) {
        
        waypoint = w;

        if(p == null) // Don't log if isn't player.
            return;
        if(waypoint == null)
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Teleporter focus removed.");
        else
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Teleporter given new focus: " + waypoint.getName());

    }


    public void teleport(Player p) {

        if(waypoint == null) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "This teleporter lacks a focus.");
            return;
        }
        waypoint.teleport(p);
        if(temporary)
            waypoint = null;

    }


}

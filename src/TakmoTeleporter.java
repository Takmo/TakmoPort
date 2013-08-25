// See license section in README.
package com.bitwisehero.takmoport;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class TakmoTeleporter {


    private Location location; // Location of the teleporter
    private boolean temporary; // True if temporary, false if not.
    private TakmoWaypoint waypoint; // The linked waypoint, if any.
    private TakmoTeleporterChunk chunk; // The chunk housing this.


    public TakmoTeleporter(TakmoTeleporterChunk c, Location l, TakmoWaypoint w) {
        if(w == null) // Null waypoint denotes temporary teleporter.
            temporary = true;
        else
            temporary = false;
        waypoint = w;
        location = l;
        chunk = c;
    }


    public boolean checkTeleportLocation(Player p) {
        // Get block location and adjust for negative values.
        // Player positions and rounding for negative numbers.
        // Means a somewhat hacky solution here. Basically, if
        // the X or Z coordinate is negative, truncate zeroes
        // and subtract one, essentially rounding down.
        Location pl = p.getLocation().clone();
        if(pl.getX() < 0) pl.setX(pl.getX()-1);
        pl.setX((int)pl.getX());
        pl.setY((int)pl.getY());
        if(pl.getZ() < 0) pl.setZ(pl.getZ()-1);
        pl.setZ((int)pl.getZ());

        // Check player location vs teleporter location.
        if(pl.getX() == location.getX() &&
                (pl.getY() > location.getY() &&
                 pl.getY() < location.getY() + 4) &&
                pl.getZ() == location.getZ())
            return true;
        return false;
    }


    public void focus(TakmoWaypoint w) {
        waypoint = w;
    }


    public TakmoTeleporterChunk getChunk() {
        return chunk;
    }


    public Location getLocation() {
        return location.clone();
    }


    public TakmoWaypoint getWaypoint() {
        return waypoint;
    }


    public boolean isTemporary() {
        return temporary;
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


    public void teleport(Player p) {
        if(waypoint == null) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "This teleporter lacks a focus.");
            return;
        }
        if(!waypoint.teleport(p))
            return; // Don't remove focus if you couldn't teleport.
        if(temporary)
            waypoint = null;
    }


    public boolean verify(int blockId) {
        if(location.getBlock().getTypeId() != blockId)
            return false;
        return true;
    }


}

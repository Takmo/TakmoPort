package com.bitwisehero.takmoport;

import org.bukkit.block.BlockFace;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;

public class TakmoWaypoint {
    

    private Location location;
    private String name;
    private String permission;
    private Material key;


    public TakmoWaypoint(Location l, String n, Material m, String p) {

        if(p == null) // Default value.
            p = "takmoport.teleport";
        location = l;
        name = n;
        key = m;
        permission = p;

    }


    public Location getLocation() {
        return location.clone();
    }


    public String getName() {
        return name;
    }


    public void sendInfo(Player p, boolean showKeyInfo) {
        if(key != null && (showKeyInfo || p.hasPermission("takmoport.admin")))
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Waypoint " +  name + " has permission " + permission +
                    " and requires key " + key);
        else
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Waypoint " + name + " has permission " + permission);
    }


    public void teleport(Player p) {

        // Check permissions.
        if(!p.hasPermission(permission)) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "You don't have permission to teleport!");
            return;
        }

        // Check key.
        if(key != null) {
            if(key != p.getItemInHand().getType()) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "You are holding the incorrect key!");
                return;
            }
        }

        // Teleport.
        for(Location dest = location.clone(); location.getY() < 256; dest.setY(dest.getX() + 1)) {
            if(dest.getBlock().getTypeId() == 0 &&
                    dest.getBlock().getRelative(BlockFace.UP).getTypeId() == 0) {
                p.teleport(dest);
                return;
            }
        }

        // Oops, couldn't teleport.
        p.sendMessage(ChatColor.LIGHT_PURPLE + "Waypoint not safe for teleport. What the heck?!");

    }


}

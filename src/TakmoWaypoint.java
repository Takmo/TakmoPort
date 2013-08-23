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
            p.sendMessage(ChatColor.LIGHT_PURPLE +
                    "Waypoint: " +  name +
                    " / Permission: " + permission +
                    " / Key: " + key);
        else
            p.sendMessage(ChatColor.LIGHT_PURPLE +
                    "Waypoint: " +name +" / Permission: " + permission);
    }


    public boolean teleport(Player p) {
        // Check permissions.
        if(!p.hasPermission(permission) || !p.hasPermission("takmoport.teleport")) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "You don't have permission to teleport!");
            return false; // Did not teleport.
        }

        // Check key.
        if(key != null && key != p.getItemInHand().getType()) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "You are holding the incorrect key!");
            return false; // Did not teleport.
        }

        // Teleport.
        Location dest = location.clone();
        while(dest.getBlock().getTypeId() != 0) dest.setY(dest.getY()+1); // Find block with air.
        dest.add(0.5, 0, 0.5); // Center in block.
        dest.setPitch(p.getLocation().getPitch());
        dest.setYaw(p.getLocation().getYaw());
        p.teleport(dest);
        p.sendMessage(ChatColor.LIGHT_PURPLE + "Whoosh!");
        return true; // Did, in fact, teleport.
    }


}

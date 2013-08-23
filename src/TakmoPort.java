package com.bitwisehero.takmoport;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TakmoPort extends JavaPlugin implements CommandExecutor, Listener {

    // Waiting for players to right click.
    public enum TakmoClickType {
        TELEPORTER,
        WAYPOINT,
        FOCUS,
        INFO
    }
    private HashMap<Player, TakmoClickType> clickingPlayers;

    private int baseBlockTypeId; // The typeId for the base block from config.

    public void onEnable() {
        saveDefaultConfig(); // Write default config.yml if none exists.
        baseBlockTypeId = getConfig().getInt("baseBlockId");
        getCommand("waypoint").setExecutor(this);
        getCommand("teleporter").setExecutor(this);
        getCommand("focus").setExecutor(this);
        getCommand("info").setExecutor(this);
        clickingPlayers = new HashMap<Player, TakmoClickType>();
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        
    }

    /*
     *  Commands and clicking
     */

    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        
        // Must be a player.
        if(s instanceof ConsoleCommandSender)
            s.sendMessage("Sorry, this command is only available in-game!");
        if(!(s instanceof Player))
            return true;

        // Commands
        if(l.equals("teleporter")) {
            clickingPlayers.put((Player) s, TakmoClickType.TELEPORTER);
        }
        if(l.equals("waypoint")) {
            clickingPlayers.put((Player) s, TakmoClickType.WAYPOINT);
        }
        if(l.equals("focus")) {
            clickingPlayers.put((Player) s, TakmoClickType.FOCUS);
        }
        if(l.equals("info")) {
            clickingPlayers.put((Player) s, TakmoClickType.INFO);
        }

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        // If we aren't waiting for player to click, exit.
        if(!clickingPlayers.containsKey(p))
            return;

        // Make sure player is clicking a block of the correct type.
        if(b.getTypeId() != baseBlockTypeId)
            p.sendMessage("That's not a base block, silly!");
        else {
            // Do stuff.
        }

        clickingPlayers.remove(p);
    }

}

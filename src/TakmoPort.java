package com.bitwisehero.takmoport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TakmoPort extends JavaPlugin implements CommandExecutor {

    public void onEnable() {
        loadConfigFile();
        getCommand("waypoint").setExecutor(this);
        getCommand("teleporter").setExecutor(this);
        getCommand("focus").setExecutor(this);
        getCommand("info").setExecutor(this);
    }

    public void onDisable() {
        
    }

    private void loadConfigFile() {
        saveDefaultConfig(); // If config.yml doesn't exist, make it.
    }

    public boolean onCommand(CommandSender s, Command c, String l, String[] args) {
        
        // Must be a player.
        if(s instanceof ConsoleCommandSender)
            s.sendMessage("Sorry, this command is only available in-game!");
        if(!(s instanceof Player))
            return true;

        // Commands!
        if(l.equals("teleporter")) {
        
        }
        if(l.equals("waypoint")) {

        }
        if(l.equals("focus")) {

        }
        if(l.equals("info")) {

        }

        return true;
    }

}

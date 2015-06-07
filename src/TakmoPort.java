// See license section in README.
package com.bitwisehero.takmoport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class TakmoPort extends JavaPlugin implements CommandExecutor, Listener, Runnable {


    private TakmoPortManager manager;
    private HashMap<Player, TakmoCommand> clickingPlayers;
    private Material baseBlockType; // The material for the base block from config.
    private boolean showKeyInfo; // Show key with info?


    public void onEnable() {
        saveDefaultConfig(); // Write default config.yml if none exists.
        baseBlockType = Material.getMaterial(getConfig().getString("baseBlockType"));
        if(baseBlockType == null) {
            getLogger().warning("config:baseBlockType was not a valid material - defaulting to LAPIS_BLOCK");
            baseBlockType = Material.LAPIS_BLOCK;
        }
        showKeyInfo = getConfig().getBoolean("showKeyInfo");

        // Register commands.
        getCommand("waypoint").setExecutor(this);
        getCommand("teleporter").setExecutor(this);
        getCommand("focus").setExecutor(this);
        getCommand("tpinfo").setExecutor(this);

        manager = new TakmoPortManager(baseBlockType, showKeyInfo);
        clickingPlayers = new HashMap<Player,TakmoCommand>();
        load();

        getServer().getPluginManager().registerEvents(this, this);
        int delay = getConfig().getInt("syncDelay");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this, delay, delay);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                save();
            }
        }, 12000, 12000);
    }


    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        save();
    }


    private void load() {
        try {
            // First waypoints...
            File loadFile = new File(getDataFolder().getAbsolutePath() + File.separator + "waypoints.txt");
            if(!loadFile.exists()) // Create file if it doens't exist.
                loadFile.createNewFile();
            BufferedReader read = new BufferedReader(new FileReader(loadFile));

            for(String s = read.readLine(); s != null; s = read.readLine()) {
                // waypointname:key:permission:w:x:y:z
                String[] parts = s.split(":");
                Material key = null;
                if(!parts[1].equals("="))
                    key = Material.getMaterial(parts[1]);
                Location loc = new Location(getServer().getWorld(parts[3]),
                        Double.parseDouble(parts[4]), Double.parseDouble(parts[5]), Double.parseDouble(parts[6]));
                manager.addWaypoint(parts[0], loc, key, parts[2]);
            }
            read.close();

            // Now teleporters...
            loadFile = new File(getDataFolder().getAbsolutePath() + File.separator + "teleporters.txt");
            if(!loadFile.exists()) // Create file if it doesn't exist.
                loadFile.createNewFile();
            read = new BufferedReader(new FileReader(loadFile));

            for(String s = read.readLine(); s != null; s = read.readLine()) {
                // waypointname:temporary:w:x:y:z
                String[] parts = s.split(":");
                boolean temp = parts[1].equals("true");
                Location loc = new Location(getServer().getWorld(parts[2]),
                        Double.parseDouble(parts[3]), Double.parseDouble(parts[4]), Double.parseDouble(parts[5]));
                String name = null;
                if(!temp && !parts[0].equals("=")) // Give name to non-temp teleporters.
                    name = parts[0];
                manager.addTeleporter(loc, name);
            }
            read.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    private void save() {
        try {
            //First waypoints...
            File saveFile = new File(getDataFolder().getAbsolutePath() + File.separator + "waypoints.txt");
            if(saveFile.exists())
                saveFile.delete();
            saveFile.createNewFile();
            FileWriter write = new FileWriter(saveFile);

            for(TakmoWaypoint w : manager.getAllWaypoints()) {
                String key = "=";
                if(w.getKey() != null)
                    key = w.getKey().toString();
                Location l = w.getLocation();
                write.write(w.getName() + ":" + key + ":" + w.getPermission() + ":" +
                        l.getWorld().getName() + ":" +l.getX() + ":" + l.getY() + ":" + l.getZ() + "\n");
            }
            write.close();

            // Now teleporters...
            saveFile = new File(getDataFolder().getAbsolutePath() + File.separator + "teleporters.txt");
            if(saveFile.exists())
                saveFile.delete();
            saveFile.createNewFile();
            write = new FileWriter(saveFile);

            for(TakmoTeleporter t : manager.getAllTeleporters()) {
                String name = "=";
                if(t.getWaypoint() != null)
                    name = t.getWaypoint().getName();
                Location l = t.getLocation();
                write.write(name + ":" + t.isTemporary() + ":" +
                        l.getWorld().getName() + ":" + l.getX() + ":" + l.getY() + ":" + l.getZ() + "\n");
            }
            write.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if(s instanceof ConsoleCommandSender)
            s.sendMessage("Sorry, this command is only available in-game!");
        if(!(s instanceof Player))
            return true;
        if(l.equals("teleporter")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.Type.TELEPORTER, a));
            message(s, "Right click the base block to create teleporter.");
        }
        if(l.equals("waypoint")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.Type.WAYPOINT, a));
            message(s, "Right click the base block to create waypoint.");
        }
        if(l.equals("focus")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.Type.FOCUS, a));
            message(s, "Right click the teleporter to focus.");
        }
        if(l.equals("tpinfo")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.Type.INFO, a));
            message(s, "Right click the teleporter or waypoint for info.");
        }
        return true;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.getBlock().getType() != baseBlockType)
            return;
        switch(manager.baseBroken(e.getBlock().getLocation())) {
            case 1:
                message(e.getPlayer(), "Waypoint destroyed.");
                break;
            case 2:
                message(e.getPlayer(), "Teleporter destroyed.");
                break;
            default: break;
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        
        if(!clickingPlayers.containsKey(e.getPlayer()))
            return; // If we aren't waiting on the player, exit.

        Player p = e.getPlayer();
        TakmoCommand c = clickingPlayers.get(p);
        clickingPlayers.remove(p); // Remove player from waitinglist.

        if(e.getClickedBlock().getType() != baseBlockType)
            return; // Make sure it's the right block type.

        // Sanitize all arguments.
        String[] args = c.getArguments();
        String[] a = new String[args.length];
        for(int i = 0; i < args.length; i++)
            a[i] = args[i].replaceAll("[^a-zA-Z0-9_.-]", "");

        // Focus command
        if(c.getType() == TakmoCommand.Type.FOCUS) {
            String name = null;
            if(a.length > 0)
                name = a[0];
            switch(manager.focusTeleporter(e.getClickedBlock().getLocation(), name)) {
                case 0:
                    message(p, "Teleporter given new focus: " + name);
                    break;
                case 1:
                    message(p, "Teleporter focus removed.");
                    break;
                case 2:
                    message(p, "This block isn't a teleporter.");
                    break;
                case 3:
                    message(p, "No waypoint exists with the name " + name);
                    break;
                default:
                    break;
            }
            return;
        }

        // Info Command
        if(c.getType() == TakmoCommand.Type.INFO) {
            if(!manager.sendInfo(p, e.getClickedBlock().getLocation()))
                message(p, "This block isn't a teleporter or waypoint.");
            return;
        }

        // Waypoint Command
        if(c.getType() == TakmoCommand.Type.WAYPOINT) {
            if(args.length == 0) { // If no arguments... Needs arguments.
                message(p, "Try \"/help waypoint\" for usage.");
                return;
            }
            String name = a[0];
            Material key = null;        // Default value.
            String permission = null;   // Default value.

            if(args.length > 1) { // If key argument exists...
                if(a[1].equalsIgnoreCase("true")) // And is true...
                    key = p.getItemInHand().getType();
                if(args.length > 2) // If permissions argument exists...
                    permission = a[2];
            }

            switch(manager.addWaypoint(name, e.getClickedBlock().getLocation(), key, permission)){
                case 0:
                    message(p, "Waypoint created: " + name);
                    break;
                case 1:
                    message(p, "A waypoint with this name already exists.");
                    break;
                case 2:
                    message(p, "A waypoint or teleporter already exists here.");
                    break;
                default:
                    break;
            }
            return;
        }

        // Teleporter
        if(c.getType() == TakmoCommand.Type.TELEPORTER) {
            String name = null;
            if(a.length > 0)
                name = a[0];
            switch(manager.addTeleporter(e.getClickedBlock().getLocation(), name)) {
                case 0:
                    message(p, "Created teleporter to " + name);
                    break;
                case 1:
                    message(p, "Created temporary teleporter.");
                    break;
                case 2:
                    message(p, "A waypoint or teleporter already exists here.");
                    break;
                case 3:
                    message(p, "Desination waypoint does not exist: " + name);
                    break;
                default:
                    break;
            }
            return; 
        }
    }


    private void message(CommandSender p, String s) {
        p.sendMessage(ChatColor.LIGHT_PURPLE + s);
    }


    public void run() {
        Collection<? extends Player> c = getServer().getOnlinePlayers();
        manager.teleportPlayers(c.toArray(new Player[c.size()]));
    }

}

package com.bitwisehero.takmoport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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


    ArrayList<TakmoTeleporter> teleporters;      // List of teleporters.
    HashMap<String, TakmoWaypoint> waypoints;    // List of waypoints.
    private HashMap<Player, TakmoCommand> clickingPlayers;
    private int baseBlockTypeId; // The typeId for the base block from config.
    private boolean showKeyInfo; // Show key with info?


    public void onEnable() {
        saveDefaultConfig(); // Write default config.yml if none exists.
        baseBlockTypeId = getConfig().getInt("baseBlockId");
        showKeyInfo = getConfig().getBoolean("showKeyInfo");

        getCommand("waypoint").setExecutor(this);
        getCommand("teleporter").setExecutor(this);
        getCommand("focus").setExecutor(this);
        getCommand("tpinfo").setExecutor(this);

        clickingPlayers = new HashMap<Player, TakmoCommand>();
        teleporters = new ArrayList<TakmoTeleporter>();
        waypoints = new HashMap<String, TakmoWaypoint>();
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
                TakmoWaypoint waypoint = new TakmoWaypoint(loc, parts[0], key, parts[2]);
                if(waypoint.verify(baseBlockTypeId)) // Verify waypoint before continuing.
                    waypoints.put(parts[0], waypoint);
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
                TakmoWaypoint waypoint = null; // Default
                if(!temp) // If not a temporary teleporter, get actual waypoint.
                    waypoint = waypoints.get(parts[0]);
                TakmoTeleporter teleporter = new TakmoTeleporter(loc, waypoint);
                if(teleporter.verify(baseBlockTypeId)) // Verify teleporter before continuing...
                    teleporters.add(teleporter);
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
            for(TakmoWaypoint w : waypoints.values()) {
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
            for(TakmoTeleporter t : teleporters) {
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
        // Must be a player.
        if(s instanceof ConsoleCommandSender)
            s.sendMessage("Sorry, this command is only available in-game!");
        if(!(s instanceof Player))
            return true;
        // Commands
        if(l.equals("teleporter")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.Type.TELEPORTER, a));
            s.sendMessage(ChatColor.LIGHT_PURPLE + "Right click the base block to create teleporter.");
        }
        if(l.equals("waypoint")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.Type.WAYPOINT, a));
            s.sendMessage(ChatColor.LIGHT_PURPLE + "Right click the base block to create waypoint.");
        }
        if(l.equals("focus")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.Type.FOCUS, a));
            s.sendMessage(ChatColor.LIGHT_PURPLE + "Right click the teleporter to focus.");
        }
        if(l.equals("tpinfo")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.Type.INFO, a));
            s.sendMessage(ChatColor.LIGHT_PURPLE + "Right click the teleporter or waypoint for info.");
        }
        return true;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Location l = e.getBlock().getLocation();
        // Check if the location matches a teleporter or waypoint.
        for(TakmoTeleporter tp : teleporters) {
            if(l.equals(tp.getLocation())) { // Remove teleporter is block is broken.
                e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Teleporter destroyed.");
                teleporters.remove(tp);
                return;
            }
        }
        for(Map.Entry<String, TakmoWaypoint> wp : waypoints.entrySet()) {
            if(l.equals(wp.getValue().getLocation())) {
                e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Waypoint destroyed.");
                for(TakmoTeleporter tp : teleporters) {
                    if(tp.getWaypoint() == wp.getValue())
                        tp.focus(null, null); // Remove focuses from teleporters.
                }
                waypoints.remove(wp.getKey()); // Remove waypoint.
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e) {
        
        // Set variables and test conditions.
        if(!clickingPlayers.containsKey(e.getPlayer()))
            return; // If we aren't waiting on the player, exit.
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();
        TakmoCommand c = clickingPlayers.get(p);
        clickingPlayers.remove(p); // Remove player from waitinglist.
        if(b.getTypeId() != baseBlockTypeId)
            return;
        String[] args = c.getArguments();

        // Check if the location matches a teleporter.
        TakmoTeleporter exiTeleporter = null;
        for(TakmoTeleporter tp : teleporters) {
            if(b.getLocation().equals(tp.getLocation())) {
                exiTeleporter = tp;
            }
        }

        // Focus Command
        if(c.getType() == TakmoCommand.Type.FOCUS) {
            if(exiTeleporter == null) { // If the block isn't a teleporter.
                p.sendMessage(ChatColor.LIGHT_PURPLE + "This block isn't a teleporter.");
                return;
            }
            TakmoWaypoint w = null;
            if(args.length > 0) {
                w = waypoints.get(args[0].replaceAll("[^a-zA-Z0-9_.-]", ""));
                if(w == null) {
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "No waypoint exists with that name!");
                    return; // Exit if invalid waypoint name.
                }
            }
            exiTeleporter.focus(p, w); // Focus teleporter to either null or waypoint.
            return;
        }

        // Check if the location matches a waypoint.
        TakmoWaypoint exiWaypoint = null;
        for(TakmoWaypoint wp : waypoints.values()) {
            if(b.getLocation().equals(wp.getLocation())) {
                exiWaypoint = wp;
            }
        }

        // Info Command
        if(c.getType() == TakmoCommand.Type.INFO) {
            if(exiTeleporter != null) { // If block is teleporter
                exiTeleporter.sendInfo(p);
                return;
            }
            if(exiWaypoint != null) { // If block is waypoint
                exiWaypoint.sendInfo(p, showKeyInfo);
                return;
            }
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Not a teleporter or waypoint.");
            return;
        }

        // Exit if waypoint or teleporter here. Following commands cannot have that.
        if(exiTeleporter != null || exiWaypoint != null) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "A teleporter or waypoint already exists here!");
            return;
        }

        // Waypoint Command
        if(c.getType() == TakmoCommand.Type.WAYPOINT) {
            if(args.length == 0) { // If no arguments... Needs arguments.
                p.sendMessage(ChatColor.LIGHT_PURPLE + "Try \"/help waypoint\" for usage.");
                return;
            }

            String name = args[0].replaceAll("[^a-zA-Z0-9_.-]", ""); // First arg, escaped.
            Material key = null;        // Default value.
            String permission = null;   // Default value.

            if(waypoints.containsKey(name)) { // Make sure name isn't taken.
                p.sendMessage(ChatColor.LIGHT_PURPLE + "A waypoint with that name already exists.");
                return;
            }

            if(args.length > 1) { // If key argument exists...
                if(args[1].equalsIgnoreCase("true")) // And is true...
                    key = p.getItemInHand().getType();
                if(args.length > 2) // If permissions argument exists...
                    permission = args[2].replaceAll("[^a-zA-Z0-9_.-]", "");
            }

            waypoints.put(name, new TakmoWaypoint(b.getLocation(), name, key, permission));
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Waypoint created: " + name);
        }

        // Teleporter
        if(c.getType() == TakmoCommand.Type.TELEPORTER) {
            TakmoWaypoint w = null;
            if(args.length > 0) { // If there is a waypoint name...
                w = waypoints.get(args[0].replaceAll("[^a-zA-Z0-9_.-]", "")); // First arg, escaped.
                if(w == null) { // Exit if invalid name.
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "No waypoint exists with that name!");
                    return;
                }
            }

            // Create teleporter.
            teleporters.add(new TakmoTeleporter(b.getLocation(), w));
            if(w == null)
                p.sendMessage(ChatColor.LIGHT_PURPLE + "Created temporary teleporter! Needs focus.");
            else
                p.sendMessage(ChatColor.LIGHT_PURPLE + "Created teleporter to " + args[0].replaceAll("[^a-zA-Z0-9_.-]", ""));
        }
    }


    public void run() {
        // TODO EXTREME OPTIMIZATION
        for(Player p : getServer().getOnlinePlayers()) {
            for(TakmoTeleporter t : teleporters) {
                if(t.checkTeleportLocation(p)){
                    if(!t.getWaypoint().verify(baseBlockTypeId)) { // Verify waypoint before teleport.
                        for(TakmoTeleporter tp : teleporters) {
                            if(tp.getWaypoint() == t.getWaypoint())
                                tp.focus(null, null); // Remove focus if waypoint is broken.
                        }
                        waypoints.remove(t.getWaypoint().getName());
                        return;
                    }
                    t.teleport(p); // If everything is good, teleport bro.
                }
            }
        }
    }

}

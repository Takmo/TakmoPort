package com.bitwisehero.takmoport;

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

        getServer().getPluginManager().registerEvents(this, this);
        int delay = getConfig().getInt("syncDelay");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this, delay, delay);

    }


    public void onDisable() {

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


    // Run every 3 seconds. Check teleports.
    public void run() {
        // TODO OPTIMIZE AND VERIFY WAYPOINT EXISTS BEFORE TELEPORT
        for(Player p : getServer().getOnlinePlayers()) {
            for(TakmoTeleporter t : teleporters) {
                if(t.checkTeleportLocation(p))
                    t.teleport(p);
            }
        }
    }

}

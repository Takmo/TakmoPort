package com.bitwisehero.takmoport;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class TakmoPort extends JavaPlugin implements CommandExecutor, Listener {


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
        getCommand("info").setExecutor(this);

        clickingPlayers = new HashMap<Player, TakmoCommand>();
        teleporters = new ArrayList<TakmoTeleporter>();
        waypoints = new HashMap<String, TakmoWaypoint>();

        getServer().getPluginManager().registerEvents(this, this);

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
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.ClickType.TELEPORTER, a));
        }
        if(l.equals("waypoint")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.ClickType.WAYPOINT, a));
        }
        if(l.equals("focus")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.ClickType.FOCUS, a));
        }
        if(l.equals("info")) {
            clickingPlayers.put((Player) s, new TakmoCommand(TakmoCommand.ClickType.INFO, a));
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

        TakmoCommand c = clickingPlayers.get(p);
        clickingPlayers.remove(p);

        // Make sure player is clicking a block of the correct type.
        if(b.getTypeId() != baseBlockTypeId) {
            p.sendMessage("That's not a base block, silly!");
            return;
        }

        // Grab arguments for future use.
        String[] args = c.getArguments();

        
        // Check if the location matches a teleporter.
        TakmoTeleporter exiTeleporter = null;
        for(TakmoTeleporter tp : teleporters) {
            if(b.getLocation().equals(tp.getLocation())) {
                exiTeleporter = tp;
            }
        }


        // Focus
        if(c.getType() == TakmoCommand.ClickType.FOCUS) {

            if(exiTeleporter == null) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "This block isn't a teleporter.");
                return;
            }
            
            // Check if there is a waypoint name.
            TakmoWaypoint w = null;
            if(args.length > 0) {
                w = waypoints.get(args[0].replaceAll("[^a-zA-Z0-9_.-]", ""));
                if(w == null) {
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "No waypoint exists with that name!");
                    return;
                }
            }

            // Set focus.
            exiTeleporter.focus(p, w);
            return;

        }


        // Check if the location matches a waypoint.
        TakmoWaypoint exiWaypoint = null;
        for(TakmoWaypoint wp : waypoints.values()) {
            if(b.getLocation().equals(wp.getLocation())) {
                exiWaypoint = wp;
            }
        }


        // Info
        if(c.getType() == TakmoCommand.ClickType.INFO) {
            
            if(exiTeleporter != null) {
                exiTeleporter.sendInfo(p);
                return;
            }

            if(exiWaypoint != null) {
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

        
        // Waypoint
        if(c.getType() == TakmoCommand.ClickType.WAYPOINT) {

            // If no arguments, suggest they seek help.
            if(args.length == 0) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "Try \"/help waypoint\" for usage.");
                return;
            }

            // Get the name, first argument.
            String name = args[0].replaceAll("[^a-zA-Z0-9_.-]", "");
            Material key = null;
            String permission = null;

            // Make sure name isn't taken already.
            if(waypoints.containsKey(name)) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "A waypoint with that name already exists.");
                return;
            }

            // Check for more arguments.
            if(args.length > 1) {
                // If this waypoint needs a key
                if(args[1].equalsIgnoreCase("true"))
                    key = p.getItemInHand().getType();
                // Check for permission argument.
                if(args.length > 2)
                    permission = args[2].replaceAll("[^a-zA-Z0-9_.-]", "");
            }

            // Create waypoint.
            waypoints.put(name, new TakmoWaypoint(b.getLocation(), name, key, permission));
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Waypoint created: " + name);

        }


        // Teleporter
        if(c.getType() == TakmoCommand.ClickType.TELEPORTER) {
            // Check if there is a waypoint name.
            TakmoWaypoint w = null;
            if(args.length > 0) {
                w = waypoints.get(args[0].replaceAll("[^a-zA-Z0-9_.-]", ""));
                if(w == null) {
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


}

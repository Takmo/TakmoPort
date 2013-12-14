TakmoPort - A Teleporting Plugin
--------------------------------

Note: No jarfile is currently provided. You may build this plugin yourself
using the ant build system if you would like to use it. When I am relatively
assured that this plugin is bug-free, I will look into a BukkitDev page.

    Copyright (c) 2013 TechnoBulldog

    This software is provided 'as-is', without any express or implied
    warranty. In no event will the authors be held liable for any damages
    arising from the use of this software.

    Permission is granted to anyone to use this software for any purpose,
    including commercial applications, and to alter it and redistribute it
    freely, subject to the following restrictions:

       1. The origin of this software must not be misrepresented; you must not
       claim that you wrote the original software. If you use this software
       in a product, an acknowledgment in the product documentation would be
       appreciated but is not required.

       2. Altered source versions must be plainly marked as such, and must not be
       misrepresented as being the original software.

       3. This notice may not be removed or altered from any source
       distribution.

### Overview ###

TakmoPort is a practical waypoint teleportation plugin. It consists of two parts: 
Waypoints and Teleporters. A waypoint serves as a destination, the receiving end
of the teleport. A teleporter is the sending end. You use a teleporter to warp to
a waypoint (but not vice-versa.) Both teleporters and waypoints are marked by a
specific type of block (default is lapis-lazuli, but you can edit it in the
config.yml file.)

To make use of a teleporter, simply stand above the base block (a lapis-lazuli
by default) and wait to be teleported to the waypoint. Some waypoints require
you to be holding a specific key in order to teleport. Others may require you
to be granted specific permissions by a server administrator.

To create a WAYPOINT (DESTINATION), use the /waypoint command and right click
the block. The only required argument is the name. After that, you may specify
if you want to require a key or if you want to set a specific permission node
requirement. By setting the second argument to true, the item you right-click
your waypoint with will become the waypoint's key, and you must be holding it
in order to teleport to the waypoint. The permission node option may be useful
for server admins who would like to restrict certain teleporters to certain
users or groups only.

    /waypoint [name] [use key? true/FALSE - not required] [perm.node - not required]

To create a TELEPORTER (SENDER), use the /teleporter command and right click
the block. There are two types of teleporters: normal and temporary. A normal
teleporter is set to one specific waypoint and standing in it will automatically
teleport you to your destination waypoint. A temporary teleporter has no set
waypoint. Instead, you must give it a waypoint using the /focus command every
time you wish to teleport. Temporary teleporters allow you to have a
multi-purpose teleporter while preventing others from being able to follow you
through the teleporter. By using the /teleporter command with a waypoint name,
you create a normal teleporter. By using /teleporter with no additional
arguments, you create a temporary teleporter.

    /teleporter [name] = Normal teleporter. Name is waypoint name.
    /teleporter = Temporary teleporter. Requires a /focus

To change the destination of a teleporter, use the /focus command. Using /focus
on a normal teleporter will permanantly change its destination waypoint. By
using /focus on a temporary teleporter, the destination waypoint will be reset
as soon as a player is teleported. Using /focus with no arguments will remove
the destination from any teleporter.

    /focus [name] = Set new destination. Name is waypoint name.
    /focus = Clear current destination.

Lastly, to check information related to a teleporter or waypoint, use /tpinfo
and click on the base block to check its information.

    /tpinfo = Get info on teleporter or waypoint.

### Configuration Options / config.yml ###

    baseBlockId: 22 # Lapiz block
    showKeyInfo: true # Show the required key using /info. False to hide key.
    syncDelay: 60 # Number of ticks between teleport checks. (20 ticks per second x 3 seconds)

### Permission Nodes ###

    takmoport.teleport - Able to make use of a teleporter.
    takmoport.admin - Able to TP to all waypoints, even those requiring permissions.
    takmoport.create.teleport - Able to create teleporters.
    takmoport.create.waypoint - Able to create waypoints.
    takmoport.focus - Able to focus temporary teleporters.
    takmoport.info - Examine info on teleporters and waypoints.
    takmoport.default - All of the above permissions excluding admin
    takmoport.* - All of the above including admin.


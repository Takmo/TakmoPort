TakmoPort - A Waypoint Teleportation Plugin
--------------------------------

Note: No jarfile is currently provided. You may build this plugin yourself
using the ant build system if you would like to use it. Given the nature
of this plugin's development as well as the nature of Minecraft server modding
and development, this plugin may require modification for your intended use.

Seems to be working as of June 7th, 2015.

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

### Basics ###

TakmoPort is a practical waypoint teleportation plugin for Bukkit.
It is composed to two types of objects: teleporters and waypoints, both
of which require a specific base block to exist before creation. (The
default type of base is a lapiz block, but this can be changed in config.yml.)

To teleport, first make a waypoint. This will be your destination. Just
choose a name for your waypoint, type the following command, and right 
click the base block you want to use. (It's a lapiz block by default, but
you can change it.)

    /waypoint NAME

Now, to teleport to your new waypoint, you need to make a teleporter.
Just type the following command, using the name of your waypoint, and
right click an unused base block. (It's the same type of block as the
waypoint.)

    /teleporter WAYPOINT_NAME

That's it! To teleport from the teleporter to your waypoint, just stand on
top of the teleporter for a few seconds and it will automatically teleport
you to your destination waypoint. Your teleporter will continue to function
as long as your teleporter and waypoint are intact.

### Waypoint Keys ###

As long as your teleporter and waypoint are intact, your teleporter will
serve as a publicly accessible one-way transporter that anyone can use.
In some cases, however, you may want to limit your waypoint to just a few
players only. Cases like this might benefit from a waypoint key.

A waypoint key is an item that must be held in the player's hand in order
to teleport to a waypoint. By default, a player simply stands on the
teleporter and is whisked away to a waypoint. If a waypoint requires a key,
however, the player must be holding the correct item in their hand in order
to be teleported.

To make a private waypoint that requires a key, use the same waypoint
command and add "true" to the end. Before right clicking the base block,
make certain that your intended key is in your hand. Right click the base
block with your intended key.

    /waypoint NAME true

Teleporters are created using the same steps as before. Now, however, in
order to successfully teleport, the player must be holding the same key
used to create the waypoint.

(Note: the exact same item is not required to teleport, only the
same type of item. If your key is a diamond sword, holding any diamond
sword will allow you to teleport.)

### Temporary Teleporters ###

For players using numerous waypoints, having a teleporter linked to every
single waypoint can be impractical. Instead, the player may opt to create
a temporary teleporter.

Simply put, a temporary teleporter loses its destination waypoint after
every use. Once a player uses a teleporter, it can no longer teleport
a player. To create a temporary teleporter, use the teleporter command
without a waypoint name.

    /teleporter

Of course, a teleporter to nowhere isn't worth much. If a teleporter lacks
a destination waypoint, you can focus it upon a waypoint using the focus
command.

    /focus WAYPOINT_NAME

Once a teleporter is focused, teleporting will remove the focus and it
will return to its dormant state. This is convenient for when you need
a generic teleporter to an unspecified location. It is also beneficial
when you don't want other players discovering the name of your destination
waypoint.

### Teleporter / Waypoint Information ###

If you would like to check information on a teleporter or waypoint, simply
use the tpinfo command and right click the base block.

    /tpinfo

### Special Permission Waypoints ###

One last option while creating waypoints is to specify a permission node
that the teleporting player is required to possess. Server admins may
take advantage of this to create special waypoints only accessible to
specific players or groups. Just specify the permission node while
creating the waypoint.

    /waypoint NAME false my.group.vips - No key required.
    /waypoint NAME true my.group.moderators - Key required.

### Configuration Options / config.yml ###

    baseBlockType: LAPIS_BLOCK # The ID for the teleporter/waypoint base block. (Default lapiz block.)
    showKeyInfo: true # Show the required key using /info. False to hide key.
    syncDelay: 60 # Number of server ticks between teleport checks. (20 ticks per second x 3 seconds)

### Permission Nodes ###

    takmoport.teleport - Able to make use of a teleporter.
    takmoport.admin - Able to TP to all waypoints, even those requiring permissions.
    takmoport.create.teleport - Able to create teleporters.
    takmoport.create.waypoint - Able to create waypoints.
    takmoport.focus - Able to focus temporary teleporters.
    takmoport.info - Examine info on teleporters and waypoints.
    takmoport.default - All of the above permissions excluding admin
    takmoport.* - All of the above including admin.

### Building ###

To create a .jar file usable with Spigot, make sure that the Spigot API is copied
into the directory as spigot.jar, then run:

    ant jar

Note: because I am unsure of the current state of Bukkit, the current build setup
is designed for Spigot. Should Bukkit be updated, compilating for Bukkit should
be as simple as switching all references to "spigot" with "bukkit" in build.xml

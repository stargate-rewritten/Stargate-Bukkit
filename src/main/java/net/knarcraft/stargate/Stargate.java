package net.knarcraft.stargate;

import net.knarcraft.stargate.event.StargateAccessEvent;
import net.knarcraft.stargate.listener.BlockEventListener;
import net.knarcraft.stargate.listener.BungeeCordListener;
import net.knarcraft.stargate.listener.EntityEventListener;
import net.knarcraft.stargate.listener.PlayerEventsListener;
import net.knarcraft.stargate.listener.PluginEventListener;
import net.knarcraft.stargate.listener.VehicleEventListener;
import net.knarcraft.stargate.listener.WorldEventListener;
import net.knarcraft.stargate.thread.BlockPopulatorThread;
import net.knarcraft.stargate.thread.StarGateThread;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * stargate - A portal plugin for Bukkit
 * Copyright (C) 2011 Shaun (sturmeh)
 * Copyright (C) 2011 Dinnerbone
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * Copyright (C) 2021 Kristian Knarvik
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@SuppressWarnings("unused")
public class Stargate extends JavaPlugin {

    public static Logger log;
    private FileConfiguration newConfig;
    private PluginManager pm;
    public static Server server;
    public static Stargate stargate;
    private static LanguageLoader languageLoader;

    private static String portalFolder;
    private static String gateFolder;
    private static String langFolder;
    private static String defNetwork = "central";

    private static boolean destroyExplosion = false;
    public static int maxGates = 0;
    private static String langName = "en";
    private static final int activeTime = 10;
    private static final int openTime = 10;
    public static boolean destMemory = false;
    public static boolean handleVehicles = true;
    public static boolean sortLists = false;
    public static boolean protectEntrance = false;
    public static boolean enableBungee = true;
    public static boolean verifyPortals = true;
    public static ChatColor signColor;

    // Temp workaround for snowmen, don't check gate entrance
    public static boolean ignoreEntrance = false;

    // Used for debug
    public static boolean debug = false;
    public static boolean permDebug = false;

    public static final ConcurrentLinkedQueue<Portal> openList = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<Portal> activeList = new ConcurrentLinkedQueue<>();

    // Used for populating gate open/closed material.
    public static Queue<BloxPopulator> blockPopulatorQueue = new LinkedList<>();

    // HashMap of player names for Bungee support
    public static Map<String, String> bungeeQueue = new HashMap<>();

    // World names that contain stargates
    public static HashSet<String> managedWorlds = new HashSet<>();

    public Stargate() {
        super();
    }

    /**
     * Special constructor used for MockBukkit
     * @param loader <p>The plugin loader to be used.</p>
     * @param descriptionFile <p>The description file to be used.</p>
     * @param dataFolder <p>The data folder to be used.</p>
     * @param file <p>The file to be used</p>
     */
    protected Stargate(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }

    @Override
    public void onDisable() {
        Portal.closeAllGates();
        Portal.clearGates();
        managedWorlds.clear();
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        pm = getServer().getPluginManager();
        newConfig = this.getConfig();
        log = Logger.getLogger("Minecraft");
        Stargate.server = getServer();
        Stargate.stargate = this;

        // Set portalFile and gateFolder to the plugin folder as defaults.
        String dataFolderPath = getDataFolder().getPath().replaceAll("\\\\", "/");
        portalFolder = dataFolderPath + "/portals/";
        gateFolder = dataFolderPath + "/gates/";
        langFolder = dataFolderPath + "/lang/";

        log.info(pdfFile.getName() + " v." + pdfFile.getVersion() + " is enabled.");

        // Register events before loading gates to stop weird things happening.
        pm.registerEvents(new PlayerEventsListener(), this);
        pm.registerEvents(new BlockEventListener(), this);

        pm.registerEvents(new VehicleEventListener(), this);
        pm.registerEvents(new EntityEventListener(), this);
        pm.registerEvents(new WorldEventListener(), this);
        pm.registerEvents(new PluginEventListener(this), this);

        this.loadConfig();

        // Enable the required channels for Bungee support
        if (enableBungee) {
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener());
        }

        // It is important to load languages here, as they are used during reloadGates()
        languageLoader = new LanguageLoader(langFolder, Stargate.langName);

        this.migrate();
        this.loadGates();
        this.loadAllPortals();

        // Check to see if Economy is loaded yet.
        if (EconomyHandler.setupEconomy(pm)) {
            if (EconomyHandler.economy != null) {
                String vaultVersion = EconomyHandler.vault.getDescription().getVersion();
                log.info(Stargate.getString("prefix") +
                        replaceVars(Stargate.getString("vaultLoaded"), "%version%", vaultVersion));
            }
        }

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new StarGateThread(), 0L, 100L);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new BlockPopulatorThread(), 0L, 1L);
    }

    public static boolean destroyedByExplosion() {
        return destroyExplosion;
    }

    public static int getOpenTime() {
        return openTime;
    }

    public static int getActiveTime() {
        return activeTime;
    }

    public void loadConfig() {
        reloadConfig();
        newConfig = this.getConfig();
        // Copy default values if required
        newConfig.options().copyDefaults(true);

        // Load values into variables
        portalFolder = newConfig.getString("portal-folder");
        gateFolder = newConfig.getString("gate-folder");
        defNetwork = newConfig.getString("default-gate-network").trim();
        destroyExplosion = newConfig.getBoolean("destroyexplosion");
        maxGates = newConfig.getInt("maxgates");
        langName = newConfig.getString("lang");
        destMemory = newConfig.getBoolean("destMemory");
        ignoreEntrance = newConfig.getBoolean("ignoreEntrance");
        handleVehicles = newConfig.getBoolean("handleVehicles");
        sortLists = newConfig.getBoolean("sortLists");
        protectEntrance = newConfig.getBoolean("protectEntrance");
        enableBungee = newConfig.getBoolean("enableBungee");
        verifyPortals = newConfig.getBoolean("verifyPortals");
        // Sign color
        String sc = newConfig.getString("signColor");
        try {
            signColor = ChatColor.valueOf(sc.toUpperCase());
        } catch (Exception ignore) {
            log.warning(Stargate.getString("prefix") + "You have specified an invalid color in your config.yml. Defaulting to BLACK");
            signColor = ChatColor.BLACK;
        }
        // Debug
        debug = newConfig.getBoolean("debug");
        permDebug = newConfig.getBoolean("permdebug");
        // Economy
        EconomyHandler.economyEnabled = newConfig.getBoolean("useeconomy");
        EconomyHandler.createCost = newConfig.getInt("createcost");
        EconomyHandler.destroyCost = newConfig.getInt("destroycost");
        EconomyHandler.useCost = newConfig.getInt("usecost");
        EconomyHandler.toOwner = newConfig.getBoolean("toowner");
        EconomyHandler.chargeFreeDestination = newConfig.getBoolean("chargefreedestination");
        EconomyHandler.freeGatesGreen = newConfig.getBoolean("freegatesgreen");

        this.saveConfig();
    }

    public void closeAllPortals() {
        // Close all gates prior to reloading
        for (Portal p : openList) {
            p.close(true);
        }
    }

    public void loadGates() {
        Gate.loadGates(gateFolder);
        log.info(Stargate.getString("prefix") + "Loaded " + Gate.getGateCount() + " gate layouts");
    }

    public void loadAllPortals() {
        for (World world : getServer().getWorlds()) {
            if (!managedWorlds.contains(world.getName())) {
                Portal.loadAllGates(world);
                managedWorlds.add(world.getName());
            }
        }
    }

    private void migrate() {
        // Only migrate if new file doesn't exist.
        File newPortalDir = new File(portalFolder);
        if (!newPortalDir.exists()) {
            newPortalDir.mkdirs();
        }
        File newFile = new File(portalFolder, getServer().getWorlds().get(0).getName() + ".db");
        if (!newFile.exists()) {
            newFile.getParentFile().mkdirs();
        }
    }

    public static void debug(String rout, String msg) {
        if (Stargate.debug) {
            log.info("[stargate::" + rout + "] " + msg);
        } else {
            log.log(Level.FINEST, "[stargate::" + rout + "] " + msg);
        }
    }

    public static void sendMessage(CommandSender player, String message) {
        sendMessage(player, message, true);
    }

    public static void sendMessage(CommandSender player, String message, boolean error) {
        if (message.isEmpty()) return;
        message = message.replaceAll("(&([a-f0-9]))", "\u00A7$2");
        if (error)
            player.sendMessage(ChatColor.RED + Stargate.getString("prefix") + ChatColor.WHITE + message);
        else
            player.sendMessage(ChatColor.GREEN + Stargate.getString("prefix") + ChatColor.WHITE + message);
    }

    public static void setLine(Sign sign, int index, String text) {
        sign.setLine(index, Stargate.signColor + text);
    }

    public static String getSaveLocation() {
        return portalFolder;
    }

    public static String getGateFolder() {
        return gateFolder;
    }

    public static String getDefaultNetwork() {
        return defNetwork;
    }

    public static String getString(String name) {
        return languageLoader.getString(name);
    }

    public static void openPortal(Player player, Portal portal) {
        Portal destination = portal.getDestination();

        // Always-open gate -- Do nothing
        if (portal.isAlwaysOn()) {
            return;
        }

        // Random gate -- Do nothing
        if (portal.isRandom())
            return;

        // Invalid destination
        if ((destination == null) || (destination == portal)) {
            Stargate.sendMessage(player, Stargate.getString("invalidMsg"));
            return;
        }

        // Gate is already open
        if (portal.isOpen()) {
            // Close if this player opened the gate
            if (portal.getActivePlayer() == player) {
                portal.close(false);
            }
            return;
        }

        // Gate that someone else is using -- Deny access
        if ((!portal.isFixed()) && portal.isActive() && (portal.getActivePlayer() != player)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        // Check if the player can use the private gate
        if (portal.isPrivate() && !Stargate.canPrivate(player, portal)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            return;
        }

        // Destination blocked
        if ((destination.isOpen()) && (!destination.isAlwaysOn())) {
            Stargate.sendMessage(player, Stargate.getString("blockMsg"));
            return;
        }

        // Open gate
        portal.open(player, false);
    }

    /*
     * Check whether the player has the given permissions.
     */
    public static boolean hasPerm(Player player, String perm) {
        if (permDebug)
            Stargate.debug("hasPerm::SuperPerm(" + player.getName() + ")", perm + " => " + player.hasPermission(perm));
        return player.hasPermission(perm);
    }

    /*
     * Check a deep permission, this will check to see if the permissions is defined for this use
     * If using Permissions it will return the same as hasPerm
     * If using SuperPerms will return true if the node isn't defined
     * Or the value of the node if it is
     */
    public static boolean hasPermDeep(Player player, String perm) {
        if (!player.isPermissionSet(perm)) {
            if (permDebug)
                Stargate.debug("hasPermDeep::SuperPerm", perm + " => true");
            return true;
        }
        if (permDebug)
            Stargate.debug("hasPermDeep::SuperPerms", perm + " => " + player.hasPermission(perm));
        return player.hasPermission(perm);
    }

    /*
     * Check whether player can teleport to dest world
     */
    public static boolean canAccessWorld(Player player, String world) {
        // Can use all stargate player features or access all worlds
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.world")) {
            // Do a deep check to see if the player lacks this specific world node
            return hasPermDeep(player, "stargate.world." + world);
        }
        // Can access dest world
        return hasPerm(player, "stargate.world." + world);
    }

    /*
     * Check whether player can use network
     */
    public static boolean canAccessNetwork(Player player, String network) {
        // Can user all stargate player features, or access all networks
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.network")) {
            // Do a deep check to see if the player lacks this specific network node
            return hasPermDeep(player, "stargate.network." + network);
        }
        // Can access this network
        if (hasPerm(player, "stargate.network." + network)) return true;
        // Is able to create personal gates (Assumption is made they can also access them)
        String playerName = player.getName();
        if (playerName.length() > 11) playerName = playerName.substring(0, 11);
        return network.equals(playerName) && hasPerm(player, "stargate.create.personal");
    }

    /*
     * Check whether the player can access this server
     */
    public static boolean canAccessServer(Player player, String server) {
        // Can user all stargate player features, or access all servers
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.servers")) {
            // Do a deep check to see if the player lacks this specific server node
            return hasPermDeep(player, "stargate.server." + server);
        }
        // Can access this server
        return hasPerm(player, "stargate.server." + server);
    }

    /*
     * Call the StargateAccessPortal event, used for other plugins to bypass Permissions checks
     */
    public static boolean canAccessPortal(Player player, Portal portal, boolean deny) {
        StargateAccessEvent event = new StargateAccessEvent(player, portal, deny);
        Stargate.server.getPluginManager().callEvent(event);
        return !event.getDeny();
    }

    /*
     * Return true if the portal is free for the player
     */
    public static boolean isFree(Player player, Portal src, Portal dest) {
        // This gate is free
        if (src.isFree()) return true;
        // Player gets free use
        if (hasPerm(player, "stargate.free") || Stargate.hasPerm(player, "stargate.free.use")) return true;
        // Don't charge for free destination gates
        return dest != null && !EconomyHandler.chargeFreeDestination && dest.isFree();
    }

    /*
     * Check whether the player can see this gate (Hidden property check)
     */
    public static boolean canSee(Player player, Portal portal) {
        // The gate is not hidden
        if (!portal.isHidden()) return true;
        // The player is an admin with the ability to see hidden gates
        if (hasPerm(player, "stargate.admin") || hasPerm(player, "stargate.admin.hidden")) return true;
        // The player is the owner of the gate
        return portal.isOwner(player);
    }

    /*
     * Check if the player can use this private gate
     */
    public static boolean canPrivate(Player player, Portal portal) {
        // Check if the player is the owner of the gate
        if (portal.isOwner(player)) return true;
        // The player is an admin with the ability to use private gates
        return hasPerm(player, "stargate.admin") || hasPerm(player, "stargate.admin.private");
    }

    /*
     * Check if the player has access to {option}
     */
    public static boolean canOption(Player player, String option) {
        // Check if the player can use all options
        if (hasPerm(player, "stargate.option")) return true;
        // Check if they can use this specific option
        return hasPerm(player, "stargate.option." + option);
    }

    /*
     * Check if the player can create gates on {network}
     */
    public static boolean canCreate(Player player, String network) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for all network create permission
        if (hasPerm(player, "stargate.create.network")) {
            // Do a deep check to see if the player lacks this specific network node
            return hasPermDeep(player, "stargate.create.network." + network);
        }
        // Check for this specific network
        return hasPerm(player, "stargate.create.network." + network);

    }

    /*
     * Check if the player can create a personal gate
     */
    public static boolean canCreatePersonal(Player player) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for personal
        return hasPerm(player, "stargate.create.personal");
    }

    /*
     * Check if the player can create this gate layout
     */
    public static boolean canCreateGate(Player player, String gate) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for all gate create permissions
        if (hasPerm(player, "stargate.create.gate")) {
            // Do a deep check to see if the player lacks this specific gate node
            return hasPermDeep(player, "stargate.create.gate." + gate);
        }
        // Check for this specific gate
        return hasPerm(player, "stargate.create.gate." + gate);
    }

    /*
     * Check if the player can destroy this gate
     */
    public static boolean canDestroy(Player player, Portal portal) {
        String network = portal.getNetwork();
        // Check for general destroy
        if (hasPerm(player, "stargate.destroy")) return true;
        // Check for all network destroy permission
        if (hasPerm(player, "stargate.destroy.network")) {
            // Do a deep check to see if the player lacks permission for this network node
            return hasPermDeep(player, "stargate.destroy.network." + network);
        }
        // Check for this specific network
        if (hasPerm(player, "stargate.destroy.network." + network)) return true;
        // Check for personal gate
        return portal.isOwner(player) && hasPerm(player, "stargate.destroy.personal");
    }

    /*
     * Charge player for {action} if required, true on success, false if can't afford
     */
    public static boolean chargePlayer(Player player, UUID target, int cost) {
        // If cost is 0
        if (cost == 0) return true;
        // Economy is disabled
        if (!EconomyHandler.useEconomy()) return true;
        // Charge player
        return EconomyHandler.chargePlayer(player, target, cost);
    }

    /*
     * Charge player for {action} if required, true on success, false if can't afford
     */
    public static boolean chargePlayer(Player player, int cost) {
        // If cost is 0
        if (cost == 0) return true;
        // Economy is disabled
        if (!EconomyHandler.useEconomy()) return true;
        // Charge player
        return EconomyHandler.chargePlayer(player, cost);
    }

    /*
     * Determine the cost of a gate
     */
    public static int getUseCost(Player player, Portal src, Portal dest) {
        // Not using Economy
        if (!EconomyHandler.useEconomy()) return 0;
        // Portal is free
        if (src.isFree()) return 0;
        // Not charging for free destinations
        if (dest != null && !EconomyHandler.chargeFreeDestination && dest.isFree()) return 0;
        // Cost is 0 if the player owns this gate and funds go to the owner
        if (src.getGate().getToOwner() && src.isOwner(player)) return 0;
        // Player gets free gate use
        if (hasPerm(player, "stargate.free") || hasPerm(player, "stargate.free.use")) return 0;

        return src.getGate().getUseCost();
    }

    /*
     * Determine the cost to create the gate
     */
    public static int getCreateCost(Player player, Gate gate) {
        // Not using Economy
        if (!EconomyHandler.useEconomy()) return 0;
        // Player gets free gate destruction
        if (hasPerm(player, "stargate.free") || hasPerm(player, "stargate.free.create")) return 0;

        return gate.getCreateCost();
    }

    /*
     * Determine the cost to destroy the gate
     */
    public static int getDestroyCost(Player player, Gate gate) {
        // Not using Economy
        if (!EconomyHandler.useEconomy()) return 0;
        // Player gets free gate destruction
        if (hasPerm(player, "stargate.free") || hasPerm(player, "stargate.free.destroy")) return 0;

        return gate.getDestroyCost();
    }

    /*
     * Check if a plugin is loaded/enabled already. Returns the plugin if so, null otherwise
     */
    private Plugin checkPlugin(String p) {
        Plugin plugin = pm.getPlugin(p);
        return checkPlugin(plugin);
    }

    private Plugin checkPlugin(Plugin plugin) {
        if (plugin != null && plugin.isEnabled()) {
            log.info(Stargate.getString("prefix") + "Found " + plugin.getDescription().getName() + " (v" + plugin.getDescription().getVersion() + ")");
            return plugin;
        }
        return null;
    }

    /**
     * Replaces a list of variables in a string in the order they are given
     * @param input <p>The input containing the variables</p>
     * @param search <p>The variables to replace</p>
     * @param values <p>The replacement values</p>
     * @return <p>The input string with the search values replaced with the given values</p>
     */
    public static String replaceVars(String input, String[] search, String[] values) {
        if (search.length != values.length) {
            throw new IllegalArgumentException("The number of search values and replace values do not match.");
        }
        for (int i = 0; i < search.length; i++) {
            input = replaceVars(input, search[i], values[i]);
        }
        return input;
    }

    /**
     * Replaces a variable in a string
     * @param input <p>The input containing the variables</p>
     * @param search <p>The variable to replace</p>
     * @param value <p>The replacement value</p>
     * @return <p>The input string with the search replaced with value</p>
     */
    public static String replaceVars(String input, String search, String value) {
        return input.replace(search, value);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName();
        if (cmd.equalsIgnoreCase("sg")) {
            if (args.length != 1) return false;
            if (args[0].equalsIgnoreCase("about")) {
                sender.sendMessage("stargate Plugin created by Drakia");
                if (!languageLoader.getString("author").isEmpty())
                    sender.sendMessage("Language created by " + languageLoader.getString("author"));
                return true;
            }
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!hasPerm(p, "stargate.admin") && !hasPerm(p, "stargate.admin.reload")) {
                    sendMessage(sender, "Permission Denied");
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("reload")) {
                // Deactivate portals
                for (Portal p : activeList) {
                    p.deactivate();
                }
                // Close portals
                closeAllPortals();
                // Clear all lists
                activeList.clear();
                openList.clear();
                managedWorlds.clear();
                Portal.clearGates();
                Gate.clearGates();

                // Store the old Bungee enabled value
                boolean oldEnableBungee = enableBungee;
                // Reload data
                loadConfig();
                loadGates();
                loadAllPortals();
                languageLoader.setChosenLanguage(langName);
                languageLoader.reload();

                // Load Economy support if enabled/clear if disabled
                if (EconomyHandler.economyEnabled && EconomyHandler.economy == null) {
                    if (EconomyHandler.setupEconomy(pm)) {
                        if (EconomyHandler.economy != null) {
                            String vaultVersion = EconomyHandler.vault.getDescription().getVersion();
                            log.info(Stargate.getString("prefix") + Stargate.replaceVars(
                                    Stargate.getString("vaultLoaded"), "%version%", vaultVersion));
                        }
                    }
                }
                if (!EconomyHandler.economyEnabled) {
                    EconomyHandler.vault = null;
                    EconomyHandler.economy = null;
                }

                // Enable the required channels for Bungee support
                if (oldEnableBungee != enableBungee) {
                    if (enableBungee) {
                        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeCordListener());
                    } else {
                        Bukkit.getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
                        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
                    }
                }

                sendMessage(sender, "stargate reloaded");
                return true;
            }
            return false;
        }
        return false;
    }

}

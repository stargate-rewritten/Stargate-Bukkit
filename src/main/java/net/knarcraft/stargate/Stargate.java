package net.knarcraft.stargate;

import net.knarcraft.knarlib.util.UpdateChecker;
import net.knarcraft.stargate.command.CommandStarGate;
import net.knarcraft.stargate.command.StarGateTabCompleter;
import net.knarcraft.stargate.config.EconomyConfig;
import net.knarcraft.stargate.config.MessageSender;
import net.knarcraft.stargate.config.StargateConfig;
import net.knarcraft.stargate.config.StargateGateConfig;
import net.knarcraft.stargate.config.StargateYamlConfiguration;
import net.knarcraft.stargate.container.BlockChangeRequest;
import net.knarcraft.stargate.container.ChunkUnloadRequest;
import net.knarcraft.stargate.listener.BlockEventListener;
import net.knarcraft.stargate.listener.EntityEventListener;
import net.knarcraft.stargate.listener.EntitySpawnListener;
import net.knarcraft.stargate.listener.PlayerEventListener;
import net.knarcraft.stargate.listener.PluginEventListener;
import net.knarcraft.stargate.listener.PortalEventListener;
import net.knarcraft.stargate.listener.TeleportEventListener;
import net.knarcraft.stargate.listener.VehicleEventListener;
import net.knarcraft.stargate.listener.WorldEventListener;
import net.knarcraft.stargate.portal.PortalHandler;
import net.knarcraft.stargate.portal.PortalRegistry;
import net.knarcraft.stargate.thread.BlockChangeThread;
import net.knarcraft.stargate.thread.ChunkUnloadThread;
import net.knarcraft.stargate.thread.StarGateThread;
import net.knarcraft.stargate.utility.BStatsHelper;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Stargate - A portal plugin for Bukkit
Copyright (C) 2011 Shaun (sturmeh)
Copyright (C) 2011 Dinnerbone
Copyright (C) 2011-2013 Steven "Drakia" Scott <Contact@TheDgtl.net>
Copyright (C) 2015-2020 Michael Smith (PseudoKnight)
Copyright (C) 2021-2022 Kristian Knarvik (EpicKnarvik97)

The following license notice applies to all source and resource files in the Stargate project:

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * The main class of the Stargate plugin
 */
@SuppressWarnings("unused")
public class Stargate extends JavaPlugin {

    private static final String configFileName = "config.yml";
    private static final Queue<BlockChangeRequest> blockChangeRequestQueue = new LinkedList<>();
    private static final Queue<ChunkUnloadRequest> chunkUnloadQueue = new PriorityQueue<>();

    private static Logger logger;
    private static Stargate stargate;
    private static String pluginVersion;
    private static PluginManager pluginManager;
    private static StargateConfig stargateConfig;
    private static String updateAvailable = null;
    private FileConfiguration configuration;

    /**
     * Empty constructor necessary for Spigot
     */
    public Stargate() {
        super();
    }

    /**
     * Special constructor used for MockBukkit
     *
     * @param loader          <p>The plugin loader to be used.</p>
     * @param descriptionFile <p>The description file to be used.</p>
     * @param dataFolder      <p>The data folder to be used.</p>
     * @param file            <p>The file to be used</p>
     */
    protected Stargate(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
        super(loader, descriptionFile, dataFolder, file);
    }

    /**
     * Stores information about an available update
     *
     * <p>If a non-null version is given, joining admins will be alerted about the new update.</p>
     *
     * @param version <p>The version of the new update available</p>
     */
    public static void setUpdateAvailable(String version) {
        updateAvailable = version;
    }

    /**
     * Gets information about an available update
     *
     * @return <p>The version number if an update is available. Null otherwise</p>
     */
    public static String getUpdateAvailable() {
        return updateAvailable;
    }

    /**
     * Gets an instance of this plugin
     *
     * @return <p>An instance of this plugin, or null if not instantiated</p>
     */
    public static Stargate getInstance() {
        return stargate;
    }

    /**
     * Adds a block change request to the request queue
     *
     * @param request <p>The request to add</p>
     */
    public static void addBlockChangeRequest(BlockChangeRequest request) {
        if (request != null) {
            blockChangeRequestQueue.add(request);
        }
    }

    /**
     * Gets the queue containing block change requests
     *
     * @return <p>A block change request queue</p>
     */
    public static Queue<BlockChangeRequest> getBlockChangeRequestQueue() {
        return blockChangeRequestQueue;
    }

    /**
     * Gets the sender for sending messages to players
     *
     * @return <p>The sender for sending messages to players</p>
     */
    public static MessageSender getMessageSender() {
        return stargateConfig.getMessageSender();
    }

    /**
     * Gets the object containing gate configuration values
     *
     * @return <p>The object containing gate configuration values</p>
     */
    public static StargateGateConfig getGateConfig() {
        return stargateConfig.getStargateGateConfig();
    }

    /**
     * Gets the version of this plugin
     *
     * @return <p>This plugin's version</p>
     */
    public static String getPluginVersion() {
        return pluginVersion;
    }

    /**
     * Gets the logger used for logging to the console
     *
     * @return <p>The logger</p>
     */
    public static Logger getConsoleLogger() {
        return logger;
    }

    /**
     * Gets the max length of portal names and networks
     *
     * @return <p>The max portal name/network length</p>
     */
    @SuppressWarnings("SameReturnValue")
    public static int getMaxNameNetworkLength() {
        return 13;
    }

    /**
     * Sends a debug message
     *
     * @param route   <p>The class name/route where something happened</p>
     * @param message <p>A message describing what happened</p>
     */
    public static void debug(String route, String message) {
        if (stargateConfig == null || stargateConfig.isNotLoaded() || stargateConfig.isDebuggingEnabled()) {
            logger.info("[Stargate::" + route + "] " + message);
        } else {
            logger.log(Level.FINEST, "[Stargate::" + route + "] " + message);
        }
    }

    /**
     * Logs an info message to the console
     *
     * @param message <p>The message to log</p>
     */
    public static void logInfo(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a severe error message to the console
     *
     * @param message <p>The message to log</p>
     */
    public static void logSevere(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Logs a warning message to the console
     *
     * @param message <p>The message to log</p>
     */
    public static void logWarning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs a message to the console
     *
     * @param severity <p>The severity of the event triggering the message</p>
     * @param message  <p>The message to log</p>
     */
    private static void log(Level severity, String message) {
        if (logger == null) {
            logger = Bukkit.getLogger();
        }
        if (getInstance() == null || stargateConfig == null || stargateConfig.isNotLoaded()) {
            logger.log(severity, "[Stargate]: " + message);
        } else {
            logger.log(severity, getBackupString("prefix") + message);
        }
    }

    /**
     * Gets the folder for saving created portals
     *
     * <p>The returned String path is the full path to the folder</p>
     *
     * @return <p>The folder for storing the portal database</p>
     */
    public static String getPortalFolder() {
        return stargateConfig.getPortalFolder();
    }

    /**
     * Gets the folder storing gate files
     *
     * <p>The returned String path is the full path to the folder</p>
     *
     * @return <p>The folder storing gate files</p>
     */
    public static String getGateFolder() {
        return stargateConfig.getGateFolder();
    }

    /**
     * Gets the default network for gates where a network is not specified
     *
     * @return <p>The default network</p>
     */
    public static String getDefaultNetwork() {
        return stargateConfig.getStargateGateConfig().getDefaultPortalNetwork();
    }

    /**
     * Gets a translated string given its string key
     *
     * <p>The name/key is the string before the equals sign in the language files</p>
     *
     * @param name <p>The name/key of the string to get</p>
     * @return <p>The full translated string</p>
     */
    public static String getString(String name) {
        return stargateConfig.getLanguageLoader().getString(name);
    }

    /**
     * Gets a backup string given its string key
     *
     * <p>The name/key is the string before the equals sign in the language files</p>
     *
     * @param name <p>The name/key of the string to get</p>
     * @return <p>The full string in the backup language (English)</p>
     */
    public static String getBackupString(String name) {
        return stargateConfig.getLanguageLoader().getBackupString(name);
    }

    /**
     * Replaces a variable in a string
     *
     * @param input  <p>The input containing the variables</p>
     * @param search <p>The variable to replace</p>
     * @param value  <p>The replacement value</p>
     * @return <p>The input string with the search replaced with value</p>
     */
    public static String replaceVars(String input, String search, String value) {
        return input.replace(search, value);
    }

    /**
     * Gets this plugin's plugin manager
     *
     * @return <p>A plugin manager</p>
     */
    public static PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * Gets the object containing economy config values
     *
     * @return <p>The object containing economy config values</p>
     */
    public static EconomyConfig getEconomyConfig() {
        return stargateConfig.getEconomyConfig();
    }

    /**
     * Gets the raw configuration
     *
     * @return <p>The raw configuration</p>
     */
    public FileConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.configuration = new StargateYamlConfiguration();
        try {
            this.configuration.load(new File(getDataFolder(), configFileName));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        super.saveConfig();
        try {
            this.configuration.save(new File(getDataFolder(), configFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        PortalHandler.closeAllPortals();
        PortalRegistry.clearPortals();
        if (stargateConfig != null) {
            stargateConfig.clearManagedWorlds();
        }
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getConfig();
        PluginDescriptionFile pluginDescriptionFile = this.getDescription();
        pluginManager = getServer().getPluginManager();
        this.configuration = new StargateYamlConfiguration();
        try {
            this.configuration.load(new File(getDataFolder(), configFileName));
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, e.getMessage());
        }
        this.configuration.options().copyDefaults(true);

        logger = Logger.getLogger("Minecraft");
        Server server = getServer();
        stargate = this;

        try {
            stargateConfig = new StargateConfig(logger);
            stargateConfig.finishSetup();
        } catch (NoClassDefFoundError exception) {
            logSevere("Could not properly load. Class not found: " +
                    exception.getMessage() + "\nThis is probably because you are using CraftBukkit, or other outdated" +
                    "Minecraft server software. Minecraft server software based on Spigot or Paper is required. Paper" +
                    " is recommended, and can be downloaded at: https://papermc.io/downloads/paper");
            this.onDisable();
            return;
        }

        pluginVersion = pluginDescriptionFile.getVersion();

        logger.info(pluginDescriptionFile.getName() + " v." + pluginDescriptionFile.getVersion() + " is enabled.");

        //Register events before loading gates to stop weird things from happening.
        registerEventListeners();

        //Run necessary threads
        runThreads();

        this.registerCommands();

        //Check for any available updates
        UpdateChecker.checkForUpdate(this, "https://api.spigotmc.org/legacy/update.php?resource=109355",
                Stargate::getPluginVersion, Stargate::setUpdateAvailable);

        BStatsHelper.initialize(this);
    }

    /**
     * Starts threads using the bukkit scheduler
     */
    private void runThreads() {
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.runTaskTimer(this, new StarGateThread(), 0L, 100L);
        scheduler.runTaskTimer(this, new BlockChangeThread(), 0L, 1L);
        scheduler.runTaskTimer(this, new ChunkUnloadThread(), 0L, 100L);
    }

    /**
     * Registers all event listeners
     */
    private void registerEventListeners() {
        pluginManager.registerEvents(new PlayerEventListener(), this);
        pluginManager.registerEvents(new BlockEventListener(), this);

        pluginManager.registerEvents(new VehicleEventListener(), this);
        pluginManager.registerEvents(new EntityEventListener(), this);
        pluginManager.registerEvents(new PortalEventListener(), this);
        pluginManager.registerEvents(new WorldEventListener(), this);
        pluginManager.registerEvents(new PluginEventListener(this), this);
        pluginManager.registerEvents(new TeleportEventListener(), this);
        pluginManager.registerEvents(new EntitySpawnListener(), this);
    }

    /**
     * Registers a command for this plugin
     */
    private void registerCommands() {
        PluginCommand stargateCommand = this.getCommand("stargate");
        if (stargateCommand != null) {
            stargateCommand.setExecutor(new CommandStarGate());
            stargateCommand.setTabCompleter(new StarGateTabCompleter());
        }
    }

    /**
     * Gets the chunk unload queue containing chunks to unload
     *
     * @return <p>The chunk unload queue</p>
     */
    public static Queue<ChunkUnloadRequest> getChunkUnloadQueue() {
        return chunkUnloadQueue;
    }

    /**
     * Adds a new chunk unload request to the chunk unload queue
     *
     * @param request <p>The new chunk unload request to add</p>
     */
    public static void addChunkUnloadRequest(ChunkUnloadRequest request) {
        chunkUnloadQueue.removeIf((item) -> item.getChunkToUnload().equals(request.getChunkToUnload()));
        chunkUnloadQueue.add(request);
    }

    /**
     * Gets the stargate configuration
     *
     * @return <p>The stargate configuration</p>
     */
    public static StargateConfig getStargateConfig() {
        return stargateConfig;
    }

}

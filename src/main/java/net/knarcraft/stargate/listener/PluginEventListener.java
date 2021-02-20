package net.knarcraft.stargate.listener;

import net.knarcraft.stargate.EconomyHandler;
import net.knarcraft.stargate.Stargate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * This listener listens for any plugins being enabled or disabled to catch the loading of vault
 */
@SuppressWarnings("unused")
public class PluginEventListener implements Listener {

    private final Stargate stargate;

    /**
     * Instantiates a new plugin event listener
     *
     * @param stargate <p>A reference to the stargate plugin to </p>
     */
    public PluginEventListener(Stargate stargate) {
        this.stargate = stargate;
    }

    /**
     * This event listens for and announces that the vault plugin was detected and enabled
     *
     * <p>Each time this event is called, the economy handler will try to enable vault</p>
     * @param ignored <p>The actual event called. This is currently not used</p>
     */
    @EventHandler
    public void onPluginEnable(PluginEnableEvent ignored) {
        if (EconomyHandler.setupEconomy(stargate.getServer().getPluginManager())) {
            String vaultVersion = EconomyHandler.vault.getDescription().getVersion();
            stargate.getLogger().info(Stargate.getString("prefix") +
                    Stargate.replaceVars(Stargate.getString("vaultLoaded"), "%version%", vaultVersion));
        }
    }

    /**
     * This event listens for the vault plugin being disabled and notifies the console
     *
     * @param event <p>The event caused by disabling a plugin</p>
     */
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(EconomyHandler.vault)) {
            stargate.getLogger().info(Stargate.getString("prefix") + "Vault plugin lost.");
        }
    }
}

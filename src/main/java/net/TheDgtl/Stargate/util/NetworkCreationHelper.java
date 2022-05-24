package net.TheDgtl.Stargate.util;

import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.config.ConfigurationHelper;
import net.TheDgtl.Stargate.config.ConfigurationOption;
import net.TheDgtl.Stargate.exception.NameErrorException;
import net.TheDgtl.Stargate.formatting.TranslatableMessage;
import net.TheDgtl.Stargate.manager.PermissionManager;
import net.TheDgtl.Stargate.network.Network;
import net.TheDgtl.Stargate.network.RegistryAPI;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.formatting.HighlightingStyle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class NetworkCreationHelper {

    /**
     * Interprets a network name and removes any characters with special behavior
     *
     * <p>Goes through some scenarios where the initial network name would need to be changed, and returns the
     * modified network name. Some special characters given in a network name may be interpreted as special flags.</p>
     *
     * @param initialNetworkName <p>The initial network name written on the sign</p>
     * @param flags              <p>All the flags of the portal</p>
     * @param player             <p>The player that wrote the network name</p>
     * @param registry           <p>The registry of all portals</p>
     * @return <p>The interpreted network name</p>
     */
    public static String interpretNetworkName(String initialNetworkName, Set<PortalFlag> flags, Player player,
                                              RegistryAPI registry) {
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(initialNetworkName);
        if (highlight != HighlightingStyle.NOTHING) {
            UUID possiblePlayer = getPlayerUUID(HighlightingStyle.getNameFromHighlightedText(initialNetworkName));
            if (registry.getNetwork(possiblePlayer.toString(), false) != null) {
                initialNetworkName = HighlightingStyle.getNameFromHighlightedText(initialNetworkName);
            } else {
                return initialNetworkName;
            }
        }

        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            if (initialNetworkName.trim().isEmpty()) {
                return HighlightingStyle.PERSONAL.getHighlightedName(player.getName());
            }
            return HighlightingStyle.PERSONAL.getHighlightedName(initialNetworkName);
        }
        if (initialNetworkName.trim().isEmpty()) {
            return ConfigurationHelper.getString(ConfigurationOption.DEFAULT_NETWORK);
        }
        if (flags.contains(PortalFlag.FANCY_INTER_SERVER)) {
            return HighlightingStyle.BUNGEE.getHighlightedName(initialNetworkName);
        }
        if (registry.getNetwork(initialNetworkName, false) != null) {
            return initialNetworkName;
        }
        if (registry.getNetwork(getPlayerUUID(initialNetworkName).toString(), false) != null) {
            return HighlightingStyle.PERSONAL.getHighlightedName(initialNetworkName);
        }

        if (player.getName().equals(initialNetworkName)) {
            return HighlightingStyle.PERSONAL.getHighlightedName(initialNetworkName);
        }

        return initialNetworkName;
    }

    /**
     * Check the name of a network, and insert the related flags into the flags collection
     *
     * @param networkName <p> The name of the network </p>
     */
    public static List<PortalFlag> getNameRelatedFlags(String networkName) {
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(networkName);
        List<PortalFlag> flags = new ArrayList<>();
        switch (highlight) {
            case PERSONAL:
                flags.add(PortalFlag.PERSONAL_NETWORK);
                break;
            case BUNGEE:
                flags.add(PortalFlag.FANCY_INTER_SERVER);
                break;
            case NOTHING:
            default:
                break;
        }
        return flags;
    }

    /**
     * Remove notations from the network name and make it ready for use
     */
    public static String parseNetworkNameName(String initialName) throws NameErrorException {
        HighlightingStyle highlight = HighlightingStyle.getHighlightType(initialName);
        String unHighlightedName = HighlightingStyle.getNameFromHighlightedText(initialName);
        if (highlight == HighlightingStyle.PERSONAL) {
            try {
                return getPlayerUUID(unHighlightedName).toString();
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new NameErrorException(TranslatableMessage.INVALID_NAME);
            }
        }
        return unHighlightedName;
    }

    /**
     * Changes the input name to a name more likely to be permissible
     *
     * @param initialNetworkName <p> The name to change </p>
     * @param permissionManager  <p> A permission manager for the actor player </p>
     * @param player             <P> The player that initiated the call </p>
     */
    public static String getAllowedNetworkName(String initialNetworkName, PermissionManager permissionManager,
                                               Player player) {
        //TODO: This is never used
        HighlightingStyle style = HighlightingStyle.getHighlightType(initialNetworkName);
        if (!permissionManager.canCreateInNetwork(initialNetworkName) && style == HighlightingStyle.NOTHING) {
            Stargate.log(Level.CONFIG,
                    String.format(" Player does not have perms to create on current network %s. Checking for private with same network name...", initialNetworkName));
            initialNetworkName = HighlightingStyle.PERSONAL.getHighlightedName(initialNetworkName);
        }

        if (!permissionManager.canCreateInNetwork(initialNetworkName)) {
            Stargate.log(Level.CONFIG,
                    String.format(" Player does not have perms to create on current network %s. Replacing to private network with the players name...", initialNetworkName));
            return HighlightingStyle.PERSONAL.getHighlightedName(player.getName());
        }
        return initialNetworkName;
    }

    /**
     * Gets the network with the given name, and creates it if it doesn't already exist
     *
     * @param name  <p>The name of the network to get</p>
     * @param flags <p>The flags of the portal that should belong to this network</p>
     * @return <p>The network the portal should be connected to</p>
     * @throws NameErrorException <p>If the network name is invalid</p>
     */
    public static Network selectNetwork(String name, Set<PortalFlag> flags) throws NameErrorException {
        //TODO: This is never used
        try {
            Stargate.getRegistry().createNetwork(name, flags);
        } catch (NameErrorException nameErrorException) {
            TranslatableMessage translatableMessage = nameErrorException.getErrorMessage();
            if (translatableMessage != null) {
                throw nameErrorException;
            }
        }
        return Stargate.getRegistry().getNetwork(name, flags.contains(PortalFlag.FANCY_INTER_SERVER));
    }

    private static UUID getPlayerUUID(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).getUniqueId();
    }

}

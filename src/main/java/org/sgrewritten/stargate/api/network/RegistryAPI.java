package org.sgrewritten.stargate.api.network;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.sgrewritten.stargate.api.network.portal.PositionType;
import org.sgrewritten.stargate.exception.UnimplementedFlagException;
import org.sgrewritten.stargate.exception.name.InvalidNameException;
import org.sgrewritten.stargate.exception.name.NameLengthException;
import org.sgrewritten.stargate.api.gate.GateStructureType;
import org.sgrewritten.stargate.api.network.portal.BlockLocation;
import org.sgrewritten.stargate.api.network.portal.Portal;
import org.sgrewritten.stargate.api.network.portal.RealPortal;
import org.sgrewritten.stargate.api.network.portal.PortalPosition;

import java.util.List;
import java.util.Map;


/**
 * The API for the stargate registry.
 *
 * @author Thorin
 */
@SuppressWarnings("unused")
public interface RegistryAPI {

    /**
     * Removes the given portal from storage
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to be removed</p>
     */
    void unregisterPortal(Portal portal);

    /**
     * Update all portals handled by this registry
     */
    void updateAllPortals();

    /**
     * Updates all portals in the given networks
     *
     * @param networkMap <p>A map of networks</p>
     */
    void updatePortals(Map<String, ? extends Network> networkMap);

    /**
     * Get the portal with the given structure type at the given location
     *
     * @param blockLocation <p>The location the portal is located at</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    RealPortal getPortal(BlockLocation blockLocation, GateStructureType structureType);

    /**
     * Get the portal with any of the given structure types at the given location
     *
     * @param blockLocation  <p>The location the portal is located at</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no such portal exists</p>
     */
    RealPortal getPortal(BlockLocation blockLocation, GateStructureType[] structureTypes);

    /**
     * Gets the portal with the given structure type at the given location
     *
     * @param location      <p>The location to check for portal structures</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    RealPortal getPortal(Location location, GateStructureType structureType);

    /**
     * Gets the portal with any of the given structure types at the given location
     *
     * @param location       <p>The location to check for portal structures</p>
     * @param structureTypes <p>The structure types to look for</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    RealPortal getPortal(Location location, GateStructureType[] structureTypes);

    /**
     * Get the portal at the given location
     *
     * @param location <p>The location to check for portal structures</p>
     * @return <p>The found portal, or null if no portal was found</p>
     */
    RealPortal getPortal(Location location);

    /**
     * Checks if any of the given blocks belong to a portal
     *
     * @param blocks <p>The blocks to check</p>
     * @return <p>True if any of the given blocks belong to a portal</p>
     */
    boolean isPartOfPortal(List<Block> blocks);

    /**
     * Checks one block away from the given location to check if it's adjacent to a portal structure
     *
     * <p>Checks North, west, south, east direction. Not up / down, as it is currently
     * not necessary and a waste of resources.</p>
     *
     * @param location      <p>The location to check for adjacency</p>
     * @param structureType <p>The structure type to look for</p>
     * @return <p>True if the given location is adjacent to a location containing the given structure type</p>
     */
    boolean isNextToPortal(Location location, GateStructureType structureType);

    /**
     * Get portal from block next to portal, will randomly chose one portal if block is
     * next to two portals
     * @param location 
     * @param structureType 
     * @return
     */
    List<RealPortal> getPortalsFromTouchingBlock(Location location, GateStructureType structureType);
    
    /**
     * Registers the existence of the given structure type in the given locations
     *
     * <p>Basically stores the portals that exist at the given locations, but using the structure type as the key to be
     * able to check locations for the given structure type.</p>
     *
     * @param structureType <p>The structure type to register</p>
     * @param locationsMap  <p>The locations and the corresponding portals to register</p>
     */
    void registerLocations(GateStructureType structureType, Map<BlockLocation, RealPortal> locationsMap);

    void registerLocation(GateStructureType structureType, BlockLocation location, RealPortal portal);

    /**
     * Un-registers all portal blocks with the given structure type, at the given block location
     *
     * @param structureType <p>The type of structure to un-register</p>
     * @param blockLocation <p>The location to un-register</p>
     */
    void unRegisterLocation(GateStructureType structureType, BlockLocation blockLocation);

    /**
     * Register a portal to this registry
     * @param portal <p> The portal to register</p>
     */
    void registerPortal(RealPortal portal);

    /**
     * Checks whether the given network name exists
     *
     * @param networkName <p>The network name to check</p>
     * @param isBungee    <p>Whether to look for a BungeeCord network</p>
     * @return <p>True if the network exists</p>
     */
    boolean networkExists(String networkName, boolean isBungee);

    /**
     * Gets the network with the given
     *
     * @param name     <p>The name of the network to get</p>
     * @param isBungee <p>Whether the network is a BungeeCord network</p>
     * @return <p>The network with the given name</p>
     */
    @Nullable Network getNetwork(String name, boolean isBungee);

    /**
     * Gets the map storing all BungeeCord networks
     *
     * @return <p>All BungeeCord networks</p>
     */
    Map<String, Network> getBungeeNetworkMap();

    /**
     * Gets the map storing all non-BungeeCord networks
     *
     * @return <p>All non-BungeeCord networks</p>
     */
    Map<String, Network> getNetworkMap();

    /**
     * Rename the network to specified name
     *
     * @param network <p> The network to rename </p>
     * @param newName <p> The new name of the network </p>
     * @throws InvalidNameException
     * @throws NameLengthException
     * @throws UnimplementedFlagException
     */
    void updateName(Network network, String newName) throws InvalidNameException, NameLengthException, UnimplementedFlagException;

    /**
     * Get a non-clashing name close to the current name of the network.
     *
     * @param network <p>The network to rename </p>
     * @throws InvalidNameException <p> If the name is a uuid </p>
     */
    String getValidNewName(Network network) throws InvalidNameException;

    /**
     * Get all portal positions
     * @return  <p> Data on all portal positions</p>
     */
    Map<BlockLocation,PortalPosition> getPortalPositions();

    /**
     * @param plugin <p> The plugin owning the positions</p>
     * @return <p> Data on the portal positions owned by specified plugin</p>
     */
    Map<BlockLocation,PortalPosition> getPortalPositionsOwnedByPlugin(Plugin plugin);

    /**
     * Save given portal position to storage and register it to the registry
     * @param portal <p>The portal the position is linked to</p>
     * @param location <p> The location of the position</p>
     * @param type <p>The type of the position</p>
     * @param plugin <p> The plugin this position relates to</p>
     */
    PortalPosition savePortalPosition(RealPortal portal, Location location, PositionType type, Plugin plugin);

    /**
     * Remove portal position from registry and storage
     * @param location
     */
    void removePortalPosition(Location location);

    /**
     * Register given portal position to registry
     * @param portalPosition <p> The portal position</p>
     * @param location <p> The location of the position</p>
     * @param portal <p>The portal of the position</p>
     */
    void registerPortalPosition(PortalPosition portalPosition, Location location, RealPortal portal);

    /**
     * Gets the portal position at given location
     * @param location <p> The location of the portal position</p>
     * @return <p>The portal position, or null if none was found</p>
     */
    @Nullable PortalPosition getPortalPosition(Location location);

    /**
     * Get the portal the given portal position belong to
     * @param portalPosition <p> A portal position</p>
     * @return <p> The portal that owns the portal position (or null if the portal position is not registered)</p>
     */
    @Nullable RealPortal getPortalFromPortalPosition(PortalPosition portalPosition);

    void registerNetwork(Network network);
}

package org.sgrewritten.stargate.database;

import org.sgrewritten.stargate.exception.NameErrorException;
import org.sgrewritten.stargate.exception.database.StorageReadException;
import org.sgrewritten.stargate.exception.database.StorageWriteException;
import org.sgrewritten.stargate.network.Network;
import org.sgrewritten.stargate.network.PortalType;
import org.sgrewritten.stargate.network.RegistryAPI;
import org.sgrewritten.stargate.network.portal.Portal;
import org.sgrewritten.stargate.network.portal.PortalFlag;
import org.sgrewritten.stargate.network.portal.PortalPosition;
import org.sgrewritten.stargate.network.portal.RealPortal;

import java.util.Set;

/**
 * A generic API for Stargate's storage methods
 */
public interface StorageAPI {

    /**
     * Loads all portals from storage and adds them to the portal registry
     *
     * @param registry <p> The registry to load the portals into </p>
     * @throws StorageReadException 
     */
    void loadFromStorage(RegistryAPI registry) throws StorageReadException;

    /**
     * Saves the given portal to storage
     *
     * @param portal     <p>The portal to save</p>
     * @param portalType <p>The type of portal to save</p>
     * @throws StorageWriteException 
     */
    boolean savePortalToStorage(RealPortal portal, PortalType portalType) throws StorageWriteException;

    /**
     * Removes a portal and its associated data from storage
     *
     * @param portal     <p>The portal to remove</p>
     * @param portalType <p>The type of portal to remove</p>
     * @throws StorageWriteException 
     */
    void removePortalFromStorage(Portal portal, PortalType portalType) throws StorageWriteException;

    /**
     * Set misc data of a portal
     *
     * @param portal <p>A portal</p>
     * @param data   <p>Any data</p>
     * @throws StorageWriteException <p>If unable to successfully set the new portal data</p>
     */
    void setPortalMetaData(Portal portal, String data, PortalType portalType) throws StorageWriteException;

    /**
     * Get misc data of a portal
     *
     * @param portal <p>A portal</p>
     * @return <p>Data</p>
     * @throws StorageReadException <p>If unable to successfully get the portal data</p>
     */
    String getPortalMetaData(Portal portal, PortalType portalType) throws StorageReadException;

    /**
     * Set misc data of a portal position
     *
     * @param portal         <p>A portal</p>
     * @param portalPosition <p>A portalPosition</p>
     * @param data           <p>Any data</p>
     * @param portalType     <p>The type of portal to set the data for</p>
     * @throws StorageWriteException <p>If unable to set the metadata for the portal</p>
     */
    void setPortalPositionMetaData(RealPortal portal, PortalPosition portalPosition, String data,
                                   PortalType portalType) throws StorageWriteException;

    /**
     * Get misc data of a portal position
     *
     * @param portal         <p>A portal</p>
     * @param portalPosition <p>A portalPosition</p>
     * @return <p>Data</p>
     * @throws StorageReadException <p>If unable to successfully read the portal metadata</p>
     */
    String getPortalPositionMetaData(Portal portal, PortalPosition portalPosition,
                                     PortalType portalType) throws StorageReadException;

    /**
     * Creates a new network unassigned to a registry
     *
     * @param networkName <p>The name of the new network</p>
     * @param flags       <p>The flag set used to look for network flags</p>
     * @return The network that was created
     * @throws NameErrorException <p>If the given network name is invalid</p>
     */
    Network createNetwork(String networkName, Set<PortalFlag> flags) throws NameErrorException;


    /**
     * "Starts" the inter-server connection by setting this server's portals as online
     * @throws StorageWriteException 
     */
    void startInterServerConnection() throws StorageWriteException;

    /**
     * Add a new flag type
     *
     * @param flagChar <p>The character identifying the flag</p>
     * @throws StorageWriteException <p>If unable to write the flag to storage</p>
     */
    void addFlagType(char flagChar) throws StorageWriteException;

    /**
     * Add a new type of portalPosition
     *
     * @param portalPositionTypeName <p>The name of the portal type to add</p>
     * @throws StorageWriteException <p>If unable to write the portal position type to storage</p>
     */
    void addPortalPositionType(String portalPositionTypeName) throws StorageWriteException;

    /**
     * Add a flag to a portal in the database
     *
     * @param flagChar   <p> The character representation of that flag </p>
     * @param portal     <p> A portal </p>
     * @param portalType <p>How the portal should be considered by the database </p>
     * @throws StorageWriteException <p>If unable to write the flag change to storage</p>
     */
    void addFlag(Character flagChar, Portal portal, PortalType portalType) throws StorageWriteException;

    /**
     * Remove a flag to a portal in the database
     *
     * @param flagChar   <p>The character representation of that flag</p>
     * @param portal     <p>A portal</p>
     * @param portalType <p>How the portal should be considered by the database</p>
     * @throws StorageWriteException <p>Uf unable to write the flag change to storage</p>
     */
    void removeFlag(Character flagChar, Portal portal, PortalType portalType) throws StorageWriteException;

    /**
     * Add a portalPosition to a portal in the database
     *
     * @param portal         <p>A portal</p>
     * @param portalType     <p>How the portal should be considered by the database</p>
     * @param portalPosition <p>A portal position</p>
     * @throws StorageWriteException <p>If unable to write the new portal position to storage</p>
     */
    void addPortalPosition(RealPortal portal, PortalType portalType,
                           PortalPosition portalPosition) throws StorageWriteException;

    /**
     * Remove a portalPosition to a portal in the database
     *
     * @param portal         <p> A portal</p>
     * @param portalType     <p> How the portal should be considered by the database </p>
     * @param portalPosition <p> A portal position</p>
     * @throws StorageWriteException <p>If unable to write the portal position change to storage</p>
     */
    void removePortalPosition(RealPortal portal, PortalType portalType,
                              PortalPosition portalPosition) throws StorageWriteException;


}

package net.TheDgtl.Stargate.database;

/**
 * All SQL queries used by this plugin
 */
public enum SQLQuery {

    /**
     * The query for creating the portal table
     */
    CREATE_TABLE_PORTAL,

    /**
     * The query for creating the inter-portal table
     */
    CREATE_TABLE_INTER_PORTAL,

    /**
     * The query for creating the position type table
     */
    CREATE_TABLE_POSITION_TYPE,

    /**
     * The query for creating the portal position table
     */
    CREATE_TABLE_PORTAL_POSITION,

    /**
     * The query for creating the inter-portal position table
     */
    CREATE_TABLE_INTER_PORTAL_POSITION,

    /**
     * The query for creating the (portal) flag table
     */
    CREATE_TABLE_PORTAL_FLAG,

    /**
     * The query for creating the server info table
     */
    CREATE_TABLE_SERVER_INFO,

    /**
     * The query for creating the last known name table
     */
    CREATE_TABLE_LAST_KNOWN_NAME,

    /**
     * The query for creating the portal flag relation table
     */
    CREATE_TABLE_PORTAL_FLAG_RELATION,

    /**
     * The query for creating the inter-portal flag relation table
     */
    CREATE_TABLE_INTER_PORTAL_FLAG_RELATION,

    /**
     * The query for creating the portal view
     */
    CREATE_VIEW_PORTAL,

    /**
     * The query for creating the inter-portal view
     */
    CREATE_VIEW_INTER_PORTAL,

    /**
     * The query for creating the (name,network) index on the portal position table
     */
    CREATE_INDEX_PORTAL_POSITION,

    /**
     * The query for creating the (name,network) index on the inter-portal position table
     */
    CREATE_INDEX_INTER_PORTAL_POSITION,

    /**
     * The query for getting every portal
     */
    GET_ALL_PORTALS,

    /**
     * The query for getting every inter-portal
     */
    GET_ALL_INTER_PORTALS,

    /**
     * The query for getting all portal flags
     */
    GET_ALL_PORTAL_FLAGS,

    /**
     * The query for getting all portal position types
     */
    GET_ALL_PORTAL_POSITION_TYPES,

    /**
     * The query for getting all portal positions
     */
    GET_PORTAL_POSITIONS,

    /**
     * The query for getting all inter-portal positions
     */
    GET_INTER_PORTAL_POSITIONS,

    /**
     * The query for inserting a new portal position type
     */
    INSERT_PORTAL_POSITION_TYPE,

    /**
     * The query for inserting a new portal position
     */
    INSERT_PORTAL_POSITION,

    /**
     * The query for inserting a new inter-portal position
     */
    INSERT_INTER_PORTAL_POSITION,

    /**
     * The query for inserting a new portal flag
     */
    INSERT_PORTAL_FLAG,

    /**
     * The query for inserting a new portal flag relation
     */
    INSERT_PORTAL_FLAG_RELATION,

    /**
     * The query for inserting a new inter-portal flag relation
     */
    INSERT_INTER_PORTAL_FLAG_RELATION,

    /**
     * The query for inserting a new portal
     */
    INSERT_PORTAL,

    /**
     * The query for inserting a new inter-portal
     */
    INSERT_INTER_PORTAL,

    /**
     * The query for updating the last known name of a player
     */
    UPDATE_LAST_KNOWN_NAME,

    /**
     * The query for replacing the known information about a server
     */
    REPLACE_SERVER_INFO,

    /**
     * The query for deleting a portal position
     */
    DELETE_PORTAL_POSITIONS,

    /**
     * The query for deleting an inter-portal position
     */
    DELETE_INTER_PORTAL_POSITIONS,

    /**
     * The query for deleting a portal flag relation
     */
    DELETE_PORTAL_FLAG_RELATION,

    /**
     * The query for deleting an inter-portal flag relation
     */
    DELETE_INTER_PORTAL_FLAG_RELATION,

    /**
     * The query for deleting a portal
     */
    DELETE_PORTAL,

    /**
     * The query for deleting an inter-portal
     */
    DELETE_INTER_PORTAL,

}

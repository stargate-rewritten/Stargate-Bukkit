package net.TheDgtl.Stargate.network;

import net.TheDgtl.Stargate.Setting;
import net.TheDgtl.Stargate.Stargate;
import net.TheDgtl.Stargate.database.Database;
import net.TheDgtl.Stargate.database.DriverEnum;
import net.TheDgtl.Stargate.database.MySqlDatabase;
import net.TheDgtl.Stargate.database.SQLiteDatabase;
import net.TheDgtl.Stargate.exception.GateConflict;
import net.TheDgtl.Stargate.exception.NameError;
import net.TheDgtl.Stargate.exception.NoFormatFound;
import net.TheDgtl.Stargate.network.portal.BungeePortal;
import net.TheDgtl.Stargate.network.portal.IPortal;
import net.TheDgtl.Stargate.network.portal.Portal;
import net.TheDgtl.Stargate.network.portal.PortalFlag;
import net.TheDgtl.Stargate.network.portal.VirtualPortal;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class StargateFactory {


    private final HashMap<String, Network> networkList = new HashMap<>();
    private final HashMap<String, InterserverNetwork> bungeeNetList = new HashMap<>();

    String sharedTableName = "interserver";
    String bungeeDataBaseName = "bungee";
    String tableName = "local";

    private final Database database;

    private SQLQuerryMaker sqlMaker;

    public StargateFactory(Stargate stargate) throws SQLException {
        database = loadDatabase(stargate);

        if (Setting.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            this.sqlMaker = new SQLQuerryMaker(tableName, bungeeDataBaseName, sharedTableName);

        } else
            this.sqlMaker = new SQLQuerryMaker(tableName);
        createTables();


        Stargate.log(Level.FINER, "Loading portals from base database");
        loadAllPortals(database, tableName);
        if (Setting.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            Stargate.log(Level.FINER, "Loading portals from local bungee database");
            loadAllPortals(database, bungeeDataBaseName);
            Stargate.log(Level.FINER, "Loading portals from interserver bungee database");
            loadAllPortals(database, sharedTableName, true);
        }

        refreshPortals(networkList);
        refreshPortals(bungeeNetList);
    }

    private Database loadDatabase(Stargate stargate) throws SQLException {
        if (Setting.getBoolean(Setting.USING_REMOTE_DATABASE)) {
            if (Setting.getBoolean(Setting.SHOW_HIKARI_CONFIG))
                return new MySqlDatabase(stargate);

            DriverEnum driver = DriverEnum.parse(Setting.getString(Setting.BUNGEE_DRIVER));
            String bungeeDatabaseName = Setting.getString(Setting.BUNGEE_DATABASE);
            int port = Setting.getInteger(Setting.BUNGEE_PORT);
            String address = Setting.getString(Setting.BUNGEE_ADDRESS);

            switch (driver) {
                case MARIADB:
                case MYSQL:
                    return new MySqlDatabase(driver, address, port, bungeeDatabaseName, stargate);
                default:
                    throw new SQLException("Unsuported driver: Stargate currently suports MariaDb and MySql for remote databases");
            }
        } else {
            String databaseName = Setting.getString(Setting.DATABASE_NAME);
            File file = new File(stargate.getDataFolder().getAbsoluteFile(), databaseName + ".db");
            return new SQLiteDatabase(file, stargate);
        }
    }

    private void refreshPortals(HashMap<String, ? extends Network> networksList) {
        for (Network net : networksList.values()) {
            net.updatePortals();
        }
    }

    private void runStatement(Database database, PreparedStatement statement) throws SQLException {
        Connection conn = database.getConnection();
        statement.execute();
        statement.close();
    }

    private void createTables() throws SQLException {
        Connection conn1 = database.getConnection();
        PreparedStatement localPortalsStatement = sqlMaker.compileCreateStatement(conn1, SQLQuerryMaker.Type.LOCAL);
        runStatement(database, localPortalsStatement);
        conn1.close();

        if (!Setting.getBoolean(Setting.USING_BUNGEE)) {
            return;
        }
        Connection conn2 = database.getConnection();
        PreparedStatement localInterserverPortalsStatement = sqlMaker.compileCreateStatement(conn2, SQLQuerryMaker.Type.BUNGEE);
        runStatement(database, localInterserverPortalsStatement);
        conn2.close();

        Connection conn3 = database.getConnection();
        PreparedStatement interserverPortalsStatement = sqlMaker.compileCreateStatement(conn3, SQLQuerryMaker.Type.INTERSERVER);
        runStatement(database, interserverPortalsStatement);
        conn3.close();
    }

    private void loadAllPortals(Database database, String databaseName) throws SQLException {
        loadAllPortals(database, databaseName, false);
    }

    private void loadAllPortals(Database database, String databaseName, boolean areVirtual) throws SQLException {
        Connection connection = database.getConnection();
        PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM " + databaseName);

        ResultSet set = statement.executeQuery();
        while (set.next()) {
            String netName = set.getString(1);
            String name = set.getString(2);

            String desti = set.getString(3);
            String worldName = set.getString(4);
            int x = set.getInt(5);
            int y = set.getInt(6);
            int z = set.getInt(7);
            String flagsMsg = set.getString(8);
            UUID ownerUUID = UUID.fromString(set.getString(9));

            EnumSet<PortalFlag> flags = PortalFlag.parseFlags(flagsMsg);

            boolean isBungee = flags.contains(PortalFlag.FANCY_INTERSERVER);
            Stargate.log(Level.FINEST, "Trying to add portal " + name + ", on network " + netName + ",isInterserver = " + isBungee);

            String targetNet = netName;
            if (flags.contains(PortalFlag.BUNGEE)) {
                targetNet = "§§§§§§#BUNGEE#§§§§§§";
            }

            try {
                createNetwork(targetNet, flags);
            } catch (NameError e) {
            }
            Network net = getNetwork(targetNet, isBungee);

            for (IPortal logPortal : net.getAllPortals()) {
                Stargate.log(Level.FINEST, logPortal.getName());
            }

            if (areVirtual) {
                String server = set.getString(10);
                if (!net.portalExists(name)) {
                    IPortal virtualPortal = new VirtualPortal(server, name, net, flags, ownerUUID);
                    net.addPortal(virtualPortal, false);
                    Stargate.log(Level.FINEST, "Added as virtual portal");
                } else {
                    Stargate.log(Level.FINEST, "Not added");
                }
                continue;
            }

            World world = Bukkit.getWorld(worldName);
            Block block = world.getBlockAt(x, y, z);
            String[] virtualSign = {name, desti, netName};


            try {
                IPortal portal = Portal.createPortalFromSign(net, virtualSign, block, flags, ownerUUID);
                net.addPortal(portal, false);
                Stargate.log(Level.FINEST, "Added as normal portal");
                if (isBungee) {
                    setInterserverPortalOnlineStatus(portal, true);
                }
            } catch (GateConflict e) {
            } catch (NoFormatFound e) {
            } catch (NameError e) {
                e.printStackTrace();
            }
        }
        statement.close();
        connection.close();
    }

    private void setInterserverPortalOnlineStatus(IPortal portal, boolean isOnline) throws SQLException {
        Connection conn = database.getConnection();
        PreparedStatement statement = sqlMaker.changePortalOnlineStatus(conn, portal, isOnline, SQLQuerryMaker.Type.INTERSERVER);
        statement.execute();
        statement.close();
        conn.close();
    }

    public void startInterServerConnection() throws SQLException {
        Connection conn = database.getConnection();
        for (Network net : bungeeNetList.values()) {
            for (IPortal portal : net.getAllPortals()) {
                if (portal instanceof VirtualPortal)
                    continue;
                PreparedStatement statement = sqlMaker.compileRefreshPortalStatement(conn, portal, SQLQuerryMaker.Type.INTERSERVER);
                statement.execute();
                statement.close();
            }
        }
        conn.close();
    }

    public void endInterserverConnection() throws SQLException {
        for (InterserverNetwork net : bungeeNetList.values()) {
            for (IPortal portal : net.getAllPortals()) {
                /*
                 * Virtual portal = portals on other servers
                 */
                if (portal instanceof VirtualPortal)
                    continue;

                setInterserverPortalOnlineStatus(portal, false);
            }
        }
    }

    public void createNetwork(String netName, EnumSet<PortalFlag> flags) throws NameError {
        if (netExists(netName, flags.contains(PortalFlag.FANCY_INTERSERVER)))
            throw new NameError(null);
        if (flags.contains(PortalFlag.FANCY_INTERSERVER)) {
            InterserverNetwork net = new InterserverNetwork(netName, database, sqlMaker);
            bungeeNetList.put(netName, net);
            return;
        }
        Network net;
        if (flags.contains(PortalFlag.PERSONAL_NETWORK)) {
            UUID id = UUID.fromString(netName);
            net = new PersonalNetwork(id, database, sqlMaker);
        } else {
            net = new Network(netName, database, sqlMaker);
        }
        networkList.put(netName, net);
    }

    public boolean netExists(String netName, boolean isBungee) {
        return (getNetwork(netName, isBungee) != null);
    }

    public Network getNetwork(String name, boolean isBungee) {
        return getNetMap(isBungee).get(name);
    }

    private HashMap<String, ? extends Network> getNetMap(boolean isBungee) {
        if (isBungee) {
            return bungeeNetList;
        } else {
            return networkList;
        }
    }

    HashMap<String, BungeePortal> bungeeList = new HashMap<>();

    public BungeePortal getBungeeGate(String name) {
        return bungeeList.get(name);
    }

    /**
     * Load portal from one line in legacy database
     *
     * @param str
     * @return
     * @throws ClassNotFoundException Issue with conversion
     */
    public IPortal createFromString(String str) throws ClassNotFoundException {
        return null;
    }
}

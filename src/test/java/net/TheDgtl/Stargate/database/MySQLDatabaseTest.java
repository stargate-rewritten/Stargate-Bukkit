package net.TheDgtl.Stargate.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import net.TheDgtl.Stargate.FakeStargate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.SQLException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MySQLDatabaseTest {

    private static DatabaseTester tester;
    private static TableNameConfig nameConfig;
    private static Database database;

    @BeforeAll
    public static void setUp() throws SQLException {
        System.out.println("Setting up test data");
        DriverEnum driver = DriverEnum.MARIADB;
        String address = "LOCALHOST";
        int port = 3306;
        String databaseName = "stargate";
        String username = "root";
        String password = "root";

        Database database = new MySqlDatabase(driver, address, port, databaseName, username, password, true);
        MySQLDatabaseTest.nameConfig = new TableNameConfig("SG_Test_", "Server_");
        SQLQueryGenerator generator = new SQLQueryGenerator(nameConfig, new FakeStargate(), DriverEnum.MYSQL);
        tester = new DatabaseTester(database, nameConfig, generator, true);
        MySQLDatabaseTest.database = database;
    }

    @AfterAll
    public static void tearDown() throws SQLException {
        MockBukkit.unmock();
        try {
            DatabaseTester.deleteAllTables(nameConfig);
        } finally {
            database.getConnection().close();
        }
    }

    @Test
    @Order(1)
    void addPortalTableTest() throws SQLException {
        tester.addPortalTableTest();
    }

    @Test
    @Order(1)
    void addInterPortalTableTest() throws SQLException {
        tester.addInterPortalTableTest();
    }

    @Test
    @Order(1)
    void createFlagTableTest() throws SQLException {
        tester.createFlagTableTest();
    }

    @Test
    @Order(1)
    void createServerInfoTableTest() throws SQLException {
        tester.createServerInfoTableTest();
    }

    @Test
    @Order(1)
    void createLastKnownNameTableTest() throws SQLException {
        tester.createLastKnownNameTableTest();
    }

    @Test
    @Order(2)
    void createPortalFlagRelationTableTest() throws SQLException {
        tester.createPortalFlagRelationTableTest();
    }

    @Test
    @Order(2)
    void createInterPortalFlagRelationTableTest() throws SQLException {
        tester.createInterPortalFlagRelationTableTest();
    }

    @Test
    @Order(3)
    void createPortalViewTest() throws SQLException {
        tester.createPortalViewTest();
    }

    @Test
    @Order(3)
    void createInterPortalViewTest() throws SQLException {
        tester.createInterPortalViewTest();
    }

    @Test
    @Order(3)
    void addFlagsTest() throws SQLException {
        tester.addFlagsTest();
    }

    @Test
    @Order(4)
    void getFlagsTest() throws SQLException {
        tester.getFlagsTest();
    }

    @Test
    @Order(4)
    void updateServerInfoTest() throws SQLException {
        tester.updateServerInfoTest();
    }

    @Test
    @Order(5)
    void updateLastKnownNameTest() throws SQLException {
        tester.updateLastKnownNameTest();
    }

    @Test
    @Order(5)
    void addPortalTest() throws SQLException {
        tester.addPortalTest();
    }

    @Test
    @Order(5)
    void addInterPortalTest() throws SQLException {
        tester.addInterPortalTest();
    }

    @Test
    @Order(6)
    void getPortalTest() throws SQLException {
        tester.getPortalTest();
    }

    @Test
    @Order(6)
    void getInterPortalTest() throws SQLException {
        tester.getInterPortalTest();
    }

    @Test
    @Order(7)
    void destroyPortalTest() throws SQLException {
        tester.destroyPortalTest();
    }

    @Test
    @Order(7)
    void destroyInterPortalTest() throws SQLException {
        tester.destroyInterPortalTest();
    }

}

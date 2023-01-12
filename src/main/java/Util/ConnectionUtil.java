package Util;

import org.h2.tools.RunScript;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The ConnectionUtil class will be utilized to create an active connection to our database. This class utilizes the singleton design pattern.
 * We will be utilizing an in-memory called h2database for the sql demos.
 *
 * DO NOT CHANGE ANYTHING IN THIS CLASS
 */
public class ConnectionUtil {

    /**
     * url will represent our connection string. Since this is an in-memory db, we will represent a file location to store the data
     */
    private static String url = "jdbc:h2:./h2/db";
    /**
     * Default username for connecting to h2
     */
    private static String username = "sa";
    /**
     * Default password for connecting to h2
     */
    private static String password = "sa";
    /**
     * a static object which represents the connection to h2. Because it is static, any DAO interacting
     * with this connection object is referring to the same object.
     */
    private static Connection connection = null;
    /**
     * @return an active connection to the database and set up the database tables if this is the first time the
     * Connection has been established
     */
    public static Connection getConnection(){
        if(connection == null){
            try {
                connection = DriverManager.getConnection(url, username, password);
                resetTestDatabase();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return connection;
    }
    /**
     * For the purpose of testing, we will need to drop and recreate our database tables to keep it consistent across
     * all tests. The method will read the sql file in resources. This will be performed before every test.
     */
    public static void resetTestDatabase(){
//        if there is no connection, use the getConnection method to set it up
        if(connection == null){
            getConnection();
        }else {
//            otherwise, recreate the tables without setting up a new connection
            try {
                FileReader sqlReader = new FileReader("src/main/resources/SocialMedia.sql");
                RunScript.execute(connection, sqlReader);
            } catch (SQLException | FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Vinpasso
 */
public class SQLManager {

    private static Connection connection;

    //Initialize Connection
    static {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bridgempp", "root", "");
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void initializeDatabase()
    {
        try {
            PreparedStatement statement;
            statement = connection.prepareStatement(
                    "CREATE TABLE Endpoints (" +
                            "id int NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                            "extra varchar(255) NOT NULL" + 
                            "target varchar(255) NOT NULL," +
                            "service varchar(255) NOT NULL," +
                            "alias varchar(255) NOT NULL," + 
                            "permissions int NOT NULL DEFAULT '0');"
            );
            statement.executeUpdate();
            statement = connection.prepareStatement(
                    "CREATE TABLE Groups (" +
                            "id int NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                            "name varchar(255) NOT NULL," +
                            "members FOREIGN_KEY "
            );
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

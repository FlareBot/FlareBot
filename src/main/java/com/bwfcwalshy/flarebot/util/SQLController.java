package com.bwfcwalshy.flarebot.util;

import com.bwfcwalshy.flarebot.FlareBot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class SQLController {

    private static File DATABASE = new File("database.db");

    private static synchronized Connection getConnection() throws SQLException {
        if(!DATABASE.exists()){
            try {
                DATABASE.createNewFile();
            } catch (IOException e) {
                FlareBot.LOGGER.error("Could not create the database!", e);
                System.exit(2);
            }
        }
        return DriverManager.getConnection("jdbc:sqlite:" + DATABASE.toURI());
    }

    public static void runSqlTask(SQLTask toRun) throws SQLException {
        Connection c = getConnection();
        toRun.execute(c);
        if(!c.isClosed())
            c.close();
    }
}

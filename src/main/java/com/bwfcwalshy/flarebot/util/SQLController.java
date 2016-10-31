package com.bwfcwalshy.flarebot.util;

import com.bwfcwalshy.flarebot.FlareBot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class SQLController {

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/flarebot?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true&useSSL=false", "flare", FlareBot.passwd);
    }

    public static void runSqlTask(SQLTask toRun) throws SQLException {
        Connection c = getConnection();
        toRun.execute(c);
        if (!c.isClosed())
            c.close();
    }
}

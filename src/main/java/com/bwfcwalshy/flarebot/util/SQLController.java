package com.bwfcwalshy.flarebot.util;

import com.bwfcwalshy.flarebot.FlareBot;
import com.mysql.cj.jdbc.MysqlDataSource;

import javax.naming.Context;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class SQLController {

    private static final Context context = null;
    private static final MysqlDataSource dataSource;

    static {
        dataSource = new MysqlDataSource();
        dataSource.setPassword(FlareBot.passwd);
        dataSource.setDatabaseName("flarebot");
        dataSource.setPort(3306);
        dataSource.setPortNumber(3306);
        dataSource.setServerName("127.0.0.1");
        dataSource.setUser("flare");
    }

    private static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection("jdbc:mysql://localhost:3306/flarebot?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true&useSSL=false", "flare", FlareBot.passwd);
        return dataSource.getConnection();
    }

    public static void runSqlTask(SQLTask toRun) throws SQLException {
        Connection c = getConnection();
        toRun.execute(c);
        if (!c.isClosed())
            c.close();
    }
}

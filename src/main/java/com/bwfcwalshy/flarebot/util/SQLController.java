package com.bwfcwalshy.flarebot.util;

import com.bwfcwalshy.flarebot.FlareBot;
import com.mysql.cj.jdbc.MysqlDataSource;

import javax.naming.Context;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class SQLController {

    private static final Context context = null;
    private static final MysqlDataSource dataSource;
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    private static final ExecutorService SQL_POOL = Executors.newCachedThreadPool(r -> new Thread(r, "SQL Thread " + COUNTER.incrementAndGet()));

    static {
        dataSource = new MysqlDataSource();
        dataSource.setPassword(FlareBot.passwd);
        dataSource.setDatabaseName("flarebot");
        dataSource.setPort(3306);
        dataSource.setPortNumber(3306);
        dataSource.setServerName("127.0.0.1");
        dataSource.setUser("flare");
        dataSource.setURL(dataSource.getURL() + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true&useSSL=false");
        dataSource.setUrl(dataSource.getUrl() + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&autoReconnect=true&useSSL=false");
    }

    private static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void runSqlTask(SQLTask toRun) throws SQLException {
        Connection c = getConnection();
        toRun.execute(c);
        if (!c.isClosed())
            c.close();
    }

    public static void asyncRunSqlTask(SQLTask toRun) {
        SQL_POOL.submit(() -> {
            try {
                runSqlTask(toRun);
            } catch (Exception e) {
                FlareBot.LOGGER.error("Exception in " + Thread.currentThread().getName(), e);
            }
        });
    }
}

package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.mod.events.StrikeListener;
import com.bwfcwalshy.flarebot.util.SQLController;
import net.dv8tion.jda.core.entities.Member;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StrikeCounter {
    private static Map<String, Map<String, Integer>> strikes;
    public static Set<StrikeListener> LISTENERS = ConcurrentHashMap.newKeySet();

    static {
        strikes = new ConcurrentHashMap<>();
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS strikes (\n" +
                        "   guild VARCHAR(20),\n" +
                        "   userid VARCHAR(20),\n" +
                        "   strikes INTEGER,\n" +
                        "   PRIMARY KEY (guild, userid)\n" +
                        ")");
                ResultSet resultSet = conn.createStatement().executeQuery("SELECT * FROM strikes;");
                while (resultSet.next()) {
                    Map<String, Integer> strikeMap = strikes
                            .computeIfAbsent(resultSet.getString("guild"), m -> new ConcurrentHashMap<>());
                    strikeMap.put(resultSet.getString("userid"),
                            strikeMap.getOrDefault(resultSet.getString("userid"), 0) + resultSet.getInt("strikes"));
                }
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Could not load strikes!", e);
        }
    }

    public static void strike(Member member, int i) {
        Map<String, Integer> strikeMap = strikes.computeIfAbsent(member.getGuild().getId(), m -> new ConcurrentHashMap<>());
        strikeMap.put(member.getUser().getId(), strikeMap.getOrDefault(member.getUser().getId(), 0) + 1);
        callListeners(member);
        updateDatabase(member);
    }

    private static void updateDatabase(Member member) {
        SQLController.asyncRunSqlTask(conn -> {
            Map<String, Integer> strikeMap = strikes.computeIfAbsent(member.getGuild().getId(), m -> new ConcurrentHashMap<>());
            int strikes = strikeMap.getOrDefault(member.getUser().getId(), 0);
            PreparedStatement statement = conn.prepareStatement("UPDATE strikes SET strikes = ? WHERE guild = ? AND userid = ?");
            statement.setInt(1, strikes);
            statement.setString(2, member.getGuild().getId());
            statement.setString(3, member.getUser().getId());
            if (statement.executeUpdate() == 0) {
                statement = conn.prepareStatement("INSERT INTO strikes (strike, guild, userid) VALUES (\n" +
                        "   ?,\n" +
                        "   ?,\n" +
                        "   ?\n" +
                        ")");
                statement.setInt(1, strikes);
                statement.setString(2, member.getGuild().getId());
                statement.setString(3, member.getUser().getId());
            }
        });
    }

    private static void callListeners(Member member) {
        LISTENERS.forEach(l -> l.accept(member));
    }

    public static void strike(Member member) {
        strike(member, 1);
    }
}

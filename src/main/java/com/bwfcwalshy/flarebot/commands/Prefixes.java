package com.bwfcwalshy.flarebot.commands;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.util.SQLController;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Prefixes {
    private Map<String, Character> prefixes = new ConcurrentHashMap<>();

    public Prefixes() {
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS prefixes (" +
                        "   guildid VARCHAR(20) PRIMARY KEY," +
                        "   prefix CHAR(1)" +
                        ");");
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM prefixes;");
                while (set.next()) {
                    prefixes.put(set.getString("guildid"), set.getString("prefix").charAt(0));
                }
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Could not load prefixes!");
        }
    }

    public char get(String guildid) {
        if (guildid == null)
            return FlareBot.COMMAND_CHAR;
        return prefixes.getOrDefault(guildid, FlareBot.COMMAND_CHAR);
    }

    public void set(String guildid, char character) {
        if (character == FlareBot.COMMAND_CHAR) {
            prefixes.remove(guildid);
            try {
                SQLController.runSqlTask(conn -> {
                    PreparedStatement statement = conn.prepareStatement("DELETE FROM prefixes WHERE guildid = ?");
                    statement.setString(1, guildid);
                    statement.execute();
                });
            } catch (SQLException e) {
                FlareBot.LOGGER.error("Could not edit the prefixes in the database!", e);
            }
            return;
        }
        prefixes.put(guildid, character);
        try {
            SQLController.runSqlTask(conn -> {
                PreparedStatement statement = conn.prepareStatement("UPDATE prefixes SET prefix = ? WHERE guildid = ?");
                statement.setString(1, String.valueOf(character));
                statement.setString(2, guildid);
                if (statement.executeUpdate() == 0) {
                    statement = conn.prepareStatement("INSERT INTO prefixes (guildid, prefix) VALUES (?, ?)");
                    statement.setString(1, guildid);
                    statement.setString(2, String.valueOf(character));
                    statement.executeUpdate();
                }
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Could not edit the prefixes in the database!", e);
        }
    }
}

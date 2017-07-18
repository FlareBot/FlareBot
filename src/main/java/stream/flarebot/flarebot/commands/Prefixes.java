package stream.flarebot.flarebot.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.database.SQLController;

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
            FlareBot.LOGGER.error("Could not load prefixes!", e);
        }
    }

    public char get(String guildId) {
        if (guildId == null)
            return FlareBot.COMMAND_CHAR;
        return prefixes.getOrDefault(guildId, FlareBot.COMMAND_CHAR);
    }

    public void set(String guildId, char character) {
        if (character == FlareBot.COMMAND_CHAR) {
            prefixes.remove(guildId);
            try {
                SQLController.runSqlTask(conn -> {
                    PreparedStatement statement = conn.prepareStatement("DELETE FROM prefixes WHERE guildid = ?");
                    statement.setString(1, guildId);
                    statement.execute();
                });
            } catch (SQLException e) {
                FlareBot.LOGGER.error("Could not edit the prefixes in the database!", e);
            }
            update(guildId, character);
            return;
        }
        prefixes.put(guildId, character);
        try {
            SQLController.runSqlTask(conn -> {
                PreparedStatement statement = conn.prepareStatement("UPDATE prefixes SET prefix = ? WHERE guildid = ?");
                statement.setString(1, String.valueOf(character));
                statement.setString(2, guildId);
                if (statement.executeUpdate() == 0) {
                    statement = conn.prepareStatement("INSERT INTO prefixes (guildid, prefix) VALUES (?, ?)");
                    statement.setString(1, guildId);
                    statement.setString(2, String.valueOf(character));
                    statement.executeUpdate();
                }
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Could not edit the prefixes in the database!", e);
        }
        update(guildId, character);
    }

    public void update(String guildId, char prefix) {
        JsonArray array = new JsonArray();
        JsonObject guildObj = new JsonObject();
        guildObj.addProperty("guildId", guildId);
        guildObj.addProperty("prefix", prefix);
        array.add(guildObj);

        FlareBot.getInstance().postToApi("updatePrefixes", "prefixes", array);
    }

    public Map<String, Character> getPrefixes() {
        return this.prefixes;
    }
}

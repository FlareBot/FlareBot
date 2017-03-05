package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.mod.events.StrikeListener;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import com.bwfcwalshy.flarebot.util.SQLController;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.entities.Member;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class StrikeCounter {
    private static Map<String, Map<String, Integer>> strikes;
    public static final Set<StrikeListener> LISTENERS = ConcurrentHashMap.newKeySet();

    private static final File FILE = new File("strikeListeners.json");

    static {
        if (FILE.exists())
            try {
                JsonArray array = FlareBot.GSON.fromJson(new FileReader(FILE), JsonArray.class);
                Runtime.getRuntime().addShutdownHook(new Thread(StrikeCounter::store));
                StreamSupport.stream(array.spliterator(), false)
                        .map(JsonElement::getAsJsonObject)
                        .forEach(o -> {
                            String guild = o.get("guild").getAsString();
                            String clazz = o.get("class").getAsString();
                            try {
                                int strikesNeeded = o.getAsJsonPrimitive("strikesNeeded").getAsInt();
                                Class<?> c = Class.forName(clazz);
                                //noinspection unchecked
                                Constructor<? extends StrikeListener> co = (Constructor<? extends StrikeListener>)
                                        c.getConstructor(Integer.TYPE, String.class);
                                co.setAccessible(true);
                                LISTENERS.add(co.newInstance(strikesNeeded, guild));
                                co.setAccessible(false);
                            } catch (Exception e) {
                                FlareBot.LOGGER.error("Could not obtain " + clazz + " listener for " + guild, e);
                            }
                        });
            } catch (Exception e) {
                FlareBot.LOGGER.error("Could not load strike listeners!", e);
            }
        Runtime.getRuntime().addShutdownHook(new Thread(StrikeCounter::store));
        new FlarebotTask("Save strike listeners") {
            @Override
            public void run() {
                store();
            }
        }.repeat(300000, 300000);
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

    private static void store() {
        JsonArray jsonArray = new JsonArray();
        for (StrikeListener s : LISTENERS) {
            JsonObject listener = new JsonObject();
            listener.addProperty("guild", s.getGuild());
            listener.addProperty("class", s.getClass().getName());
            listener.addProperty("strikesNeeded", s.getNeededStrikes());
        }
        try {
            FileWriter writer = new FileWriter(FILE);
            FlareBot.GSON.toJson(jsonArray, writer);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            FlareBot.LOGGER.error("Could not save strike listeners!", e);
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
                statement = conn.prepareStatement("INSERT INTO strikes (strikes, guild, userid) VALUES (\n" +
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

    public static int getStrikes(Member member) {
        return strikes.computeIfAbsent(member.getGuild().getId(), m -> new ConcurrentHashMap<>()).getOrDefault(member.getUser().getId(), 0);
    }

    public static void resetStrikes(Member member) {
        strikes.computeIfAbsent(member.getGuild().getId(), m -> new ConcurrentHashMap<>()).put(member.getUser().getId(), 0);
    }
}

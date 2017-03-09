package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.util.SQLController;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StrikeCounter {
    private static Map<String, Map<String, Integer>> strikes;

    private static final File FILE = new File("strikeListeners.json");
    private static final Map<String, String> CHANNELS = new ConcurrentHashMap<>();

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
        for (OnStrikeActions action : OnStrikeActions.values()) {
            if (action.on(member, i)) {
                if (CHANNELS.containsKey(member.getGuild().getId())) {
                    if (member.getGuild().getTextChannelById(CHANNELS.get(member.getGuild().getId())) == null) {
                        CHANNELS.remove(member.getGuild().getId());
                    } else {
                        if (!member.getGuild().getSelfMember().hasPermission(member.getGuild().getTextChannelById(CHANNELS.get(member.getGuild().getId())),
                                Permission.MESSAGE_WRITE,
                                Permission.MESSAGE_EMBED_LINKS)) {
                            CHANNELS.remove(member.getGuild().getId());
                            return;
                        }
                        member.getGuild().getTextChannelById(CHANNELS.get(member.getGuild().getId()))
                                .sendMessage(MessageUtils.getEmbed()
                                        .addField("User", member.getAsMention(), true)
                                        .addField("Reason", "Too many violations!", true)
                                        .addField("Action", action.toString(), true)
                                        .setAuthor(member.getUser().getName(), null, member.getUser().getEffectiveAvatarUrl())
                                        .setTimestamp(ZonedDateTime.now())
                                        .build());
                    }
                }
                break;
            }
        }
        // Before anyone asks, purpose of anyMatch is so it only does one (successful) action. Also, when I add modlog it will be useful to have findFirst.
        // Will probably switch it to a for loop though
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

    public static void strike(Member member) {
        strike(member, 1);
    }

    public static int getStrikes(Member member) {
        return strikes.computeIfAbsent(member.getGuild().getId(), m -> new ConcurrentHashMap<>()).getOrDefault(member.getUser().getId(), 0);
    }

    public static void resetStrikes(Member member) {
        strikes.computeIfAbsent(member.getGuild().getId(), m -> new ConcurrentHashMap<>()).put(member.getUser().getId(), 0);
    }

    public static void setChannel(TextChannel t, Guild guild) {
        if (!t.getGuild().equals(guild)) throw new IllegalArgumentException("t.getGuild() must be guild!");
        CHANNELS.put(guild.getId(), t.getId());
    }
}

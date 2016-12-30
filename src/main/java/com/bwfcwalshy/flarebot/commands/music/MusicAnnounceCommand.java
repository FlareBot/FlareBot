package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.util.SQLController;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class MusicAnnounceCommand implements Command {
    private static final Pattern ARGS_PATTERN = Pattern.compile("(here)|(off)", Pattern.CASE_INSENSITIVE);
    private static Map<String, String> announcements = new ConcurrentHashMap<>();

    static {
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS announces (" +
                        "   guildid VARCHAR(20) PRIMARY KEY," +
                        "   channelid VARCHAR(20) UNIQUE" +
                        ");");
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM announces;");
                while (set.next()) {
                    announcements
                            .put(set.getString("guildid"), set.getString("channelid"));
                }
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Could not load song announces!!");
        }
    }

    public static Map<String, String> getAnnouncements() {
        return announcements;
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 1 && ARGS_PATTERN.matcher(args[0]).matches()) {
            if (args[0].equalsIgnoreCase("here")) {
                announcements.put(channel.getGuild().getID(), channel.getID());
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .withDesc("Set music announcements to appear in " + channel).build(), channel);
                try {
                    SQLController.runSqlTask(conn -> {
                        PreparedStatement statement = conn.prepareStatement("UPDATE announces SET channelid = ? WHERE guildid = ?");
                        statement.setString(1, channel.getID());
                        statement.setString(2, channel.getGuild().getID());
                        if (statement.executeUpdate() == 0) {
                            statement = conn.prepareStatement("INSERT INTO announces (guildid, channelid) VALUES (?, ?)");
                            statement.setString(1, channel.getGuild().getID());
                            statement.setString(2, channel.getID());
                            statement.executeUpdate();
                        }
                    });
                } catch (SQLException e) {
                    FlareBot.LOGGER.error("Could not edit the announces in the database!", e);
                }
            } else {
                announcements.remove(channel.getGuild().getID());
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .withDesc(String.format("Disabled announcements for `%s`", channel.getGuild().getName()))
                        .build(), channel);
                try {
                    SQLController.runSqlTask(conn -> {
                        PreparedStatement statement = conn.prepareStatement("DELETE FROM announces WHERE guildid = ?");
                        statement.setString(1, channel.getGuild().getID());
                    });
                } catch (SQLException e) {
                    FlareBot.LOGGER.error("Could not edit the announces in the database!", e);
                }
            }
        } else {
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                    .withDesc("Bad syntax! Must have either `HERE` or `OFF` as your first, and only, argument." +
                            "\nCase insensitive.").build(), channel);
        }
    }

    @Override
    public String getCommand() {
        return "announce";
    }

    @Override
    public String getDescription() {
        return "Announces a track start in a text channel. Usage: `_announce HERE|OFF`";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.songannounce";
    }
}

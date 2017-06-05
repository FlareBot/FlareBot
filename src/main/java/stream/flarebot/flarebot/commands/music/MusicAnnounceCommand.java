package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.SQLController;

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
            FlareBot.LOGGER.error("Could not load song announces!!", e);
        }
    }

    public static Map<String, String> getAnnouncements() {
        return announcements;
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1 && ARGS_PATTERN.matcher(args[0]).matches()) {
            if (args[0].equalsIgnoreCase("here")) {
                announcements.put(channel.getGuild().getId(), channel.getId());
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("Set music announcements to appear in " + channel
                                .getAsMention()).build()).queue();
                try {
                    SQLController.runSqlTask(conn -> {
                        PreparedStatement statement = conn
                                .prepareStatement("UPDATE announces SET channelid = ? WHERE guildid = ?");
                        statement.setString(1, channel.getId());
                        statement.setString(2, channel.getGuild().getId());
                        if (statement.executeUpdate() == 0) {
                            statement = conn
                                    .prepareStatement("INSERT INTO announces (guildid, channelid) VALUES (?, ?)");
                            statement.setString(1, channel.getGuild().getId());
                            statement.setString(2, channel.getId());
                            statement.executeUpdate();
                        }
                    });
                } catch (SQLException e) {
                    FlareBot.LOGGER.error("Could not edit the announces in the database!", e);
                }
            } else {
                announcements.remove(channel.getGuild().getId());
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription(String
                                .format("Disabled announcements for `%s`", channel.getGuild()
                                        .getName()))
                        .build()).queue();
                try {
                    SQLController.runSqlTask(conn -> {
                        PreparedStatement statement = conn.prepareStatement("DELETE FROM announces WHERE guildid = ?");
                        statement.setString(1, channel.getGuild().getId());
                    });
                } catch (SQLException e) {
                    FlareBot.LOGGER.error("Could not edit the announces in the database!", e);
                }
            }
        } else {
            MessageUtils.getUsage(this, channel).queue();
        }
    }

    @Override
    public String getCommand() {
        return "announce";
    }

    @Override
    public String getDescription() {
        return "Announces a track start in a text channel.";
    }

    @Override
    public String getUsage() {
        return "`{%}announce <here|off>` - Sets the music announce channel or turns it off";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.songannounce";
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}

package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.util.SQLController;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class SaveCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendMessage("Usage: _save [NAME]", channel);
            return;
        }
        String name = "";
        for (String arg : args) name += arg + ' ';
        name = name.trim();
        if (name.length() > 20) {
            MessageUtils.sendMessage("Name must be up to 20 characters!", channel);
            return;
        }
        if (!FlareBot.getInstance().getMusicManager().hasPlayer(channel.getGuild().getID())) {
            MessageUtils.sendMessage("Your playlist is empty!", channel);
            return;
        }
        Queue<Track> playlist = FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getID()).getPlaylist();
        if (playlist.isEmpty()) {
            MessageUtils.sendMessage("Your playlist is empty!", channel);
            return;
        }
        channel.setTypingStatus(true);
        try {
            String finalName = name;
            SQLController.runSqlTask(connection -> {
                connection.createStatement().execute("CREATE TABLE IF NOT EXISTS playlist (\n" +
                        "  name  VARCHAR(60),\n" +
                        "  guild VARCHAR(20),\n" +
                        "  list  TEXT,\n" +
                        "  scope  VARCHAR(7) DEFAULT 'local',\n" +
                        "  PRIMARY KEY(name, guild)\n" +
                        ")");
                PreparedStatement exists = connection.prepareStatement("SELECT * FROM playlist WHERE name = ? AND guild = ?");
                exists.setString(1, finalName);
                exists.setString(2, channel.getGuild().getID());
                exists.execute();
                if (exists.getResultSet().isBeforeFirst()) {
                    MessageUtils.sendMessage("That name is already taken!", channel);
                    return;
                }
                PreparedStatement statement = connection.prepareStatement("INSERT INTO playlist (name, guild, list) VALUES (" +
                        "   ?," +
                        "   ?," +
                        "   ?" +
                        ")");
                statement.setString(1, finalName);
                statement.setString(2, channel.getGuild().getID());
                statement.setString(3, playlist.stream()
                        .map(track -> track.getTrack().getIdentifier())
                        .collect(Collectors.joining(",")));
                statement.executeUpdate();
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("Success!"), channel);
            });
        } catch (SQLException e) {
            MessageUtils.sendException("**Database error!**", e, channel);
            FlareBot.LOGGER.error("Database error!", e);
        }
    }

    @Override
    public String getCommand() {
        return "save";
    }

    @Override
    public String getDescription() {
        return "Save the current playlist! Usage: `_save NAME`";
    }

    @Override
    public String getPermission() {
        return "flarebot.playlist.save";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

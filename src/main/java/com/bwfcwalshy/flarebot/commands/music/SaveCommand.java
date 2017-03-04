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
            channel.sendMessage("Usage: _save [NAME]").queue();
            return;
        }
        String name = "";
        for (String arg : args) name += arg + ' ';
        name = name.trim();
        if (name.length() > 20) {
            channel.sendMessage("Name must be up to 20 characters!").queue();
            return;
        }
        if (!FlareBot.getInstance().getMusicManager().hasPlayer(channel.getGuild().getId())) {
            channel.sendMessage("Your playlist is empty!").queue();
            return;
        }
        Queue<Track> playlist = FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getId()).getPlaylist();
        if (playlist.isEmpty()) {
            channel.sendMessage("Your playlist is empty!").queue();
            return;
        }
        channel.sendTyping().complete();
        try {
            String finalName = name;
            SQLController.runSqlTask(connection -> {
                connection.createStatement().execute("CREATE TABLE IF NOT EXISTS playlist (\n" +
                        "  playlist_name  VARCHAR(60),\n" +
                        "  guild VARCHAR(20),\n" +
                        "  list  TEXT,\n" +
                        "  scope  VARCHAR(7) DEFAULT 'local',\n" +
                        "  PRIMARY KEY(playlist_name, guild)\n" +
                        ")");
                PreparedStatement exists = connection.prepareStatement("SELECT * FROM playlist WHERE playlist_name = ? AND guild = ?");
                exists.setString(1, finalName);
                exists.setString(2, channel.getGuild().getId());
                exists.execute();
                if (exists.getResultSet().isBeforeFirst()) {
                    channel.sendMessage("That name is already taken!").queue();
                    return;
                }
                PreparedStatement statement = connection.prepareStatement("INSERT INTO playlist (playlist_name, guild, list) VALUES (" +
                        "   ?," +
                        "   ?," +
                        "   ?" +
                        ")");
                statement.setString(1, finalName);
                statement.setString(2, channel.getGuild().getId());
                statement.setString(3, playlist.stream()
                        .map(track -> track.getTrack().getIdentifier())
                        .collect(Collectors.joining(",")));
                statement.executeUpdate();
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("Success!").build()).queue();
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

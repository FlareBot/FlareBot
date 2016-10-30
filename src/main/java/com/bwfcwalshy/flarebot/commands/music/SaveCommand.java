package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.util.SQLController;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.audio.AudioPlayer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class SaveCommand implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length == 0){
            MessageUtils.sendMessage(channel, "Usage: _save [NAME]");
            return;
        }
        String name = "";
        for (String arg : args) name += arg + ' ';
        name = name.trim();
        if(name.length() > 20){
            MessageUtils.sendMessage(channel, "Name must be up to 20 characters!");
            return;
        }
        if(!FlareBot.getInstance().getMusicManager().getPlayers().containsKey(channel.getGuild().getID())){
            MessageUtils.sendMessage(channel, "Your playlist is empty!");
            return;
        }
        List<AudioPlayer.Track> playlist = FlareBot.getInstance().getMusicManager().getPlayers().get(channel.getGuild().getID()).getPlaylist();
        if(playlist.isEmpty()){
            MessageUtils.sendMessage(channel, "Your playlist is empty!");
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
                        "  PRIMARY KEY(name, guild)\n" +
                        ")");
                PreparedStatement exists = connection.prepareStatement("SELECT * FROM playlist WHERE name = ? AND guild = ?");
                exists.setString(1, finalName);
                exists.setString(2, channel.getGuild().getID());
                exists.execute();
                if (exists.getResultSet().isBeforeFirst()) {
                    MessageUtils.sendMessage(channel, "That name is already taken!");
                    return;
                }
                PreparedStatement statement = connection.prepareStatement("INSERT INTO playlist (name, guild, list) VALUES (" +
                        "   ?," +
                        "   ?," +
                        "   ?" +
                        ")");
                statement.setString(1, finalName);
                statement.setString(2, channel.getGuild().getID());
                statement.setString(3, playlist.stream().map(track -> (String) track.getMetadata().get("id")).collect(Collectors.joining(",")));
                statement.executeUpdate();
                MessageUtils.sendMessage(channel, "Success!");
            });
        } catch (SQLException e) {
            MessageUtils.sendException("**Database error!**", e, channel);
        }
    }

    @Override
    public String getCommand() {
        return "save";
    }

    @Override
    public String getDescription() {
        return "Save the current playlist!";
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

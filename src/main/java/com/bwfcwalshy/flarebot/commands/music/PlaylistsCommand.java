package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.util.SQLController;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class PlaylistsCommand implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length != 0){
            MessageUtils.sendMessage(channel, "Usage: _playlists");
            return;
        }
        channel.setTypingStatus(true);
        try {
            SQLController.runSqlTask(connection -> {
                connection.createStatement().execute("CREATE TABLE IF NOT EXISTS playlist (\n" +
                        "  name  VARCHAR(60),\n" +
                        "  guild VARCHAR(20),\n" +
                        "  list  TEXT,\n" +
                        "  PRIMARY KEY(name, guild)\n" +
                        ")");
                PreparedStatement get = connection.prepareStatement("SELECT name FROM playlist WHERE guild = ?");
                get.setString(1, channel.getGuild().getID());
                get.execute();
                ResultSet set = get.getResultSet();
                StringBuilder sb = new StringBuilder();
                List<String> songs = new ArrayList<>();
                int i = 1;
                while (set.next() && songs.size() < 25) {
                    String toAppend = String.format("%s. %s\n", i++, set.getString("name"));
                    if (sb.length() + toAppend.length() > 1024) {
                        songs.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    sb.append(toAppend);
                }
                songs.add(sb.toString());
                EmbedBuilder builder = MessageUtils.getEmbed(sender);
                i = 1;
                for (String s : songs) {
                    builder.appendField("Page " + i++, s.isEmpty() ? "**No playlists!**" : s, false);
                }
                MessageUtils.sendMessage(builder.build(), channel);
            });
        } catch (SQLException e) {
            MessageUtils.sendException("**Database error!**", e, channel);
            FlareBot.LOGGER.error("Database error!", e);
        }
    }

    @Override
    public String getCommand() {
        return "playlists";
    }

    @Override
    public String getDescription() {
        return "Lists all playlists saved in the current guild";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

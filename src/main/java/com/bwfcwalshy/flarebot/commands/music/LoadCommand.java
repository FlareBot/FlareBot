package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.VideoThread;
import com.bwfcwalshy.flarebot.util.SQLController;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class LoadCommand implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 0) {
            MessageUtils.sendMessage("Usage: _load [NAME]", channel);
            return;
        }
        String name = "";
        for (String arg : args) name += arg + ' ';
        name = name.trim();
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
                PreparedStatement exists = connection.prepareStatement("SELECT list FROM playlist WHERE name = ? AND guild = ? OR scope = 'global'");
                exists.setString(1, finalName);
                exists.setString(2, channel.getGuild().getID());
                exists.execute();
                ResultSet set = exists.getResultSet();
                if (set.isBeforeFirst()) {
                    set.next();
                    VideoThread.getThread(finalName + '\u200B' + set.getString("list"), channel, sender).start();
                } else
                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("*That playlist does not exist!*"), channel);
            });
        } catch (SQLException e) {
            MessageUtils.sendException("**Database error!**", e, channel);
            FlareBot.LOGGER.error("Database error!", e);
        }
    }

    @Override
    public String getCommand() {
        return "load";
    }

    @Override
    public String getDescription() {
        return "Loads a playlist. Usage `load NAME`";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.playlist.load";
    }
}

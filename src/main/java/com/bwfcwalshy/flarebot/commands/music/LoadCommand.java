package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.VideoThread;
import com.bwfcwalshy.flarebot.util.SQLController;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class LoadCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            channel.sendMessage("Usage: _load [NAME]").queue();
            return;
        }
        String name = "";
        for (String arg : args) name += arg + ' ';
        name = name.trim();
        channel.sendTyping().queue();
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
                exists.setString(2, channel.getGuild().getId());
                exists.execute();
                ResultSet set = exists.getResultSet();
                if (set.isBeforeFirst()) {
                    set.next();
                    VideoThread.getThread(finalName + '\u200B' + set.getString("list"), channel, sender).start();
                } else
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("*That playlist does not exist!*").build()).queue();
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

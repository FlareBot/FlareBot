package stream.flarebot.flarebot.commands.music;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.SQLController;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <br>
 * Created by Arsen on 29.10.16..
 */
public class DeleteCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel).queue();
            return;
        }
        String name = "";
        for (String arg : args) name += arg + ' ';
        name = name.trim();
        channel.sendTyping().complete();
        try {
            String finalName = name;
            SQLController.runSqlTask(connection -> {
                connection.createStatement().execute("CREATE TABLE IF NOT EXISTS playlist (\n" +
                        "  playlist_name VARCHAR(10),\n" +
                        "  guild VARCHAR(10),\n" +
                        "  list VARCHAR(80),\n" +
                        "  scope  VARCHAR(7) DEFAULT 'local',\n" +
                        "  PRIMARY KEY(playlist_name, guild)\n" +
                        ")");
                PreparedStatement update = connection
                        .prepareStatement("DELETE FROM playlist WHERE playlist_name = ? AND guild = ?");
                update.setString(1, finalName);
                update.setString(2, channel.getGuild().getId());
                if (update.executeUpdate() > 0) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String
                                    .format("*Removed the playlist %s*", finalName)).build())
                            .queue();
                } else channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription(String
                                .format("*The playlist %s doesn't exist!", finalName))
                        .build()).queue();
            });
        } catch (SQLException e) {
            MessageUtils.sendException("**Database error!**", e, channel);
        }
    }

    @Override
    public String getCommand() {
        return "delete";
    }

    @Override
    public String getDescription() {
        return "Deletes a playlist.";
    }

    @Override
    public String getUsage() {
        return "`{%}delete <playlist>` - Deletes a playlist";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.playlist.delete";
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

}

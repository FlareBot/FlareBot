package stream.flarebot.flarebot.commands.music;

import com.datastax.driver.core.ResultSet;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class DeleteCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
            return;
        }
        channel.sendTyping().complete();
        String name = MessageUtils.getMessage(args, 0);
        CassandraController.runTask(session -> {
            ResultSet set = session.execute(session
                    .prepare("SELECT playlist_name FROM flarebot.playlist WHERE playlist_name = ? AND guild_id = ?")
                    .bind().setString(0, name).setString(1, channel.getGuild().getId()));
            if (set.one() != null) {
                session.execute(session
                        .prepare("DELETE FROM flarebot.playlist WHERE playlist_name = ? AND guild_id = ?").bind()
                        .setString(0, name).setString(1, channel.getGuild().getId()));
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription(String
                                .format("Removed the playlist '%s'", name)).setColor(Color.green)
                        .build()).queue();
            } else {
                MessageUtils.sendErrorMessage(String.format("The playlist '%s' does not exist!", name), channel, sender);
            }
        });
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
        return "`{%}delete <playlist>` - Deletes a playlist.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.queue.delete";
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

}

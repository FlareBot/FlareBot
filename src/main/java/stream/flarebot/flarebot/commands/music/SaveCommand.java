package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Track;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

import java.util.Queue;
import java.util.stream.Collectors;


public class SaveCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            channel.sendMessage("Usage: " + FlareBot.getPrefix(channel.getGuild().getId()) + "save [NAME]").queue();
            return;
        }

        String name = FlareBot.getMessage(args, 0);
        if (name.length() > 20) {
            channel.sendMessage("Name must be up to 20 characters!").queue();
            return;
        }
        if (!FlareBot.getInstance().getMusicManager().hasPlayer(channel.getGuild().getId())) {
            channel.sendMessage("Your playlist is empty!").queue();
            return;
        }
        Queue<Track> playlist = FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getId())
                .getPlaylist();
        if (playlist.isEmpty()) {
            channel.sendMessage("Your playlist is empty!").queue();
            return;
        }

        channel.sendTyping().complete();

        FlareBot.getInstance().getManager().savePlaylist(channel, sender.getId(), name, playlist.stream()
                .map(track -> track
                        .getTrack()
                        .getIdentifier())
                .collect(Collectors
                        .joining(",")));
    }

    @Override
    public String getCommand() {
        return "save";
    }

    @Override
    public String getDescription() {
        return "Save the current playlist! Usage: `save NAME`";
    }

    @Override
    public String getUsage() {
        return "{%}save <name>";
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

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
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;


public class SaveCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
            return;
        }

        String name = MessageUtils.getMessage(args, 0);
        if (name.length() > 20) {
            MessageUtils.sendErrorMessage("Name can only be a maximum of 20 characters!", channel);
            return;
        }
        if (!FlareBot.getInstance().getMusicManager().hasPlayer(channel.getGuild().getId())) {
            MessageUtils.sendErrorMessage("Your playlist is empty!", channel);
            return;
        }
        Queue<Track> playlist = FlareBot.getInstance().getMusicManager().getPlayer(guild.getGuildId())
                .getPlaylist();
        Track currentlyPlaying =
                FlareBot.getInstance().getMusicManager().getPlayer(guild.getGuildId()).getPlayingTrack();

        channel.sendTyping().complete();

        List<String> tracks = playlist.stream()
                .map(track -> track
                        .getTrack()
                        .getIdentifier()).collect(Collectors.toList());
        if (currentlyPlaying != null) {
            tracks.add(currentlyPlaying.getTrack().getIdentifier());
        }

        if (tracks.isEmpty()) {
            MessageUtils.sendErrorMessage("Your playlist is empty!", channel);
            return;
        }

        FlareBot.getInstance().getManager().savePlaylist(this,
                channel,
                sender.getId(),
                this.getPermissions(channel).hasPermission(member, "flarebot.queue.save.overwrite"),
                name,
                tracks);
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
    public String getUsage() {
        return "`{%}save <name>` - Saves a playlist.";
    }

    @Override
    public String getPermission() {
        return "flarebot.queue.save";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RepeatCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        Player player = FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getId());
        if (player.getPlayingTrack() == null) {
            MessageUtils.sendErrorMessage("Can't repeat when their is no song to repeat!", channel);
        } else {
            Queue<Track> queue = new ConcurrentLinkedQueue<>();
            queue.add(player.getPlayingTrack().makeClone());
            queue.addAll(player.getPlaylist());
            player.getPlaylist().clear();
            player.getPlaylist().addAll(queue);
            channel.sendMessage(new EmbedBuilder().setColor(Color.green).setDescription("The player will now repeat the current track!").build()).queue();
        }
    }

    @Override
    public String getCommand() {
        return "repeat";
    }

    @Override
    public String getDescription() {
        return "Repeat the current song";
    }

    @Override
    public String getUsage() {
        return "`{%}repeat` - Repeats the current song";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

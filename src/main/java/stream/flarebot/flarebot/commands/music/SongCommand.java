package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.player.Track;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.music.extractors.YouTubeExtractor;
import stream.flarebot.flarebot.util.MessageUtils;

import java.text.DecimalFormat;

public class SongCommand implements Command {

    private PlayerManager manager;

    public SongCommand(FlareBot bot) {
        this.manager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (manager.getPlayer(channel.getGuild().getId()).getPlayingTrack() != null) {
            Track track = manager.getPlayer(channel.getGuild().getId()).getPlayingTrack();
            channel.sendMessage(MessageUtils.getEmbed(sender)
                    .addField("Current song", getLink(track), false)
                    .addField("Amount Played", getProgressBar(track), true)
                    .addField("Time", String.format("%s / %s", formatDuration(track.getTrack().getPosition()), formatDuration(track.getTrack().getDuration())), false)
                    .build())
                    .queue();
        } else {
            channel.sendMessage(MessageUtils.getEmbed(sender)
                    .addField("Current song", "**No song playing right now!**", false)
                    .build()).queue();
        }
    }

    public static String getLink(Track track) {
        String name = String.valueOf(track.getTrack().getInfo().title);
        String link = YouTubeExtractor.WATCH_URL + track.getTrack().getIdentifier();
        return String.format("[`%s`](%s)", name, link);
    }

    @Override
    public String getCommand() {
        return "song";
    }

    @Override
    public String getDescription() {
        return "Get the current song playing.";
    }

    @Override
    public String getUsage() {
        return "{%}song";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    public static String formatDuration(long duration) {
        long totalSeconds = duration / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = (totalSeconds / 3600);
        return (hours > 0 ? (hours < 10 ? "0" + hours : hours) + ":" : "")
                + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static String getProgressBar(Track track) {
        float percentage = (100f / track.getTrack().getDuration() * track.getTrack().getPosition());
        StringBuilder progress = new StringBuilder("[");
        progress.append(StringUtils.repeat("▬", (int) Math.round((double) percentage / 10)));
        progress.append("]()");
        progress.append(StringUtils.repeat("▬", 10 - (int) Math.round((double) percentage / 10)));
        progress.append(" " + new DecimalFormat("#.##").format(percentage) + "%");
        return progress.toString();
    }

}

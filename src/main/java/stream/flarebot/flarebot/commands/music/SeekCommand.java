package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Track;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;


public class SeekCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            Long millis = GeneralUtils.parseTime(args[0]);
            if (millis == null || millis > Integer.MAX_VALUE) {
                MessageUtils.sendErrorMessage("You have entered an invalid duration to skip to!\n" + getExtraInfo(), channel);
                return;
            }
            Track t = FlareBot.getInstance().getMusicManager().getPlayer(guild.getGuildId()).getPlayingTrack();
            if (t == null) {
                MessageUtils.sendErrorMessage("There is no song currently playing!", channel);
                return;
            } else {
                if (t.getTrack().getInfo().isStream) {
                    MessageUtils.sendErrorMessage("Cannot seek on a livestream!", channel);
                    return;
                } else if (!t.getTrack().isSeekable()) {
                    MessageUtils.sendErrorMessage("Cannot seek on this track!", channel);
                    return;
                } else {
                    if (millis >= t.getTrack().getDuration()) {
                        MessageUtils.sendErrorMessage("The duration specified is bigger than the length of the video!", channel);
                        return;
                    } else {
                        t.getTrack().setPosition(millis);
                        MessageUtils.sendSuccessMessage("The track has been skipped to: " + GeneralUtils.formatJodaTime(new Duration(millis).toPeriod()), channel);
                        return;
                    }
                }
            }
        }
        MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "seek";
    }

    @Override
    public String getDescription() {
        return "Allows users to skip to a certain point in a video";
    }

    @Override
    public String getUsage() {
        return "`{%}seek <time>` - Seeks to a specific time in the currently playing video.";
    }

    @Override
    public String getExtraInfo() {
        return "**Time formatting**\n" +
                "`h:m:s` - Skip to a specific amount of hours, minutes and seconds\n" +
                "`m:s` - Skip to a specific amount of minutes and seconds\n" +
                "`s` - Skip to a specific amount of seconds";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Track;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.joda.time.Period;
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
            PeriodFormatter formatter = new PeriodFormatterBuilder()
                    .appendHours().appendSuffix("h")
                    .appendMinutes().appendSuffix("m")
                    .appendSeconds().appendSuffix("s")
                    .toFormatter();
            Period period;
            try {
                period = formatter.parsePeriod(args[0]);
            } catch (IllegalArgumentException e) {
                MessageUtils.sendErrorMessage("The duration is not in the correct format!", channel);
                return;
            }
            long millis = period.toStandardDuration().getMillis();
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
                        MessageUtils.sendSuccessMessage("The track has been skipped to: " + GeneralUtils.formatJodaTime(period), channel);
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
        return "`{%}seek <time>` - Seeks to a specific time in the currently playing video";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

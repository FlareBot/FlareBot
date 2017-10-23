package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.joda.time.Period;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.FutureAction;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

public class RemindCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length < 2) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            Period period;
            if ((period = GeneralUtils.getTimeFromInput(args[0], channel)) == null) return;
            String reminder = FlareBot.getMessage(args, 1);
            channel.sendMessage("\uD83D\uDC4D I will remind you in " + GeneralUtils.formatJodaTime(period) + " (at "
                    + GeneralUtils.formatPrecisely(period) + ")").queue();

            Scheduler.queueFutureAction(guild.getGuildIdLong(), channel.getIdLong(), sender.getIdLong(), reminder.substring(0,
                    Math.min(reminder.length(), 1000)), period, FutureAction.Action.REMINDER);
        }
    }

    @Override
    public String getCommand() {
        return "remind";
    }

    @Override
    public String getDescription() {
        return "Get reminders about things easily!";
    }

    @Override
    public String getUsage() {
        return "`{%}remind <duration> <reminder>` - Reminds a user about something after a duration";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"r"};
    }

    @Override
    public boolean isBetaTesterCommand() {
        return true;
    }
}

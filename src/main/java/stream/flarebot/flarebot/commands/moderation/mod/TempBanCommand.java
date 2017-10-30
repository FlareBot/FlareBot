package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.joda.time.Period;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.ModlogAction;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.FutureAction;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class TempBanCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 2) {
            if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.BAN_MEMBERS)) {
                User user = GeneralUtils.getUser(args[0]);
                if (user == null) {
                    MessageUtils.sendErrorMessage("We cannot find that user! Try their ID if you didn't already.", channel, sender);
                    return;
                }
                String reason = null;
                if (args.length >= 3)
                    reason = MessageUtils.getMessage(args, 2);
                try {
                    Period period = GeneralUtils.getTimeFromInput(args[1], channel);
                    if (period == null) return;
                    channel.getGuild().getController().ban(channel.getGuild().getMember(user), 7, reason).queue();
                    guild.getAutoModConfig().postToModLog(user, sender, ModlogAction.TEMP_BAN.toPunishment(
                            period.toStandardDuration().getMillis()), reason);
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("The ban hammer has been struck on " + user.getName() + " for "
                                    + GeneralUtils.formatJodaTime(period)).setImage(channel.getGuild().getId()
                                    .equals(FlareBot.OFFICIAL_GUILD) ? "https://flarebot.stream/img/banhammer.png" : null)
                            .setColor(Color.WHITE).build()).queue();
                    Scheduler.queueFutureAction(channel.getGuild().getIdLong(), channel.getIdLong(), sender.getIdLong(),
                            user.getIdLong(), reason, period, FutureAction.Action.TEMP_BAN);
                } catch (PermissionException e) {
                    MessageUtils.sendErrorMessage(String.format("Cannot ban user **%s#%s**! I do not have permission!", user.getName(), user.getDiscriminator()), channel);
                }
            } else {
                MessageUtils.sendErrorMessage("We can't ban users! Make sure we have the `Ban Members` permission!", channel, sender);
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    @Override
    public String getCommand() {
        return "tempban";
    }

    @Override
    public String getDescription() {
        return "Temp bans a user";
    }

    @Override
    public String getUsage() {
        return "`{%}tempban <user> <time> [reason]` - Temp bans a user.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

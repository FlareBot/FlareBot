package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import org.joda.time.Period;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.ModlogAction;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.FutureAction;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

public class TempMuteCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length < 2) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            User user = GeneralUtils.getUser(args[0], guild.getGuildId());
            if (user == null) {
                MessageUtils.sendErrorMessage("Invalid user!!", channel);
                return;
            }
            if (guild.getMutedRole() == null) {
                MessageUtils.sendErrorMessage("Error getting the \"Muted\" role! Check FlareBot has permissions to create it!", channel);
                return;
            }

            try {
                guild.getAutoModGuild().muteUser(guild.getGuild(), guild.getGuild().getMember(user));
            } catch (HierarchyException e) {
                MessageUtils.sendErrorMessage("Cannot apply the mute role, make sure it is below FlareBot in the role hierarchy.",
                        channel);
                return;
            }

            Period period;
            if ((period = GeneralUtils.getTimeFromInput(args[1], channel)) == null) return;
            String reason = args.length >= 3 ? FlareBot.getMessage(args, 2) : null;
            guild.getAutoModConfig().postToModLog(user, sender, ModlogAction.TEMP_MUTE.toPunishment(period.toStandardDuration().getMillis()), reason);
            MessageUtils.sendSuccessMessage("Temporarily Muted " + user.getAsMention() + " for " + GeneralUtils.formatJodaTime(period)
                    + (reason == null ? "" : " (`" + reason.replaceAll("`", "'") + "`)"), channel, sender);

            new FutureAction(guild.getGuild().getIdLong(), channel.getIdLong(), sender.getIdLong(), user.getIdLong(),
                    null, period, FutureAction.Action.TEMP_MUTE).queue();
        }
    }

    @Override
    public String getCommand() {
        return "tempmute";
    }

    @Override
    public String getDescription() {
        return "Temporarily mute a user!";
    }

    @Override
    public String getUsage() {
        return "`{%}tempmute <duration> [reason]` - Temp mutes a user for a specified amount of time";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

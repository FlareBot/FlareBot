package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.requests.RestAction;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.ModlogAction;
import stream.flarebot.flarebot.mod.Punishment;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.RestActionTask;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

// THIS IS NOT FOR v4
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
                guild.getAutoModConfig().postToModLog(user, sender, ModlogAction.BAN.toPunishment(), reason);
                try {
                    channel.getGuild().getController().ban(channel.getGuild().getMember(user), 7, reason).queue();
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("The ban hammer has been struck on " + user.getName() + " \uD83D\uDD28")
                            .setImage(channel.getGuild().getId().equals(FlareBot.OFFICIAL_GUILD) ?
                                    "https://cdn.discordapp.com/attachments/226785954537406464/309414200344707084/logo-no-background.png" : null)
                            .build()).queue();
                    while (!guild.getGuild().getBans().complete().contains(user)) {
                        //Nothing!!
                    }
                    PeriodFormatter formatter = new PeriodFormatterBuilder()
                            .appendDays().appendSuffix("d")
                            .appendHours().appendSuffix("h")
                            .appendMinutes().appendSuffix("m")
                            .appendSeconds().appendSuffix("s")
                            .toFormatter();
                    Period period = formatter.parsePeriod(args[1]);
                    long mills = period.toStandardSeconds().getSeconds() * 1000;
                    RestAction action = guild.getGuild().getController().unban(user);
                    new RestActionTask(action, "Unban user: " + user.getName()).delay(mills);
                } catch (PermissionException e) {
                    MessageUtils.sendErrorMessage(String.format("Cannot ban player **%s#%s**! I do not have permission!", user.getName(), user.getDiscriminator()), channel);
                }
            } else {
                MessageUtils.sendErrorMessage("We can't ban users! Make sure we have the `Ban Members` permission!", channel, sender);
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender);
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

package stream.flarebot.flarebot.commands.useful;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.joda.time.Period;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.scheduler.FutureAction;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.FormatUtils;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;
import stream.flarebot.flarebot.util.pagination.PagedEmbedBuilder;
import stream.flarebot.flarebot.util.pagination.PaginationUtil;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TimeZone;

public class RemindCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length < 2) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("list")) {
                    Set<FutureAction> futureActions = FlareBot.instance().getFutureActions();
                    StringBuilder actionBuilder = new StringBuilder();
                    for (FutureAction action : futureActions) {
                        if ((action.getAction().equals(FutureAction.Action.REMINDER)
                                || action.getAction().equals(FutureAction.Action.DM_REMINDER))
                                && action.getResponsible() == sender.getIdLong()) {
                            LocalDateTime time = LocalDateTime.ofInstant(action.getExpires().toDate().toInstant(),
                                    TimeZone.getTimeZone("UTC").toZoneId());
                            actionBuilder.append("`").append(FormatUtils.truncate(100, action.getContent()))
                                    .append("` at ").append(FormatUtils.formatTime(time)).append(" via ")
                                    .append(action.getAction().equals(FutureAction.Action.REMINDER) ?
                                            GuildUtils.getChannel(String.valueOf(action.getChannelId())).getAsMention()
                                            : "Direct Messages").append("\n\n");
                        }
                    }
                    PagedEmbedBuilder<String> pagedEmbedBuilder = new PagedEmbedBuilder<>(PaginationUtil
                            .splitStringToList(actionBuilder.toString(), PaginationUtil.SplitMethod.CHAR_COUNT, 1000));
                    pagedEmbedBuilder.setTitle("Reminders for " + MessageUtils.getTag(sender));
                    PaginationUtil.sendEmbedPagedMessage(pagedEmbedBuilder.build(), 0, channel, sender);
                } else if (args[0].equalsIgnoreCase("clear")) {
                    Set<FutureAction> futureActions = FlareBot.instance().getFutureActions();
                    for (FutureAction action : futureActions) {
                        if (action.getAction().equals(FutureAction.Action.REMINDER) || action.getAction().equals(FutureAction.Action.DM_REMINDER)) {
                            if (action.getResponsible() == sender.getIdLong()) {
                                action.delete();
                            }
                        }
                    }
                    MessageUtils.sendSuccessMessage("Cleared your reminders successfully", channel, sender);
                } else {
                    MessageUtils.sendUsage(this, channel, sender, args);
                }
            } else {
                MessageUtils.sendUsage(this, channel, sender, args);
            }
        } else {
            Period period;
            if ((period = GeneralUtils.getTimeFromInput(args[0], channel)) == null) return;
            String reminder;
            FutureAction.Action action;
            if (args[1].equalsIgnoreCase("dm")) {
                reminder = MessageUtils.getMessage(args, 2);
                action = FutureAction.Action.DM_REMINDER;
            } else {
                reminder = MessageUtils.getMessage(args, 1);
                action = FutureAction.Action.REMINDER;
            }
            channel.sendMessage("\uD83D\uDC4D I will remind you in " + FormatUtils.formatJodaTime(period).toLowerCase() + " (at "
                    + FormatUtils.formatPrecisely(period) + ") to `" + reminder + "`").queue();

            Scheduler.queueFutureAction(guild.getGuildIdLong(), channel.getIdLong(), sender.getIdLong(), reminder.substring(0,
                    Math.min(reminder.length(), 1000)), period, action);
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
        return "`{%}remind <duration> <reminder>` - Reminds a user about something after a duration.\n" +
                "`{%}remind <duration> dm <reminder>` - Reminds a user about something after a duration via Direct Messages.\n" +
                "`{%}remind list` - Lists your current reminders.\n" +
                "`{%}remind clear` - Clears your current reminders.";
    }

    @Override
    public Permission getPermission() {
        return Permission.REMIND_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.USEFUL;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"r", "reminder"};
    }

    @Override
    public boolean isBetaTesterCommand() {
        return true;
    }
}

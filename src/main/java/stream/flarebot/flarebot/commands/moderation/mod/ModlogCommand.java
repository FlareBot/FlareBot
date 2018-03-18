package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.Moderation;
import stream.flarebot.flarebot.mod.modlog.ModlogAction;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;
import stream.flarebot.flarebot.util.pagination.PagedEmbedBuilder;
import stream.flarebot.flarebot.util.pagination.PagedTableBuilder;
import stream.flarebot.flarebot.util.pagination.PaginationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModlogCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                int page = 1;
                if (args.length == 2) {
                    page = GeneralUtils.getInt(args[1], 1);
                }
                Set<ModlogAction> actions = guild.getModeration().getEnabledActions();
                List<ModlogAction> events = new ArrayList<>(actions);

                if (events.isEmpty()) {
                    MessageUtils.sendErrorMessage("No Events are enabled", channel);
                    return;
                }
                PagedTableBuilder tb = new PagedTableBuilder();
                tb.addColumn("Event");
                tb.addColumn("Compact");
                tb.addColumn("Channel");

                tb.setRowCount(10);

                for (ModlogAction modlogAction : events) {
                    if (guild.getModeration().isEventEnabled(guild, modlogAction.getEvent())) {
                        List<String> part = new ArrayList<>();
                        part.add(modlogAction.getEvent().getName());
                        part.add(String.valueOf(modlogAction.isCompacted()));
                        part.add(modlogAction.getModlogChannel(guild.getGuild()).getName());
                        tb.addRow(part);
                    }
                }
                PaginationUtil.sendPagedMessage(channel, tb.build(), page - 1, sender);
                return;
            }
            if (args[0].equalsIgnoreCase("features")) {
                int page = 1;
                if (args.length == 2) {
                    page = GeneralUtils.getInt(args[1], 1);
                }

                listEvents(channel, page, sender);
                return;
            }
        }
        if (args.length >= 2) {
            String eventArgument = MessageUtils.getMessage(args, 1,
                    args[0].equalsIgnoreCase("enable") ? Math.max(2, args.length - 1) : args.length);
            ModlogEvent event = ModlogEvent.getEvent(eventArgument);
            boolean all = false;
            boolean defaultEvents = false;
            if (event == null) {
                if (eventArgument.equalsIgnoreCase("all"))
                    all = true;
                else if (eventArgument.equalsIgnoreCase("default"))
                    defaultEvents = true;
                else {
                    MessageUtils.sendErrorMessage("Invalid Event: `" + eventArgument + "`\n"
                            + "For a list of all events do `{%}modlog features`, "
                            + "for a list of all enabled events do `{%}modlog list`.", channel);
                    return;
                }
            }
            Moderation moderation = guild.getModeration();
            if (args[0].equalsIgnoreCase("enable")) {
                TextChannel tc = GuildUtils.getChannel(args[args.length - 1], guild);
                if (tc == null) {
                    MessageUtils.sendErrorMessage("I cannot find the channel `" + args[args.length - 1] + "` try to mention the channel " +
                            "or use the channel ID", channel);
                    return;
                }
                long channelId = tc.getIdLong();

                if (!guild.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
                    MessageUtils.sendErrorMessage("I don't have permission to view audit logs so you can't use Modlog events!", channel);
                    return;
                }
                if (all) {
                    moderation.enableAllEvents(guild, channelId);
                    MessageUtils.sendSuccessMessage("Successfully enabled **all** events in " + tc.getAsMention(),
                            channel, sender);
                    return;
                } else if (defaultEvents) {
                    moderation.enableDefaultEvents(guild, channelId);
                    MessageUtils.sendSuccessMessage("Successfully enabled **default** events in " + tc.getAsMention(),
                            channel, sender);
                    return;
                } else {
                    if (moderation.enableEvent(guild, channelId, event)) {
                        MessageUtils.sendSuccessMessage("Successfully enabled event `" +
                                        WordUtils.capitalize(event.getTitle().toLowerCase().replaceAll("_", " "))
                                        + "`\nThis event will be displayed in the " + tc.getAsMention() + " channel.",
                                channel, sender);
                        return;
                    } else {
                        MessageUtils.sendErrorMessage("Error enabling event, I couldn't find that channel!", channel, sender);
                        return;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("disable")) {
                if (all) {
                    moderation.disableAllEvents();
                    MessageUtils.sendSuccessMessage("Successfully disabled **all** events", channel, sender);
                    return;
                } else if (defaultEvents) {
                    moderation.disableDefaultEvents();
                    MessageUtils.sendSuccessMessage("Successfully disabled all **default** events", channel, sender);
                    return;
                } else {
                    if (moderation.isEventEnabled(guild, event)) {
                        moderation.disableEvent(event);
                        MessageUtils.sendSuccessMessage("Successfully disabled event `" + WordUtils.capitalize(event
                                .getTitle()) + "`", channel, sender);
                        return;
                    } else {
                        MessageUtils.sendErrorMessage("Error disabling event (Probably already disabled)", channel, sender);
                        return;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("compact")) {
                if (all) {
                    int compact = 0;
                    int uncompact = 0;
                    for (ModlogEvent modlogEvent : ModlogEvent.values) {
                        if (moderation.isEventEnabled(guild, modlogEvent)) {
                            if (moderation.isEventCompacted(modlogEvent)) {
                                compact++;
                            } else {
                                uncompact++;
                            }
                        }
                    }
                    for (ModlogEvent modlogEvents : ModlogEvent.values) {
                        moderation.setEventCompact(modlogEvents, uncompact >= compact);
                    }
                    MessageUtils.sendSuccessMessage((uncompact >= compact ? "Compacted" : "Un-compacted") +
                            " all the modlog events", channel, sender);
                    return;
                } else if (defaultEvents) {
                    int compact = 0;
                    int uncompact = 0;
                    for (ModlogEvent modlogEvent : ModlogEvent.values) {
                        if (modlogEvent.isDefaultEvent() && moderation.isEventEnabled(guild, modlogEvent)) {
                            if (moderation.isEventCompacted(modlogEvent))
                                compact++;
                            else
                                uncompact++;
                        }
                    }
                    for (ModlogEvent modlogEvent : ModlogEvent.values) {
                        if (modlogEvent.isDefaultEvent())
                            moderation.setEventCompact(modlogEvent, compact >= uncompact);
                    }
                    MessageUtils.sendSuccessMessage((compact >= uncompact ? "Un-compacted" : "Compacted") +
                            " all the default modlog events", channel, sender);
                } else {
                    if (moderation.isEventEnabled(guild, event)) {
                        boolean compact = moderation.setEventCompact(event, !moderation.isEventCompacted(event));
                        if (compact) {
                            MessageUtils.sendSuccessMessage("Compacted event `" + WordUtils.capitalize(event.getTitle())
                                    + "`", channel, sender);
                            return;
                        } else {
                            MessageUtils.sendSuccessMessage("Un-compacted event `" + WordUtils.capitalize(event.getTitle())
                                    + "`", channel, sender);
                            return;
                        }
                    } else {
                        MessageUtils.sendErrorMessage("You can't compact an event that isn't enabled!", channel);
                        return;
                    }
                }
            }
        }
        MessageUtils.sendUsage(this, channel, member.getUser(), args);
    }

    @Override
    public String getCommand() {
        return "modlog";
    }

    @Override
    public String getDescription() {
        return "Used for setting modlog options.";
    }

    @Override
    public String getUsage() {
        return "`{%}modlog enable <feature> <channel>` - Enables or a modlog feature in a specific channel. (Only 1 channel per event)\n" +
                "`{%}modlog disable <feature>` - Disables a modlog feature.\n" +
                "`{%}modlog compact <feature>` - Toggles the compacting of modlog features (Compacted is plain text, no embed).\n" +
                "`{%}modlog list [page]` - List enabled events.\n" +
                "`{%}modlog features` - Lists all the modlog features you can enable.";
    }

    @Override
    public stream.flarebot.flarebot.permissions.Permission getPermission() {
        return stream.flarebot.flarebot.permissions.Permission.MODLOG_COMMAND;
    }

    @Override
    public String getExtraInfo() {
        return "Events can only be set to one channel at a time, if an event is already enabled and you enable it " +
                "again in a different channel it **will overwrite** the channel ID with the new one.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    private void listEvents(TextChannel channel, int page, User user) {
        StringBuilder sb = new StringBuilder();
        String groupKey = null;
        for (ModlogEvent modlogEvent : ModlogEvent.events) {
            String name = modlogEvent.getName();
            String[] split = name.split(" ");
            if (groupKey == null || groupKey.isEmpty())
                groupKey = split[0];

            if (!groupKey.equals(split[0])) {
                sb.append('\n');
                groupKey = split[0];
            }

            sb.append("`").append(modlogEvent.getTitle()).append("` - ").append(modlogEvent.getDescription()).append('\n');
        }
        sb.append("`Default` - Is for all the normal default events\n");
        sb.append("`All` - Is for targeting all events");
        PaginationUtil.sendEmbedPagedMessage(
                new PagedEmbedBuilder<>(PaginationUtil.splitStringToList(sb.toString(), PaginationUtil.SplitMethod.CHAR_COUNT, 1024))
                        .setTitle("Modlog Events")
                        .build(), page - 1, channel, user);

    }
}

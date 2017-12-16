package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
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
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModlogCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                int page = 1;
                if (args.length == 2) {
                    page = GeneralUtils.getInt(args[1], 1);
                }
                int pageSize = 15;
                Set<ModlogAction> actions = guild.getModeration().getEnabledActions();
                int pages = actions.size() < pageSize ? 1 : (actions.size() / pageSize)
                        + (actions.size() % pageSize != 0 ? 1 : 0);

                int start;
                int end;

                start = pageSize * (page - 1);
                end = Math.min(start + pageSize, actions.size());

                if (page > pages || page < 0) {
                    MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
                    return;
                } else {
                    List<ModlogAction> events = new ArrayList<>(actions).subList(start, end);

                    if (events.isEmpty()) {
                        MessageUtils.sendErrorMessage("No Events are enabled", channel);
                        return;
                    }
                    List<String> header = new ArrayList<>();
                    header.add("Event");
                    header.add("Compact");
                    header.add("Channel");

                    List<List<String>> body = new ArrayList<>();
                    for (ModlogAction modlogAction : events) {
                        if (guild.getModeration().isEventEnabled(guild, modlogAction.getEvent())) {
                            List<String> part = new ArrayList<>();
                            part.add(modlogAction.getEvent().getName());
                            part.add(String.valueOf(modlogAction.isCompacted()));
                            part.add(modlogAction.getModlogChannel(guild.getGuild()).getName());
                            body.add(part);
                        }
                    }

                    channel.sendMessage(MessageUtils.makeAsciiTable(header, body, " Page " + page + "/" + pages)).queue();
                    return;
                }
            }
            if (args[0].equalsIgnoreCase("features")) {
                int page = 1;
                if (args.length == 2) {
                    page = GeneralUtils.getInt(args[1], 1);
                }
<<<<<<< HEAD
                listEvents(channel, page, guild, false);
                return;
=======
                
                listEvents(channel, page, guild, false);
>>>>>>> 87715b7e7bbccf90dfbb6eaa6d8d025e084d0a23
            }
        }
        if (args.length >= 2) {
            String eventArgument = MessageUtils.getMessage(args, 1, Math.max(2, args.length - 1));
            ModlogEvent event = ModlogEvent.getEvent(eventArgument);
            boolean all = false;
            boolean defaultEvents = false;
            if (event == null) {
                if (eventArgument.equalsIgnoreCase("all"))
                    all = true;
                else if (eventArgument.equalsIgnoreCase("default"))
                    defaultEvents = true;
                else {
                    EmbedBuilder errorBuilder = new EmbedBuilder();
                    errorBuilder.setDescription("Invalid Event: `" + eventArgument + "`\n"
                                    + "For a list of all events do `{%}modlog features`, "
                                    + "for a list of all enabled events do `{%}modlog list`.");
                    MessageUtils.sendErrorMessage(errorBuilder, channel);
                    return;
                }
            }
            Moderation moderation = guild.getModeration();
            if (args[0].equalsIgnoreCase("enable")) {
                TextChannel tc = GeneralUtils.getChannel(args[args.length - 1], guild);
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
                        moderation.setEventCompact(modlogEvents, compact >= uncompact);
                    }
                    MessageUtils.sendSuccessMessage((compact >= uncompact ? "Un-compacted" : "compacted") +
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
                    MessageUtils.sendSuccessMessage((compact >= uncompact ? "Un-compacted" : "compacted") +
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
    public String getExtraInfo() {
        return "Events can only be set to one channel at a time, if an event is already enabled and you enable it " +
                "again in a different channel it **will overwrite** the channel ID with the new one.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
    
    private void listEvents(TextChannel channel, int page, GuildWrapper wrapper, boolean enabledEvents) {
        int pageSize = 15;
        List<ModlogEvent> events;
        if (enabledEvents)
            events = wrapper.getModeration().getEnabledActions().stream().map(action -> action.getEvent()).collect(Collectors.toList());
        else
            events = ModlogEvent.events;
        int pages = events.size() < pageSize ? 1 : (events.size() / pageSize)
                + (events.size() % pageSize != 0 ? 1 : 0);

        int start;
        int end;

        start = pageSize * (page - 1);
        end = Math.min(start + pageSize, events.size());

        if (page > pages || page < 0) {
            MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
            return;
        } else {
            StringBuilder sb = new StringBuilder();
            Map<String, List<ModlogEvent>> groups = new HashMap<>();
            String groupKey = null;
            for (ModlogEvent modlogEvent : ModlogEvent.events.subList(start, end)) {
                String name = modlogEvent.getName();
                String[] split = name.split(" ");
                if (groupKey == null || groupKey.isEmpty())
                     groupKey = split[0];
                        
                if (!groupKey.equals(split[0]))
                     sb.append('\n');
                        
                sb.append("`").append(modlogEvent.getTitle()).append("` - ").append(modlogEvent.getDescription()).append('\n');
            }
            if (!enabledEvents) {
                sb.append("`Default` - Is for all the normal default events\n");
                sb.append("`All` - Is for targeting all events");
            }
            channel.sendMessage(new EmbedBuilder().setTitle("Features").setDescription(sb.toString())
                    .setFooter("Page " + page + "/" + pages, null).build()).queue();
            return;
        }
    }
}

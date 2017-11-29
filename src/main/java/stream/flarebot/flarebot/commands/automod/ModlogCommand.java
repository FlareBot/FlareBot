package stream.flarebot.flarebot.commands.automod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.ModlogEvent;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
                int pages =
                        guild.getEnabledEvents().size() < pageSize ? 1 : (guild.getEnabledEvents().size() / pageSize) + (guild.getEnabledEvents().size() % pageSize != 0 ? 1 : 0);

                int start;
                int end;

                start = pageSize * (page - 1);
                end = Math.min(start + pageSize, guild.getEnabledEvents().size());

                if (page > pages || page < 0) {
                    MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
                    return;
                } else {
                    List<ModlogEvent> events = new ArrayList<>(guild.getEnabledEvents()).subList(start, end);

                    if (events.isEmpty()) {
                        MessageUtils.sendErrorMessage("No Events are enabled", channel);
                        return;
                    }
                    List<String> header = new ArrayList<>();
                    header.add("Event");
                    header.add("Compacted");

                    List<List<String>> body = new ArrayList<>();
                    for (ModlogEvent modlogEvent : events) {
                        if (guild.isEventEnabled(modlogEvent)) {
                            List<String> part = new ArrayList<>();
                            part.add(modlogEvent.toString());
                            part.add(String.valueOf(guild.isEventCompact(modlogEvent)));
                            body.add(part);
                        }
                    }

                    channel.sendMessage(MessageUtils.makeAsciiTable(header, body, " Page " + page + "/" + pages)).queue();
                    return;
                }
            }
            if (args[0].equalsIgnoreCase("features")) {
                StringBuilder sb = new StringBuilder();
                Map<String, List<ModlogEvent>> groups = new HashMap<>();
                for (ModlogEvent modlogEvent : ModlogEvent.values()) {
                    String name = modlogEvent.getTitle();
                    String[] split = name.split("_");
                    String groupKey = split[0];
                    if (groups.containsKey(groupKey)) {
                        List<ModlogEvent> group = groups.get(groupKey);
                        group.add(modlogEvent);
                        groups.put(groupKey, group);
                    } else {
                        List<ModlogEvent> group = new ArrayList<>();
                        group.add(modlogEvent);
                        groups.put(groupKey, group);
                    }
                }

                Iterator<Map.Entry<String, List<ModlogEvent>>> it = groups.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<ModlogEvent>> pair = it.next();
                    for (ModlogEvent event : pair.getValue()) {
                        sb.append("`").append(event.getTitle()).append("` - ").append(event.getDescription()).append("\n");
                    }
                    sb.append("\n");
                    it.remove();
                }
                sb.append("`all` - Is for targeting all events");
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Features");
                eb.setDescription(sb.toString());
                channel.sendMessage(eb.build()).queue();
                return;
            }
        }
        if (args.length >= 2) {
            ModlogEvent event = null;
            boolean all = false;
            try {
                event = ModlogEvent.valueOf(MessageUtils.getMessage(args, 1).toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                if (args[1].equalsIgnoreCase("all")) {
                    all = true;
                } else {
                    EmbedBuilder errorBuilder = new EmbedBuilder();
                    errorBuilder.setDescription("Invalid Event: `" + MessageUtils.getMessage(args, 1) + "`");
                    errorBuilder.addField("Events", "`" + Arrays.stream(ModlogEvent.values()).map(ModlogEvent::toString).collect(Collectors.joining("`\n`")) + "`", false);
                    MessageUtils.sendErrorMessage(errorBuilder, channel);
                    return;
                }
            }
            if (args[0].equalsIgnoreCase("enable")) {
                if (!guild.getGuild().getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
                    MessageUtils.sendErrorMessage("I don't have permission to view audit logs so you can't use Modlog events!", channel);
                    return;
                }
                if (all) {
                    for (ModlogEvent modlogEvent : ModlogEvent.values()) {
                        guild.enableEvent(modlogEvent);
                    }
                    MessageUtils.sendSuccessMessage("Successfully enabled all events", channel, sender);
                    return;
                } else {
                    if (!guild.isEventEnabled(event)) {
                        guild.enableEvent(event);
                        MessageUtils.sendSuccessMessage("Successfully enabled event `" + WordUtils.capitalize(event.getTitle().toLowerCase().replaceAll("_", " ")) + "`", channel, sender);
                        return;
                    } else {
                        MessageUtils.sendErrorMessage("Error enabling event (Probably already disabled)", channel, sender);
                        return;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("disable")) {
                if (all) {
                    for (ModlogEvent modlogEvent : ModlogEvent.values()) {
                        guild.disableEvent(modlogEvent);
                    }
                    MessageUtils.sendSuccessMessage("Successfully disabled all events", channel, sender);
                } else {
                    if (guild.isEventEnabled(event)) {
                        guild.disableEvent(event);
                        MessageUtils.sendSuccessMessage("Successfully disabled event `" + WordUtils.capitalize(event.getTitle().toLowerCase().replaceAll("_", " ")) + "`", channel, sender);
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
                    for (ModlogEvent modlogEvent : ModlogEvent.values()) {
                        if (guild.isEventEnabled(modlogEvent)) {
                            if (guild.isEventCompact(modlogEvent)) {
                                compact++;
                            } else {
                                uncompact++;
                            }
                            if (compact >= uncompact) {
                                for (ModlogEvent modlogEvents : ModlogEvent.values()) {
                                    guild.setCompact(modlogEvents, false);
                                }
                                MessageUtils.sendSuccessMessage("Un-compacted all the modlog events", channel, sender);
                                return;
                            } else {
                                for (ModlogEvent modlogEvents : ModlogEvent.values()) {
                                    guild.setCompact(modlogEvents, true);
                                }
                                MessageUtils.sendSuccessMessage("Compacted all the modlog events", channel, sender);
                                return;
                            }
                        }
                    }
                } else {
                    if (guild.isEventEnabled(event)) {
                        boolean compact = guild.toggleCompactEvent(event);
                        if (compact) {
                            MessageUtils.sendSuccessMessage("Compacted event `" + WordUtils.capitalize(event.getTitle().toLowerCase().replaceAll("_", " ")) + "`", channel, sender);
                            return;
                        } else {
                            MessageUtils.sendSuccessMessage("Un-compacted event `" + WordUtils.capitalize(event.getTitle().toLowerCase().replaceAll("_", " ")) + "`", channel, sender);
                            return;
                        }
                    } else {
                        MessageUtils.sendErrorMessage("You can't compact an event that isn't enabled!", channel);
                        return;
                    }
                }
            }
            if (args[0].equalsIgnoreCase("setchannel")) {
                if (all) {
                    guild.getAutoModConfig().setModLogChannel(channel.getId());
                    MessageUtils.sendSuccessMessage("Set the modlog channel for ALL to " + channel.getAsMention(), channel, sender);
                    return;
                } else {
                    //Walshy
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
        return "`{%}modlog enable|disable <feature>` - Enables or disables a modlog feature.\n" +
                "`{%}modlog compact <feature>` - Toggles the compacting of modlog features (Compacted is plain text).\n" +
                "`{%}modlog setchannel <feature>` - Sets the modlog channel.\n" +
                "`{%}modlog list [page]` - List enabled events.\n" +
                "`{%}modlog features` - Lists all the modlog features you can enable.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

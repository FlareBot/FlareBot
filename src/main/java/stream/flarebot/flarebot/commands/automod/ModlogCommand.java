package stream.flarebot.flarebot.commands.automod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.ModlogEvent;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ModlogCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                int page = 1;
                if (args.length == 2) {
                    try {
                        page = Integer.valueOf(args[1]);
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage("Invalid page number: " + args[1] + ".", channel);
                        return;
                    }
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
                    List<ModlogEvent> events = guild.getEnabledEvents().subList(start, end);

                    if (events.isEmpty()) {
                        MessageUtils.sendErrorMessage("No Events are enabled", channel);
                        return;
                    }
                    List<String> header = new ArrayList<>();
                    header.add("Event");
                    header.add("Compacted");

                    List<List<String>> body = new ArrayList<>();
                    for (ModlogEvent modlogEvent : events) {
                        List<String> part = new ArrayList<>();
                        part.add(modlogEvent.toString());
                        part.add(String.valueOf(modlogEvent.isCompact()));
                        body.add(part);
                    }

                    channel.sendMessage(MessageUtils.makeAsciiTable(header, body, " Page " + page + "/" + pages)).queue();
                    return;
                }
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
                    errorBuilder.setDescription("Invalid Event: `" + args[1] + "`");
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
                    if (guild.enableEvent(event)) {
                        MessageUtils.sendSuccessMessage("Successfully enabled event " + event.toString(), channel, sender);
                        return;
                    } else {
                        MessageUtils.sendErrorMessage("Error enabling event (Probably already enabled)", channel, sender);
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
                    if (guild.disableEvent(event)) {
                        MessageUtils.sendSuccessMessage("Successfully disabled event " + event.toString(), channel, sender);
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
                        if (modlogEvent.isCompact()) {
                            compact++;
                        } else {
                            uncompact++;
                        }
                    }
                    if (compact >= uncompact) {
                        for (ModlogEvent modlogEvent : ModlogEvent.values()) {
                            modlogEvent.setCompact(false);
                        }
                        MessageUtils.sendSuccessMessage("Un-compacted all the modlog events", channel, sender);
                        return;
                    } else {
                        for (ModlogEvent modlogEvent : ModlogEvent.values()) {
                            modlogEvent.setCompact(true);
                        }
                        MessageUtils.sendSuccessMessage("Compacted all the modlog events", channel, sender);
                        return;
                    }
                } else {
                    boolean compact = guild.toogleCompactEvent(event);
                    if (compact) {
                        MessageUtils.sendSuccessMessage("Compacted event `" + event.toString() + "`", channel, sender);
                        return;
                    } else {
                        MessageUtils.sendSuccessMessage("Un-compacted event `" + event.toString() + "`", channel, sender);
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
                "`{%}modlog list [page]` - List enabled events";
    }

    @Override
    public String getExtraInfo() {
        return "**Features:**\n" +
                "`MEMBER_JOIN` - Triggers when user joins the server.\n" +
                "`MEMBER_LEAVE` - Triggers when user leaves the server.\n\n" +
                "`MEMBER_ROLE_GIVE` - Triggers when a role is added to a user.\n" +
                "`MEMBER_ROLE_REMOVE` - Triggers when a role is taken away a user.\n\n" +
                "`MEMBER_VOICE_JOIN` - Triggers when a user joins a voice channel.\n" +
                "`MEMBER_VOICE_LEAVE` - Triggers when a user leaves a voice channel.\n\n" +
                "`ROLE_CREATE` - Triggers when a role is created.\n" +
                "`ROLE_DELETE` - Triggers when a role is deleted.\n" +
                "`ROLE_EDIT` - Triggers when a role is edited.\n" +
                "`ROLE_MOVE` - Triggers when a role is moved on the higharchy.\n\n" +
                "`CHANNEL_CREATE` - Triggers when a channel is created.\n" +
                "`CHANNEL_DELETE` - Triggers when a channel is deleted.\n\n" +
                "`COMMAND` - Triggers when a user runs a **FlareBot** command.\n\n" +
                "`MESSAGE_DELETE` - Triggers when a message is deleted.\n\n" +
                "`all` - This is for targeting everything.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

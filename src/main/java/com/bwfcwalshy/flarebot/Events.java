package com.bwfcwalshy.flarebot;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.util.Welcome;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.*;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class Events {

    private FlareBot flareBot;

    public Events(FlareBot bot) {
        this.flareBot = bot;
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) {
        flareBot.run();
    }

    @EventSubscriber
    public void onJoin(UserJoinEvent e) {
        if (flareBot.getWelcomeForGuild(e.getGuild()) != null) {
            Welcome welcome = flareBot.getWelcomeForGuild(e.getGuild());
            IChannel channel = flareBot.getClient().getChannelByID(welcome.getChannelId());
            if (channel != null) {
                String msg = welcome.getMessage().replace("%user%", e.getUser().getName()).replace("%guild%", e.getGuild().getName()).replace("%mention%", e.getUser().mention());
                MessageUtils.sendMessage(channel, msg);
            } else flareBot.getWelcomes().remove(welcome);
        }
        if (flareBot.getAutoAssignRoles().containsKey(e.getGuild().getID())) {
            List<String> autoAssignRoles = flareBot.getAutoAssignRoles().get(e.getGuild().getID());
            for (String s : autoAssignRoles) {
                IRole role = e.getGuild().getRoleByID(s);
                if (role != null) {
                    RequestBuffer.request(() -> {
                        try {
                            e.getUser().addRole(role);
                        } catch (DiscordException e1) {
                            FlareBot.LOGGER.error("Could not auto-assign a role!", e1);
                        } catch (MissingPermissionsException e1) {
                            if (!e1.getErrorMessage().startsWith("Edited roles")) {
                                MessageUtils.sendPM(e.getGuild().getOwner(), "**Could not auto assign a role!**\n" + e1.getErrorMessage());
                                return;
                            }
                            StringBuilder message = new StringBuilder();

                            message.append("**Hello!\nI am here to tell you that I could not give the role ``");
                            message.append(role.getName()).append("`` to one of your new users!\n");
                            message.append("Please move one of the following roles above ``").append(role.getName())
                                    .append("`` in your server's role tab!\n```");
                            for (IRole i : FlareBot.getInstance().getClient().getOurUser().getRolesForGuild(role.getGuild())) {
                                message.append(i.getName()).append('\n');
                            }
                            message.append("\n```\nSo the role can be given.**");

                            MessageUtils.sendPM(e.getGuild().getOwner(), message);
                        }
                    });
                } else autoAssignRoles.remove(s);
            }
        }
    }

    @EventSubscriber
    public void onGuildCreate(GuildCreateEvent e) {
        if (e.getClient().isReady())
            MessageUtils.sendMessage(new EmbedBuilder()
                    .withColor(new Color(96, 230, 144))
                    .withThumbnail(e.getGuild().getIconURL())
                    .withFooterIcon(e.getGuild().getIconURL())
                    .withFooterText(e.getGuild().getCreationDate().format(DateTimeFormatter.RFC_1123_DATE_TIME))
                    .withAuthorName(e.getGuild().getName())
                    .withAuthorIcon(e.getGuild().getIconURL())
                    .withDesc("Guild Created: `" + e.getGuild().getName() + "` :smile: :heart:")
                    .build(), FlareBot.getInstance().getGuildLogChannel());
    }

    @EventSubscriber
    public void onGuildDelete(GuildLeaveEvent e) {
        MessageUtils.sendMessage(new EmbedBuilder()
                .withColor(new Color(244, 23, 23))
                .withThumbnail(e.getGuild().getIconURL())
                .withFooterIcon(e.getGuild().getIconURL())
                .withFooterText(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME))
                .withAuthorName(e.getGuild().getName())
                .withAuthorIcon(e.getGuild().getIconURL())
                .withDesc("Guild Deleted: `" + e.getGuild().getName() + "` L :broken_heart:")
                .build(), FlareBot.getInstance().getGuildLogChannel());
    }

    @EventSubscriber
    public void onVoice(UserVoiceChannelLeaveEvent e){
        if(e.getChannel().getConnectedUsers().size() < 2){
            FlareBot.getInstance().getMusicManager().getPlayer(e.getChannel().getGuild().getID()).setPaused(true);
            e.getChannel().leave();
        }
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getContent() != null
                && e.getMessage().getContent().startsWith(String.valueOf(FlareBot.COMMAND_CHAR))
                && !e.getMessage().getAuthor().isBot()) {
            EnumSet<Permissions> perms = e.getMessage().getChannel()
                    .getModifiedPermissions(FlareBot.getInstance().getClient().getOurUser());
            if (!perms.contains(Permissions.ADMINISTRATOR)) {
                if (!perms.contains(Permissions.SEND_MESSAGES)) {
                    return;
                }
                if (!perms.contains(Permissions.EMBED_LINKS)) {
                    MessageUtils.sendMessage(e.getMessage().getChannel(), "Hey! I can't be used here." +
                            "\nI do not have the `Embed Links` permission! Please go to your permissions adn give me Embed Links." +
                            "\nThanks :D");
                    return;
                }
            }
            String message = e.getMessage().getContent();
            String command = message.substring(1);
            String[] args = new String[0];
            if (message.contains(" ")) {
                command = command.substring(0, message.indexOf(" ") - 1);

                args = message.substring(message.indexOf(" ") + 1).split(" ");
            }
            for (Command cmd : flareBot.getCommands()) {
                if (cmd.getCommand().equalsIgnoreCase(command)) {
                    FlareBot.LOGGER.info(
                            "Dispatching command '" + cmd.getCommand() + "' " + Arrays.toString(args) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                    e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator());
                    if (cmd.getType() == CommandType.MUSIC) {
                        if (e.getMessage().getChannel().isPrivate()) {
                            MessageUtils.sendMessage(e.getMessage().getChannel(), "**Music commands cannot be used in DM's!**");
                            return;
                        }
                    }
                    if (cmd.getPermission() != null && cmd.getPermission().length() > 0) {
                        if (!cmd.getPermissions(e.getMessage().getChannel()).hasPermission(e.getMessage().getAuthor(), cmd.getPermission())) {
                            MessageUtils.sendMessage(e.getMessage().getChannel(), "You are missing the permission ``"
                                    + cmd.getPermission() + "`` which is required for use of this command!");
                            return;
                        }
                    }
                    try {
                        cmd.onCommand(e.getMessage().getAuthor(), e.getMessage().getChannel(), e.getMessage(), args);
                    } catch (Exception ex) {
                        MessageUtils.sendException("**There was an internal error trying to execute your command**", ex, e.getMessage().getChannel());
                        FlareBot.LOGGER.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                                + Arrays.toString(args) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator(), ex);
                    }
                    return;
                } else {
                    for (String alias : cmd.getAliases()) {
                        if (alias.equalsIgnoreCase(command)) {
                            FlareBot.LOGGER.info(
                                    "Dispatching command '" + cmd.getCommand() + "' " + Arrays.toString(args) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                            e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator());
                            if (cmd.getType() == CommandType.MUSIC) {
                                if (e.getMessage().getChannel().isPrivate()) {
                                    MessageUtils.sendMessage(e.getMessage().getChannel(), "**Music commands cannot be used in DM's!**");
                                    return;
                                }
                            }
                            if (cmd.getPermission() != null && cmd.getPermission().length() > 0) {
                                if (!cmd.getPermissions(e.getMessage().getChannel()).hasPermission(e.getMessage().getAuthor(), cmd.getPermission())) {
                                    MessageUtils.sendMessage(e.getMessage().getChannel(), "You are missing the permission ``"
                                            + cmd.getPermission() + "`` which is required for use of this command!");
                                    return;
                                }
                            }
                            try {
                                cmd.onCommand(e.getMessage().getAuthor(), e.getMessage().getChannel(), e.getMessage(), args);
                            } catch (Exception ex) {
                                FlareBot.LOGGER.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                                        + Arrays.toString(args) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                        e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator(), ex);
                                MessageUtils.sendException("**There was an internal error trying to execute your command**", ex, e.getMessage().getChannel());
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}

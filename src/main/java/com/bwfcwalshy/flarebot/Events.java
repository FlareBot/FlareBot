package com.bwfcwalshy.flarebot;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.util.Welcome;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.Arrays;
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
            }
        }
        if (flareBot.getAutoAssignRoles().containsKey(e.getGuild().getID())) {
            List<String> autoAssignRoles = flareBot.getAutoAssignRoles().get(e.getGuild().getID());
            for (String s : autoAssignRoles) {
                IRole role = e.getGuild().getRoleByID(s);
                if (role != null) {
                    RequestBuffer.request(() -> {
                        try {
                            e.getUser().addRole(role);
                        } catch (MissingPermissionsException | DiscordException e1) {
                            FlareBot.LOGGER.error("Could not auto-assign a role!", e1);
                        }
                    });
                } else autoAssignRoles.remove(s);
            }
        }
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getContent().startsWith(String.valueOf(FlareBot.COMMAND_CHAR)) && !e.getMessage().getAuthor().isBot()) {
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
                    }
                    return;
                } else {
                    for (String alias : cmd.getAliases()) {
                        if (alias.equalsIgnoreCase(command)) {
                            FlareBot.LOGGER.info(
                                    "Dispatching command '" + cmd.getCommand() + "' " + Arrays.toString(args) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                            e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator());
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
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}

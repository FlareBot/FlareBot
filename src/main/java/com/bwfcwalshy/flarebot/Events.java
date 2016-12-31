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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Events {

    private FlareBot flareBot;
    private AtomicBoolean bool = new AtomicBoolean(false);
    private static final ThreadGroup COMMAND_THREADS = new ThreadGroup("Command Threads");
    private static final ExecutorService CACHED_POOL = Executors.newCachedThreadPool(r ->
            new Thread(COMMAND_THREADS, r, "Command Pool-" + COMMAND_THREADS.activeCount()));

    public static final Map<String, AtomicInteger> COMMAND_COUNTER = new ConcurrentHashMap<>();

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
        if (bool.get())
            MessageUtils.sendMessage(new EmbedBuilder()
                    .withColor(new Color(96, 230, 144))
                    .withThumbnail(e.getGuild().getIconURL())
                    .withFooterIcon(e.getGuild().getIconURL())
                    .withFooterText(OffsetDateTime.now()
                            .format(DateTimeFormatter.RFC_1123_DATE_TIME) + " | " + e.getGuild().getID())
                    .withAuthorName(e.getGuild().getName())
                    .withAuthorIcon(e.getGuild().getIconURL())
                    .withDesc("Guild Created: `" + e.getGuild().getName() + "` :smile: :heart:\nGuild Owner: " + e.getGuild().getOwner().getName())
                    .build(), FlareBot.getInstance().getGuildLogChannel());
    }

    @EventSubscriber
    public void onGuildDelete(GuildLeaveEvent e) {
        COMMAND_COUNTER.remove(e.getGuild().getID());
        MessageUtils.sendMessage(new EmbedBuilder()
                .withColor(new Color(244, 23, 23))
                .withThumbnail(e.getGuild().getIconURL())
                .withFooterIcon(e.getGuild().getIconURL())
                .withFooterText(OffsetDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME) + " | " + e.getGuild().getID())
                .withAuthorName(e.getGuild().getName())
                .withAuthorIcon(e.getGuild().getIconURL())
                .withDesc("Guild Deleted: `" + e.getGuild().getName() + "` L :broken_heart:\nGuild Owner: " + (e.getGuild().getOwner() != null ? e.getGuild().getOwner().getName() : "Non-existent, they had to much L"))
                .build(), FlareBot.getInstance().getGuildLogChannel());
    }

    @EventSubscriber
    public void onVoice(UserVoiceChannelLeaveEvent e) {
        if (e.getChannel().getConnectedUsers().contains(e.getClient().getOurUser())
                && e.getChannel().getConnectedUsers().size() < 2) {
            FlareBot.getInstance().getMusicManager().getPlayer(e.getChannel().getGuild().getID()).setPaused(true);
            e.getChannel().leave();
        }
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        if (e.getMessage().getContent() != null
                && e.getMessage().getContent().startsWith(String.valueOf(FlareBot.getPrefixes().get(getGuildId(e))))
                && !e.getMessage().getAuthor().isBot()) {
            bool.set(true);
            EnumSet<Permissions> perms = e.getMessage().getChannel()
                    .getModifiedPermissions(FlareBot.getInstance().getClient().getOurUser());
            if (!perms.contains(Permissions.ADMINISTRATOR)) {
                if (!perms.contains(Permissions.SEND_MESSAGES)) {
                    return;
                }
                if (!perms.contains(Permissions.EMBED_LINKS)) {
                    MessageUtils.sendMessage(e.getMessage().getChannel(), "Hey! I can't be used here." +
                            "\nI do not have the `Embed Links` permission! Please go to your permissions and give me Embed Links." +
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
                    if(cmd.getType() == CommandType.HIDDEN){
                        if (!cmd.getPermissions(e.getMessage().getChannel()).isCreator(e.getMessage().getAuthor())) {
                            return;
                        }
                    }
                    if (!cmd.getType().usableInDMs()) {
                        if (e.getMessage().getChannel().isPrivate()) {
                            MessageUtils.sendMessage(e.getMessage().getChannel(), String.format("**%s commands cannot be used in DM's!**", cmd.getType().formattedName()));
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
                        if (!e.getMessage().getChannel().isPrivate())
                            COMMAND_COUNTER.computeIfAbsent(e.getMessage().getChannel().getGuild().getID(),
                                    g -> new AtomicInteger()).incrementAndGet();
                        String[] finalArgs = args;
                        CACHED_POOL.submit(() ->{
                            cmd.onCommand(e.getMessage().getAuthor(), e.getMessage().getChannel(), e.getMessage(), finalArgs);
                            FlareBot.LOGGER.info(
                                    "Dispatching command '" + cmd.getCommand() + "' " + Arrays.toString(finalArgs) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                            e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator());
                            delete(e);
                        });
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
                            if(cmd.getType() == CommandType.HIDDEN){
                                if (!cmd.getPermissions(e.getMessage().getChannel()).isCreator(e.getMessage().getAuthor())) {
                                    return;
                                }
                            }
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
                                if (!e.getMessage().getChannel().isPrivate())
                                    COMMAND_COUNTER.computeIfAbsent(e.getMessage().getChannel().getGuild().getID(),
                                            g -> new AtomicInteger()).incrementAndGet();
                                String[] finalArgs = args;
                                CACHED_POOL.submit(() ->{
                                    cmd.onCommand(e.getMessage().getAuthor(), e.getMessage().getChannel(), e.getMessage(), finalArgs);
                                    FlareBot.LOGGER.info(
                                            "Dispatching command '" + cmd.getCommand() + "' " + Arrays.toString(finalArgs) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                                    e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator());
                                    delete(e);
                                });
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

    private void delete(MessageReceivedEvent e) {
        RequestBuffer.request(() -> {
            try {
                e.getMessage().delete();
            } catch (DiscordException | MissingPermissionsException ignored) {
            }
        });
    }

    private String getGuildId(MessageReceivedEvent e) {
        return e.getMessage().getChannel().getGuild() != null ? e.getMessage().getChannel().getGuild().getID() : null;
    }
}

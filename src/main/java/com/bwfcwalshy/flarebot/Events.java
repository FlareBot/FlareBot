package com.bwfcwalshy.flarebot;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.commands.secret.UpdateCommand;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import com.bwfcwalshy.flarebot.util.Welcome;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.GuildLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.VoiceDisconnectedEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
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
                MessageUtils.sendMessage(msg, channel);
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
        if (e.getUser().equals(e.getClient().getOurUser())) {
            if (flareBot.getActiveVoiceChannels() == 0 && UpdateCommand.NOVOICE_UPDATING.get()) {
                MessageUtils.sendMessage("I am now updating, there are no voice channels active!", flareBot.getClient().getChannelByID("229704246004547585"));
                UpdateCommand.update(true, null);
            }
            return;
        }
        if (e.getVoiceChannel().getConnectedUsers().contains(e.getClient().getOurUser())
                && e.getVoiceChannel().getConnectedUsers().size() < 2) {
            e.getVoiceChannel().leave();
        }
    }

    @EventSubscriber
    public void onChannelJoin(UserVoiceChannelJoinEvent event) {
        if (event.getUser().equals(event.getClient().getOurUser())) {
            if (FlareBot.getInstance().getMusicManager().hasPlayer(event.getGuild().getID())) {
                FlareBot.getInstance().getMusicManager().getPlayer(event.getGuild().getID()).setPaused(false);
            }
        }
    }

    public void onChannelLeave(VoiceDisconnectedEvent event) {
        FlareBot.getInstance().getMusicManager().getPlayer(event.getGuild().getID()).setPaused(true);
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent e) {
        bool.set(true);
        if (e.getMessage().getContent() != null
                && e.getMessage().getContent().startsWith(String.valueOf(FlareBot.getPrefixes().get(getGuildId(e))))
                && !e.getMessage().getAuthor().isBot()) {
            EnumSet<Permissions> perms = e.getMessage().getChannel()
                    .getModifiedPermissions(FlareBot.getInstance().getClient().getOurUser());
            if (!perms.contains(Permissions.ADMINISTRATOR)) {
                if (!perms.contains(Permissions.SEND_MESSAGES)) {
                    return;
                }
                if (!perms.contains(Permissions.EMBED_LINKS)) {
                    MessageUtils.sendMessage("Hey! I can't be used here." +
                            "\nI do not have the `Embed Links` permission! Please go to your permissions and give me Embed Links." +
                            "\nThanks :D", e.getMessage().getChannel());
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
                    if (cmd.getType() == CommandType.HIDDEN) {
                        if (!cmd.getPermissions(e.getMessage().getChannel()).isCreator(e.getMessage().getAuthor())) {
                            return;
                        }
                    }
                    if (UpdateCommand.UPDATING.get()) {
                        MessageUtils.sendMessage("**Currently updating!**", e.getMessage().getChannel());
                        return;
                    }
                    if (!cmd.getType().usableInDMs()) {
                        if (e.getMessage().getChannel().isPrivate()) {
                            MessageUtils.sendMessage(String.format("**%s commands cannot be used in DM's!**", cmd.getType().formattedName()), e.getMessage().getChannel());
                            return;
                        }
                    }
                    if (handleMissingPermission(cmd, e))
                        return;
                    if (!e.getMessage().getChannel().isPrivate())
                        COMMAND_COUNTER.computeIfAbsent(e.getMessage().getChannel().getGuild().getID(),
                                g -> new AtomicInteger()).incrementAndGet();
                    String[] finalArgs = args;
                    CACHED_POOL.submit(() -> {
                        FlareBot.LOGGER.info(
                                "Dispatching command '" + cmd.getCommand() + "' " + Arrays.toString(finalArgs) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                        e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator());
                        try {
                            cmd.onCommand(e.getMessage().getAuthor(), e.getMessage().getChannel(), e.getMessage(), finalArgs);
                        } catch (Exception ex) {
                            MessageUtils.sendException("**There was an internal error trying to execute your command**", ex, e.getMessage().getChannel());
                            FlareBot.LOGGER.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                                    + Arrays.toString(finalArgs) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                    e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator(), ex);
                        }
                        delete(e.getMessage());
                    });
                    return;
                } else {
                    for (String alias : cmd.getAliases()) {
                        if (alias.equalsIgnoreCase(command)) {
                            if (cmd.getType() == CommandType.HIDDEN) {
                                if (!cmd.getPermissions(e.getMessage().getChannel()).isCreator(e.getMessage().getAuthor())) {
                                    return;
                                }
                            }
                            if (UpdateCommand.UPDATING.get()) {
                                MessageUtils.sendMessage("**Currently updating!**", e.getMessage().getChannel());
                                return;
                            }
                            FlareBot.LOGGER.info(
                                    "Dispatching command '" + cmd.getCommand() + "' " + Arrays.toString(args) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                            e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator());
                            if (cmd.getType() == CommandType.MUSIC) {
                                if (e.getMessage().getChannel().isPrivate()) {
                                    MessageUtils.sendMessage("**Music commands cannot be used in DM's!**", e.getMessage().getChannel());
                                    return;
                                }
                            }
                            if (handleMissingPermission(cmd, e))
                                return;
                            if (!e.getMessage().getChannel().isPrivate())
                                COMMAND_COUNTER.computeIfAbsent(e.getMessage().getChannel().getGuild().getID(),
                                        g -> new AtomicInteger()).incrementAndGet();
                            String[] finalArgs = args;
                            CACHED_POOL.submit(() -> {
                                FlareBot.LOGGER.info(
                                        "Dispatching command '" + cmd.getCommand() + "' " + Arrays.toString(finalArgs) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                                e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator());
                                try {
                                    cmd.onCommand(e.getMessage().getAuthor(), e.getMessage().getChannel(), e.getMessage(), finalArgs);
                                } catch (Exception ex) {
                                    FlareBot.LOGGER.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                                            + Arrays.toString(finalArgs) + " in " + e.getMessage().getChannel() + "! Sender: " +
                                            e.getMessage().getAuthor().getName() + '#' + e.getMessage().getAuthor().getDiscriminator(), ex);
                                    MessageUtils.sendException("**There was an internal error trying to execute your command**", ex, e.getMessage().getChannel());
                                }
                                delete(e.getMessage());
                            });
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean handleMissingPermission(Command cmd, MessageReceivedEvent e) {
        if (cmd.getPermission() != null && cmd.getPermission().length() > 0) {
            if (!cmd.getPermissions(e.getMessage().getChannel()).hasPermission(e.getMessage().getAuthor(), cmd.getPermission())) {
                IMessage msg = MessageUtils.sendErrorMessage(MessageUtils.getEmbed(e.getMessage().getAuthor())
                        .withDesc("You are missing the permission ``"
                                + cmd.getPermission() + "`` which is required for use of this command!"), e.getMessage().getChannel());
                delete(e.getMessage());
                new FlarebotTask("Delete message " + msg.getChannel().toString() + msg.getID()) {
                    @Override
                    public void run() {
                        delete(msg);
                    }
                }.delay(5000);
                return true;
            }
        }
        return false;
    }

    private void delete(IMessage message) {
        RequestBuffer.request(() -> {
            try {
                message.delete();
            } catch (DiscordException | MissingPermissionsException ignored) {
            }
        });
    }

    private String getGuildId(MessageReceivedEvent e) {
        return e.getMessage().getChannel().getGuild() != null ? e.getMessage().getChannel().getGuild().getID() : null;
    }
}

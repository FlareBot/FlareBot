package stream.flarebot.flarebot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.secret.UpdateCommand;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.objects.Welcome;
import stream.flarebot.flarebot.scheduler.FlarebotTask;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.WebUtils;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Events extends ListenerAdapter {

    private volatile boolean sd = false;
    private FlareBot flareBot;
    protected static Map<String, Integer> spamMap = new ConcurrentHashMap<>();
    private static final ThreadGroup COMMAND_THREADS = new ThreadGroup("Command Threads");
    private static final ExecutorService CACHED_POOL = Executors.newCachedThreadPool(r ->
            new Thread(COMMAND_THREADS, r, "Command Pool-" + COMMAND_THREADS.activeCount()));

    public Events(FlareBot bot) {
        this.flareBot = bot;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> sd = true));
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if(!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_READ)) return;
        if (!event.getGuild().getId().equals(FlareBot.OFFICIAL_GUILD)) return;
        event.getChannel().getMessageById(event.getMessageId()).queue(message -> {
            MessageReaction reaction = message.getReactions().stream().filter(r -> r.getEmote().getName().equals(event.getReactionEmote().getName())).findFirst().orElse(null);
            if (reaction != null) {
                if (reaction.getCount() == 5) {
                    message.pin().complete();
                    event.getChannel().getHistory().retrievePast(1).complete().get(0).delete().queue();
                }
            }
        });
    }

    @Override
    public void onReady(ReadyEvent event) {
        FlareBot.getInstance().latch.countDown();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        PlayerCache cache = flareBot.getPlayerCache(event.getMember().getUser().getId());
        cache.setLastSeen(LocalDateTime.now());
        if (FlareBotManager.getInstance().getGuild(event.getGuild().getId()).isBlocked()) return;
        if (flareBot.getManager().getGuild(event.getGuild().getId()).getWelcome() != null) {
            Welcome welcome = flareBot.getManager().getGuild(event.getGuild().getId()).getWelcome();
            if ((welcome.getChannelId() != null && flareBot.getChannelByID(welcome.getChannelId()) != null)
                    || welcome.isDmEnabled()) {
                if(welcome.getChannelId() != null && flareBot.getChannelByID(welcome.getChannelId()) != null) {
                    TextChannel channel = flareBot.getChannelByID(welcome.getChannelId());
                    if (!channel.canTalk()) {
                        welcome.setGuildEnabled(false);
                        MessageUtils.sendPM(event.getGuild().getOwner().getUser(), "Cannot send welcome messages in "
                                + channel.getAsMention() + " due to this, welcomes have been disabled!");
                    }
                    if (welcome.isGuildEnabled()) {
                        String guildMsg = welcome.getRandomGuildMessage()
                                .replace("%user%", event.getMember().getUser().getName())
                                .replace("%guild%", event.getGuild().getName())
                                .replace("%mention%", event.getMember().getUser().getAsMention());
                        channel.sendMessage(guildMsg).queue();
                    }
                }
                if(welcome.isDmEnabled()) {
                    String dmMsg = welcome.getRandomDmMessage()
                            .replace("%user%", event.getMember().getUser().getName())
                            .replace("%guild%", event.getGuild().getName())
                            .replace("%mention%", event.getMember().getUser().getAsMention());
                    MessageUtils.sendPM(event.getMember().getUser(), dmMsg);
                }
            } else welcome.setGuildEnabled(false);
        }
        GuildWrapper wrapper = FlareBotManager.getInstance().getGuild(event.getGuild().getId());
        if (!wrapper.getAutoAssignRoles().isEmpty()) {
            Set<String> autoAssignRoles = wrapper.getAutoAssignRoles();
            List<Role> roles = new ArrayList<>();
            for (String s : autoAssignRoles) {
                Role role = event.getGuild().getRoleById(s);
                if (role != null) {
                    roles.add(role);
                } else autoAssignRoles.remove(s);
            }
            try {
                event.getGuild().getController().addRolesToMember(event.getMember(), roles).queue((n) -> {
                }, e1 -> handle(e1, event, roles));
            } catch (Exception e1) {
                handle(e1, event, roles);
            }
        }
    }

    private void handle(Throwable e1, GuildMemberJoinEvent event, List<Role> roles) {
        if (!e1.getMessage().startsWith("Can't modify a role with higher")) {
            MessageUtils.sendPM(event.getGuild().getOwner().getUser(),
                    "**Could not auto assign a role!**\n" + e1.getMessage());
            return;
        }
        MessageUtils.sendPM(event.getGuild().getOwner().getUser(), "**Hello!\nI am here to tell you that I could not give the role(s) ```\n" +
                roles.stream().map(Role::getName).collect(Collectors.joining("\n")) +
                "\n``` to one of your new users!\n" +
                "Please move one of the following roles so they are higher up than any of the above: \n```" +
                event.getGuild().getSelfMember().getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining("\n")) +
                "``` in your server's role tab!**");
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (event.getJDA().getStatus() == JDA.Status.CONNECTED &&
                event.getGuild().getSelfMember().getJoinDate().plusMinutes(2).isAfter(OffsetDateTime.now()))
            FlareBot.getInstance().getGuildLogChannel().sendMessage(new EmbedBuilder()
                    .setColor(new Color(96, 230, 144))
                    .setThumbnail(event.getGuild().getIconUrl())
                    .setFooter(event.getGuild().getId(), event.getGuild().getIconUrl())
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setTimestamp(event.getGuild().getSelfMember().getJoinDate())
                    .setDescription("Guild Created: `" + event.getGuild().getName() + "` :smile: :heart:\n" +
                            "Guild Owner: " + event.getGuild().getOwner().getUser().getName() + "\nGuild Members: " +
                            event.getGuild().getMembers().size()).build()).queue();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        FlareBot.getInstance().getGuildLogChannel().sendMessage(new EmbedBuilder()
                .setColor(new Color(244, 23, 23))
                .setThumbnail(event.getGuild().getIconUrl())
                .setFooter(event.getGuild().getId(), event.getGuild().getIconUrl())
                .setTimestamp(OffsetDateTime.now())
                .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                .setDescription("Guild Deleted: `" + event.getGuild().getName() + "` L :broken_heart:\n" +
                        "Guild Owner: " + (event.getGuild().getOwner() != null ?
                        event.getGuild().getOwner().getUser().getName()
                        : "Non-existent, they had to much L")).build()).queue();
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            event.getGuild().getAudioManager().setSelfDeafened(true);
            if (FlareBot.getInstance().getMusicManager().hasPlayer(event.getGuild().getId())) {
                FlareBot.getInstance().getMusicManager().getPlayer(event.getGuild().getId()).setPaused(false);
            }
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            if (FlareBot.getInstance().getMusicManager().hasPlayer(event.getGuild().getId())) {
                FlareBot.getInstance().getMusicManager().getPlayer(event.getGuild().getId()).setPaused(true);
            }
        } else {
            if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
                if (flareBot.getActiveVoiceChannels() == 0 && UpdateCommand.NOVOICE_UPDATING.get()) {
                    FlareBot.getInstance().getImportantLogChannel()
                            .sendMessage("I am now updating, there are no voice channels active!").queue();
                    UpdateCommand.update(true, null);
                }
                return;
            }
            if (event.getChannelLeft().getMembers().contains(event.getGuild().getMember(event.getJDA().getSelfUser()))
                    && event.getChannelLeft().getMembers().size() < 2) {
                event.getChannelLeft().getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        PlayerCache cache = flareBot.getPlayerCache(event.getAuthor().getId());
        cache.setLastMessage(LocalDateTime.from(event.getMessage().getCreationTime()));
        cache.setLastSeen(LocalDateTime.now());
        cache.setLastSpokeGuild(event.getGuild().getId());

        if (FlareBot.getPrefixes() == null) return;
        if (event.getMessage().getRawContent().startsWith(String.valueOf(FlareBot.getPrefixes().get(getGuildId(event))))
                && !event.getAuthor().isBot()) {
            List<Permission> perms = event.getChannel().getGuild().getSelfMember().getPermissions(event.getChannel());
            if (!perms.contains(Permission.ADMINISTRATOR)) {
                if (!perms.contains(Permission.MESSAGE_WRITE)) {
                    return;
                }
                if (!perms.contains(Permission.MESSAGE_EMBED_LINKS)) {
                    event.getChannel().sendMessage("Hey! I can't be used here." +
                            "\nI do not have the `Embed Links` permission! Please go to your permissions and give me Embed Links." +
                            "\nThanks :D").queue();
                    return;
                }
            }
            String message = event.getMessage().getRawContent();
            //message = message.replaceAll("`", "'");
            message = MessageUtils.escapeMarkdown(message);
            String command = message.substring(1);
            String[] args = new String[0];
            if (message.contains(" ")) {
                command = command.substring(0, message.indexOf(" ") - 1);
                args = message.substring(message.indexOf(" ") + 1).split(" ");
            }
            Command cmd = flareBot.getCommand(command);
            if (cmd != null)
                handleCommand(event, cmd, command, args);
        } else {
            if (FlareBot.getPrefixes().get(getGuildId(event)) != FlareBot.COMMAND_CHAR
                    && !event.getAuthor().isBot()) {
                if (event.getMessage().getRawContent().startsWith("_prefix")) {
                    event.getChannel().sendMessage(MessageUtils.getEmbed(event.getAuthor())
                            .setDescription("The server prefix is `" + FlareBot
                                    .getPrefixes().get(getGuildId(event)) + "`")
                            .build()).queue();
                }
            }
        }
    }

    @Override
    public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {
        if (event.getPreviousOnlineStatus() == OnlineStatus.OFFLINE) {
            flareBot.getPlayerCache(event.getUser().getId()).setLastSeen(LocalDateTime.now());
        }
    }

    @Override
    public void onStatusChange(StatusChangeEvent event) {
        if (sd) return;
        if (FlareBot.getStatusHook() == null) return;
        Request.Builder request = new Request.Builder().url(FlareBot.getStatusHook());
        RequestBody body = RequestBody.create(WebUtils.APPLICATION_JSON, new JSONObject()
                .put("content", String.format("onStatusChange: %s -> %s SHARD: %d",
                        event.getOldStatus(), event.getStatus(),
                        event.getJDA().getShardInfo() != null ? event.getJDA().getShardInfo().getShardId()
                                : null)).toString());
        WebUtils.postAsync(request.post(body));
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        if (event.isClosedByServer())
            FlareBot.LOGGER.error(String.format("---- DISCONNECT [SERVER] CODE: [%d] %s%n", event.getServiceCloseFrame()
                    .getCloseCode(), event
                    .getCloseCode()));
        else
            FlareBot.LOGGER.error(String.format("---- DISCONNECT [CLIENT] CODE: [%d] %s%n", event.getClientCloseFrame()
                    .getCloseCode(), event
                    .getClientCloseFrame().getCloseReason()));
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        if (FlareBotManager.getInstance().getGuild(event.getGuild().getId()).getSelfAssignRoles().contains(event.getRole().getId())) {
            FlareBotManager.getInstance().getGuild(event.getGuild().getId()).getSelfAssignRoles().remove(event.getRole().getId());
        }
    }

    private void handleCommand(GuildMessageReceivedEvent event, Command cmd, String command, String[] args) {
        GuildWrapper guild = flareBot.getManager().getGuild(event.getGuild().getId());
        if (guild.isBlocked()) {
            if (System.currentTimeMillis() > guild.getUnBlockTime() && guild.getUnBlockTime() != -1) {
                guild.revokeBlock();
            }
        }
        handleSpamDetection(event, guild);
        if (cmd.getType() == CommandType.SECRET) {
            if (!cmd.getPermissions(event.getChannel()).isCreator(event.getAuthor()) && !(FlareBot.getInstance().isTestBot()
                    && cmd.getPermissions(event.getChannel()).isContributor(event.getAuthor()))) {
                GeneralUtils.sendImage("https://flarebot.stream/img/trap.jpg", "trap.jpg", event.getAuthor());
                FlareBot.getInstance().logEG("It's a trap", guild.getGuild(), event.getAuthor());
                return;
            }
        }
        if (guild.isBlocked() && !(cmd.getType() == CommandType.SECRET)) {
            return;
        }
        if (UpdateCommand.UPDATING.get()) {
            event.getChannel().sendMessage("**Currently updating!**").queue();
            return;
        }
        if (handleMissingPermission(cmd, event))
            return;

        if(flareBot.getManager().isCommandDisabled(cmd.getCommand())) {
            event.getChannel().sendMessage(MessageUtils.getEmbed(event.getAuthor()).setColor(Color.red)
                    .setDescription(flareBot.getManager().getDisabledCommandReason(cmd.getCommand())).build()).queue();
            return;
        }

        // TODO: Replace with new API endpoints.
        flareBot.postToApi("commands", new JSONObject().put("command", command).put("guild", event.getGuild().getId()).put("guildName", event.getGuild().getName()));

        CACHED_POOL.submit(() -> {
            FlareBot.LOGGER.info(
                    "Dispatching command '" + cmd.getCommand() + "' " + Arrays
                            .toString(args) + " in " + event.getChannel() + "! Sender: " +
                            event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator());
            try {
                cmd.onCommand(event.getAuthor(), guild, event.getChannel(), event.getMessage(), args, event
                        .getMember());
            } catch (Exception ex) {
                MessageUtils
                        .sendException("**There was an internal error trying to execute your command**", ex, event
                                .getChannel());
                FlareBot.LOGGER.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                        + Arrays.toString(args) + " in " + event.getChannel() + "! Sender: " +
                        event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator(), ex);
            }
            if (cmd.deleteMessage())
                delete(event.getMessage());
        });
    }

    private boolean handleMissingPermission(Command cmd, GuildMessageReceivedEvent e) {
        if (cmd.getDiscordPermission() != null) {
            if(!cmd.getDiscordPermission().isEmpty())
                if (e.getMember().getPermissions().containsAll(cmd.getDiscordPermission()))
                    return false;
        }
        if (cmd.getPermission() != null && cmd.getPermission().length() > 0) {
            if (!cmd.getPermissions(e.getChannel()).hasPermission(e.getMember(), cmd.getPermission())) {
                Message msg = MessageUtils.sendErrorMessage(MessageUtils.getEmbed(e.getAuthor())
                        .setDescription("You are missing the permission ``"
                                + cmd
                                .getPermission() + "`` which is required for use of this command!"), e
                        .getChannel());
                delete(e.getMessage());
                new FlarebotTask("Delete message " + msg.getChannel().toString()) {
                    @Override
                    public void run() {
                        delete(msg);
                    }
                }.delay(TimeUnit.SECONDS.toMillis(5));
                return true;
            }
        }
        return false;
    }

    private void delete(Message message) {
        if (message.getTextChannel().getGuild().getSelfMember()
                .getPermissions(message.getTextChannel()).contains(Permission.MESSAGE_MANAGE))
            message.delete().queue();
    }

    private String getGuildId(GenericGuildMessageEvent e) {
        return e.getChannel().getGuild() != null ? e.getChannel().getGuild().getId() : null;
    }

    private void handleSpamDetection(GuildMessageReceivedEvent event, GuildWrapper guild) {
        if (spamMap.containsKey(event.getGuild().getId())) {
            int messages = spamMap.get(event.getGuild().getId());
            double allowed = Math.floor(Math.sqrt(GeneralUtils.getGuildUserCount(event.getGuild()) / 2.5));
            allowed = allowed == 0 ? 1 : allowed;
            if (messages > allowed) {
                if (!guild.isBlocked()) {
                    event.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).appendDescription(
                            "We detected command spam in this guild. No commands will be able to be run in this guild for a little bit.").build()).queue();
                    guild.addBlocked("Command spam", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
                }
            } else {
                spamMap.put(event.getGuild().getId(), messages + 1);
            }
        } else {
            spamMap.put(event.getGuild().getId(), 1);
        }
    }
}

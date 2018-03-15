package stream.flarebot.flarebot;

import io.prometheus.client.Histogram;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.secret.UpdateCommand;
import stream.flarebot.flarebot.database.RedisController;
import stream.flarebot.flarebot.metrics.Metrics;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.mod.modlog.ModlogHandler;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.objects.Welcome;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.WebUtils;
import stream.flarebot.flarebot.util.buttons.ButtonUtil;
import stream.flarebot.flarebot.util.errorhandling.Markers;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;
import stream.flarebot.flarebot.util.objects.ButtonGroup;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Events extends ListenerAdapter {

    public static final ThreadGroup COMMAND_THREADS = new ThreadGroup("Command Threads");
    private static final ExecutorService CACHED_POOL = Executors.newCachedThreadPool(r ->
            new Thread(COMMAND_THREADS, r, "Command Pool-" + COMMAND_THREADS.activeCount()));
    private static final List<Long> removedByMe = new ArrayList<>();

    private final Logger LOGGER = FlareBot.getLog(this.getClass());
    private final Pattern multiSpace = Pattern.compile(" {2,}");

    private FlareBot flareBot;

    private Map<String, Integer> spamMap = new ConcurrentHashMap<>();

    private final Map<Integer, Long> shardEventTime = new HashMap<>();
    private final AtomicInteger commandCounter = new AtomicInteger(0);

    private Map<Long, Double> maxButtonClicksPerSec = new HashMap<>();
    private Map<Long, List<Double>> buttonClicksPerSec = new HashMap<>();

    Events(FlareBot bot) {
        this.flareBot = bot;
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_READ)) return;
        if (event.getUser().isBot()) return;
        if (ButtonUtil.isButtonMessage(event.getMessageId())) {
            for (ButtonGroup.Button button : ButtonUtil.getButtonGroup(event.getMessageId()).getButtons()) {
                if (event.getReactionEmote() != null && (event.getReactionEmote().getIdLong() == button.getEmoteId())
                        || (button.getUnicode() != null && event.getReactionEmote().getName().equals(button.getUnicode()))) {
                    button.onClick(event.getUser());
                    String emote = event.getReactionEmote() != null ? event.getReactionEmote().getName() + "(" + event.getReactionEmote().getId() + ")" : button.getUnicode();
                    Metrics.buttonsPressed.labels(emote, event.getMessageId());
                    Long messageId = event.getMessageIdLong();
                    event.getChannel().getMessageById(event.getMessageId()).queue(message -> {
                        for (MessageReaction reaction : message.getReactions()) {
                            if (reaction.getReactionEmote().equals(event.getReactionEmote())) {
                                reaction.removeReaction(event.getUser()).queue();
                            }
                        }
                    });
                    return;
                }
            }
        }
        if (!FlareBotManager.instance().getGuild(event.getGuild().getId()).getBetaAccess()) return;
        if (!event.getReactionEmote().getName().equals("\uD83D\uDCCC")) return; // Check if it's a :pushpin:
        event.getChannel().getMessageById(event.getMessageId()).queue(message -> {
            MessageReaction reaction =
                    message.getReactions().stream().filter(r -> r.getReactionEmote().getName()
                            .equals(event.getReactionEmote().getName())).findFirst().orElse(null);
            if (reaction != null) {
                if (reaction.getCount() == 5) {
                    message.pin().queue((aVoid) -> event.getChannel().getHistory().retrievePast(1).complete().get(0)
                            .delete().queue());
                }
            }
        });
    }

    @Override
    public void onReady(ReadyEvent event) {
        if (FlareBot.instance().isReady())
            FlareBot.instance().run();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        PlayerCache cache = flareBot.getPlayerCache(event.getMember().getUser().getId());
        cache.setLastSeen(LocalDateTime.now());
        GuildWrapper wrapper = FlareBotManager.instance().getGuild(event.getGuild().getId());
        if (wrapper == null) return;
        if (wrapper.isBlocked()) return;
        if (flareBot.getManager().getGuild(event.getGuild().getId()).getWelcome() != null) {
            Welcome welcome = wrapper.getWelcome();
            if ((welcome.getChannelId() != null && Getters.getChannelById(welcome.getChannelId()) != null)
                    || welcome.isDmEnabled()) {
                if (welcome.getChannelId() != null && Getters.getChannelById(welcome.getChannelId()) != null) {
                    TextChannel channel = Getters.getChannelById(welcome.getChannelId());
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
                if (welcome.isDmEnabled()) {
                    if (event.getMember().getUser().isBot()) return; // We can't DM other bots.
                    MessageUtils.sendPM(event.getMember().getUser(), welcome.getRandomDmMessage()
                            .replace("%user%", event.getMember().getUser().getName())
                            .replace("%guild%", event.getGuild().getName())
                            .replace("%mention%", event.getMember().getUser().getAsMention()));
                }
            } else welcome.setGuildEnabled(false);
        }
        if (event.getMember().getUser().isBot()) return;
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
                StringBuilder sb = new StringBuilder("```\n");
                for (Role role : roles) {
                    sb.append(role.getName()).append(" (").append(role.getId()).append(")\n");
                }
                sb.append("```");
                ModlogHandler.getInstance().postToModlog(wrapper, ModlogEvent.FLAREBOT_AUTOASSIGN_ROLE, event.getUser(),
                        new MessageEmbed.Field("Roles", sb.toString(), false));
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
                event.getGuild().getSelfMember().getJoinDate().plusMinutes(2).isAfter(OffsetDateTime.now())) {
            if (Metrics.guilds.get() == 0)
                Metrics.guilds.set(Getters.getGuildsCache().size());
            else
                Metrics.guilds.inc();
                    Constants.getGuildLogChannel().sendMessage(new EmbedBuilder()
                    .setColor(new Color(96, 230, 144))
                    .setThumbnail(event.getGuild().getIconUrl())
                    .setFooter(event.getGuild().getId(), event.getGuild().getIconUrl())
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setTimestamp(event.getGuild().getSelfMember().getJoinDate())
                    .setDescription("Guild Created: `" + event.getGuild().getName() + "` :smile: :heart:\n" +
                            "Guild Owner: " + event.getGuild().getOwner().getUser().getName() + "\nGuild Members: " +
                            event.getGuild().getMembers().size()).build()).queue();
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if (Metrics.guilds.get() == 0)
            Metrics.guilds.set(Getters.getGuildsCache().size());
        else
            Metrics.guilds.dec();
        Constants.getGuildLogChannel().sendMessage(new EmbedBuilder()
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
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser()) && flareBot.getMusicManager()
                .hasPlayer(event.getGuild().getId())) {
            flareBot.getMusicManager().getPlayer(event.getGuild().getId()).setPaused(false);
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            if (flareBot.getMusicManager().hasPlayer(event.getGuild().getId())) {
                flareBot.getMusicManager().getPlayer(event.getGuild().getId()).setPaused(true);
            }
            if (Getters.getActiveVoiceChannels() == 0 && FlareBot.NOVOICE_UPDATING.get()) {
                Constants.getImportantLogChannel()
                        .sendMessage("I am now updating, there are no voice channels active!").queue();
                UpdateCommand.update(true, null);
            }
        } else {
            handleVoiceConnectivity(event.getChannelLeft());
        }
    }

    private void handleVoiceConnectivity(VoiceChannel channel) {
        if (channel.getMembers().contains(channel.getGuild().getSelfMember()) &&
                (channel.getMembers().size() < 2 || channel.getMembers()
                        .stream().filter(m -> m.getUser().isBot()).count() == channel.getMembers().size())) {
            channel.getGuild().getAudioManager().closeAudioConnection();
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        handleVoiceConnectivity(event.getChannelJoined());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        PlayerCache cache = flareBot.getPlayerCache(event.getAuthor().getId());
        cache.setLastMessage(LocalDateTime.from(event.getMessage().getCreationTime()));
        cache.setLastSeen(LocalDateTime.now());
        cache.setLastSpokeGuild(event.getGuild().getId());

        if (event.getAuthor().isBot()) return;
        String message = multiSpace.matcher(event.getMessage().getContentRaw()).replaceAll(" ");
        if (message.startsWith("" + FlareBotManager.instance().getGuild(getGuildId(event)).getPrefix())) {
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

            String command = message.substring(1);
            String[] args = new String[0];
            if (message.contains(" ")) {
                command = command.substring(0, message.indexOf(" ") - 1);
                args = message.substring(message.indexOf(" ") + 1).split(" ");
            }
            Command cmd = FlareBot.getCommandManager().getCommand(command, event.getAuthor());
            if (cmd != null)
                handleCommand(event, cmd, args);
        } else {
            if (FlareBotManager.instance().getGuild(getGuildId(event)).getPrefix() != Constants.COMMAND_CHAR &&
                    (message.startsWith("_prefix")) || message.startsWith(event.getGuild().getSelfMember().getAsMention())) {
                event.getChannel().sendMessage(MessageUtils.getEmbed(event.getAuthor())
                        .setDescription("The server prefix is `" + FlareBotManager
                                .instance().getGuild(getGuildId(event)).getPrefix() + "`")
                        .build()).queue();
            }
            if (!message.isEmpty()) {
                RedisController.set(event.getMessageId(), GeneralUtils.getRedisMessageJson(event.getMessage()), "nx", "ex", 86400);
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
        if (FlareBot.EXITING.get()) return;
        String statusHook = FlareBot.getStatusHook();
        if (statusHook == null) return;
        Request.Builder request = new Request.Builder().url(statusHook);
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
            LOGGER.error(Markers.NO_ANNOUNCE, String.format("---- DISCONNECT [SERVER] CODE: [%d] %s%n", event.getServiceCloseFrame()
                    .getCloseCode(), event
                    .getCloseCode()));
        else
            LOGGER.error(Markers.NO_ANNOUNCE, String.format("---- DISCONNECT [CLIENT] CODE: [%d] %s%n", event.getClientCloseFrame()
                    .getCloseCode(), event
                    .getClientCloseFrame().getCloseReason()));
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        if (FlareBotManager.instance().getGuild(event.getGuild().getId()) == null) return;
        if (FlareBotManager.instance().getGuild(event.getGuild().getId()).getSelfAssignRoles().contains(event.getRole().getId())) {
            FlareBotManager.instance().getGuild(event.getGuild().getId()).getSelfAssignRoles().remove(event.getRole().getId());
        }
    }

    private void handleCommand(GuildMessageReceivedEvent event, Command cmd, String[] args) {
        Metrics.commandsReceived.labels(cmd.getClass().getSimpleName()).inc();
        GuildWrapper guild = flareBot.getManager().getGuild(event.getGuild().getId());

        if (guild.hasBetaAccess()) {
            if (guild.getSettings().getChannelBlacklist().contains(event.getChannel().getIdLong())
                    && !guild.getPermissions().hasPermission(event.getMember(), stream.flarebot.flarebot.permissions.Permission.BLACKLIST_BYPASS))
                return;
            else if (guild.getSettings().getUserBlacklist().contains(event.getAuthor().getIdLong())
                    && !guild.getPermissions().hasPermission(event.getMember(), stream.flarebot.flarebot.permissions.Permission.BLACKLIST_BYPASS))
                return;
        }

        if (guild.isBlocked()) {
            if (System.currentTimeMillis() > guild.getUnBlockTime() && guild.getUnBlockTime() != -1)
                guild.revokeBlock();
        }
        handleSpamDetection(event, guild);
        if (!GeneralUtils.canRunCommand(cmd, event.getAuthor())) {
            GeneralUtils.sendImage("https://flarebot.stream/img/trap.jpg", "trap.jpg", event.getAuthor());
            Constants.logEG("It's a trap", cmd, guild.getGuild(), event.getAuthor());
            return;
        }
        if (guild.isBlocked() && !(cmd.getType() == CommandType.SECRET)) return;
        if (handleMissingPermission(cmd, event)) return;
        if (!guild.hasBetaAccess() && cmd.isBetaTesterCommand()) {
            if (flareBot.isTestBot())
                LOGGER.error("Guild " + event.getGuild().getId() + " tried to use the beta command '"
                        + cmd.getCommand() + "'!");
            return;
        }
        if (FlareBot.UPDATING.get()) {
            event.getChannel().sendMessage("**Currently updating!**").queue();
            return;
        }

        if (flareBot.getManager().isCommandDisabled(cmd.getCommand())) {
            MessageUtils.sendErrorMessage(flareBot.getManager().getDisabledCommandReason(cmd.getCommand()), event.getChannel(), event.getAuthor());
            return;
        }

        // Internal stuff
        if (event.getGuild().getId().equals(Constants.OFFICIAL_GUILD) && !handleOfficialGuildStuff(event, cmd))
            return;

        CACHED_POOL.submit(() -> {
            LOGGER.info(
                    "Dispatching command '" + cmd.getCommand() + "' " + Arrays
                            .toString(args) + " in " + event.getChannel() + "! Sender: " +
                            event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator());
            // We're sending a lot of commands... Let's change the way this works soon :D
            /*ApiRequester.requestAsync(ApiRoute.DISPATCH_COMMAND, new JSONObject().put("command", cmd.getCommand())
                    .put("guildId", guild.getGuildId()));*/
            commandCounter.incrementAndGet();
            try {
                Histogram.Timer executionTimer = Metrics.commandExecutionTime.labels(cmd.getClass().getSimpleName()).startTimer();
                cmd.onCommand(event.getAuthor(), guild, event.getChannel(), event.getMessage(), args, event
                        .getMember());
                executionTimer.observeDuration();
                Metrics.commandsExecuted.labels(cmd.getClass().getSimpleName()).inc();

                MessageEmbed.Field field = null;
                if (args.length > 0) {
                    String s = MessageUtils.getMessage(args, 0).replaceAll("`", "'");
                    if (s.length() > 1000)
                        s = s.substring(0, 1000) + "...";
                    field = new MessageEmbed.Field("Args", "`" + s + "`", false);
                }
                ModlogHandler.getInstance().postToModlog(guild, ModlogEvent.FLAREBOT_COMMAND, event.getAuthor(),
                        new MessageEmbed.Field("Command", cmd.getCommand(), true), field);
            } catch (Exception ex) {
                Metrics.commandExceptions.labels(ex.getClass().getSimpleName()).inc();
                MessageUtils
                        .sendException("**There was an internal error trying to execute your command**", ex, event
                                .getChannel());
                LOGGER.error("Exception in guild " + event.getGuild().getId() + "!\n" + '\'' + cmd.getCommand() + "' "
                        + Arrays.toString(args) + " in " + event.getChannel() + "! Sender: " +
                        event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator(), ex);
            }
            if ((guild.hasBetaAccess() && guild.getSettings().shouldDeleteCommands()) || cmd.deleteMessage()) {
                delete(event.getMessage());
                removedByMe.add(event.getMessageIdLong());
            }
        });
    }

    private boolean handleOfficialGuildStuff(GuildMessageReceivedEvent event, Command command) {
        Guild guild = event.getGuild();
        GuildWrapper wrapper = FlareBotManager.instance().getGuild(guild.getId());

        if (event.getChannel().getIdLong() == 226785954537406464L && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().sendMessage("Please use me in <#226786507065786380>!").queue();
            return false;
        }
        return true;
    }

    private boolean handleMissingPermission(Command cmd, GuildMessageReceivedEvent e) {
        if (cmd.getDiscordPermission() != null) {
            if (!cmd.getDiscordPermission().isEmpty())
                if (e.getMember().getPermissions().containsAll(cmd.getDiscordPermission()))
                    return false;
        }
        if (cmd.getPermission() != null) {
            if (!cmd.getPermissions(e.getChannel()).hasPermission(e.getMember(), cmd.getPermission())) {
                MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(e.getAuthor()).setColor(Color.red)
                                .setDescription("You are missing the permission ``"
                                        + cmd
                                        .getPermission() + "`` which is required for use of this command!").build(), 5000,
                        e.getChannel());
                delete(e.getMessage());
                return true;
            }
        }
        return !cmd.getPermissions(e.getChannel()).hasPermission(
                e.getMember(),
                stream.flarebot.flarebot.permissions.Permission.getPermission(cmd.getType())
        ) && cmd.getPermission() == null && cmd.getType() != CommandType.SECRET;
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
            double allowed = Math.floor(Math.sqrt(GuildUtils.getGuildUserCount(event.getGuild()) / 2.5));
            allowed = allowed == 0 ? 1 : allowed;
            if (messages > allowed) {
                if (!guild.isBlocked()) {
                    MessageUtils.sendErrorMessage("We detected command spam in this guild. No commands will be able to " +
                            "be run in this guild for a little bit.", event.getChannel());
                    guild.addBlocked("Command spam", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
                    Metrics.blocksGivenOut.labels(guild.getGuildId()).inc();
                }
            } else {
                spamMap.put(event.getGuild().getId(), messages + 1);
            }
        } else {
            spamMap.put(event.getGuild().getId(), 1);
        }
    }

    public int getCommandCount() {
        return commandCounter.get();
    }

    @Override
    public void onGenericEvent(Event e) {
        shardEventTime.put(e.getJDA().getShardInfo() == null ? 0 : e.getJDA().getShardInfo().getShardId(), System.currentTimeMillis());
    }

    public Map<Integer, Long> getShardEventTime() {
        return this.shardEventTime;
    }

    public Map<String, Integer> getSpamMap() {
        return spamMap;
    }

    public List<Long> getRemovedByMeList() {
        return removedByMe;
    }

    public Map<Long, Double> getMaxButtonClicksPerSec() {
        return maxButtonClicksPerSec;
    }

    public Map<Long, List<Double>> getButtonClicksPerSec() {
        return buttonClicksPerSec;
    }
}

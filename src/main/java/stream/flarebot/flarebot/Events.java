package stream.flarebot.flarebot;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
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
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONObject;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.secret.UpdateCommand;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.scheduler.FlarebotTask;
import stream.flarebot.flarebot.objects.Welcome;
import stream.flarebot.flarebot.util.ExpiringMap;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Events extends ListenerAdapter {

    private volatile boolean sd = false;
    private FlareBot flareBot;
    private HashMap<String, Integer> spamMap = new HashMap<>();
    private static final ThreadGroup COMMAND_THREADS = new ThreadGroup("Command Threads");
    private static final ExecutorService CACHED_POOL = Executors.newCachedThreadPool(r ->
            new Thread(COMMAND_THREADS, r, "Command Pool-" + COMMAND_THREADS.activeCount()));

    public Events(FlareBot bot) {
        this.flareBot = bot;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> sd = true));
    }

    @Override
    public void onReady(ReadyEvent event) {
        FlareBot.getInstance().latch.countDown();
        new Timer().schedule(new TimerTask() { //If you're not ok with this in the ready event just say so.
            @Override
            public void run() {
                spamMap = new HashMap<>(); //According to stack overflow it's better to just create a new hashmap and let the garbage collector get the other one.
            }
        }, TimeUnit.SECONDS.toMillis(1l), TimeUnit.SECONDS.toMillis(1l));
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        PlayerCache cache = flareBot.getPlayerCache(event.getMember().getUser().getId());
        cache.setLastSeen(LocalDateTime.now());
        if (FlareBotManager.getInstance().getGuild(event.getGuild().getId()).isBlocked()) return;
        if (flareBot.getManager().getGuild(event.getGuild().getId()).getWelcome() != null) {
            Welcome welcome = flareBot.getManager().getGuild(event.getGuild().getId()).getWelcome();
            TextChannel channel = flareBot.getChannelByID(welcome.getChannelId());
            if (channel != null) {
                if (!channel.canTalk()) {
                    welcome.setGuildEnabled(false);
                    MessageUtils.sendPM(event.getGuild().getOwner().getUser(), "Cannot send welcome messages in "
                            + channel.getAsMention() + " due to this, welcomes have been disabled!");
                }
                String guildMsg = welcome.getRandomGuildMessage()
                        .replace("%user%", event.getMember().getUser().getName())
                        .replace("%guild%", event.getGuild().getName())
                        .replace("%mention%", event.getMember().getUser().getAsMention());
                channel.sendMessage(guildMsg).queue(MessageUtils.noOpConsumer(), MessageUtils.noOpConsumer());
                String dmMsg = welcome.getRandomDmMessage()
                        .replace("%user%", event.getMember().getUser().getName())
                        .replace("%guild%", event.getGuild().getName())
                        .replace("%mention%", event.getMember().getUser().getAsMention());
                MessageUtils.sendPM(event.getMember().getUser(), dmMsg);
            } else welcome.setGuildEnabled(false);
        }
        GuildWrapper wrapper = FlareBotManager.getInstance().getGuild(event.getGuild().getId());
        if (wrapper.getAutoAssignRoles().contains(event.getGuild().getId())) {
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
        StringBuilder message = new StringBuilder();

        message.append("**Hello!\nI am here to tell you that I could not give the role(s) ```\n");
        message.append(roles.stream().map(Role::getName).collect(Collectors.joining("\n")))
                .append("\n``` to one of your new users!\n");
        message.append("Please move one of the following roles so they are higher up than any of the above: \n```")
                .append(event.getGuild().getSelfMember().getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining("\n"))).append("``` in your server's role tab!**");
        MessageUtils.sendPM(event.getGuild().getOwner().getUser(), message.toString());
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
                    FlareBot.getInstance().getUpdateChannel()
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
        if(FlareBot.getPrefixes() == null) return;
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
            String command = message.substring(1);
            String[] args = new String[0];
            if (message.contains(" ")) {
                command = command.substring(0, message.indexOf(" ") - 1);

                args = message.substring(message.indexOf(" ") + 1).split(" ");
            }
            for (Command cmd : flareBot.getCommands()) {
                if (cmd.getCommand().equalsIgnoreCase(command)) {
                    if(spamMap.containsKey(event.getGuild().getId())){
                        int messages = spamMap.get(event.getGuild().getId());
                        double allowed = Math.floor(Math.sqrt(GeneralUtils.getGuildUserCount(event.getGuild()) / 2.5));
                        allowed = allowed == 0 ? 1 : allowed;
                        if(messages > allowed){
                            GuildWrapper guild = flareBot.getManager().getGuild(event.getGuild().getId());
                            if(!guild.isBlocked()){
                                event.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).appendDescription("We detected command spam in this guild. No commands will be able to be run in this guild for a little bit.").build()).queue();
                                guild.addBlocked("Command spam", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5l));
                            }
                        } else {
                            spamMap.put(event.getGuild().getId(), messages++);
                        }
                    } else {
                        spamMap.put(event.getGuild().getId(), 1);
                    }
                    if (cmd.getType() == CommandType.HIDDEN) {
                        if (!cmd.getPermissions(event.getChannel()).isCreator(event.getAuthor())) {
                            try {
                                File dir = new File("imgs");
                                if (!dir.exists())
                                    dir.mkdir();
                                File trap = new File("imgs" + File.separator + "trap.jpg");
                                if (!trap.exists()) {
                                    trap.createNewFile();
                                    URL url = new URL("https://cdn.discordapp.com/attachments/242297848123621376/293873454678147073/trap.jpg");
                                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 FlareBot");
                                    InputStream is = conn.getInputStream();
                                    OutputStream os = new FileOutputStream(trap);
                                    byte[] b = new byte[2048];
                                    int length;
                                    while ((length = is.read(b)) != -1) {
                                        os.write(b, 0, length);
                                    }
                                    is.close();
                                    os.close();
                                }
                                event.getAuthor().openPrivateChannel().complete().sendFile(trap, "trap.jpg", null)
                                        .queue();
                            } catch (IOException e) {
                                FlareBot.LOGGER.error("Unable to save 'It's a trap' Easter Egg :(", e);
                            }
                            return;
                        }
                    }
                    if (FlareBotManager.getInstance().getGuild(event.getGuild().getId()).isBlocked() && !(cmd.getType() == CommandType.HIDDEN)) {
                        return;
                    }
                    if (UpdateCommand.UPDATING.get()) {
                        event.getChannel().sendMessage("**Currently updating!**").queue();
                        return;
                    }
                    if (handleMissingPermission(cmd, event))
                        return;
                    flareBot.postToApi("commands", new JSONObject().put("command", command).put("guild", event.getGuild().getId()).put("guildName", event.getGuild().getName()));
                    String[] finalArgs = args;
                    CACHED_POOL.submit(() -> {
                        FlareBot.LOGGER.info(
                                "Dispatching command '" + cmd.getCommand() + "' " + Arrays
                                        .toString(finalArgs) + " in " + event.getChannel() + "! Sender: " +
                                        event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator());
                        try {
                            cmd.onCommand(event.getAuthor(), flareBot.getManager().getGuild(event.getGuild().getId()), event.getChannel(), event.getMessage(), finalArgs, event
                                    .getMember());
                        } catch (Exception ex) {
                            MessageUtils
                                    .sendException("**There was an internal error trying to execute your command**", ex, event
                                            .getChannel());
                            FlareBot.LOGGER.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                                    + Arrays.toString(finalArgs) + " in " + event.getChannel() + "! Sender: " +
                                    event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator(), ex);
                        }
                        if (cmd.deleteMessage())
                            delete(event.getMessage());
                    });
                    return;
                } else {
                    for (String alias : cmd.getAliases()) {
                        if (alias.equalsIgnoreCase(command)) {
                            if(spamMap.containsKey(event.getGuild().getId())){
                                int messages = spamMap.get(event.getGuild().getId());
                                double allowed = Math.floor(Math.sqrt(GeneralUtils.getGuildUserCount(event.getGuild()) / 2.5));
                                allowed = allowed == 0 ? 1 : allowed;
                                if(messages > allowed){
                                    GuildWrapper guild = flareBot.getManager().getGuild(event.getGuild().getId());
                                    if(!guild.isBlocked()){
                                        event.getChannel().sendMessage(new EmbedBuilder().setColor(Color.RED).appendDescription("We detected command spam in this guild. No commands will be able to be run in this guild for a little bit.").build()).queue();
                                        guild.addBlocked("Command spam", System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5l));
                                    }
                                } else {
                                    spamMap.put(event.getGuild().getId(), messages++);
                                }
                            } else {
                                spamMap.put(event.getGuild().getId(), 1);
                            }
                            if (cmd.getType() == CommandType.HIDDEN) {
                                if (!cmd.getPermissions(event.getChannel()).isCreator(event.getAuthor())) {
                                    return;
                                }
                            }
                            if (UpdateCommand.UPDATING.get()) {
                                event.getChannel().sendMessage("**Currently updating!**").queue();
                                return;
                            }
                            FlareBot.LOGGER.info(
                                    "Dispatching command '" + cmd.getCommand() + "' " + Arrays
                                            .toString(args) + " in " + event.getChannel() + "! Sender: " +
                                            event.getAuthor().getName() + '#' + event.getAuthor().getDiscriminator());
                            if (handleMissingPermission(cmd, event))
                                return;
                            flareBot.postToApi("commands", new JSONObject().put("command", cmd.getCommand()).put("guild", event.getGuild().getId()));
                            String[] finalArgs = args;
                            CACHED_POOL.submit(() -> {
                                FlareBot.LOGGER.info(
                                        "Dispatching command '" + cmd.getCommand() + "' " + Arrays
                                                .toString(finalArgs) + " in " + event.getChannel() + "! Sender: " +
                                                event.getAuthor().getName() + '#' + event.getAuthor()
                                                .getDiscriminator());
                                try {
                                    cmd.onCommand(event.getAuthor(), flareBot.getManager().getGuild(event.getGuild().getId()), event.getChannel(), event
                                            .getMessage(), finalArgs, event.getMember());
                                } catch (Exception ex) {
                                    FlareBot.LOGGER.error("Exception in guild " + "!\n" + '\'' + cmd.getCommand() + "' "
                                            + Arrays.toString(finalArgs) + " in " + event.getChannel() + "! Sender: " +
                                            event.getAuthor().getName() + '#' + event.getAuthor()
                                            .getDiscriminator(), ex);
                                    MessageUtils
                                            .sendException("**There was an internal error trying to execute your command**", ex, event
                                                    .getChannel());
                                }
                                if (cmd.deleteMessage())
                                    delete(event.getMessage());
                            });
                            return;
                        }
                    }
                }
            }
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
        Unirest.post(FlareBot.getStatusHook())
                .header("Content-Type", "application/json")
                .body(new JSONObject()
                        .put("content", String.format("onStatusChange: %s -> %s SHARD: %d",
                                event.getOldStatus(), event.getStatus(),
                                event.getJDA().getShardInfo() != null ? event.getJDA().getShardInfo().getShardId()
                                        : null)))
                .asStringAsync();
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

    private boolean handleMissingPermission(Command cmd, GuildMessageReceivedEvent e) {
        if (cmd.getDiscordPermission() != null) {
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
                }.delay(5000);
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
}

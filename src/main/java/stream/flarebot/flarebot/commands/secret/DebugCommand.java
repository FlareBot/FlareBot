package stream.flarebot.flarebot.commands.secret;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.Events;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.InternalCommand;
import stream.flarebot.flarebot.music.VideoThread;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.tasks.VoiceChannelCleanup;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.web.DataInterceptor;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public class DebugCommand implements InternalCommand {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length < 1) {
            MessageUtils.sendUsage(this, channel, sender, args);
            return;
        }
        FlareBot fb = FlareBot.instance();

        EmbedBuilder eb = MessageUtils.getEmbed();
        if (args[0].equalsIgnoreCase("flarebot") || args[0].equalsIgnoreCase("bot")) {
            eb.setTitle("Bot Debug").setDescription(String.format("Debug for FlareBot v" + FlareBot.getVersion()
                            + "\nUptime: %s"
                            + "\nMemory Usage: %s"
                            + "\nMemory Free: %s"
                            + "\nVideo Threads: %d"
                            + "\nCommand Threads: %d"
                            + "\nTotal Threads: %d"
                            + "\n\nGuilds: %d"
                            + "\nLoaded Guilds: %d"
                            + "\nVoice Channels: %d"
                            + "\nActive Voice Channels: %d"
                            + "\nCommands Executed: %d"
                            + "\nQueued RestActions: %s",

                    fb.getUptime(),
                    getMB(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()),
                    getMB(Runtime.getRuntime().freeMemory()),
                    VideoThread.VIDEO_THREADS.activeCount(),
                    Events.COMMAND_THREADS.activeCount(),
                    Thread.getAllStackTraces().size(),
                    fb.getShardManager().getGuildCache().size(),
                    FlareBotManager.instance().getGuilds().size(),
                    Getters.getConnectedVoiceChannels(),
                    Getters.getActiveVoiceChannels(),
                    fb.getEvents().getCommandCount(),
                    getQueuedRestActions()
            ));

            StringBuilder sb = new StringBuilder();
            for (DataInterceptor interceptor : DataInterceptor.getInterceptors())
                sb.append(WordUtils.capitalize(interceptor.getSender().getName())).append(" - ").append(interceptor.getRequests())
                        .append(" requests").append("\n");
            eb.addField("HTTP Requests", sb.toString(), false);
        } else if (args[0].equalsIgnoreCase("threads")) {
            eb.setTitle("Thread Debug").setDescription(String.format("Video Threads: %d"
                            + "\nCommand Threads: %d"
                            + "\nTotal Threads: %d"
                            + "\nThread list: %s",

                    VideoThread.VIDEO_THREADS.activeCount(),
                    Events.COMMAND_THREADS.activeCount(),
                    Thread.getAllStackTraces().size(),
                    MessageUtils.paste(Thread.getAllStackTraces().keySet().stream()
                            .map(th -> th.getName() + " - " + th.getState() + " (" + th.getThreadGroup().getName() + ")")
                            .collect(Collectors.joining("\n")))
            ));
        } else if (args[0].equalsIgnoreCase("server") || args[0].equalsIgnoreCase("guild")) {
            GuildWrapper wrapper = guild;
            if (args.length == 2)
                wrapper = FlareBotManager.instance().getGuild(args[1]);
            if (wrapper == null) {
                channel.sendMessage("I can't find that guild!").queue();
                return;
            }

            eb.setTitle("Server Debug").setDescription(String.format("Debug for " + wrapper.getGuildIdLong()
                    + "\nData Ver: %s (%s)"
                    + "\nPrefix: %s"
                    + "\nBlocked: %b"
                    + "\nBeta: %b"
                    + "\nWelcomes: %b/%b"
                    + "\nNINO: %b"
                    + "\nSong nick: "
                    + "\nPerms: %s"
                    + "\nMute role: %s"
                    + "\nsettings: %s"
                    + "\n\nFor full guild data do `_guild data " + wrapper.getGuildIdLong() + "`",

                    wrapper.dataVersion, GuildWrapper.DATA_VERSION,
                    MessageUtils.escapeMarkdown(String.valueOf(wrapper.getPrefix())),
                    wrapper.isBlocked(),
                    wrapper.getBetaAccess(),
                    wrapper.getWelcome().isGuildEnabled(), wrapper.getWelcome().isDmEnabled(),
                    wrapper.getNINO().isEnabled(),
                    wrapper.isSongnickEnabled(),
                    MessageUtils.paste(FlareBot.GSON.toJson(wrapper.getPermissions())),
                    wrapper.getMutedRole(),
                    wrapper.getSettings().toString()
            ));
        } else if (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("music")) {
            GuildWrapper wrapper = guild;
            if (args.length == 2)
                wrapper = FlareBotManager.instance().getGuild(args[1]);
            if (wrapper == null) {
                channel.sendMessage("I can't find that guild!").queue();
                return;
            }
            Player player = FlareBot.instance().getMusicManager().getPlayer(wrapper.getGuildId());

            AudioManager manager = wrapper.getGuild().getAudioManager();

            @Nullable
            VoiceChannel vc = manager.getConnectedChannel();

            String lastActive = "Not tracked.";
            if (VoiceChannelCleanup.VC_LAST_USED.containsKey(vc != null ? vc.getIdLong() : guild.getGuildIdLong())) {
                long ms = VoiceChannelCleanup.VC_LAST_USED.get(vc != null ? vc.getIdLong()
                        : guild.getGuildIdLong());
                lastActive = String.valueOf(ms) + " (" + (System.currentTimeMillis() - ms) + "ms ago)";
            }

            boolean isPlaying = player.getPlayingTrack() != null;
            Track track = player.getPlayingTrack();

            eb.setTitle("Bot Debug").setDescription(String.format("Player Debug for `" + wrapper.getGuildId() + "`"
                            + "\nCurrent Track: %s"
                            + "\nCurrent Position: %s"
                            + "\nIs Paused: %b"
                            + "\nPlaylist Length: %s"
                            + "\nIs Looping: %b"
                            + "\nConnecting: %b"
                            + "\nVoice Channel: %s"
                            + "\nConnection State: %s"
                            + "\nLast Active: %s",

                    (isPlaying ? track.getTrack().getIdentifier() : "No current track"),
                    (isPlaying ? track.getTrack().getPosition() + "/" + track.getTrack().getDuration() : "N/A"),
                    player.getPaused(),
                    player.getPlaylist().size(),
                    player.getLooping(),
                    manager.isAttemptingToConnect(),
                    (vc == null ? "null" : vc.toString()),
                    manager.getConnectionStatus().toString(),
                    lastActive
            ));
        } else {
            channel.sendMessage("Invalid debug option").queue();
            return;
        }

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public String getCommand() {
        return "debug";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return "{%}debug flarebot|server|player|threads";
    }

    @Override
    public CommandType getType() {
        return CommandType.DEBUG;
    }

    private String getMB(long bytes) {
        return (bytes / 1024 / 1024) + "MB";
    }

    private String getQueuedRestActions() {
        String queued = FlareBot.instance().getShardManager().getShards().stream()
                .filter(shard -> !((JDAImpl) shard).getRequester().getRateLimiter().getQueuedRouteBuckets().isEmpty())
                .map(shard -> "`" + shard.getShardInfo().getShardId() + "`: " + ((JDAImpl) shard).getRequester().getRateLimiter().getQueuedRouteBuckets().size())
                .collect(Collectors.joining(", "));
        if (queued.isEmpty())
            queued = "No queued rest actions";

        return queued;
    }
}

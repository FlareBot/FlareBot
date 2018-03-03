package stream.flarebot.flarebot.audio;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.music.MusicAnnounceCommand;
import stream.flarebot.flarebot.commands.music.SongCommand;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.Queue;

public class PlayerListener extends AudioEventAdapter {

    private Player player;

    public PlayerListener(Player player) {
        this.player = player;
    }

    @Override
    public void onTrackEnd(AudioPlayer aplayer, AudioTrack atrack, AudioTrackEndReason reason) {
        GuildWrapper wrapper = FlareBotManager.getInstance().getGuild(player.getGuildId());

        // No song on next
        if (player.getPlaylist().isEmpty()) {
            FlareBotManager.getInstance().getLastActive().put(Long.parseLong(player.getGuildId()), System.currentTimeMillis());
        }

        if (wrapper.isSongnickEnabled()) {
            if (GeneralUtils.canChangeNick(player.getGuildId())) {
                Guild c = wrapper.getGuild();
                if (c == null) {
                    wrapper.setSongnick(false);
                } else {
                    if (player.getPlaylist().isEmpty())
                        c.getController().setNickname(c.getSelfMember(), null).queue();
                }
            } else {
                if (!GeneralUtils.canChangeNick(player.getGuildId())) {
                    MessageUtils.sendPM(FlareBot.getInstance().getGuildById(player.getGuildId()).getOwner().getUser(),
                            "FlareBot can't change it's nickname so SongNick has been disabled!");
                }
            }
        }
    }

    @Override
    public void onTrackStart(AudioPlayer aplayer, AudioTrack atrack) {
        FlareBotManager.getInstance().getLastActive().remove(Long.parseLong(player.getGuildId()));

        GuildWrapper wrapper = FlareBotManager.getInstance().getGuild(player.getGuildId());
        if (MusicAnnounceCommand.getAnnouncements().containsKey(player.getGuildId())) {
            TextChannel c =
                    FlareBot.getInstance().getChannelById(MusicAnnounceCommand.getAnnouncements().get(player.getGuildId()));
            if (c != null) {
                if (c.getGuild().getSelfMember().hasPermission(c,
                        Permission.MESSAGE_EMBED_LINKS,
                        Permission.MESSAGE_READ,
                        Permission.MESSAGE_WRITE)) {
                    Track track = player.getPlayingTrack();
                    Queue<Track> playlist = player.getPlaylist();
                    c.sendMessage(MessageUtils.getEmbed()
                            .addField("Now Playing", SongCommand.getLink(track), false)
                            .addField("Duration", GeneralUtils
                                    .formatDuration(track.getTrack().getDuration()), false)
                            .addField("Requested by",
                                    String.format("<@!%s>", track.getMeta()
                                            .get("requester")), false)
                            .addField("Next up", playlist.isEmpty() ? "Nothing" :
                                    SongCommand.getLink(playlist.peek()), false)
                            .setImage("https://img.youtube.com/vi/" + track.getTrack().getIdentifier() + "/hqdefault.jpg")
                            .build()).queue();
                } else {
                    MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                }
            } else {
                MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
            }
        }
        if (wrapper.isSongnickEnabled()) {
            Guild c = wrapper.getGuild();
            if (c == null || !GeneralUtils.canChangeNick(player.getGuildId())) {
                if (!GeneralUtils.canChangeNick(player.getGuildId())) {
                    wrapper.setSongnick(false);
                    MessageUtils.sendPM(wrapper.getGuild().getOwner().getUser(),
                            "FlareBot can't change it's nickname so SongNick has been disabled!");
                }
            } else {
                Track track = player.getPlayingTrack();
                String str = null;
                if (track != null) {
                    str = track.getTrack().getInfo().title;
                    if (str.length() > 32)
                        str = str.substring(0, 32);
                    str = str.substring(0, str.lastIndexOf(' ') + 1);
                } // Even I couldn't make this a one-liner
                c.getController()
                        .setNickname(c.getSelfMember(), str)
                        .queue();
            }
        }
    }
}

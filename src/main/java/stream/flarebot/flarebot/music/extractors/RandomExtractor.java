package stream.flarebot.flarebot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.MessageUtils;

public class RandomExtractor implements Extractor {
    @Override
    public Class<? extends AudioSourceManager> getSourceManagerClass() {
        return YoutubeAudioSourceManager.class;
    }

    @SuppressWarnings("Duplicates") // I don't give a damn
    @Override
    public void process(String input, Player player, Message message, User user) throws Exception {
        int i = 0;
        for (String s : input.split(",")) {
            try {
                AudioItem probablyATrack = player.resolve(s);
                if (probablyATrack == null)
                    continue;
                Track track = new Track((AudioTrack) probablyATrack);
                track.getMeta().put("requester", user.getId());
                track.getMeta().put("guildId", player.getGuildId());
                player.queue(track);
                i++;
            } catch (FriendlyException ignored) {
            }
        }
        MessageUtils.editMessage("", MessageUtils.getEmbed()
                                                 .setDescription("Added " + i + " random songs to the playlist!"), message);
    }

    @Override
    public boolean valid(String input) {
        return input.matches("([^,]{11},)*[^,]{11}");
    }

    @Override
    public AudioSourceManager newSourceManagerInstance() throws Exception {
        YoutubeAudioSourceManager manager = new YoutubeAudioSourceManager();
        manager.setPlaylistPageCount(100);
        return manager;
    }
}

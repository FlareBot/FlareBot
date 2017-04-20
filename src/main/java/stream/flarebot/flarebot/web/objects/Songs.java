package stream.flarebot.flarebot.web.objects;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import stream.flarebot.flarebot.FlareBot;
import net.dv8tion.jda.core.entities.User;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Songs {
    public long length;
    public long progress;
    public String avatar;
    public String discrim;
    public String name;
    public String identifier;
    public String requester;
    public String title;

    public Songs(Track track) {
        title = track.getTrack().getInfo().title;
        identifier = track.getTrack().getIdentifier();
        requester = track.getMeta().getOrDefault("requester", "Unknown").toString();
        length = track.getTrack().getDuration();
        progress = track.getTrack().getPosition();
        User user = FlareBot.getInstance().getUserByID(requester);
        if (user != null) {
            discrim = user.getDiscriminator();
            name = user.getName();
            avatar = user.getEffectiveAvatarUrl();
        }
    }

    public static LinkedList<ResponsePlayer> get() {
        LinkedList<ResponsePlayer> list = new LinkedList<>();
        FlareBot.getInstance().getMusicManager().getPlayers().stream()
                .filter(player -> !player.getPlaylist().isEmpty())
                .map(ResponsePlayer::new)
                .forEach(list::add);
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Songs songs = (Songs) o;

        if (!identifier.equals(songs.identifier)) return false;
        return requester.equals(songs.requester);
    }

    @Override
    public int hashCode() {
        int result = identifier.hashCode();
        result = 31 * result + requester.hashCode();
        return result;
    }

    private static class ResponsePlayer {
        private List<Songs> playlist;
        public Songs current;
        public String guildId;

        public ResponsePlayer(Player p) {
            guildId = p.getGuildId();
            if (p.getPlayingTrack() != null)
                current = new Songs(p.getPlayingTrack());
            this.playlist = p.getPlaylist().stream().map(Songs::new)
                    .collect(Collectors.toList());
        }
    }
}

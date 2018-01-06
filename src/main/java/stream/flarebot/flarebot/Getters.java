package stream.flarebot.flarebot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Getters {

    private static FlareBot flareBot() {
        return FlareBot.getInstance();
    }

    public static TextChannel getChannelById(String id) {
        return getGuilds().stream()
                .map(g -> g.getTextChannelById(id))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    public static TextChannel getChannelById(long id) {
        return getGuilds().stream().map(g -> g.getTextChannelById(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static Guild getGuildById(String id) {
        return getGuilds().stream().filter(g -> g.getId().equals(id)).findFirst().orElse(null);
    }

    public static Guild getGuildById(long id) {
        return getGuilds().stream().filter(g -> g.getIdLong() == id).findFirst().orElse(null);
    }

    public static Emote getEmoteById(long emoteId) {
        for (Guild g : getGuilds())
            if (g.getEmoteById(emoteId) != null)
                return g.getEmoteById(emoteId);
        return null;
    }

    public static List<Guild> getGuilds() {
        return flareBot().getShardManager().getGuilds();
    }

    public static SnowflakeCacheView<Guild> getGuildsCache() {
        return flareBot().getShardManager().getGuildCache();
    }

    public static List<Channel> getChannels() {
        return getGuilds().stream().flatMap(g -> g.getTextChannels().stream()).collect(Collectors.toList());
    }

    public static long getConnectedVoiceChannels() {
        return getGuilds().stream().filter(c -> c.getAudioManager().getConnectedChannel() != null).count();
    }

    public static List<VoiceChannel> getConnectedVoiceChannelList() {
        return getGuilds().stream().map(g -> g.getAudioManager().getConnectedChannel())
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Set<User> getUsers() {
        return flareBot().getShardManager().getUserCache().asSet();
    }

    public static User getUserById(String id) {
        return flareBot().getShardManager().getUserById(id);
    }

    public static User getUserById(long id) {
        return flareBot().getShardManager().getUserById(id);
    }

    public static User retrieveUserById(long id) {
        return flareBot().getClient().retrieveUserById(id).complete();
    }

    public static List<VoiceChannel> getVoiceChannels() {
        return flareBot().getShardManager().getVoiceChannels();
    }

    public static long getActiveVoiceChannels() {
        return getConnectedVoiceChannelList().stream()
                .map(vc -> flareBot().getMusicManager().getPlayer(vc.getGuild().getId()))
                .filter(p -> p != null && p.getPlayingTrack() != null && !p.getPaused())
                .count();
    }

    public static List<JDA> getShards() {
        return flareBot().getShardManager().getShards();
    }

    public static JDA[] getShardsArray() {
        return flareBot().getShardManager().getShards().toArray(new JDA[flareBot().getShardManager().getShards().size()]);
    }


    public static SelfUser getSelfUser() {
        return FlareBot.getInstance().getClient().getSelfUser();
    }
}

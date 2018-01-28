package stream.flarebot.flarebot.analytics;

import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * This data is for tracking how much activity is happening on the bot at one time.
 *
 * Data that is sent:
 *   Amount of guilds we have
 *   Amount of guilds loaded - In our cache
 *   The number of songs queued on the bot
 */
public class ActivityAnalytics implements AnalyticSender {

    @Override
    public JSONObject processData() {
        JSONObject object = new JSONObject();
        // All guilds currently with a song in their playlist (This will not count playing song)
        Stream<Guild> guilds = FlareBot.getInstance().getGuildsCache().stream().filter(g -> FlareBot.getInstance()
                .getMusicManager().getPlayer(g.getId()).getPlaylist().size() > 0);
        object.put("data", new JSONObject()
                .put("guilds", FlareBotManager.getInstance().getGuilds().size())
                .put("guilds_with_songs", guilds.count())
                .put("songs", guilds.mapToInt(g -> FlareBot.getInstance().getMusicManager().getPlayer(g.getId())
                        .getPlaylist().size()).sum()));
        return object;
    }

    @Override
    public long dataDeliveryFrequency() {
        return TimeUnit.MINUTES.toMillis(5);
    }

    @Override
    public String endpoint() {
        return "/activity";
    }
}

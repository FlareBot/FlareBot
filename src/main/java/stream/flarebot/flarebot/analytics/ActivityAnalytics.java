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

        int activeGuilds = 0;
        int queuedSongs = 0;
        for(Guild g : FlareBot.getInstance().getGuildsCache()) {
            if (!FlareBot.getInstance().getMusicManager().getPlayer(g.getId()).getPlaylist().isEmpty()) {
                activeGuilds++;
                queuedSongs += FlareBot.getInstance().getMusicManager().getPlayer(g.getId()).getPlaylist().size();
            }
        }

        object.put("data", new JSONObject()
                .put("guilds", FlareBotManager.getInstance().getGuilds().size())
                .put("guilds_with_songs", activeGuilds)
                .put("songs", queuedSongs));
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

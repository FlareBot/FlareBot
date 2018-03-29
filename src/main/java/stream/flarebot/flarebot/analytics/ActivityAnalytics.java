package stream.flarebot.flarebot.analytics;

import java.util.IntSummaryStatistics;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.Getters;

/**
 * This data is for tracking how much activity is happening on the bot at one time.
 * <p>
 * Data that is sent:
 * - Amount of guilds we have
 * - Amount of guilds loaded - In our cache
 * - Amount of guilds which have songs loaded.
 * - The number of songs queued on the bot
 */
public class ActivityAnalytics implements AnalyticSender {

    @Override
    public JSONObject processData() {
        JSONObject object = new JSONObject();

        IntSummaryStatistics statistics;
        try {
            statistics = FlareBot.instance().getMusicManager().getPlayers().stream()
                    .mapToInt(player -> player.getPlaylist().size())
                    .summaryStatistics();
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        object.put("data", new JSONObject()
                .put("guilds", Getters.getGuildCache().size())
                .put("loaded_guilds", FlareBotManager.instance().getGuilds().size())
                .put("guilds_with_songs", statistics.getCount())
                .put("songs", statistics.getSum()));
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

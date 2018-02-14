package stream.flarebot.flarebot.analytics;

import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;

import java.util.concurrent.TimeUnit;

/**
 * This collects the data on the amount of guilds we're currently in.
 * <p>
 * Data that is sent:
 * - The amount of guilds FlareBot is in
 */
public class GuildCountAnalytics implements AnalyticSender {

    @Override
    public JSONObject processData() {
        JSONObject object = new JSONObject();
        object.put("data", new JSONObject()
                .put("guilds", FlareBot.getInstance().getGuilds().size()));
        return object;
    }

    @Override
    public long dataDeliveryFrequency() {
        return TimeUnit.MINUTES.toMillis(5);
    }

    @Override
    public String endpoint() {
        return "/guild-count";
    }
}

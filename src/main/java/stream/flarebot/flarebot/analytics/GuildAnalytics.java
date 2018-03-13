package stream.flarebot.flarebot.analytics;

import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.util.general.GeneralUtils;

/**
 * This data is for basic tracking of our guilds, it is for use in finding out how big our guilds are generally
 * and who the "big dogs" are in terms of size.
 * This will give us some info on the top 50 guilds so that we may create a better experience for them.
 * <p>
 * Data that is sent:
 * - Guild ID
 * - Guild name
 * - Guild region
 * - Guild size
 * - Guild users
 * - Guild bots
 * // No individual user or bot ID is ever sent.
 */
public class GuildAnalytics implements AnalyticSender {

    @Override
    public JSONObject processData() {
        JSONObject object = new JSONObject();
        JSONArray guildDataArray = new JSONArray();

        Getters.getGuildsCache().forEach(g -> {
            // Don't really want to loop massive guilds twice.
            int users = GeneralUtils.getGuildUserCount(g);
            JSONObject guildObj = new JSONObject()
                    .put("id", g.getId())
                    .put("name", g.getName())
                    .put("region", g.getRegionRaw())
                    .put("size", g.getMemberCache().size())
                    .put("users", users)
                    .put("bots", g.getMemberCache().size() - users);
            guildDataArray.put(guildObj);
        });

        object.put("data", guildDataArray);
        return object;
    }

    @Override
    public long dataDeliveryFrequency() {
        return TimeUnit.HOURS.toMillis(1);
    }

    @Override
    public String endpoint() {
        return "/guild-data";
    }

    @Override
    public boolean compressData() {
        return true;
    }
}

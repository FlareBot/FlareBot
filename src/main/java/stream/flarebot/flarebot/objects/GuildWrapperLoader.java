package stream.flarebot.flarebot.objects;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.cache.CacheLoader;
import com.google.gson.JsonParser;
import io.github.binaryoverload.JSONConfig;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.api.ApiRequester;
import stream.flarebot.flarebot.api.ApiRoute;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.permissions.Group;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.errorhandling.Markers;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuildWrapperLoader extends CacheLoader<String, GuildWrapper> {

    private List<Long> loadTimes = new CopyOnWriteArrayList<>();

    @Override
    public GuildWrapper load(String id) {
        long start = System.currentTimeMillis();
        ResultSet set =
                CassandraController.execute("SELECT data FROM " + FlareBotManager.instance().GUILD_DATA_TABLE + " WHERE guild_id = '"
                        + id + "'");
        GuildWrapper wrapper = null;
        Row row = set != null ? set.one() : null;
        try {
            if (row != null)
                wrapper = FlareBot.GSON.fromJson(row.getString("data"), GuildWrapper.class);
            else
                wrapper = new GuildWrapper(id);
        } catch (Exception e) {
            // MIGRATION: This is quite important! :D
            if (e.getMessage().contains("permission")) {
                if (row != null) {
                    String json = row.getString("data");
                    JSONConfig config =
                            new JSONConfig(new JsonParser().parse(json).getAsJsonObject());
                    if (config.getSubConfig("permissions.groups").isPresent()) {
                        List<Group> groups = new ArrayList<>();
                        JSONConfig config1 = config.getSubConfig("permissions.groups").get();
                        for (String s : config1.getKeys(false)) {
                            groups.add(FlareBot.GSON.fromJson(config1.getElement(s).get().getAsJsonObject().toString(), Group.class));
                        }
                        config.set("permissions.groups", groups);
                    }
                    wrapper =
                            FlareBot.GSON.fromJson(config.getObject().toString(), GuildWrapper.class);
                }
            } else {
                FlareBot.LOGGER.error(Markers.TAG_DEVELOPER, "Failed to load GuildWrapper!!\n" +
                        "Guild ID: " + id + "\n" +
                        "Guild JSON: " + (row != null ? row.getString("data") : "New guild data!") + "\n" +
                        "Error: " + e.getMessage(), e);
                return null;
            }
        }
        long total = (System.currentTimeMillis() - start);
        loadTimes.add(total);

        if (total >= 200) {
            Constants.getImportantLogChannel().sendMessage(MessageUtils.getEmbed()
                    .setColor(new Color(166, 0, 255)).setTitle("Long guild load time!", null)
                    .setDescription("Guild " + id + " loaded!").addField("Time", "Millis: " + System.currentTimeMillis()
                            + "\nTime: " + LocalDateTime.now().toString(), false)
                    .addField("Load time", total + "ms", false)
                    .build()).queue();
        }
        ApiRequester.requestAsync(ApiRoute.LOAD_TIME, new JSONObject().put("loadTime", total)
                .put("guildId", id));
        return wrapper;
    }

    public List<Long> getLoadTimes() {
        return loadTimes;
    }

}

package stream.flarebot.flarebot.objects;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.cache.CacheLoader;
import com.google.gson.JsonParser;
import io.github.binaryoverload.JSONConfig;
import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.permissions.Group;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.errorhandling.Markers;

import javax.annotation.ParametersAreNonnullByDefault;

public class GuildWrapperLoader extends CacheLoader<String, GuildWrapper> {

    private final JsonParser parser = new JsonParser();

    private List<Long> loadTimes = new CopyOnWriteArrayList<>();

    public static final char[] ALLOWED_SPECIAL_CHARACTERS = {'$', '_', ' ', '&', '%', '£', '!', '*', '@', '#', ':'};
    public static final Pattern ALLOWED_CHARS_REGEX = Pattern.compile("[^\\w" + new String(ALLOWED_SPECIAL_CHARACTERS) + "]");

    @Override
    @ParametersAreNonnullByDefault
    public GuildWrapper load(String id) {
        long start = System.currentTimeMillis();
        ResultSet set = CassandraController.execute("SELECT data FROM " + FlareBotManager.instance().GUILD_DATA_TABLE
                + " WHERE guild_id = '"  + id + "'");
        GuildWrapper wrapper;
        Row row = set != null ? set.one() : null;
        String json = null;
        JSONConfig data;
        try {
            if (row != null) {
                json = row.getString("data");

                if (json.isEmpty() || json.equalsIgnoreCase("null")) {
                    return new GuildWrapper(id);
                }

                data = new JSONConfig(parser.parse(json).getAsJsonObject(), '.', ALLOWED_SPECIAL_CHARACTERS);
                if (data.getLong("dataVersion").isPresent() && data.getLong("dataVersion").getAsLong() == 0)
                    data = firstMigration(data);

                wrapper = FlareBot.GSON.fromJson(data.getObject().toString(), GuildWrapper.class);
            } else
                return new GuildWrapper(id);
        } catch (Exception e) {
            if (json == null) {
                FlareBot.LOGGER.error(Markers.TAG_DEVELOPER, "Failed to load GuildWrapper!!\n" +
                        "Guild ID: " + id + "\n" +
                        "Guild JSON: " + (row != null ? row.getString("data") : "New guild data!") + "\n" +
                        "Error: " + e.getMessage(), e);
                return null;
            }

            try {
                data = new JSONConfig(parser.parse(json).getAsJsonObject(), '.', ALLOWED_SPECIAL_CHARACTERS);
            } catch (Exception e1) {
                FlareBot.LOGGER.error(Markers.TAG_DEVELOPER, "Failed to load GuildWrapper!!\n" +
                        "Guild ID: " + id + "\n" +
                        "Guild JSON: " + json + "\n" +
                        "Error: " + e.getMessage(), e);
                throw new IllegalArgumentException("Invalid JSON! '" + json + "'", e1);
            }
            if (!data.getLong("dataVersion").isPresent()) {
                try {
                    data = firstMigration(data);
                } catch (Exception e1) {
                    FlareBot.LOGGER.error(Markers.TAG_DEVELOPER, "Failed to load GuildWrapper!!\n" +
                            "Guild ID: " + id + "\n" +
                            "Guild JSON: " + json + "\n" +
                            "Error: " + e.getMessage(), e);
                    throw new IllegalArgumentException("Invalid JSON! '" + json + "'", e1);
                }
                data.set("dataVersion", 1);
            }

            long version = data.getLong("dataVersion").getAsLong();
            if (version != GuildWrapper.DATA_VERSION) {
                // Migrations
            }
            json = data.getObject().toString();

            try {
                wrapper = FlareBot.GSON.fromJson(json, GuildWrapper.class);
            } catch (Exception e1) {
                FlareBot.LOGGER.error(Markers.TAG_DEVELOPER, "Failed to load GuildWrapper!!\n" +
                        "Guild ID: " + id + "\n" +
                        "Guild JSON: " + json + "\n" +
                        "Error: " + e1.getMessage(), e1);
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
        return wrapper;
    }

    public List<Long> getLoadTimes() {
        return loadTimes;
    }

    private JSONConfig firstMigration(JSONConfig data) {
        if (data.getSubConfig("permissions.groups").isPresent()) {
            List<Group> groups = new ArrayList<>();
            data.setAllowedSpecialCharacters(ALLOWED_SPECIAL_CHARACTERS);
            JSONConfig config = data.getSubConfig("permissions.groups").get();
            for (String s : config.getKeys(false)) {
                if (config.getElement(s).isPresent()) {
                    groups.add(FlareBot.GSON.fromJson(config.getElement(s).get().getAsJsonObject().toString(),
                            Group.class));
                }
            }
            data.set("permissions.groups", groups);
        }
        return data;
    }
}

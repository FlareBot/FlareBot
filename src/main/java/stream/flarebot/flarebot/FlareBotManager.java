package stream.flarebot.flarebot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.binaryoverload.JSONConfig;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;
import stream.flarebot.flarebot.api.ApiRequester;
import stream.flarebot.flarebot.api.ApiRoute;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.GuildWrapperBuilder;
import stream.flarebot.flarebot.util.ExpiringMap;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.database.SQLController;

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FlareBotManager {

    private static FlareBotManager instance;

    public static final Gson GSON = new GsonBuilder().create();
    private Map<Language.Locales, JSONConfig> configs = new ConcurrentHashMap<>();

    private ExpiringMap<String, GuildWrapper> guilds = new ExpiringMap<>(TimeUnit.MINUTES.toMillis(15));

    public FlareBotManager() {
        instance = this;
    }

    public static FlareBotManager getInstance() {
        return instance;
    }

    public void executeCreations() {
        try {
            SQLController.runSqlTask(conn -> {
                conn.createStatement().execute("CREATE TABLE IF NOT EXISTS playlist (\n" +
                        "  playlist_name  VARCHAR(60),\n" +
                        "  guild VARCHAR(20),\n" +
                        "  owner VARCHAR(20),\n" +
                        "  list  TEXT,\n" +
                        "  scope  VARCHAR(7) DEFAULT 'local',\n" +
                        "  PRIMARY KEY(playlist_name, guild)\n" +
                        ")");
                conn.createStatement()
                        .execute("CREATE TABLE IF NOT EXISTS selfassign (guild_id VARCHAR(20) PRIMARY KEY NOT NULL, roles TEXT)");
                conn.createStatement()
                        .execute("CREATE TABLE IF NOT EXISTS automod (guild_id VARCHAR(20) PRIMARY KEY NOT NULL, automod_data TEXT)");
                conn.createStatement()
                        .execute("CREATE TABLE IF NOT EXISTS localisation (guild_id VARCHAR(20) PRIMARY KEY NOT NULL, locale TEXT)");
            });
        } catch (SQLException e) {
            FlareBot.LOGGER.error("Database error!", e);
        }
    }

    public void savePlaylist(TextChannel channel, String owner, String name, String list) {
        try {
            SQLController.runSqlTask(connection -> {
                PreparedStatement exists = connection
                        .prepareStatement("SELECT * FROM playlist WHERE playlist_name = ? AND guild = ?");
                exists.setString(1, name);
                exists.setString(2, channel.getGuild().getId());
                exists.execute();
                if (exists.getResultSet().isBeforeFirst()) {
                    channel.sendMessage("That name is already taken!").queue();
                    return;
                }
                PreparedStatement statement = connection
                        .prepareStatement("INSERT INTO playlist (playlist_name, guild, owner, list) VALUES (" +
                                "   ?," +
                                "   ?," +
                                "   ?," +
                                "   ?" +
                                ")");
                statement.setString(1, name);
                statement.setString(2, channel.getGuild().getId());
                statement.setString(3, owner);
                statement.setString(4, list);
                statement.executeUpdate();
                channel.sendMessage(MessageUtils.getEmbed(FlareBot.getInstance().getUserByID(owner))
                        .setDescription("Success!").build()).queue();
            });
        } catch (SQLException e) {
            FlareBot.reportError(channel, "The playlist could not be saved!", e);
            FlareBot.LOGGER.error("Database error!", e);
        }
    }

    public JSONConfig loadLang(Language.Locales l) {
        return configs.computeIfAbsent(l, locale -> new JSONConfig(getClass().getResourceAsStream("/langs/" + l.getCode() + ".json")));
    }

    public String getLang(Language lang, String id) {
        String path = lang.name().toLowerCase().replaceAll("_", ".");
        JSONConfig config = loadLang(getGuild(id).getLocale());
        return config.getString(path).isPresent() ? config.getString(path).get() : "";
    }

    public String loadPlaylist(TextChannel channel, User sender, String name) {
        final String[] list = new String[1];
        try {
            SQLController.runSqlTask(connection -> {
                PreparedStatement exists = connection
                        .prepareStatement("SELECT list FROM playlist WHERE (playlist_name = ? AND guild = ?) " +
                                "OR (playlist_name=? AND scope = 'global')");
                exists.setString(1, name);
                exists.setString(2, channel.getGuild().getId());
                exists.setString(3, channel.getGuild().getId());
                exists.execute();
                ResultSet set = exists.getResultSet();
                if (set.next()) {
                    list[0] = set.getString("list");
                } else
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("*That playlist does not exist!*").build()).queue();
            });
        } catch (SQLException e) {
            FlareBot.reportError(channel, "Unable to load the playlist!", e);
            FlareBot.LOGGER.error("Database error!", e);
        }
        return list[0];
    }

    public Set<String> getProfanity() {
        // TODO: This will need to be done at some point. Not sure if I want to get this from the API or if I want to have a JSON file or something yet.
        return new HashSet<>();
    }

    public GuildWrapper getGuild(String id) {
        ApiRequester.request(ApiRoute.LOAD_TIME).setBody(new JSONObject().put("load_time", guilds.getValue(id))).sendAsync();
        if(!guilds.containsKey(id))
            FlareBot.getInstance().getChannelByID("242297848123621376").sendMessage(MessageUtils.getEmbed().setColor(Color.MAGENTA).setTitle("Guild loaded!", null)
                    .setDescription("Guild " + id + " loaded!").addField("Time", "Millis: " + System.currentTimeMillis() + "\nTime: " + LocalDateTime.now().toString(), false)
                    .build()).queue();
            guilds.computeIfAbsent(id, guildId -> new GuildWrapperBuilder(id).build());
        return guilds.get(id);
    }

    public ExpiringMap<String, GuildWrapper> getGuilds() {
        return guilds;
    }
}

package stream.flarebot.flarebot;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import io.github.binaryoverload.JSONConfig;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.GuildWrapperBuilder;
import stream.flarebot.flarebot.util.ExpiringMap;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FlareBotManager {

    private static FlareBotManager instance;

    private Map<Language.Locales, JSONConfig> configs = new ConcurrentHashMap<>();

    private ExpiringMap<String, GuildWrapper> guilds = new ExpiringMap<>(TimeUnit.MINUTES.toMillis(15));

    public FlareBotManager() {
        instance = this;
    }

    public static FlareBotManager getInstance() {
        return instance;
    }

    public void executeCreations() {
        CassandraController.executeAsync("CREATE TABLE IF NOT EXISTS flarebot.guild_data (" +
                "guild_id varchar, " +
                "data text, " +
                "last_retrieved timestamp, " +
                "PRIMARY KEY(guild_id, last_retrieved)) " +
                "WITH CLUSTERING ORDER BY (last_retrieved DESC)");
        CassandraController.executeAsync("CREATE TABLE IF NOT EXISTS flarebot.playlist (" +
                "playlist_name varchar, " +
                "guild_id varchar, " +
                "owner varchar, " +
                "songs list<varchar>, " +
                "scope varchar, " +
                "times_played int, " +
                "PRIMARY KEY(playlist_name, guild_id))");

                // Can't seem to cluster this because times_played is a bitch to have as a primary key.
                //"PRIMARY KEY((playlist_name, guild_id), times_played)) " +
                //"WITH CLUSTERING ORDER BY (times_played DESC)");
    }

    public void savePlaylist(TextChannel channel, String owner, String name, List<String> songs) {
        CassandraController.runTask(session -> {
            PreparedStatement exists = session
                    .prepare("SELECT * FROM flarebot.playlist WHERE playlist_name = ? AND guild_id = ?");
            ResultSet set = session.execute(exists.bind().setString(0, name).setString(1, channel.getGuild().getId()));
            if (set.one() != null) {
                channel.sendMessage("That name is already taken!").queue();
                return;
            }
            session.execute(session.prepare("INSERT INTO flarebot.playlist (playlist_name, guild_id, owner, songs, " +
                    "scope, times_played) VALUES (?, ?, ?, ?, ?, ?)").bind()
                    .setString(0, name).setString(1, channel.getGuild().getId()).setString(2, owner).setList(3, songs)
                    .setString(4, "local").setInt(5, 0));
            channel.sendMessage(MessageUtils.getEmbed(FlareBot.getInstance().getUserByID(owner))
                    .setDescription("Successfully saved the playlist '" + MessageUtils.escapeMarkdown(name) + "'").build()).queue();
        });
    }

    public JSONConfig loadLang(Language.Locales l) {
        return configs.computeIfAbsent(l, locale -> new JSONConfig(getClass().getResourceAsStream("/langs/" + l.getCode() + ".json")));
    }

    public String getLang(Language lang, String id) {
        String path = lang.name().toLowerCase().replaceAll("_", ".");
        JSONConfig config = loadLang(getGuild(id).getLocale());
        return config.getString(path).isPresent() ? config.getString(path).get() : "";
    }

    public ArrayList<String> loadPlaylist(TextChannel channel, User sender, String name) {
        final ArrayList<String> list = new ArrayList<>();
        CassandraController.runTask(session -> {
            ResultSet set = session.execute(session
                    .prepare("SELECT songs FROM flarebot.playlist WHERE playlist_name = ?").bind()
            .setString(0, name));

            //Row row = set.one();
            //System.out.println(row);
            Row row = set.one();
            if (row != null) {
                list.addAll(row.getList("songs", String.class));
            } else
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("*That playlist does not exist!*").build()).queue();
        });
        return list;
    }

    public Set<String> getProfanity() {
        // TODO: This will need to be done at some point. Not sure if I want to get this from the API or if I want to have a JSON file or something yet.
        return new HashSet<>();
    }

    public synchronized GuildWrapper getGuild(String id) {
        //ApiRequester.requestAsync(ApiRoute.LOAD_TIME, new JSONObject().put("load_time", guilds.getValue(id)), new EmptyCallback());
        if (!guilds.containsKey(id))
            FlareBot.getInstance().getChannelByID("242297848123621376").sendMessage(MessageUtils.getEmbed().setColor(Color.MAGENTA).setTitle("Guild loaded!", null)
                    .setDescription("Guild " + id + " loaded!").addField("Time", "Millis: " + System.currentTimeMillis() + "\nTime: " + LocalDateTime.now().toString(), false)
                    .build()).queue();
        guilds.computeIfAbsent(id, guildId -> {
            ResultSet set = CassandraController.execute("SELECT data FROM flarebot.guild_data WHERE guild_id = '" + guildId + "'");
            if(set.one() != null)
                return FlareBot.GSON.fromJson(set.one().getString("data"), GuildWrapper.class);
            else
                return new GuildWrapperBuilder(id).build();
        });
        return guilds.get(id);
    }

    public ExpiringMap<String, GuildWrapper> getGuilds() {
        return guilds;
    }

}

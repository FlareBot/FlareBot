package stream.flarebot.flarebot;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import io.github.binaryoverload.JSONConfig;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.api.ApiRequester;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.database.CassandraTask;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.GuildWrapperBuilder;
import stream.flarebot.flarebot.scheduler.FlarebotTask;
import stream.flarebot.flarebot.util.expiringmap.ExpiredEvent;
import stream.flarebot.flarebot.util.expiringmap.ExpiringMap;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class FlareBotManager {

    private static FlareBotManager instance;

    private Map<Language.Locales, JSONConfig> configs = new ConcurrentHashMap<>();
    // Command - reason
    private Map<String, String> disabledCommands = new ConcurrentHashMap<>();

    private ExpiringMap<String, GuildWrapper> guilds;
    private final long GUILD_EXPIRE = TimeUnit.MINUTES.toMillis(15);
    private final long INACTIVITY_CHECK = TimeUnit.MINUTES.toMillis(2);

    private final String GUILD_DATA_TABLE;

    private PreparedStatement saveGuildStatement;

    public FlareBotManager() {
        instance = this;
        GUILD_DATA_TABLE = (FlareBot.getInstance().isTestBot() ? "flarebot.guild_data_test" : "flarebot.guild_data");
    }

    public static FlareBotManager getInstance() {
        return instance;
    }

    public void executeCreations() {
        // Note to self: Figure out a way to order this but where I can still update the damn last_retrieved key
        CassandraController.executeAsync("CREATE TABLE IF NOT EXISTS " + GUILD_DATA_TABLE + " (" +
                "guild_id varchar, " +
                "data text, " +
                "last_retrieved timestamp, " +
                "PRIMARY KEY(guild_id))");
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

        initGuildSaving();
    }

    private void initGuildSaving() {
        guilds = new ExpiringMap<>(GUILD_EXPIRE, new ExpiredEvent<String, GuildWrapper>() {
            @Override
            public void run(String guildId, GuildWrapper guildWrapper, long expired, long last_retrieved) {
                //ApiRequester.requestAsync(ApiRoute.UNLOAD, );
                if((System.currentTimeMillis() - INACTIVITY_CHECK) > guilds.getLastRetrieved(guildId))
                    saveGuild(guildId, guildWrapper, last_retrieved, true);
                else {
                    setCancelled(true);
                    guilds.resetTime(guildId);
                    saveGuild(guildId, guildWrapper, last_retrieved, false);
                }
            }
        });
        new FlarebotTask() {
            @Override
            public void run() {
                guilds.purge();
            }
        }.repeat(30_000, 30_000);
    }

    // Do not use this method!
    protected void saveGuild(String guildId, GuildWrapper guildWrapper, final long last_retrieved, boolean unload) {
        long last_r = (last_retrieved == -1 ? System.currentTimeMillis() : last_retrieved);
        CassandraController.runTask(session -> {
            if(saveGuildStatement == null) saveGuildStatement = session.prepare("UPDATE " + GUILD_DATA_TABLE
                    + " SET last_retrieved = ?, data = ? WHERE guild_id = ?");
            session.executeAsync(saveGuildStatement.bind()
                    .setTimestamp(0, new Date(last_r))
                    .setString(1, FlareBot.GSON.toJson(guildWrapper)).setString(2, guildId));
        });
        FlareBot.LOGGER.info("Guild " + guildId + "'s data got saved! Last retrieved: " + last_r
                + " (" + new Date(last_r) + ")");
        if(unload)
            FlareBot.getInstance().getChannelByID(FlareBot.GUILD_LOG).sendMessage(MessageUtils.getEmbed()
                    .setColor(Color.MAGENTA).setTitle("Guild unloaded!", null)
                    .setDescription("Guild " + guildId + " unloaded!").addField("Time", "Millis: " + System.currentTimeMillis()
                            + "\nTime: " + LocalDateTime.now().toString() + "\nLast retrieved: " + last_r, false)
                    .build()).queue();
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
        guilds.computeIfAbsent(id, guildId -> {
            long start = System.nanoTime();
            ResultSet set = CassandraController.execute("SELECT data FROM " + GUILD_DATA_TABLE + " WHERE guild_id = '"
                    + guildId + "'");
            GuildWrapper wrapper;
            Row row = set != null ? set.one() : null;
            if(row != null)
                wrapper = FlareBot.GSON.fromJson(row.getString("data"), GuildWrapper.class);
            else
                wrapper = new GuildWrapperBuilder(id).build();
            long total = (System.nanoTime() - start);
            long millis = total / 1000000;
            FlareBot.getInstance().getChannelByID(FlareBot.GUILD_LOG).sendMessage(MessageUtils.getEmbed()
                    .setColor(new Color(166, 0, 255)).setTitle("Guild loaded!", null)
                    .setDescription("Guild " + id + " loaded!").addField("Time", "Millis: " + System.currentTimeMillis()
                            + "\nTime: " + LocalDateTime.now().toString(), false)
                    .addField("Load time", millis + "ms (" + total + "ns)", false)
                    .build()).queue();
            return wrapper;
        });
        return guilds.get(id);
    }

    public ExpiringMap<String, GuildWrapper> getGuilds() {
        return guilds;
    }

    public boolean isCommandDisabled(String command) {
        return disabledCommands.containsKey(command);
    }

    public String getDisabledCommandReason(String command) {
        return this.disabledCommands.get(command);
    }

    public boolean toggleCommand(String command, String reason) {
        return disabledCommands.containsKey(command) ? disabledCommands.remove(command) != null :
                disabledCommands.put(command, reason) != null;
    }

    public Map<String, String> getDisabledCommands() {
        return disabledCommands;
    }
}

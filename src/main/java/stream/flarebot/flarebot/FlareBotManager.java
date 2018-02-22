package stream.flarebot.flarebot;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.Runnables;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import stream.flarebot.flarebot.annotations.DoNotUse;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.GuildWrapperLoader;
import stream.flarebot.flarebot.util.ConfirmUtil;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.objects.RunnableWrapper;

public class FlareBotManager {

    private final Logger LOGGER = FlareBot.getLog(getClass());

    private static FlareBotManager instance;

    // Command - reason
    private Map<String, String> disabledCommands = new ConcurrentHashMap<>();

    private Map<Long, Long> lastActive = new ConcurrentHashMap<>();
    private GuildWrapperLoader guildWrapperLoader = new GuildWrapperLoader();

    private LoadingCache<String, GuildWrapper> guilds;
    private final long GUILD_EXPIRE = TimeUnit.MINUTES.toMillis(15);
    private final long INACTIVITY_CHECK = TimeUnit.MINUTES.toMillis(2);

    public final String GUILD_DATA_TABLE;

    private PreparedStatement saveGuildStatement;
    private PreparedStatement loadPlaylistStatement;
    private PreparedStatement savePlaylistStatement;
    private PreparedStatement insertPlaylistStatement;

    public FlareBotManager() {
        instance = this;
        GUILD_DATA_TABLE = (FlareBot.instance().isTestBot() ? "flarebot.guild_data_test" : "flarebot.guild_data");
        FlareBot.LOGGER.info("Using " + (FlareBot.instance().isTestBot() ? "test bot data" : "production bot data"));
    }

    public static FlareBotManager instance() {
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

        // Also used in FutureAction - Make sure to update if changes are done.
        CassandraController.executeAsync("CREATE TABLE IF NOT EXISTS future_tasks (" +
                "guild_id bigint, " +
                "channel_id bigint, " +
                "responsible bigint, " +
                "target bigint, " +
                "content text, " +
                "expires_at timestamp, " +
                "created_at timestamp, " +
                "action varchar, " +
                "PRIMARY KEY(guild_id, channel_id, created_at))");

        initGuildSaving();
    }

    private void initGuildSaving() {
        guilds = CacheBuilder.newBuilder()
                .expireAfterAccess(GUILD_EXPIRE, TimeUnit.MILLISECONDS)
                .removalListener((RemovalListener<String, GuildWrapper>) removalNotification -> saveGuild(removalNotification.getKey(), removalNotification.getValue(), -1))
                .build(guildWrapperLoader);
    }

    // Do not use this method!
    @DoNotUse(expressUse = "GuildCommand save")
    public void saveGuild(String guildId, GuildWrapper guildWrapper, final long last_retrieved) {
        long last_r = (last_retrieved == -1 ? System.currentTimeMillis() : last_retrieved);
        CassandraController.runTask(session -> {
            if (saveGuildStatement == null) saveGuildStatement = session.prepare("UPDATE " + GUILD_DATA_TABLE
                    + " SET last_retrieved = ?, data = ? WHERE guild_id = ?");
            session.executeAsync(saveGuildStatement.bind()
                    .setTimestamp(0, new Date(last_r))
                    .setString(1, FlareBot.GSON.toJson(guildWrapper)).setString(2, guildId));
        });
        LOGGER.debug("Guild " + guildId + "'s data got saved! Last retrieved: " + last_r
                + " (" + new Date(last_r) + ") - " + guilds.size() + " currently loaded.");
    }

    public void savePlaylist(Command command, TextChannel channel, String ownerId, boolean overwriteAllowed, String name, List<String> songs) {
        CassandraController.runTask(session -> {
            if (savePlaylistStatement == null)
                savePlaylistStatement = session.prepare("SELECT * FROM flarebot.playlist " +
                        "WHERE playlist_name = ? AND guild_id = ?");

            ResultSet set =
                    session.execute(savePlaylistStatement.bind().setString(0, name).setString(1, channel.getGuild().getId()));
            if (set.one() != null) {
                if (ConfirmUtil.checkExists(ownerId, command.getClass())) {
                    MessageUtils.sendWarningMessage("Overwriting playlist!", channel);
                } else if (!overwriteAllowed) {
                    MessageUtils.sendErrorMessage("That name is already taken! You need the `flarebot.queue.save.overwrite` permission to overwrite", channel);
                    return;
                } else {
                    MessageUtils.sendErrorMessage("That name is already taken! Do this again within 1 minute to overwrite!", channel);
                    ConfirmUtil.pushAction(ownerId, new RunnableWrapper(Runnables.doNothing(), command.getClass()));
                    return;
                }
            }
            if (insertPlaylistStatement == null)
                insertPlaylistStatement = session.prepare("INSERT INTO flarebot.playlist" +
                        " (playlist_name, guild_id, owner, songs, scope, times_played) VALUES (?, ?, ?, ?, ?, ?)");

            session.execute(insertPlaylistStatement.bind().setString(0, name).setString(1, channel.getGuild().getId())
                    .setString(2, ownerId).setList(3, songs).setString(4, "local").setInt(5, 0));
            channel.sendMessage(MessageUtils.getEmbed(Getters.getUserById(ownerId))
                    .setDescription("Successfully saved the playlist '" + MessageUtils.escapeMarkdown(name) + "'").build()).queue();
        });
    }

    public ArrayList<String> loadPlaylist(TextChannel channel, User sender, String name) {
        final ArrayList<String> list = new ArrayList<>();
        CassandraController.runTask(session -> {
            if (loadPlaylistStatement == null) loadPlaylistStatement = session.prepare("SELECT songs FROM " +
                    "flarebot.playlist WHERE playlist_name = ? AND guild_id = ?");

            ResultSet set =
                    session.execute(loadPlaylistStatement.bind().setString(0, name).setString(1, channel.getGuild().getId()));
            Row row = set.one();
            if (row != null) {
                list.addAll(row.getList("songs", String.class));
            } else
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("That playlist does not exist!").build()).queue();
        });
        return list;
    }

    public synchronized GuildWrapper getGuild(String id) {
        if (guilds == null) return null; //This is if it's ran before even being loaded
        try {
            return guilds.get(id);
        } catch (ExecutionException e) {
            LOGGER.error("Failed to load guild from cache!", e);
            return null;
        }
    }

    public GuildWrapper getGuildNoCache(String id) {
        if (guilds == null) return null; //This is if it's ran before even being loaded
        guilds.invalidate(id);
        try {
            return guilds.get(id);
        } catch (ExecutionException e) {
            LOGGER.error("Failed to load guild from cache!", e);
            return null;
        }
    }

    public LoadingCache<String, GuildWrapper> getGuilds() {
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

    public GuildWrapperLoader getGuildWrapperLoader() {
        return guildWrapperLoader;
    }

    public Map<Long, Long> getLastActive() {
        return lastActive;
    }
}

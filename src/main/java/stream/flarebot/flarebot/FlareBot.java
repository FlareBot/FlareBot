package stream.flarebot.flarebot;

import ch.qos.logback.classic.Level;
import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.arsenarsen.lavaplayerbridge.utils.JDAMultiShard;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import io.github.binaryoverload.JSONConfig;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.IllegalStateException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import stream.flarebot.flarebot.analytics.ActivityAnalytics;
import stream.flarebot.flarebot.analytics.AnalyticsHandler;
import stream.flarebot.flarebot.analytics.GuildAnalytics;
import stream.flarebot.flarebot.analytics.GuildCountAnalytics;
import stream.flarebot.flarebot.api.ApiRequester;
import stream.flarebot.flarebot.api.ApiRoute;
import stream.flarebot.flarebot.api.GzipRequestInterceptor;
import stream.flarebot.flarebot.audio.PlayerListener;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.Prefixes;
import stream.flarebot.flarebot.commands.currency.ConvertCommand;
import stream.flarebot.flarebot.commands.currency.CurrencyCommand;
import stream.flarebot.flarebot.commands.general.*;
import stream.flarebot.flarebot.commands.informational.BetaCommand;
import stream.flarebot.flarebot.commands.informational.DonateCommand;
import stream.flarebot.flarebot.commands.moderation.*;
import stream.flarebot.flarebot.commands.moderation.mod.*;
import stream.flarebot.flarebot.commands.music.*;
import stream.flarebot.flarebot.commands.random.AvatarCommand;
import stream.flarebot.flarebot.commands.random.JumboCommand;
import stream.flarebot.flarebot.commands.secret.*;
import stream.flarebot.flarebot.commands.secret.internal.ChangelogCommand;
import stream.flarebot.flarebot.commands.secret.internal.PostUpdateCommand;
import stream.flarebot.flarebot.commands.useful.RemindCommand;
import stream.flarebot.flarebot.commands.useful.TagsCommand;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.database.RedisController;
import stream.flarebot.flarebot.music.QueueListener;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.scheduler.FlareBotTask;
import stream.flarebot.flarebot.scheduler.FutureAction;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.ConfirmUtil;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.ShardUtils;
import stream.flarebot.flarebot.util.WebUtils;
import stream.flarebot.flarebot.web.ApiFactory;
import stream.flarebot.flarebot.web.DataInterceptor;

public class FlareBot {

    private static final Map<String, Logger> LOGGERS;
    public static final Logger LOGGER;
    public static final String INVITE_URL = "https://discord.gg/TTAUGvZ";
    public static final char COMMAND_CHAR = '_';

    static {
        handleLogArchive();
        LOGGERS = new ConcurrentHashMap<>();
        LOGGER = getLog(FlareBot.class.getName());
    }

    private static FlareBot instance;
    private static String youtubeApi;

    private static JSONConfig config;
    private FlareBotManager manager;
    private static boolean apiEnabled = true;

    public static final Gson GSON = new GsonBuilder().create();

    public static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    public static final AtomicBoolean EXITING = new AtomicBoolean(false);

    private Map<String, PlayerCache> playerCache = new ConcurrentHashMap<>();

    private static boolean testBot = false;

    private Events events;
    private String version = null;
    private ShardManager shardManager;

    private Set<Command> commands = new ConcurrentHashSet<>();
    private PlayerManager musicManager;
    private long startTime;
    private static Prefixes prefixes;

    private static final DataInterceptor dataInterceptor = new DataInterceptor(DataInterceptor.RequestSender.JDA);
    private static final OkHttpClient client =
            new OkHttpClient.Builder().connectionPool(new ConnectionPool(4, 10, TimeUnit.SECONDS))
                    .addInterceptor(dataInterceptor).build();

    private AnalyticsHandler analyticsHandler;

    public static void main(String[] args) {
        Spark.port(8080);
        try {
            File file = new File("config.json");
            if (!file.exists() && !file.createNewFile())
                throw new IllegalStateException("Can't create config file!");
            try {
                config = new JSONConfig("config.json");
            } catch (NullPointerException e) {
                LOGGER.error("Invalid JSON!", e);
                System.exit(1);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to create config.json!", e);
            System.exit(1);
        }

        List<String> required = new ArrayList<>();
        required.add("bot.token");
        required.add("cassandra.username");
        required.add("cassandra.password");
        required.add("misc.yt");
        required.add("redis.host");
        required.add("redis.port");
        required.add("redis.password");
        required.add("sentry.dsn");

        boolean good = true;
        for (String req : required) {
            if (config.getString(req) != null) {
                if (!config.getString(req).isPresent()) {
                    good = false;
                    LOGGER.error("Missing required json " + req);
                }
            } else {
                good = false;
                LOGGER.error("Missing required json " + req);
            }
        }

        if (!good) {
            LOGGER.error("One or more of the required JSON objects where missing. Exiting to prevent problems");
            System.exit(1);
        }

        new CassandraController(config);
        new RedisController(config);

        FlareBot.youtubeApi = config.getString("misc.yt").get();

        if (config.getArray("options").isPresent()) {
            for (JsonElement em : config.getArray("options").get()) {
                if (em.getAsString() != null) {
                    if (em.getAsString().equals("tb")) {
                        FlareBot.testBot = true;
                    }
                    if (em.getAsString().equals("debug")) {
                        ((ch.qos.logback.classic.Logger) LoggerFactory
                                .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME))
                                .setLevel(Level.DEBUG);
                    }
                }
            }
        }

        SentryClient sentryClient =
                Sentry.init(config.getString("sentry.dsn").get() + "?stacktrace.app.packages=stream.flarebot.flarebot");
        sentryClient.setEnvironment(testBot ? "TestBot" : "Production");
        sentryClient.setServerName(testBot ? "Test Server" : "Production Server");
        sentryClient.setRelease(GitHandler.getLatestCommitId());

        if (!config.getString("misc.apiKey").isPresent() || config.getString("misc.apiKey").get().isEmpty())
            apiEnabled = false;

        Thread.setDefaultUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        Thread.currentThread()
                .setUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        try {
            (instance = new FlareBot()).init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    public static OkHttpClient getOkHttpClient() {
        return client;
    }

    public Events getEvents() {
        return events;
    }

    public static Prefixes getPrefixes() {
        return prefixes;
    }

    public void init() throws InterruptedException {
        LOGGER.info("Starting init!");
        manager = new FlareBotManager();
        RestAction.DEFAULT_FAILURE = t -> {
        };
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        events = new Events(this);
        LOGGER.info("Starting builders");

        try {
            shardManager = new DefaultShardManagerBuilder()
                    .addEventListeners(events)
                    .addEventListeners(new ModlogEvents())
                    .setToken(config.getString("bot.token").get())
                    .setAudioSendFactory(new NativeAudioSendFactory())
                    .setShardsTotal(-1)
                    //.setGameProvider(shardId -> setStatus("_help | _invite", shardId))
                    .setHttpClientBuilder(client.newBuilder())
                    .setBulkDeleteSplittingEnabled(false)
                    .build();

            prefixes = new Prefixes();
            commands = new ConcurrentHashSet<>();
        } catch (Exception e) {
            LOGGER.error("Could not log in!", e);
            Thread.sleep(500);
            System.exit(1);
            return;
        }
        System.setErr(new PrintStream(new OutputStream() {
            // Nothing really so all good.
            @Override
            public void write(int b) {
            }
        })); // No operation STDERR. Will not do much of anything, except to filter out some Jsoup spam

        manager = new FlareBotManager();
        manager.executeCreations();
    }

    protected void run() {
        if (RUNNING.getAndSet(true))
            return;
        LOGGER.info("Starting run sequence");
        try {
            musicManager =
                    PlayerManager.getPlayerManager(LibraryFactory.getLibrary(new JDAMultiShard(shardManager)));
        } catch (UnknownBindingException e) {
            LOGGER.error("Failed to initialize musicManager", e);
        }
        musicManager.getPlayerCreateHooks()
                .register(player -> player.getQueueHookManager().register(new QueueListener()));

        /* Any migration
        MigrationHandler migrationHandler = new MigrationHandler();
        migrationHandler.migrateSinglePermissionForAllGuilds("flarebot.playlist", "flarebot.queue");*/

        registerCommand(new HelpCommand());
        registerCommand(new SearchCommand());
        registerCommand(new JoinCommand());
        registerCommand(new LeaveCommand());
        registerCommand(new InfoCommand());
        registerCommand(new BetaCommand());
        registerCommand(new DonateCommand());
        registerCommand(new ResumeCommand());
        registerCommand(new PlayCommand());
        registerCommand(new PauseCommand());
        registerCommand(new StopCommand());
        registerCommand(new SkipCommand());
        registerCommand(new ShuffleCommand());
        registerCommand(new QueueCommand());
        registerCommand(new SongCommand());
        registerCommand(new InviteCommand());
        registerCommand(new AutoAssignCommand());
        registerCommand(new QuitCommand());
        registerCommand(new RolesCommand());
        registerCommand(new WelcomeCommand());
        registerCommand(new PermissionsCommand());
        registerCommand(new UpdateCommand());
        registerCommand(new LogsCommand());
        registerCommand(new LoopCommand());
        registerCommand(new LoadCommand());
        registerCommand(new SaveCommand());
        registerCommand(new DeleteCommand());
        registerCommand(new PlaylistsCommand());
        registerCommand(new SeekCommand());
        registerCommand(new PurgeCommand());
        registerCommand(new EvalCommand());
        registerCommand(new MusicAnnounceCommand());
        registerCommand(new SetPrefixCommand());
        registerCommand(new ChangeAvatarCommand());
        registerCommand(new RandomCommand());
        registerCommand(new UserInfoCommand());
        registerCommand(new PinCommand());
        registerCommand(new ShardRestartCommand());
        registerCommand(new QueryCommand());
        registerCommand(new SelfAssignCommand());
        registerCommand(new WarnCommand());
        registerCommand(new WarningsCommand());
        registerCommand(new CommandUsageCommand());
        registerCommand(new CurrencyCommand());
        registerCommand(new ConvertCommand());

//        registerCommand(new AutoModCommand());
        registerCommand(new ModlogCommand());

        registerCommand(new TestCommand());

        registerCommand(new KickCommand());
        registerCommand(new ForceBanCommand());
        registerCommand(new BanCommand());
        registerCommand(new TempBanCommand());
        registerCommand(new UnbanCommand());
        registerCommand(new MuteCommand());
        registerCommand(new TempMuteCommand());
        registerCommand(new UnmuteCommand());
        registerCommand(new LockChatCommand());

        registerCommand(new ReportsCommand());
        registerCommand(new ReportCommand());
        registerCommand(new ShardInfoCommand());
        registerCommand(new SongNickCommand());
        registerCommand(new StatsCommand());
        registerCommand(new PruneCommand());
        registerCommand(new ServerInfoCommand());
        registerCommand(new FixCommand());
        registerCommand(new GuildCommand());
        registerCommand(new RepeatCommand());
        registerCommand(new DisableCommandCommand());

        registerCommand(new TagsCommand());
        registerCommand(new PostUpdateCommand());
        registerCommand(new StatusCommand());
        registerCommand(new RemindCommand());
        registerCommand(new AvatarCommand());
        registerCommand(new UpdateJDACommand());
        registerCommand(new ChangelogCommand());
        registerCommand(new JumboCommand());

        registerCommand(new NINOCommand());

        registerCommand(new DebugCommand());

        LOGGER.info("Loaded " + commands.size() + " commands!");

        ApiFactory.bind();
        LOGGER.info("Bound API");

        musicManager.getPlayerCreateHooks().register(player -> player.addEventListener(new PlayerListener(player)));

        analyticsHandler = new AnalyticsHandler();
        analyticsHandler.registerAnalyticSender(new ActivityAnalytics());
        analyticsHandler.registerAnalyticSender(new GuildAnalytics());
        analyticsHandler.registerAnalyticSender(new GuildCountAnalytics());
        analyticsHandler.run(isTestBot() ? 1000 : -1);

        GeneralUtils.methodErrorHandler(LOGGER, null,
                "Executed creations!", "Failed to execute creations!",
                () -> manager.executeCreations());

        GeneralUtils.methodErrorHandler(LOGGER, null,
                "Loaded future tasks!", "Failed to load future tasks!",
                this::loadFutureTasks);

        startTime = System.currentTimeMillis();
        LOGGER.info("FlareBot v" + getVersion() + " booted!");

        GeneralUtils.methodErrorHandler(LOGGER, null,
                "Sent commands to site!", "Failed to send commands to site!",
                this::sendCommands);

        GeneralUtils.methodErrorHandler(LOGGER, "Starting tasks!",
                "Started all tasks, run complete!", "Failed to start all tasks!",
                this::runTasks);

    } 
    
    /**
     * This possibly-null will return the first connected JDA shard.
     * This means that a lot of methods like sending embeds works even with shard 0 offline.
     *
     * @return The first possible JDA shard which is connected or null otherwise.
     */
    @Nullable
    public JDA getClient() {
        for (JDA jda : shardManager.getShardCache()) {
            if (jda.getStatus() == JDA.Status.CONNECTED)
                return jda;
        }
        return null;
    }

    /**
     * Get the SelfUser of the bot, this will be null if no shards are connected.
     *
     * @return The bot SelfUser or null if no CONNECTED shard is found.
     */
    @Nullable
    public SelfUser getSelfUser() {
        JDA shard = getClient();
        return shard == null ? null : shard.getSelfUser();
    }

    private void loadFutureTasks() {
        final int[] loaded = {0};
        CassandraController.runTask(session -> {
            ResultSet set = session.execute("SELECT * FROM flarebot.future_tasks");
            Row row;
            while ((row = set.one()) != null) {
                FutureAction fa =
                        new FutureAction(row.getLong("guild_id"), row.getLong("channel_id"), row.getLong("responsible"),
                                row.getLong("target"), row.getString("content"), new DateTime(row.getTimestamp("expires_at")),
                                new DateTime(row.getTimestamp("created_at")),
                                FutureAction.Action.valueOf(row.getString("action").toUpperCase()));
                try {
                if (new DateTime().isAfter(fa.getExpires()))
                    fa.execute();
                else {
                    fa.queue();
                    loaded[0]++;
                }
                } catch (NullPointerException e) {
                    LOGGER.error("Failed to execute/queue future task"
                             + "\nAction: " + fa.getAction() + "\nResponsible: " + fa.getResponsible() 
                             + "\nTarget: " + fa.getTarget() + "\nContent: " + fa.getContent(), e);
                }
            }
        });

        LOGGER.info("Loaded " + loaded[0] + " future tasks");
    }

    // TODO: Spread this out a little so we don't just burst.
    private void postToBotList(String auth, String url) {
        for (JDA client : shardManager.getShards()) {
            if (shardManager.getShardsTotal() == 1) {
                Request.Builder request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", auth)
                        .addHeader("User-Agent", "Mozilla/5.0 FlareBot");
                RequestBody body = RequestBody.create(WebUtils.APPLICATION_JSON,
                        new JSONObject().put("server_count", client.getGuilds().size()).toString());
                WebUtils.postAsync(request.post(body));
                return;
            }
            try {
                Request.Builder request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", auth)
                        .addHeader("User-Agent", "Mozilla/5.0 FlareBot");
                RequestBody body = RequestBody.create(WebUtils.APPLICATION_JSON,
                        (new JSONObject()
                                .put("server_count", client.getGuilds().size())
                                .put("shard_id", client.getShardInfo().getShardId())
                                .put("shard_count", client.getShardInfo().getShardTotal()).toString()));
                WebUtils.postAsync(request.post(body));

                // Gonna spread these out just a bit so we don't burst (insert shard number here) requests all at once
                Thread.sleep(20_000);
            } catch (Exception e1) {
                FlareBot.LOGGER.error("Could not POST data to a botlist", e1);
            }
        }
        LOGGER.debug("Sent " + shardManager.getShardsTotal() + " requests to " + url);
    }

    public void scheduleUpdate() {
        new FlareBotTask("Auto-Update") {
            @Override
            public void run() {
                quit(true);
            }
        }.delay(LocalDateTime.now().until(LocalDate.now()
                .plusDays(LocalDateTime.now().getHour() >= 13 ? 1 : 0)
                .atTime(13, 0, 0), ChronoUnit.MILLIS));
    }

    private Runtime runtime = Runtime.getRuntime();

    private void sendData() {
        JSONObject data = new JSONObject()
                .put("guilds", getGuilds().size())
                //.put("loaded_guilds", FlareBotManager.getInstance().getGuilds().size())
                .put("official_guild_users", getGuildById(Constants.OFFICIAL_GUILD).getMembers().size())
                .put("text_channels", getChannels().size())
                .put("voice_channels", getVoiceChannels().size())
                .put("connected_voice_channels", getConnectedVoiceChannels())
                .put("active_voice_channels", getActiveVoiceChannels())
                .put("num_queued_songs", getGuilds().stream()
                        .mapToInt(guild -> musicManager.getPlayer(guild.getId())
                                .getPlaylist().size()).sum())
                .put("ram", (((runtime.totalMemory() - runtime.freeMemory()) / 1024) / 1024) + "MB")
                .put("uptime", getUptime())
                .put("http_requests", dataInterceptor.getRequests().intValue());

        ApiRequester.requestAsync(ApiRoute.UPDATE_DATA, data);
    }

    private void sendCommands() {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        for (Command cmd : commands) {
            JSONObject cmdObj = new JSONObject()
                    .put("command", cmd.getCommand())
                    .put("description", cmd.getDescription())
                    .put("permission", cmd.getPermission() == null ? "" : cmd.getPermission())
                    .put("type", cmd.getType().toString());
            JSONArray aliases = new JSONArray();
            for (String s : cmd.getAliases())
                aliases.put(s);
            cmdObj.put("aliases", aliases);
            array.put(cmdObj);
        }
        obj.put("commands", array);

        ApiRequester.requestAsync(ApiRoute.COMMANDS, obj);
    }

    public void quit(boolean update) {
        if (update) {
            LOGGER.info("Updating bot!");
            try {
                File git = new File("FlareBot" + File.separator);
                if (!(git.exists() && git.isDirectory())) {
                    LOGGER.info("Cloning git!");
                    ProcessBuilder clone =
                            new ProcessBuilder("git", "clone", "https://github.com/FlareBot/FlareBot.git", git
                                    .getAbsolutePath());
                    clone.redirectErrorStream(true);
                    Process p = clone.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String out = "";
                    String line;
                    if ((line = reader.readLine()) != null) {
                        out += line + '\n';
                    }
                    p.waitFor();
                    if (p.exitValue() != 0) {
                        LOGGER.error("Could not update!!!!\n" + out);
                        UpdateCommand.UPDATING.set(false);
                        return;
                    }
                } else {
                    LOGGER.info("Pulling git!");
                    ProcessBuilder builder = new ProcessBuilder("git", "pull");
                    builder.directory(git);
                    Process p = builder.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String out = "";
                    String line;
                    if ((line = reader.readLine()) != null) {
                        out += line + '\n';
                    }
                    p.waitFor();
                    if (p.exitValue() != 0) {
                        LOGGER.error("Could not update!!!!\n" + out);
                        UpdateCommand.UPDATING.set(false);
                        return;
                    }
                }
                LOGGER.info("Building!");
                ProcessBuilder maven = new ProcessBuilder("mvn", "clean", "package", "-e", "-U");
                maven.directory(git);
                Process p = maven.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String out = "";
                String line;
                if ((line = reader.readLine()) != null) {
                    out += line + '\n';
                }
                p.waitFor();
                if (p.exitValue() != 0) {
                    UpdateCommand.UPDATING.set(false);
                    LOGGER.error("Could not update! Log:** {} **", MessageUtils.paste(out));
                    return;
                }
                LOGGER.info("Replacing jar!");
                File current = new File(URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation()
                        .getPath(), "UTF-8")); // pfft this will go well..
                Files.copy(current.toPath(), Paths
                        .get(current.getPath().replace(".jar", ".backup.jar")), StandardCopyOption.REPLACE_EXISTING);
                File built = new File(git, "target" + File.separator + "FlareBot-jar-with-dependencies.jar");
                Files.copy(built.toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (InterruptedException | IOException e) {
                LOGGER.error("Could not update!", e);
                UpdateCommand.UPDATING.set(false);
            }
        } else
            LOGGER.info("Exiting.");
        stop();
        System.exit(0);
    }

    private void stop() {
        if (EXITING.get()) return;
        LOGGER.info("Saving data.");
        EXITING.set(true);
        getImportantLogChannel().sendMessage("Average load time of this session: " + manager.getLoadTimes()
                .stream().mapToLong(v -> v).average().orElse(0) + "\nTotal loads: " + manager.getLoadTimes().size())
                .complete();
        for (ScheduledFuture<?> scheduledFuture : Scheduler.getTasks().values())
            scheduledFuture.cancel(false); // No tasks in theory should block this or cause issues. We'll see
        for (JDA client : shardManager.getShards())
            client.removeEventListener(events); //todo: Make a replacement for the array
        sendData();
        for (String s : manager.getGuilds().keySet()) {
            manager.saveGuild(s, manager.getGuilds().get(s), manager.getGuilds().getLastRetrieved(s));
        }
        shardManager.shutdown();
        LOGGER.info("Finished saving!");
        for (JDA client : shardManager.getShards())
            client.shutdown();
    }

    private void registerCommand(Command command) {
        this.commands.add(command);
    }

    // https://bots.are-pretty.sexy/214501.png
    // New way to process commands, this way has been proven to be quicker overall.
    @Nullable
    public Command getCommand(String s, User user) {
        if (PerGuildPermissions.isCreator(user) || (isTestBot() && PerGuildPermissions.isContributor(user))) {
            for (Command cmd : getCommandsByType(CommandType.SECRET)) {
                if (cmd.getCommand().equalsIgnoreCase(s))
                    return cmd;
                for (String alias : cmd.getAliases())
                    if (alias.equalsIgnoreCase(s)) return cmd;
            }
        }
        for (Command cmd : getCommands()) {
            if (cmd.getType() == CommandType.SECRET) continue;
            if (cmd.getCommand().equalsIgnoreCase(s))
                return cmd;
            for (String alias : cmd.getAliases())
                if (alias.equalsIgnoreCase(s)) return cmd;
        }
        return null;
    }

    @Nonnull
    public Set<Command> getCommands() {
        return this.commands;
    }
    
    @Nonnull
    public Set<Command> getCommandsByType(CommandType type) {
        return commands.stream().filter(command -> command.getType() == type).collect(Collectors.toSet());
    }

    @Nonnull
    public static FlareBot getInstance() {
        return instance;
    }

    public String getUptime() {
        long totalSeconds = (System.currentTimeMillis() - startTime) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = (totalSeconds / 3600);
        return (hours < 10 ? "0" + hours : hours) + "h " + (minutes < 10 ? "0" + minutes : minutes) + "m " + (seconds < 10 ? "0" + seconds : seconds) + "s";
    }

    public PlayerManager getMusicManager() {
        return this.musicManager;
    }

    public String getVersion() {
        if (version == null) {
            Properties p = new Properties();
            try {
                p.load(getClass().getClassLoader().getResourceAsStream("version.properties"));
            } catch (IOException e) {
                LOGGER.error("There was an error trying to load the version!", e);
                return null;
            }
            version = (String) p.get("version");
        }
        return version;
    }

    public String getInvite() {
        return String.format("https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot&permissions=%s",
                getSelfUser().getId(), Permission.getRaw(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ,
                        Permission.MANAGE_ROLES, Permission.MESSAGE_MANAGE, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK,
                        Permission.VOICE_MOVE_OTHERS, Permission.KICK_MEMBERS, Permission.BAN_MEMBERS,
                        Permission.MANAGE_CHANNEL, Permission.MESSAGE_EMBED_LINKS, Permission.NICKNAME_CHANGE,
                        Permission.MANAGE_PERMISSIONS, Permission.VIEW_AUDIT_LOGS, Permission.MESSAGE_HISTORY,
                        Permission.MANAGE_WEBHOOKS, Permission.MANAGE_SERVER, Permission.MESSAGE_ADD_REACTION));
    }

    public static char getPrefix(String id) {
        return getPrefixes().get(id);
    }

    public void setStatus(String status) {
        //TODO: Check if we're actually streaming or not.
        if (shardManager.getShardsTotal() == 1) {
            shardManager.setGameProvider(shardId -> Game.streaming(status, "https://www.twitch.tv/discordflarebot"));
            return;
        }
        shardManager.setGameProvider(shardId -> Game.streaming(status + " | Shard: " + shardId + "/" + shardManager.getShardsTotal(),
                "https://www.twitch.tv/discordflarebot"));
    }

    public boolean isReady() {
        return shardManager.getShards().size() == shardManager.getShardsTotal();
    }

    public static String getMessage(String[] args) {
        StringBuilder msg = new StringBuilder();
        for (String arg : args) {
            msg.append(arg).append(" ");
        }
        return msg.toString().trim();
    }

    public static String getMessage(String[] args, int min) {
        return Arrays.stream(args).skip(min).collect(Collectors.joining(" ")).trim();
    }

    public static String getMessage(String[] args, int min, int max) {
        StringBuilder message = new StringBuilder();
        for (int index = min; index < max; index++) {
            message.append(args[index]).append(" ");
        }
        return message.toString().trim();
    }

    // Disabled for now.
    // TODO: Make sure the API has a way to handle this and also update that page.
    public static void reportError(TextChannel channel, String s, Exception e) {
        JsonObject message = new JsonObject();
        message.addProperty("message", s);
        message.addProperty("exception", GeneralUtils.getStackTrace(e));
        MessageUtils.sendErrorMessage(s, channel);
        //String id = instance.postToApi("postReport", "error", message);
        //MessageUtils.sendErrorMessage(s + "\nThe error has been reported! You can follow the report on the website, https://flarebot.stream/report?id=" + id, channel);
    }

    public static String getStatusHook() {
        return config.getString("bot.statusHook").isPresent() ? config.getString("bot.statusHook").get() : null;
    }

    public String formatTime(long duration, TimeUnit durUnit, boolean fullUnits, boolean append0) {
        long totalSeconds = 0;
        switch (durUnit) {
            case MILLISECONDS:
                totalSeconds = duration / 1000;
                break;
            case SECONDS:
                totalSeconds = duration;
                break;
            case MINUTES:
                totalSeconds = duration * 60;
                break;
            case HOURS:
                totalSeconds = (duration * 60) * 60;
                break;
            case DAYS:
                totalSeconds = ((duration * 60) * 60) * 24;
                break;
        }
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = (totalSeconds / 3600) % 24;
        long days = (totalSeconds / 86400);
        return (days > 0 ? (append0 && days < 10 ? "0" + days : days) + (fullUnits ? " days " : "d ") : "")
                + (hours > 0 ? (append0 && hours < 10 ? "0" + hours : hours) + (fullUnits ? " hours " : "h ") : "")
                + (minutes > 0 ? (append0 && minutes < 10 ? "0" + minutes : minutes) + (fullUnits ? " minutes" : "m ") : "")
                + (seconds > 0 ? (append0 && seconds < 10 ? "0" + seconds : seconds) + (fullUnits ? " seconds" : "s") : "")
                .trim();
    }

    public TextChannel getErrorLogChannel() {
        return (testBot ? getChannelById(Constants.FLARE_TEST_BOT_CHANNEL) : getChannelById("226786557862871040"));
    }

    public TextChannel getGuildLogChannel() {
        return (testBot ? getChannelById(Constants.FLARE_TEST_BOT_CHANNEL) : getChannelById("260401007685664768"));
    }

    private TextChannel getEGLogChannel() {
        return (testBot ? getChannelById(Constants.FLARE_TEST_BOT_CHANNEL) : getChannelById("358950369642151937"));
    }

    public void logEG(String eg, Command command, Guild guild, User user) {
        EmbedBuilder builder = new EmbedBuilder().setTitle("Found `" + eg + "`")
                .addField("Guild", guild.getId() + " (`" + guild.getName() + "`) ", true)
                .addField("User", user.getAsMention() + " (`" + user.getName() + "#" + user.getDiscriminator() + "`)", true)
                .setTimestamp(LocalDateTime.now(Clock.systemUTC()));
        if (command != null) builder.addField("Command", command.getCommand(), true);
        getEGLogChannel().sendMessage(builder.build()).queue();
    }

    public TextChannel getImportantLogChannel() {
        return (testBot ? getChannelById(Constants.FLARE_TEST_BOT_CHANNEL) : getChannelById("358978253966278657"));
    }


    public static String getYoutubeKey() {
        return youtubeApi;
    }

    public List<VoiceChannel> getVoiceChannels() {
        return shardManager.getVoiceChannels();
    }

    public long getActiveVoiceChannels() {
        return getConnectedVoiceChannelList().stream()
                .map(vc -> getMusicManager().getPlayer(vc.getGuild().getId()))
                .filter(p -> p != null && p.getPlayingTrack() != null && !p.getPaused())
                .count();
    }

    public FlareBotManager getManager() {
        return this.manager;
    }

    public PlayerCache getPlayerCache(String userId) {
        this.playerCache.computeIfAbsent(userId, k -> new PlayerCache(userId, null, null, null));
        return this.playerCache.get(userId);
    }

    private static Logger getLog(String name) {
        return LOGGERS.computeIfAbsent(name, LoggerFactory::getLogger);
    }

    public static Logger getLog(Class<?> clazz) {
        return getLog(clazz.getName());
    }

    // getXById

    public TextChannel getChannelById(String id) {
        return getGuilds().stream()
                .map(g -> g.getTextChannelById(id))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    public TextChannel getChannelById(long id) {
        return getGuilds().stream().map(g -> g.getTextChannelById(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public Guild getGuildById(String id) {
        return getGuilds().stream().filter(g -> g.getId().equals(id)).findFirst().orElse(null);
    }

    public Guild getGuildById(long id) {
        return getGuilds().stream().filter(g -> g.getIdLong() == id).findFirst().orElse(null);
    }

    public Emote getEmoteById(long emoteId) {
        for (Guild g : getGuilds())
            if (g.getEmoteById(emoteId) != null)
                return g.getEmoteById(emoteId);
        return null;
    }

    // getXs

    public List<Guild> getGuilds() {
        return shardManager.getGuilds();
    }

    public SnowflakeCacheView<Guild> getGuildsCache() {
        return shardManager.getGuildCache();
    }

    public List<Channel> getChannels() {
        return getGuilds().stream().flatMap(g -> g.getTextChannels().stream()).collect(Collectors.toList());
    }

    public long getConnectedVoiceChannels() {
        return getGuilds().stream().filter(c -> c.getAudioManager().getConnectedChannel() != null).count();
    }

    public List<VoiceChannel> getConnectedVoiceChannelList() {
        return getGuilds().stream().map(g -> g.getAudioManager().getConnectedChannel())
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Set<User> getUsers() {
        return shardManager.getUserCache().asSet();
    }

    public User getUserById(String id) {
        return shardManager.getUserById(id);
    }

    // Keep consistent with the naming of JDA.
    public User getUserById(long id) {
        return shardManager.getUserById(id);
    }

    public User retrieveUserById(long id) {
        return getClient().retrieveUserById(id).complete();
    }

    public boolean isTestBot() {
        return testBot;
    }

    public String getPasteKey() {
        return config.getString("bot.pasteAccessKey").isPresent() ? config.getString("bot.pasteAccessKey").get() : null;
    }

    public String getApiKey() {
        if (config.getString("misc.apiKey").isPresent())
            return config.getString("misc.apiKey").get();
        else {
            apiEnabled = false;
            return null;
        }
    }

    public List<JDA> getShards() {
        return shardManager.getShards();
    }

    private JDA[] getShardsArray() {
        return shardManager.getShards().toArray(new JDA[shardManager.getShards().size()]);
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public boolean isApiDisabled() {
        return !apiEnabled;
    }

    private WebhookClient importantHook;

    private WebhookClient getImportantWebhook() {
        if (!config.getString("bot.importantHook").isPresent())
            return null;
        if (importantHook == null)
            importantHook = new WebhookClientBuilder(config.getString("bot.importantHook").get()).build();
        return importantHook;
    }

    public Guild getOfficialGuild() {
        return getGuildById(Constants.OFFICIAL_GUILD);
    }

    private static void handleLogArchive() {
        try {
            byte[] buffer = new byte[1024];
            String time = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date());

            File dir = new File("logs");
            if (!dir.exists() && !dir.mkdir())
                LOGGER.error("Failed to create directory for latest log!");
            File f = new File(dir, "latest.log " + time + ".zip");
            File latestLog = new File("latest.log");

            FileOutputStream fos = new FileOutputStream(f);
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry entry = new ZipEntry(latestLog.getName());
            zos.putNextEntry(entry);
            FileInputStream in = new FileInputStream(latestLog);

            int len;
            while ((len = in.read(buffer)) > 0)
                zos.write(buffer, 0, len);

            in.close();
            zos.closeEntry();
            zos.close();
            fos.close();

            if (!latestLog.delete()) {
                throw new IllegalStateException("Failed to delete the old log file!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runTasks() {
        new FlareBotTask("FixThatStatus") {
            @Override
            public void run() {
                if (!UpdateCommand.UPDATING.get())
                    setStatus("_help | _invite");
            }
        }.repeat(10, TimeUnit.SECONDS.toMillis(32));

        new FlareBotTask("PostDbotsData") {
            @Override
            public void run() {
                if (config.getString("botlists.discordBots").isPresent()) {
                    postToBotList(config.getString("botlists.discordBots").get(), String
                            .format("https://bots.discord.pw/api/bots/%s/stats", getClient().getSelfUser().getId()));
                }
            }
        }.repeat(10, TimeUnit.MINUTES.toMillis(10));

        new FlareBotTask("PostBotlistData") {
            @Override
            public void run() {
                if (config.getString("botlists.botlist").isPresent()) {
                    postToBotList(config.getString("botlists.botlist").get(), String
                            .format("https://discordbots.org/api/bots/%s/stats", getClient().getSelfUser().getId()));
                }
            }
        }.repeat(10, TimeUnit.MINUTES.toMillis(10));

        new FlareBotTask("PostCarbonData") {
            @Override
            public void run() {
                if (config.getString("botlists.carbon").isPresent()) {
                    try {
                        WebUtils.post("https://www.carbonitex.net/discord/data/botdata.php", WebUtils.APPLICATION_JSON,
                                new JSONObject()
                                        .put("key", config.getString("botlists.carbon").get())
                                        .put("servercount", getGuilds().size())
                                        .put("shardcount", getShards().size())
                                        .toString());
                    } catch (IOException e) {
                        LOGGER.error("Failed to update carbon!", e);
                    }
                }
            }
        }.repeat(10, TimeUnit.MINUTES.toMillis(10));

        new FlareBotTask("UpdateWebsite" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (!isTestBot())
                    sendData();
            }
        }.repeat(10, TimeUnit.SECONDS.toMillis(5));

        new FlareBotTask("spam" + System.currentTimeMillis()) {
            @Override
            public void run() {
                events.getSpamMap().clear();
            }
        }.repeat(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(3));

        new FlareBotTask("ClearConfirmMap" + System.currentTimeMillis()) {
            @Override
            public void run() {
                ConfirmUtil.clearConfirmMap();
            }

        }.repeat(10, TimeUnit.MINUTES.toMillis(1));

        new FlareBotTask("DeadShard-Checker") {
            @Override
            public void run() {
                if (getImportantWebhook() == null) {
                    LOGGER.warn("No webhook for the important-log channel! Due to this the dead shard checker has been disabled!");
                    cancel();
                    return;
                }
                if (getShards().size() == 1) {
                    LOGGER.warn("Single sharded bot, the DeadShard-Checker has been disabled!");
                    cancel();
                    return;
                }
                // 10 mins without an event... this son bitch is dead.
                if (getShards().stream().anyMatch(shard -> ShardUtils.isDead(shard, TimeUnit.MINUTES.toMillis(10)))) {
                    getShards().stream().filter(shard -> ShardUtils.isDead(shard, TimeUnit.MINUTES.toMillis(10)))
                            .forEach(shard -> {
                                getImportantWebhook().send("Restarting " + ShardUtils.getShardId(shard)
                                        + " as it seems to be dead.");
                                shardManager.restart(ShardUtils.getShardId(shard));
                            });
                }

                Set<Integer> deadShards = getShards().stream().filter(ShardUtils::isDead).map(ShardUtils::getShardId)
                        .collect(Collectors.toSet());

                if (!deadShards.isEmpty()) {
                    getImportantWebhook().send("Found " + deadShards.size() + " possibly dead shards! Shards: " +
                            deadShards.toString());
                }
            }
        }.repeat(TimeUnit.MINUTES.toMillis(10), TimeUnit.MINUTES.toMillis(5));

        new FlareBotTask("ActivityChecker") {
            @Override
            public void run() {
                for (VoiceChannel channel : getConnectedVoiceChannelList()) {
                    if (channel.getMembers().stream().filter(member -> !member.getUser().isBot() && !member.getUser().isFake()).count() > 0 
                            && !getMusicManager().getPlayer(channel.getGuild().getId()).getPlaylist().isEmpty() 
                            && !getMusicManager().getPlayer(channel.getGuild().getId()).getPaused()) {
                        manager.getLastActive().remove(channel.getGuild().getIdLong());
                        return;
                    }
                    if (manager.getLastActive().containsKey(channel.getGuild().getIdLong())
                           && System.currentTimeMillis() >= (manager.getLastActive().get(channel.getGuild().getIdLong()) 
                               + TimeUnit.MINUTES.toMillis(10)))
                        channel.getGuild().getAudioManager().closeAudioConnection();
                }
            }
        }.repeat(10_000, 10_000);
    }

    public static JSONConfig getConfig() {
        return config;
    }

    public AnalyticsHandler getAnalyticsHandler() {
        return analyticsHandler;
    }
}

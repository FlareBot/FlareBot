package stream.flarebot.flarebot;

import ch.qos.logback.classic.Level;
import com.arsenarsen.githubwebhooks4j.WebhooksBuilder;
import com.arsenarsen.githubwebhooks4j.web.HTTPRequest;
import com.arsenarsen.githubwebhooks4j.web.Response;
import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.arsenarsen.lavaplayerbridge.utils.JDAMultiShard;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import io.github.binaryoverload.JSONConfig;
import io.sentry.Sentry;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.WebSocketCode;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import stream.flarebot.flarebot.api.ApiRequester;
import stream.flarebot.flarebot.api.ApiRoute;
import stream.flarebot.flarebot.commands.*;
import stream.flarebot.flarebot.commands.currency.*;
import stream.flarebot.flarebot.commands.general.*;
import stream.flarebot.flarebot.commands.moderation.*;
import stream.flarebot.flarebot.commands.moderation.mod.*;
import stream.flarebot.flarebot.commands.music.*;
import stream.flarebot.flarebot.commands.random.*;
import stream.flarebot.flarebot.commands.secret.*;
import stream.flarebot.flarebot.commands.secret.internal.*;
import stream.flarebot.flarebot.commands.useful.*;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.database.RedisController;
import stream.flarebot.flarebot.github.GithubListener;
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FlareBot {

    private static final Map<String, Logger> LOGGERS;
    public static final Logger LOGGER;
    public static final String INVITE_URL = "https://discord.gg/TTAUGvZ";
    public static final char COMMAND_CHAR = '_';

    static {
        handleLogArchive();
        LOGGERS = new ConcurrentHashMap<>();
        LOGGER = getLog(FlareBot.class);
    }

    private static FlareBot instance;
    private static String youtubeApi;

    private static JSONConfig config;
    private FlareBotManager manager;
    private static boolean apiEnabled = true;

    public static final Gson GSON = new GsonBuilder().create();

    public static final AtomicBoolean EXITING = new AtomicBoolean(false);

    private Map<String, PlayerCache> playerCache = new ConcurrentHashMap<>();
    protected CountDownLatch latch;
    private static String importantHookUrl;
    private static String token;

    private static boolean testBot = false;

    private static OkHttpClient client =
            new OkHttpClient.Builder().connectionPool(new ConnectionPool(4, 10, TimeUnit.SECONDS))
                    .addInterceptor(new DataInterceptor()).build();

    public static void main(String[] args) {
        Spark.port(8080);
        Sentry.init();
        try {
            File file = new File("config.json");
            if (!file.exists())
                file.createNewFile();
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

        String tkn = config.getString("bot.token").get();

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

        if (!config.getString("misc.apiKey").isPresent() || config.getString("misc.apiKey").get().isEmpty())
            apiEnabled = false;

        Thread.setDefaultUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        Thread.currentThread()
                .setUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        try {
            (instance = new FlareBot()).init(tkn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getToken() {
        return token;
    }

    public static OkHttpClient getOkHttpClient() {
        return client;
    }

    public Events getEvents() {
        return events;
    }

    private Events events;
    private String version = null;
    private JDA[] clients;

    private Set<Command> commands = ConcurrentHashMap.newKeySet();
    private PlayerManager musicManager;
    private long startTime;
    private static String secret = null;
    private static Prefixes prefixes;

    public static Prefixes getPrefixes() {
        return prefixes;
    }

    public void init(String tkn) throws InterruptedException {
        LOGGER.info("Starting init!");
        token = tkn;
        manager = new FlareBotManager();
        RestAction.DEFAULT_FAILURE = t -> {
        };
        try {
            clients = new JDA[WebUtils.getShards(tkn)];
        } catch (IOException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        latch = new CountDownLatch(1);
        events = new Events(this);
        LOGGER.info("Starting builders");
        try {
            JDABuilder builder = new JDABuilder(AccountType.BOT)
                    .addEventListener(events)
                    .addEventListener(new ModlogEvents())
                    .setToken(tkn)
                    .setHttpClientBuilder(client.newBuilder())
                    .setAudioSendFactory(new NativeAudioSendFactory());
            if (clients.length == 1) {
                clients[0] = builder.buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
                Thread.sleep(5000);
            } else {
                builder = builder.setReconnectQueue(new SessionReconnectQueue());
                for (int i = 0; i < clients.length; i++) {
                    clients[i] = builder.useSharding(i, clients.length).buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
                    Thread.sleep(5000); // 5 second backoff
                }
            }
            prefixes = new Prefixes();
            commands = ConcurrentHashMap.newKeySet();
            musicManager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(new JDAMultiShard(clients)));
            musicManager.getPlayerCreateHooks().register(player -> player.addEventListener(new AudioEventAdapter() {
                @Override
                public void onTrackEnd(AudioPlayer aplayer, AudioTrack atrack, AudioTrackEndReason reason) {
                    if (manager.getGuild(player.getGuildId()).isSongnickEnabled()) {
                        if (GeneralUtils.canChangeNick(player.getGuildId())) {
                            Guild c = getGuildById(player.getGuildId());
                            if (c == null) {
                                manager.getGuild(player.getGuildId()).setSongnick(false);
                            } else {
                                if (player.getPlaylist().isEmpty())
                                    c.getController().setNickname(c.getSelfMember(), null).queue();
                            }
                        } else {
                            if (!GeneralUtils.canChangeNick(player.getGuildId())) {
                                MessageUtils.sendPM(getGuildById(player.getGuildId()).getOwner().getUser(),
                                        "FlareBot can't change it's nickname so SongNick has been disabled!");
                            }
                        }
                    }
                }

                @Override
                public void onTrackStart(AudioPlayer aplayer, AudioTrack atrack) {
                    if (MusicAnnounceCommand.getAnnouncements().containsKey(player.getGuildId())) {
                        TextChannel c =
                                getChannelByID(MusicAnnounceCommand.getAnnouncements().get(player.getGuildId()));
                        if (c != null) {
                            if (c.getGuild().getSelfMember().hasPermission(c,
                                    Permission.MESSAGE_EMBED_LINKS,
                                    Permission.MESSAGE_READ,
                                    Permission.MESSAGE_WRITE)) {
                                Track track = player.getPlayingTrack();
                                Queue<Track> playlist = player.getPlaylist();
                                c.sendMessage(MessageUtils.getEmbed()
                                        .addField("Now Playing", SongCommand.getLink(track), false)
                                        .addField("Duration", GeneralUtils
                                                .formatDuration(track.getTrack().getDuration()), false)
                                        .addField("Requested by",
                                                String.format("<@!%s>", track.getMeta()
                                                        .get("requester")), false)
                                        .addField("Next up", playlist.isEmpty() ? "Nothing" :
                                                SongCommand.getLink(playlist.peek()), false)
                                        .setImage("https://img.youtube.com/vi/" + track.getTrack().getIdentifier() + "/0.jpg")
                                        .build()).queue();
                            } else {
                                MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                            }
                        } else {
                            MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                        }
                    }
                    if (manager.getGuild(player.getGuildId()).isSongnickEnabled()) {
                        Guild c = getGuildById(player.getGuildId());
                        if (c == null || !GeneralUtils.canChangeNick(player.getGuildId())) {
                            manager.getGuild(player.getGuildId()).setSongnick(false);
                            if (!GeneralUtils.canChangeNick(player.getGuildId())) {
                                MessageUtils.sendPM(getGuildById(player.getGuildId()).getOwner().getUser(),
                                        "FlareBot can't change it's nickname so SongNick has been disabled!");
                            }
                        } else {
                            Track track = player.getPlayingTrack();
                            String str = null;
                            if (track != null) {
                                str = track.getTrack().getInfo().title;
                                if (str.length() > 32)
                                    str = str.substring(0, 32);
                                str = str.substring(0, str.lastIndexOf(' ') + 1);
                            } // Even I couldn't make this a one-liner
                            c.getController()
                                    .setNickname(c.getSelfMember(), str)
                                    .queue();
                        }
                    }
                }
            }));
            try {
                new WebhooksBuilder()
                        .withBinder((request, ip, port, webhooks) -> Spark.post(request, (request1, response) -> {
                            Map<String, String> headers = new HashMap<>();
                            request1.headers().forEach(s -> headers.put(s, request1.headers(s)));
                            Response res = webhooks.callHooks(new HTTPRequest("POST",
                                    new ByteArrayInputStream(request1.bodyAsBytes()),
                                    headers));
                            response.status(res.getCode());
                            return res.getResponse();
                        }))
                        .withSecret(secret)
                        .addListener(new GithubListener()).forRequest("/payload").onPort(8080).build();
            } catch (IOException e) {
                LOGGER.error("Could not set up webhooks!", e);
            }
        } catch (Exception e) {
            LOGGER.error("Could not log in!", e);
            Thread.sleep(500);
            System.exit(1);
            return;
        }
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        })); // No operation STDERR. Will not do much of anything, except to filter out some Jsoup spam

        manager = new FlareBotManager();
        manager.executeCreations();

        musicManager.getPlayerCreateHooks()
                .register(player -> player.getQueueHookManager().register(new QueueListener()));

        latch.await();
        run();
    }

    protected void run() {
        registerCommand(new HelpCommand());
        registerCommand(new SearchCommand());
        registerCommand(new JoinCommand());
        registerCommand(new LeaveCommand());
        registerCommand(new InfoCommand());
        registerCommand(new ResumeCommand());
        registerCommand(new PlayCommand());
        registerCommand(new PauseCommand(this));
        registerCommand(new StopCommand(this));
        registerCommand(new SkipCommand(this));
        registerCommand(new ShuffleCommand(this));
        registerCommand(new PlaylistCommand(this));
        registerCommand(new SongCommand(this));
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
        registerCommand(new PollCommand());
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

        ApiFactory.bind();

        manager.executeCreations();

        loadFutureTasks();

        startTime = System.currentTimeMillis();
        LOGGER.info("FlareBot v" + getVersion() + " booted!");

        sendCommands();

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
                            .format("https://bots.discord.pw/api/bots/%s/stats", clients[0].getSelfUser().getId()));
                }
            }
        }.repeat(10, TimeUnit.MINUTES.toMillis(10));

        new FlareBotTask("PostBotlistData") {
            @Override
            public void run() {
                if (config.getString("botlists.botlist").isPresent()) {
                    postToBotList(config.getString("botlists.botlist").get(), String
                            .format("https://discordbots.org/api/bots/%s/stats", clients[0].getSelfUser().getId()));
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
                                        .put("shardcount", clients.length)
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
                if (getClients().length == 1) return;
                Set<Integer> deadShards = Arrays.stream(getClients()).map(c -> c.getShardInfo().getShardId())
                        .filter(ShardUtils::isDead).collect(Collectors.toSet());
                if (deadShards.size() > 0) {
                    if (getImportantWebhook() == null) {
                        FlareBot.LOGGER.warn("No webhook for the important-log channel! Due to this the dead shard checker has been disabled!");
                        cancel();
                    }
                    getImportantWebhook().send("Found " + deadShards.size() + " possibly dead shards! Shards: " +
                            deadShards.toString());
                }
            }
        }.repeat(TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(5));

        setupUpdate();
    }

    /**
     * This will always return the main shard or just the client itself.
     * For reference the main shard will always be shard 0 - the shard responsible for DMs
     *
     * @return The main shard or actual client in the case of only 1 shard.
     */
    public JDA getClient() {
        return clients[0];
    }

    private void loadFutureTasks() {
        final int[] loaded = {0};
        CassandraController.runTask(session -> {
            ResultSet set = session.execute("SELECT * FROM flarebot.future_tasks");
            Row row;
            while ((row = set.one()) != null) {
                FutureAction fa = new FutureAction(row.getLong("guild_id"), row.getLong("channel_id"), row.getLong("responsible"),
                        row.getLong("target"), row.getString("content"), new DateTime(row.getTimestamp("expires_at")),
                        new DateTime(row.getTimestamp("created_at")),
                        FutureAction.Action.valueOf(row.getString("action").toUpperCase()));
                if (new DateTime().isAfter(fa.getExpires()))
                    fa.execute();
                else
                    fa.queue();
                loaded[0]++;
            }
        });

        LOGGER.info("Loaded " + loaded[0] + " future tasks");
    }

    private void postToBotList(String auth, String url) {
        for (JDA client : clients) {
            if (clients.length == 1) {
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

                // Gonna spread these out just a bit so we don't burst 15 requests all at once
                Thread.sleep(20_000);
            } catch (Exception e1) {
                FlareBot.LOGGER.error("Could not POST data to a botlist", e1);
            }
        }
        LOGGER.debug("Sent " + clients.length + " requests to " + url);
    }

    private void setupUpdate() {
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
                .put("connected_voice_channels", getConnectedVoiceChannels().size())
                .put("active_voice_channels", getActiveVoiceChannels())
                .put("num_queued_songs", getGuilds().stream()
                        .mapToInt(guild -> musicManager.getPlayer(guild.getId())
                                .getPlaylist().size()).sum())
                .put("ram", (((runtime.totalMemory() - runtime.freeMemory()) / 1024) / 1024) + "MB")
                .put("uptime", getUptime())
                .put("http_requests", DataInterceptor.getRequests().intValue());

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
                setupUpdate();
                UpdateCommand.UPDATING.set(false);
            }
        } else
            LOGGER.info("Exiting.");
        stop();
        System.exit(0);
    }

    protected void stop() {
        if (EXITING.get()) return;
        LOGGER.info("Saving data.");
        EXITING.set(true);
        getImportantLogChannel().sendMessage("Average load time of this session: " + manager.getLoadTimes()
                .stream().mapToLong(v -> v).average().orElse(0) + "\nTotal loads: " + manager.getLoadTimes().size())
                .queue();
        getImportantLogChannel().sendMessage("Average delete messages of this session (mins): "
                + (Events.durations.stream().mapToLong(v -> v).average().orElse(0) / 60000)
                + "\nHighest time: " + (Events.durations.stream().mapToLong(v -> v).max().orElse(0) / 60000))
                .complete();
        for (ScheduledFuture<?> scheduledFuture : Scheduler.getTasks().values())
            scheduledFuture.cancel(false); // No tasks in theory should block this or cause issues. We'll see
        for (JDA client : clients)
            client.removeEventListener(events); //todo: Make a replacement for the array
        sendData();
        for (String s : manager.getGuilds().keySet()) {
            manager.saveGuild(s, manager.getGuilds().get(s), manager.getGuilds().getLastRetrieved(s));
        }
        LOGGER.info("Finished saving!");
        for (JDA client : clients)
            client.shutdown();
    }

    private void registerCommand(Command command) {
        this.commands.add(command);
    }

    public Command getCommand(String s, User user) {
        Command tmp = null;
        for (Command cmd : getCommands()) {
            if (cmd.getType() == CommandType.SECRET && (isTestBot() && !PerGuildPermissions.isContributor(user))
                    && !PerGuildPermissions.isCreator(user)) {
                if (cmd.getCommand().equalsIgnoreCase(s))
                    tmp = cmd;
                for (String alias : cmd.getAliases())
                    if (alias.equalsIgnoreCase(s)) tmp = cmd;
                continue;
            }
            if (cmd.getCommand().equalsIgnoreCase(s))
                return cmd;
            for (String alias : cmd.getAliases())
                if (alias.equalsIgnoreCase(s)) return cmd;
        }
        return tmp;
    }

    public Set<Command> getCommands() {
        return this.commands;
    }

    public List<Command> getCommandsByType(CommandType type) {
        return commands.stream().filter(command -> command.getType() == type).collect(Collectors.toList());
    }

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
                clients[0].getSelfUser().getId(), Permission.getRaw(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ,
                        Permission.MANAGE_ROLES, Permission.MESSAGE_MANAGE, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK,
                        Permission.VOICE_MOVE_OTHERS, Permission.KICK_MEMBERS, Permission.BAN_MEMBERS,
                        Permission.MANAGE_CHANNEL, Permission.MESSAGE_EMBED_LINKS, Permission.NICKNAME_CHANGE));
    }

    public static char getPrefix(String id) {
        return getPrefixes().get(id);
    }

    public void setStatus(String status) {
        if (clients.length == 1) {
            clients[0].getPresence().setGame(Game.streaming(status, "https://www.twitch.tv/discordflarebot"));
            return;
        }

        // Let's have some fun :p

        JSONObject obj = new JSONObject();
        JSONObject game = new JSONObject();
        game.put("url", "https://www.twitch.tv/discordflarebot");
        game.put("type", 3);
        obj.put("afk", false);
        obj.put("status", OnlineStatus.ONLINE.getKey());
        obj.put("since", System.currentTimeMillis());
        for (JDA jda : clients) {
            //jda.getPresence().setGame(Game.of(status + " | Shard: " + (jda.getShardInfo().getShardId() + 1) + "/" +
            //        clients.length, "https://www.twitch.tv/discordflarebot"));
            game.put("name", "over shard " + (jda.getShardInfo().getShardId() + 1) + "/" + clients.length + " | " + status);
            obj.put("game", game);
            ((JDAImpl) jda).getClient().send(new JSONObject().put("d", obj).put("op", WebSocketCode.PRESENCE).toString());
        }
    }

    public boolean isReady() {
        return Arrays.stream(clients).mapToInt(c -> (c.getStatus() == JDA.Status.CONNECTED ? 1 : 0))
                .sum() == clients.length;
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
    // TODO: Make sure the API has a way to hadle this and also update that page.
    public static void reportError(TextChannel channel, String s, Exception e) {
        JsonObject message = new JsonObject();
        message.addProperty("message", s);
        message.addProperty("exception", GeneralUtils.getStackTrace(e));
        //String id = instance.postToApi("postReport", "error", message);
        //MessageUtils.sendErrorMessage(s + "\nThe error has been reported! You can follow the report on the website, https://flarebot.stream/report?id=" + id, channel);
    }

    public static String getStatusHook() {
        return config.getString("bot.statusHook").isPresent() ? config.getString("bot.statusHook").get() : null;
    }

    public String formatTime(long duration, TimeUnit durUnit, boolean fullUnits, boolean append0) {
        long totalSeconds = 0;
        if (durUnit == TimeUnit.MILLISECONDS)
            totalSeconds = duration / 1000;
        else if (durUnit == TimeUnit.SECONDS)
            totalSeconds = duration;
        else if (durUnit == TimeUnit.MINUTES)
            totalSeconds = duration * 60;
        else if (durUnit == TimeUnit.HOURS)
            totalSeconds = (duration * 60) * 60;
        else if (durUnit == TimeUnit.DAYS)
            totalSeconds = ((duration * 60) * 60) * 24;
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
        return (testBot ? getChannelByID(Constants.FLARE_TEST_BOT_CHANNEL) : getChannelByID("226786557862871040"));
    }

    public TextChannel getGuildLogChannel() {
        return (testBot ? getChannelByID(Constants.FLARE_TEST_BOT_CHANNEL) : getChannelByID("260401007685664768"));
    }

    public TextChannel getEGLogChannel() {
        return (testBot ? getChannelByID(Constants.FLARE_TEST_BOT_CHANNEL) : getChannelByID("358950369642151937"));
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
        return (testBot ? getChannelByID(Constants.FLARE_TEST_BOT_CHANNEL) : getChannelByID("358978253966278657"));
    }


    public static String getYoutubeKey() {
        return youtubeApi;
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

    // getXByID

    public TextChannel getChannelByID(String id) {
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

    // getXs

    public List<Guild> getGuilds() {
        return Arrays.stream(clients).flatMap(j -> j.getGuilds().stream()).collect(Collectors.toList());
    }

    public JDA[] getClients() {
        return clients;
    }

    public List<Channel> getChannels() {
        return getGuilds().stream().flatMap(g -> g.getTextChannels().stream()).collect(Collectors.toList());
    }

    public List<VoiceChannel> getVoiceChannels() {
        return Arrays.stream(getClients()).flatMap(c -> c.getVoiceChannels().stream()).collect(Collectors.toList());
    }

    public List<VoiceChannel> getConnectedVoiceChannels() {
        return Arrays.stream(getClients()).flatMap(c -> c.getGuilds().stream())
                .map(c -> c.getAudioManager().getConnectedChannel())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public long getActiveVoiceChannels() {
        return getConnectedVoiceChannels().stream()
                .map(VoiceChannel::getGuild)
                .map(ISnowflake::getId)
                .filter(gid -> FlareBot.getInstance().getMusicManager().hasPlayer(gid))
                .map(g -> FlareBot.getInstance().getMusicManager().getPlayer(g))
                .filter(p -> p.getPlayingTrack() != null)
                .filter(p -> !p.getPaused()).count();
    }

    public Set<User> getUsers() {
        return Arrays.stream(clients).flatMap(jda -> jda.getUsers().stream())
                .distinct().collect(Collectors.toSet());
    }

    public User getUserByID(String id) {
        return Arrays.stream(clients).map(jda -> {
            try {
                return jda.getUserById(id);
            } catch (Exception ignored) {
            }
            return null;
        })
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    // Keep consistent with the naming of JDA.
    public User getUserById(long id) {
        return Arrays.stream(clients).map(jda -> {
            try {
                return jda.getUserById(id);
            } catch (Exception ignored) {
            }
            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
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

    public boolean isApiDisabled() {
        return !apiEnabled;
    }

    private WebhookClient importantHook;

    private WebhookClient getImportantWebhook() {
        if (importantHookUrl == null) return null;
        if (importantHook == null)
            importantHook = new WebhookClientBuilder(importantHookUrl).build();
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
            if (!dir.exists())
                if (!dir.mkdir())
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
}

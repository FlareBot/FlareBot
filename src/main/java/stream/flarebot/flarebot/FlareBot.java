package stream.flarebot.flarebot;

import ch.qos.logback.classic.Level;
import com.arsenarsen.githubwebhooks4j.WebhooksBuilder;
import com.arsenarsen.githubwebhooks4j.web.HTTPRequest;
import com.arsenarsen.githubwebhooks4j.web.Response;
import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.arsenarsen.lavaplayerbridge.utils.JDAMultiShard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import io.github.binaryoverload.JSONConfig;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import net.dv8tion.jda.core.utils.SimpleLog;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.Prefixes;
import stream.flarebot.flarebot.commands.automod.ModlogCommand;
import stream.flarebot.flarebot.commands.general.*;
import stream.flarebot.flarebot.commands.moderation.AutoAssignCommand;
import stream.flarebot.flarebot.commands.moderation.FixCommand;
import stream.flarebot.flarebot.commands.moderation.PermissionsCommand;
import stream.flarebot.flarebot.commands.moderation.PinCommand;
import stream.flarebot.flarebot.commands.moderation.PruneCommand;
import stream.flarebot.flarebot.commands.moderation.PurgeCommand;
import stream.flarebot.flarebot.commands.moderation.ReportsCommand;
import stream.flarebot.flarebot.commands.moderation.RolesCommand;
import stream.flarebot.flarebot.commands.moderation.SetPrefixCommand;
import stream.flarebot.flarebot.commands.moderation.WelcomeCommand;
import stream.flarebot.flarebot.commands.moderation.mod.BanCommand;
import stream.flarebot.flarebot.commands.moderation.mod.ForceBanCommand;
import stream.flarebot.flarebot.commands.moderation.mod.KickCommand;
import stream.flarebot.flarebot.commands.moderation.mod.MuteCommand;
import stream.flarebot.flarebot.commands.moderation.mod.UnmuteCommand;
import stream.flarebot.flarebot.commands.moderation.mod.WarnCommand;
import stream.flarebot.flarebot.commands.moderation.mod.WarningsCommand;
import stream.flarebot.flarebot.commands.music.*;
import stream.flarebot.flarebot.commands.secret.AvatarCommand;
import stream.flarebot.flarebot.commands.secret.DisableCommandCommand;
import stream.flarebot.flarebot.commands.secret.EvalCommand;
import stream.flarebot.flarebot.commands.secret.GuildCommand;
import stream.flarebot.flarebot.commands.secret.LogsCommand;
import stream.flarebot.flarebot.commands.secret.QueryCommand;
import stream.flarebot.flarebot.commands.secret.QuitCommand;
import stream.flarebot.flarebot.commands.secret.ShardRestartCommand;
import stream.flarebot.flarebot.commands.secret.TestCommand;
import stream.flarebot.flarebot.commands.secret.UpdateCommand;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.github.GithubListener;
import stream.flarebot.flarebot.mod.AutoModTracker;
import stream.flarebot.flarebot.music.QueueListener;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.scheduler.FlarebotTask;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.ConfirmUtil;
import stream.flarebot.flarebot.util.ExceptionUtils;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.WebUtils;
import stream.flarebot.flarebot.web.ApiFactory;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class FlareBot {

    private static final Map<String, Logger> LOGGERS;
    public static final Logger LOGGER;
    public static final String INVITE_URL = "https://discord.gg/TTAUGvZ";

    private static FlareBot instance;
    private static String youtubeApi;

    static {
        new File("latest.log").delete();
        LOGGERS = new ConcurrentHashMap<>();
        LOGGER = getLog(FlareBot.class);
    }

    private static String botListAuth;
    private static String dBotsAuth;
    private FlareBotManager manager;
    private static String webSecret;
    private static boolean apiEnabled = true;

    public static final Gson GSON = new GsonBuilder().create();

    public static final String OFFICIAL_GUILD = "226785954537406464";
    public static final String OLD_FLAREBOT_API = "https://flarebot.stream/api/";
    public static final String FLAREBOT_API = "https://api.flarebot.stream/";
    //public static final String FLAREBOT_API = "http://localhost:8880/";

    public static final String FLARE_TEST_BOT_CHANNEL = "242297848123621376";

    public static final AtomicBoolean EXITING = new AtomicBoolean(false);

    private Map<String, PlayerCache> playerCache = new ConcurrentHashMap<>();
    protected CountDownLatch latch;
    private static String statusHook;
    private static String token;

    private static boolean testBot = false;

    private static OkHttpClient client =
            new OkHttpClient.Builder().connectionPool(new ConnectionPool(4, 10, TimeUnit.SECONDS)).build();

    public static void main(String[] args) throws Exception {
        SimpleLog.LEVEL = SimpleLog.Level.OFF;
        SimpleLog.addListener(new SimpleLog.LogListener() {
            @Override
            public void onLog(SimpleLog log, SimpleLog.Level logLevel, Object message) {
                switch (logLevel) {
                    case ALL:
                    case INFO:
                        getLog(log.name).info(String.valueOf(message));
                        break;
                    case FATAL:
                        getLog(log.name).error(String.valueOf(message));
                        break;
                    case WARNING:
                        getLog(log.name).warn(String.valueOf(message));
                        break;
                    case DEBUG:
                        getLog(log.name).debug(String.valueOf(message));
                        break;
                    case TRACE:
                        getLog(log.name).trace(String.valueOf(message));
                        break;
                    case OFF:
                        break;
                }
            }

            @Override
            public void onError(SimpleLog log, Throwable err) {

            }
        });
        Spark.port(8080);

        JSONConfig config = null;
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

        new CassandraController().init(config);
        if (config.getString("misc.hook").isPresent()) {
            FlareBot.secret = config.getString("misc.hook").get();
        }
        if (config.getString("botlists.discordBots").isPresent()) {
            FlareBot.dBotsAuth = config.getString("botlists.discordBots").get();
        }
        if (config.getString("misc.web").isPresent()) {
            FlareBot.webSecret = config.getString("misc.web").get();
        }
        if (config.getString("bot.statusHook").isPresent())
            FlareBot.statusHook = config.getString("bot.statusHook").get();
        if (config.getString("botlists.botlist").isPresent()) {
            FlareBot.botListAuth = config.getString("botlists.botlist").get();
        }
        FlareBot.youtubeApi = config.getString("misc.yt").get();

        if (config.getArray("options").isPresent()) {
            Iterator<JsonElement> it = config.getArray("options").get().iterator();
            while (it.hasNext()) {
                JsonElement em = it.next();
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

        if (webSecret == null || webSecret.isEmpty()) apiEnabled = false;

        Thread.setDefaultUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        Thread.currentThread()
                .setUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        try {
            (instance = new FlareBot()).init(tkn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final char COMMAND_CHAR = '_';

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

    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMMM yyyy HH:mm:ss");

    private Set<Command> commands = ConcurrentHashMap.newKeySet();
    private PlayerManager musicManager;
    private long startTime;
    private static String secret = null;
    private static Prefixes prefixes;
    private AutoModTracker tracker;

    public static Prefixes getPrefixes() {
        return prefixes;
    }

    public void init(String tkn) throws InterruptedException, UnirestException, FileNotFoundException {
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
        //tracker = new AutoModTracker();
        LOGGER.info("Starting builders");
        try {
            JDABuilder builder = new JDABuilder(AccountType.BOT)
                    .addEventListener(events)
                    .setToken(tkn)
                    .setAudioSendFactory(new NativeAudioSendFactory());
            if (clients.length == 1) {
                clients[0] = builder.buildAsync();
                Thread.sleep(5000);
            } else {
                builder = builder.setReconnectQueue(new SessionReconnectQueue());
                for (int i = 0; i < clients.length; i++) {
                    clients[i] = builder.useSharding(i, clients.length).buildAsync();
                    Thread.sleep(5000); // 5 second backoff
                }
            }
            prefixes = new Prefixes();
            commands = ConcurrentHashMap.newKeySet();
            musicManager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(new JDAMultiShard(clients)));
            musicManager.getPlayerCreateHooks().register(player -> player.addEventListener(new AudioEventAdapter() {
                @Override
                public void onTrackEnd(AudioPlayer aplayer, AudioTrack atrack, AudioTrackEndReason reason) {
                    if (manager.getGuild(player.getGuildId()).isSongnickEnabled() && GeneralUtils.canChangeNick(player.getGuildId())) {
                        Guild c = getGuildByID(player.getGuildId());
                        if (c == null) {
                            manager.getGuild(player.getGuildId()).setSongnick(false);
                        } else {
                            if (player.getPlaylist().isEmpty())
                                c.getController().setNickname(c.getSelfMember(), null).queue();
                        }
                    } else {
                        if (!GeneralUtils.canChangeNick(player.getGuildId())) {
                            MessageUtils.sendPM(getGuildByID(player.getGuildId()).getOwner().getUser(),
                                    "FlareBot can't change it's nickname so SongNick has been disabled!");
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
                                        .build()).queue();
                            } else {
                                MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                            }
                        } else {
                            MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                        }
                    }
                    if (manager.getGuild(player.getGuildId()).isSongnickEnabled()) {
                        Guild c = getGuildByID(player.getGuildId());
                        if (c == null || !GeneralUtils.canChangeNick(player.getGuildId())) {
                            manager.getGuild(player.getGuildId()).setSongnick(false);
                            if (!GeneralUtils.canChangeNick(player.getGuildId())) {
                                MessageUtils.sendPM(getGuildByID(player.getGuildId()).getOwner().getUser(),
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
                                    .queue(MessageUtils.noOpConsumer(), MessageUtils.noOpConsumer());
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
            public void write(int b) throws IOException {
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
        registerCommand(new PlayCommand(this));
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
        registerCommand(new PurgeCommand());
        registerCommand(new EvalCommand());
        registerCommand(new MusicAnnounceCommand());
        registerCommand(new SetPrefixCommand());
        registerCommand(new AvatarCommand());
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
        registerCommand(new MuteCommand());
        registerCommand(new UnmuteCommand());
//        registerCommand(new TempBanCommand());

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

        ApiFactory.bind();

        //manager.executeCreations();

        startTime = System.currentTimeMillis();
        LOGGER.info("FlareBot v" + getVersion() + " booted!");

        //sendCommands();

        new FlarebotTask("FixThatStatus" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (!UpdateCommand.UPDATING.get())
                    setStatus("_help | _invite");
            }
        }.repeat(10, TimeUnit.SECONDS.toMillis(32));

        new FlarebotTask("PostDbotsData" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (FlareBot.dBotsAuth != null) {
                    postToBotList(FlareBot.dBotsAuth, String
                            .format("https://bots.discord.pw/api/bots/%s/stats", clients[0].getSelfUser().getId()));
                }
            }
        }.repeat(10, TimeUnit.MINUTES.toMillis(10));

        new FlarebotTask("PostBotlistData" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (FlareBot.botListAuth != null) {
                    postToBotList(FlareBot.botListAuth, String
                            .format("https://discordbots.org/api/bots/%s/stats", clients[0].getSelfUser().getId()));
                }
            }
        }.repeat(10, TimeUnit.MINUTES.toMillis(10));

        new FlarebotTask("UpdateWebsite" + System.currentTimeMillis()) {
            @Override
            public void run() {
                sendData();
            }
        }.repeat(10, TimeUnit.SECONDS.toMillis(30));

        new FlarebotTask("spam" + System.currentTimeMillis()) {
            @Override
            public void run() {
                Events.spamMap.clear();
            }
        }.repeat(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(3));

        new FlarebotTask("ClearConfirmMap" + System.currentTimeMillis()) {

            @Override
            public void run() {
                ConfirmUtil.clearConfirmMap();
            }

        }.repeat(10, TimeUnit.MINUTES.toMillis(1));

        setupUpdate();
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
            } catch (Exception e1) {
                FlareBot.LOGGER.error("Could not POST data to a botlist", e1);
            }
        }
    }

    private void setupUpdate() {
        new FlarebotTask("Auto-Update" + System.currentTimeMillis()) {
            @Override
            public void run() {
                quit(true);
            }
        }.delay(LocalDateTime.now().until(LocalDate.now()
                .plusDays(LocalDateTime.now().getHour() >= 13 ? 1 : 0)
                .atTime(13, 0, 0), ChronoUnit.MILLIS));
    }

    private Runtime runtime = Runtime.getRuntime();
    private JsonParser parser = new JsonParser();

    private void sendData() {
        /*JsonObject data = new JsonObject();
        data.addProperty("guilds", getGuilds().size());
        data.addProperty("official_guild_users", getGuildByID(OFFICIAL_GUILD).getMembers().size());
        data.addProperty("text_channels", getChannels().size());
        data.addProperty("voice_channels", getConnectedVoiceChannels().size());
        data.addProperty("active_voice_channels", getActiveVoiceChannels());
        data.addProperty("num_queued_songs", getGuilds().stream()
                .mapToInt(guild -> musicManager.getPlayer(guild.getId())
                        .getPlaylist().size()).sum());
        data.addProperty("ram", (((runtime.totalMemory() - runtime.freeMemory()) / 1024) / 1024) + "MB");
        data.addProperty("uptime", getUptime());

        postToApi("postData", "data", data);*/
    }

    private void sendCommands() {
        JsonArray array = new JsonArray();
        for (Command cmd : commands) {
            JsonObject cmdObj = new JsonObject();
            cmdObj.addProperty("command", cmd.getCommand());
            cmdObj.addProperty("description", cmd.getDescription());
            cmdObj.addProperty("permission", cmd.getPermission() == null ? "" : cmd.getPermission());
            cmdObj.addProperty("type", cmd.getType().toString());
            JsonArray aliases = new JsonArray();
            for (String s : cmd.getAliases())
                aliases.add(s);
            cmdObj.add("aliases", aliases);
            array.add(cmdObj);
        }

        postToApi("updateCommands", "commands", array);
    }

    private static volatile int api = 0;
    public static final ExecutorService API_THREAD_POOL =
            Executors.newCachedThreadPool(r -> new Thread(() -> {
                try {
                    r.run();
                } catch (Exception e) {
                    LOGGER.error("Error in " + Thread.currentThread(), e);
                }
            }, "API Thread " + api++));

    // TODO: Remove this in favour of the new API requester
    @Deprecated
    public String postToApi(String action, String property, JsonElement data) {
        if (!apiEnabled) return null;
        final String[] message = new String[1];
        CountDownLatch latch = new CountDownLatch(1);
        API_THREAD_POOL.submit(() -> {
            JsonObject object = new JsonObject();
            object.addProperty("secret", webSecret);
            object.addProperty("action", action);
            object.add(property, data);

            try {
                HttpsURLConnection con = (HttpsURLConnection) new URL(OLD_FLAREBOT_API + "update.php").openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 FlareBot");
                con.setRequestProperty("Content-Type", "application/json");

                OutputStream out = con.getOutputStream();
                out.write(object.toString().getBytes());
                out.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                JsonObject obj = parser.parse(br.readLine()).getAsJsonObject();
                int code = obj.get("code").getAsInt();

                if (code % 100 == 0 && code != 500) {
                    message[0] = obj.get("message").getAsString();
                } else {
                    LOGGER.error("Error updating site! " + obj.get("error").getAsString());
                }
                con.disconnect();
                latch.countDown();
            } catch (IOException e) {
                FlareBot.LOGGER
                        .error("Could not make POST request!\n\nDetails:\nAction: " + action + "\nProperty: " + property + "\nData: " + data
                                .toString(), e);
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return message[0];
    }

    @Deprecated
    public void postToApi(String endpoint, JSONObject body) {
        Unirest.post(OLD_FLAREBOT_API + endpoint).body(body).asJsonAsync();
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
                    System.out.println(line);
                    out += line + '\n';
                }
                p.waitFor();
                if (p.exitValue() != 0) {
                    UpdateCommand.UPDATING.set(false);
                    LOGGER.error("Could not update! Log:** {} **", MessageUtils.hastebin(out));
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
        LOGGER.info("Saving data.");
        EXITING.set(true);
        getImportantLogChannel().sendMessage("Average load time of this session: " + manager.getLoadTimes()
                .stream().mapToLong(v -> v).average().orElse(0) + "\nTotal loads: " + manager.getLoadTimes().size())
                .queue();
        for (ScheduledFuture<?> scheduledFuture : Scheduler.getTasks().values())
            scheduledFuture.cancel(false); // No tasks in theory should block this or cause issues. We'll see
        for (JDA client : clients)
            client.removeEventListener(events); //todo: Make a replacement for the array
        sendData();
        for (String s : manager.getGuilds().keySet()) {
            manager.saveGuild(s, manager.getGuilds().get(s), manager.getGuilds().getLastRetrieved(s));
        }
        LOGGER.info("Finished saving!");
    }

    private void registerCommand(Command command) {
        this.commands.add(command);
    }

    public Command getCommand(String s) {
        for (Command cmd : getCommands()) {
            if (cmd.getCommand().equalsIgnoreCase(s))
                return cmd;
            for (String alias : cmd.getAliases())
                if (alias.equalsIgnoreCase(s)) return cmd;
        }
        return null;
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
            clients[0].getPresence().setGame(Game.of(status, "https://www.twitch.tv/discordflarebot"));
            return;
        }
        for (JDA jda : clients)
            jda.getPresence().setGame(Game.of(status + " | Shard: " + (jda.getShardInfo().getShardId() + 1) + "/" +
                    clients.length, "https://www.twitch.tv/discordflarebot"));
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

    public static void reportError(TextChannel channel, String s, Exception e) {
        JsonObject message = new JsonObject();
        message.addProperty("message", s);
        message.addProperty("exception", ExceptionUtils.getStackTrace(e));
        String id = instance.postToApi("postReport", "error", message);
        channel.sendMessage(new EmbedBuilder().setColor(Color.red)
                .setDescription(s + "\nThe error has been reported! You can follow the report on the website, https://flarebot.stream/report?id=" + id)
                .build()).queue();
    }

    public static String getStatusHook() {
        return statusHook;
    }

    public AutoModTracker getAutoModTracker() {
        return tracker;
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
        return (testBot ? getChannelByID(FLARE_TEST_BOT_CHANNEL) : getChannelByID("226786557862871040"));
    }

    public TextChannel getGuildLogChannel() {
        return (testBot ? getChannelByID(FLARE_TEST_BOT_CHANNEL) : getChannelByID("260401007685664768"));
    }

    public TextChannel getEGLogChannel() {
        return (testBot ? getChannelByID(FLARE_TEST_BOT_CHANNEL) : getChannelByID("358950369642151937"));
    }

    public void logEG(String eg, Guild guild, User user) {
        getEGLogChannel().sendMessage(new EmbedBuilder().setTitle("Found `" + eg + "`")
                .addField("Guild", guild.getId() + " (`" + guild.getName() + "`) ", true)
                .addField("User", user.getAsMention() + " (`" + user.getName() + "#" + user.getDiscriminator() + "`)", true)
                .setTimestamp(LocalDateTime.now(Clock.systemUTC()))
                .build()).queue();
    }

    public TextChannel getImportantLogChannel() {
        return (testBot ? getChannelByID(FLARE_TEST_BOT_CHANNEL) : getChannelByID("358978253966278657"));
    }


    public static String getYoutubeKey() {
        return youtubeApi;
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

    public Guild getGuildByID(String id) {
        return getGuilds().stream().filter(g -> g.getId().equals(id)).findFirst().orElse(null);
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

    public List<VoiceChannel> getConnectedVoiceChannels() {
        return Arrays.stream(getClients()).flatMap(c -> c.getGuilds().stream())
                .map(c -> c.getAudioManager().getConnectedChannel())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
        return Arrays.stream(clients).map(jda -> {
            try {
                return jda.retrieveUserById(id).complete();
            } catch (ErrorResponseException ex) {
            }
            return null;
        }).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public DateTimeFormatter getTimeFormatter() {
        return this.timeFormat;
    }

    public String formatTime(LocalDateTime dateTime) {
        dateTime = LocalDateTime.from(dateTime.atOffset(ZoneOffset.UTC));
        return dateTime.getDayOfMonth() + getDayOfMonthSuffix(dateTime.getDayOfMonth()) + " " + dateTime
                .format(timeFormat) + " UTC";
    }

    private String getDayOfMonthSuffix(final int n) {
        if (n < 1 || n > 31) throw new IllegalArgumentException("illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public boolean isTestBot() {
        return testBot;
    }
}

package stream.flarebot.flarebot;

import ch.qos.logback.classic.Level;
import com.arsenarsen.githubwebhooks4j.GithubWebhooks4J;
import com.arsenarsen.githubwebhooks4j.WebhooksBuilder;
import com.arsenarsen.githubwebhooks4j.web.HTTPRequest;
import com.arsenarsen.githubwebhooks4j.web.Response;
import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.arsenarsen.lavaplayerbridge.utils.JDAMultiShard;
import com.google.gson.*;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.commons.cli.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.FlareBotManager;
import stream.flarebot.flarebot.commands.Prefixes;
import stream.flarebot.flarebot.commands.administrator.*;
import stream.flarebot.flarebot.commands.automod.AutoModCommand;
import stream.flarebot.flarebot.commands.automod.ModlogCommand;
import stream.flarebot.flarebot.commands.automod.SetSeverityCommand;
import stream.flarebot.flarebot.commands.general.*;
import stream.flarebot.flarebot.commands.music.*;
import stream.flarebot.flarebot.commands.secret.*;
import stream.flarebot.flarebot.github.GithubListener;
import stream.flarebot.flarebot.mod.AutoModTracker;
import stream.flarebot.flarebot.music.QueueListener;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.objects.Poll;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.permissions.Permissions;
import stream.flarebot.flarebot.scheduler.FlarebotTask;
import stream.flarebot.flarebot.util.ExceptionUtils;
import stream.flarebot.flarebot.util.SQLController;
import stream.flarebot.flarebot.util.Welcome;
import stream.flarebot.flarebot.web.ApiFactory;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class FlareBot {

    private static final Map<String, Logger> LOGGERS;
    public static final Logger LOGGER;

    private static FlareBot instance;
    public static String passwd;
    private static String youtubeApi;


    static {
        LOGGERS = new ConcurrentHashMap<>();
        LOGGER = getLog(FlareBot.class);
        new File("latest.log").delete();
    }

    private static String botListAuth;
    private static String dBotsAuth;
    private Permissions permissions;
    private GithubWebhooks4J gitHubWebhooks;
    private FlareBotManager manager;
    @SuppressWarnings("FieldCanBeLocal")
    public static final File PERMS_FILE = new File("perms.json");
    private static String webSecret;
    private static boolean apiEnabled = true;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final String OFFICIAL_GUILD = "226785954537406464";
    public static final String FLAREBOT_API = "https://flarebot.stream/api/";

    private Welcomes welcomes = new Welcomes();
    private File welcomeFile;
    private Map<String, PlayerCache> playerCache = new ConcurrentHashMap<>();
    CountDownLatch latch;
    private static String statusHook;
    private static String token;

    public static void main(String[] args) throws ClassNotFoundException, UnknownBindingException, InterruptedException, UnirestException {
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
        Options options = new Options();

        Option token = new Option("t", true, "Bot log in token");
        token.setArgName("token");
        token.setLongOpt("token");
        token.setRequired(true);
        options.addOption(token);

        Option sqlpassword = new Option("sql", true, "MySQL login password for user flare at localhost for database FlareBot");
        sqlpassword.setArgName("sqlpassword");
        sqlpassword.setLongOpt("sqlpassword");
        sqlpassword.setRequired(true);
        options.addOption(sqlpassword);

        Option secret = new Option("s", true, "Webhooks secret");
        secret.setArgName("secret");
        secret.setLongOpt("secret");
        secret.setRequired(false);
        options.addOption(secret);

        Option dBots = new Option("db", true, "Discord Bots token");
        dBots.setArgName("discord-bots");
        dBots.setLongOpt("discord-bots-token");
        dBots.setRequired(false);
        options.addOption(dBots);

        Option botList = new Option("bl", true, "Botlist token");
        botList.setArgName("botlist-token");
        botList.setLongOpt("botlist-token");
        botList.setRequired(false);
        options.addOption(botList);

        Option youtubeApi = new Option("yt", true, "YouTube search API token");
        youtubeApi.setArgName("yt-api-token");
        youtubeApi.setLongOpt("yt-api-token");
        youtubeApi.setRequired(true);
        options.addOption(youtubeApi);

        Option websiteSecret = new Option("websecret", true, "The website secret");
        websiteSecret.setArgName("web-secret");
        websiteSecret.setLongOpt("web-secret");
        websiteSecret.setRequired(false);
        options.addOption(websiteSecret);

        Option statusHook = new Option("sh", true, "Status webhook");
        statusHook.setArgName("statushook");
        statusHook.setLongOpt("status-hook");
        statusHook.setRequired(true);
        options.addOption(statusHook);

        String tkn;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine parsed = parser.parse(options, args);
            tkn = parsed.getOptionValue("t");
            passwd = parsed.getOptionValue("sql");
            Class.forName("stream.flarebot.flarebot.util.SQLController");
            if (parsed.hasOption("s"))
                FlareBot.secret = parsed.getOptionValue("s");
            if (parsed.hasOption("db"))
                FlareBot.dBotsAuth = parsed.getOptionValue("db");
            if (parsed.hasOption("web-secret"))
                FlareBot.webSecret = parsed.getOptionValue("web-secret");
            if (parsed.hasOption("debug")) {
                ((ch.qos.logback.classic.Logger) LoggerFactory
                        .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME))
                        .setLevel(Level.DEBUG);
            }
            if (parsed.hasOption("sh")) {
                FlareBot.statusHook = parsed.getOptionValue("sh");
            }
            if (parsed.hasOption("bl"))
                FlareBot.botListAuth = parsed.getOptionValue("bl");
            FlareBot.youtubeApi = parsed.getOptionValue("yt");
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter
                    .printHelp("java -jar FlareBot.jar", "FlareBot", options, "https://github.com/FlareBot/FlareBot", true);
            e.printStackTrace();
            return;
        }

        if (webSecret == null || webSecret.isEmpty()) apiEnabled = false;

        Thread.setDefaultUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        Thread.currentThread()
                .setUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        (instance = new FlareBot()).init(tkn);
    }

    public static final char COMMAND_CHAR = '_';

    public static String getToken() {
        return token;
    }

    public Events getEvents() {
        return events;
    }

    private Events events;
    private String version = null;
    private JDA[] clients;

    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMMM yyyy HH:mm:ss");

    private List<Command> commands = new CopyOnWriteArrayList<>();
    // Guild ID | List role ID
    private Map<String, CopyOnWriteArrayList<String>> autoAssignRoles;
    private File roleFile;
    private PlayerManager musicManager;
    private long startTime;
    private static String secret = null;
    private static Prefixes prefixes;
    private AutoModTracker tracker;

    public static Prefixes getPrefixes() {
        return prefixes;
    }

    public Permissions getPermissions() {
        return permissions;
    }

    public PerGuildPermissions getPermissions(MessageChannel channel) {
        return this.permissions.getPermissions(channel);
    }

    public void init(String tkn) throws InterruptedException, UnirestException {
        token = tkn;
        RestAction.DEFAULT_FAILURE = t -> {
        };
        clients = new JDA[Unirest.get("https://discordapp.com/api/gateway/bot")
                .header("Authorization", "Bot " + tkn)
                .asJson()
                .getBody()
                .getObject()
                .getInt("shards")];
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        latch = new CountDownLatch(1);
        events = new Events(this);
        tracker = new AutoModTracker();
        try {
            if (clients.length == 1) {
                while (true) {
                    try {
                        clients[0] = new JDABuilder(AccountType.BOT)
                                .addListener(events, tracker)
                                .setToken(tkn)
                                .setAudioSendFactory(new NativeAudioSendFactory())
                                .buildAsync();
                        break;
                    } catch (RateLimitedException e) {
                        Thread.sleep(e.getRetryAfter());
                    }
                }
            } else
                for (int i = 0; i < clients.length; i++) {
                    while (true) {
                        try {
                            clients[i] = new JDABuilder(AccountType.BOT)
                                    .addListener(events, tracker)
                                    .useSharding(i, clients.length)
                                    .setToken(tkn)
                                    .setAudioSendFactory(new NativeAudioSendFactory())
                                    .buildAsync();
                            break;
                        } catch (RateLimitedException e) {
                            Thread.sleep(e.getRetryAfter());
                        }
                    }
                }
            prefixes = new Prefixes();
            commands = new ArrayList<>();
            musicManager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(new JDAMultiShard(clients)));
            musicManager.getPlayerCreateHooks().register(player -> player.addEventListener(new AudioEventAdapter() {
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
                                        .addField("Now Playing: ", SongCommand.getLink(track), false)
                                        .addField("Duration: ", SongCommand
                                                .formatDuration(track), false)
                                        .addField("Requested by: ",
                                                String.format("<@!%s>", track.getMeta()
                                                        .get("requester")), false)
                                        .addField("Next up: ", playlist.isEmpty() ? "Nothing" :
                                                SongCommand.getLink(playlist.peek()), false)
                                        .build()).queue();
                            } else {
                                MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                            }
                        } else {
                            MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                        }
                    }
                }
            }));
            roleFile = new File("roles.json");
            loadRoles();
            loadPerms();
            welcomeFile = new File("welcomes.json");
            loadWelcomes();
            try {
                gitHubWebhooks = new WebhooksBuilder()
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
        manager.loadRandomSongs();
        manager.loadProfanity();
        manager.loadAutoMod();
        manager.loadLocalisation();

        loadPolls();
        loadSelfAssign();

        musicManager.getPlayerCreateHooks()
                .register(player -> player.getQueueHookManager().register(new QueueListener()));

        latch.await();
        run();
    }

    private void loadPerms() {
        if (PERMS_FILE.exists()) {
            try {
                permissions = GSON.fromJson(new FileReader(PERMS_FILE), Permissions.class);
                if (permissions == null) {
                    permissions = new Permissions();
                    try {
                        permissions.save();
                    } catch (IOException e1) {
                        LOGGER.error("Could not create PERMS_FILE!", e1);
                    }
                }
            } catch (JsonIOException | JsonSyntaxException e) {
                LOGGER.error("Could not parse permissions! Ignoring and making new.");
                permissions = new Permissions();
                try {
                    permissions.save();
                } catch (IOException e1) {
                    LOGGER.error("Could not create PERMS_FILE!", e1);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                PERMS_FILE.createNewFile();
                permissions = new Permissions();
                permissions.save();
            } catch (IOException e) {
                LOGGER.error("Could not create PERMS_FILE!", e);
            }

        }
    }

    protected void run() {
        registerCommand(new HelpCommand());
        registerCommand(new SearchCommand(this));
        registerCommand(new JoinCommand());
        registerCommand(new LeaveCommand());
        registerCommand(new InfoCommand());
        registerCommand(new ResumeCommand(this));
        registerCommand(new PlayCommand(this));
        registerCommand(new PauseCommand(this));
        registerCommand(new StopCommand(this));
        registerCommand(new SkipCommand(this));
        registerCommand(new ShuffleCommand(this));
        registerCommand(new PlaylistCommand(this));
        registerCommand(new SongCommand(this));
        registerCommand(new InviteCommand());
        registerCommand(new AutoAssignCommand(this));
        registerCommand(new QuitCommand());
        registerCommand(new RolesCommand());
        registerCommand(new WelcomeCommand(this));
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
        registerCommand(new AutoModCommand());
        registerCommand(new ModlogCommand());
        registerCommand(new SetSeverityCommand());
        registerCommand(new TestCommand());
        registerCommand(new BanCommand());
        registerCommand(new ReportsCommand());

        ApiFactory.bind();

        manager.executeCreations();

        startTime = System.currentTimeMillis();
        LOGGER.info("FlareBot v" + getVersion() + " booted!");

        sendCommands();
        sendPrefixes();

        new FlarebotTask("AutoSave" + System.currentTimeMillis()) {
            @Override
            public void run() {
                try {
                    getPermissions().save();
                    saveWelcomes();
                    manager.saveLocalisation();

                } catch (IOException e) {
                    LOGGER.error("Could not save permissions!", e);
                }
            }
        }.repeat(300000, 60000);
        new FlarebotTask("FixThatStatus" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (!UpdateCommand.UPDATING.get())
                    setStatus("_help | _invite");
            }
        }.repeat(10, 32000);
        new FlarebotTask("PostDbotsData" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (FlareBot.dBotsAuth != null) {
                    postToBotlist(FlareBot.dBotsAuth, String
                            .format("https://bots.discord.pw/api/bots/%s/stats", clients[0].getSelfUser().getId()));
                }
            }
        }.repeat(10, 600000);
        new FlarebotTask("PostBotlistData" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (FlareBot.botListAuth != null) {
                    postToBotlist(FlareBot.botListAuth, String
                            .format("https://discordbots.org/api/bots/%s/stats", clients[0].getSelfUser().getId()));
                }
            }
        }.repeat(10, 600000);

        new FlarebotTask("UpdateWebsite" + System.currentTimeMillis()) {
            @Override
            public void run() {
                sendData();
            }
        }.repeat(10, 30000);

        new FlarebotTask("PollChecker" + System.currentTimeMillis()) {
            @Override
            public void run() {
                for (Poll poll : getManager().getPolls().values()) {
                    if (poll.isOpen()) {
                        if (LocalDateTime.now().until(poll.getEndTime(), ChronoUnit.SECONDS) <= 0) {
                            poll.setStatus(Poll.PollStatus.CLOSED);
                        }
                    }
                }
            }
        }.repeat(1000, 1000);

        setupUpdate();

        Scanner scanner = new Scanner(System.in);

        try {
            if (scanner.next().equalsIgnoreCase("exit")) {
                quit(false);
            } else if (scanner.next().equalsIgnoreCase("update")) {
                quit(true);
            }
        } catch (NoSuchElementException ignored) {
        }
    }

    private void postToBotlist(String auth, String url) {
        for (JDA client : clients) {
            if (clients.length == 1) {
                Unirest.post(url)
                        .header("Authorization", auth)
                        .header("User-Agent", "Mozilla/5.0 FlareBot")
                        .header("Content-Type", "application/json")
                        .body(new JSONObject()
                                .put("server_count", client.getGuilds().size()))
                        .asStringAsync();
                return;
            }
            try {
                Unirest.post(url)
                        .header("Authorization", auth)
                        .header("User-Agent", "Mozilla/5.0 FlareBot")
                        .header("Content-Type", "application/json")
                        .body(new JSONObject()
                                .put("server_count", client.getGuilds().size())
                                .put("shard_id", client.getShardInfo().getShardId())
                                .put("shard_count", client.getShardInfo().getShardTotal()))
                        .asStringAsync();
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
        JsonObject data = new JsonObject();
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

        postToApi("postData", "data", data);
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

    private void sendPrefixes() {
        JsonArray array = new JsonArray();
        for (Guild guild : getGuilds()) {
            JsonObject object = new JsonObject();
            object.addProperty("guildId", guild.getId());
            object.addProperty("prefix", prefixes.getPrefixes().getOrDefault(guild.getId(), FlareBot.COMMAND_CHAR));
            array.add(object);
        }

        postToApi("updatePrefixes", "prefixes", array);
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
                HttpsURLConnection con = (HttpsURLConnection) new URL(FLAREBOT_API + "update.php").openConnection();
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

                if (code % 100 == 0) {
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

    public void quit(boolean update) {
        if (update) {
            LOGGER.info("Updating bot!");
            try {
                File git = new File("FlareBot" + File.separator);
                if (!(git.exists() && git.isDirectory())) {
                    ProcessBuilder clone = new ProcessBuilder("git", "clone", "https://github.com/FlareBot/FlareBot.git", git
                            .getAbsolutePath());
                    clone.redirectErrorStream(true);
                    Process p = clone.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String out = "";
                    String line;
                    if ((line = reader.readLine()) != null) {
                        out += line + '\n';
                    }
                    if (p.exitValue() != 0) {
                        LOGGER.error("Could not update!!!!\n" + out);
                        UpdateCommand.UPDATING.set(false);
                        return;
                    }
                } else {
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
                    LOGGER.error("Could not update! Log:** {} **", MessageUtils.hastebin(out));
                    return;
                }
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
        try {
            saveRoles();
            saveWelcomes();
            sendData();
            savePolls();
            saveSelfAssign();
            manager.saveAutoMod();
            manager.saveLocalisation();
        } catch (Exception e) {
            LOGGER.error("Something failed on stop!", e);
        }
    }

    private void saveSelfAssign() {
        for (String guild : getManager().getSelfAssignRoles().keySet()) {
            try {
                SQLController.runSqlTask(conn -> {
                    PreparedStatement statement = conn
                            .prepareStatement("INSERT INTO selfassign (guild_id, roles) VALUES (?, ?) ON DUPLICATE KEY UPDATE roles = VALUES(roles)");
                    statement.setString(1, guild);
                    statement.setString(2, getManager().getSelfAssignRoles(guild).stream()
                            .collect(Collectors.joining(",")));
                    statement.execute();
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSelfAssign() {
        try {
            SQLController.runSqlTask(conn -> {
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM selfassign");
                while (set.next()) {
                    getManager().getSelfAssignRoles().put(set.getString("guild_id"),
                            Arrays.stream(set.getString("roles").replaceAll(" ", "").split(","))
                                    .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet)));
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void savePolls() {
        for (String guildId : getManager().getPolls().keySet()) {
            Guild g = getGuildByID(guildId);
            if (g == null)
                continue;
            String pollJson = GSON.toJson(getManager().getPollFromGuild(g));

            try {
                SQLController.runSqlTask((conn) -> {
                    PreparedStatement statement = conn
                            .prepareStatement("INSERT INTO polls (guild_id, poll) VALUES (?, ?) ON DUPLICATE KEY UPDATE poll = poll");
                    statement.setString(1, guildId);
                    statement.setString(2, pollJson);
                    statement.execute();
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPolls() {
        try {
            SQLController.runSqlTask((conn) -> {
                ResultSet set = conn.createStatement().executeQuery("SELECT * FROM polls");
                while (set.next()) {
                    getManager().getPolls()
                            .put(set.getString("guild_id"), GSON.fromJson(set.getString("poll"), Poll.class));
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registerCommand(Command command) {
        this.commands.add(command);
    }

    public List<Command> getCommands() {
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

    public Map<String, CopyOnWriteArrayList<String>> getAutoAssignRoles() {
        return this.autoAssignRoles;
    }

    private void loadRoles() {
        autoAssignRoles = new HashMap<>();
        if (roleFile.exists()) {
            try {
                // Check if it is empty since Gson doesn't like empty files -_-
                BufferedReader br = new BufferedReader(new FileReader(roleFile));
                if (br.readLine() == null) return; // File is empty.
                JsonParser parser = new JsonParser();
                JsonObject parsed = parser.parse(new FileReader(roleFile)).getAsJsonObject();
                JsonArray guilds = parsed.getAsJsonArray("guilds");
                for (JsonElement o : guilds) {
                    JsonObject guild = o.getAsJsonObject();
                    List<String> roles = autoAssignRoles
                            .computeIfAbsent(guild.get("guildId").getAsString(), key -> new CopyOnWriteArrayList<>());
                    for (JsonElement e : guild.get("roles").getAsJsonArray()) {
                        if (!roles.contains(e.getAsString()))
                            roles.add(e.getAsString());
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load roles!", e);
            }
        } else {
            try {
                roleFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Failed to load roles!", e);
            }
        }
    }

    private void saveRoles() {
        if (!roleFile.exists()) {
            try {
                roleFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Failed to save roles!", e);
            }
        }

        JsonObject obj = new JsonObject();
        JsonArray guildsArray = new JsonArray();

        for (String guild : autoAssignRoles.keySet()) {
            JsonObject guildObj = new JsonObject();
            guildObj.addProperty("guildId", guild);
            JsonArray roles = new JsonArray();
            autoAssignRoles.get(guild).forEach(roles::add);
            guildObj.add("roles", roles);
            guildsArray.add(guildObj);
        }
        obj.add("guilds", guildsArray);

        try {
            FileWriter fw = new FileWriter(roleFile);
            fw.write(GSON.toJson(obj));
            fw.flush();
            fw.close();
        } catch (IOException e) {
            LOGGER.error("Failed to save roles!", e);
        }
    }

    public void loadWelcomes() {
        try {
            if (welcomeFile.exists()) {
                welcomes = GSON.fromJson(new FileReader(welcomeFile), Welcomes.class);
            } else {
                welcomeFile.createNewFile();
                welcomes = new Welcomes();
                saveWelcomes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveWelcomes() {
        if (!welcomeFile.exists()) {
            try {
                welcomeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fw = new FileWriter(welcomeFile);
            fw.write(GSON.toJson(welcomes));
            fw.flush();
            fw.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public GithubWebhooks4J getWebHooks() {
        return this.gitHubWebhooks;
    }

    public List<Welcome> getWelcomes() {
        return welcomes;
    }

    public Welcome getWelcomeForGuild(Guild guild) {
        for (Welcome welcome : welcomes) {
            if (welcome.getGuildId().equals(guild.getId()))
                return welcome;
        }
        return null;
    }

    public String getInvite() {
        return String.format("https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot&permissions=372337664",
                clients[0].getSelfUser().getId());
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

    public static class Welcomes extends CopyOnWriteArrayList<Welcome> {
    }

    public TextChannel getUpdateChannel() {
        return getChannelByID("226786557862871040");
    }

    public TextChannel getGuildLogChannel() {
        return getChannelByID("260401007685664768");
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

    public Map<String, PlayerCache> getPlayerCache() {
        return this.playerCache;
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

    public DateTimeFormatter getTimeFormatter() {
        return this.timeFormat;
    }

    public String formatTime(LocalDateTime dateTime) {
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

    private TextChannel getModLogChannel(String guildId) {
        return (this.getManager().getAutoModGuild(guildId).getConfig().isEnabled()
                ? getChannelByID(getManager().getAutoModGuild(guildId).getConfig().getModLogChannel()) : null);
    }
}

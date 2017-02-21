package com.bwfcwalshy.flarebot;

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
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.commands.FlareBotManager;
import com.bwfcwalshy.flarebot.commands.Prefixes;
import com.bwfcwalshy.flarebot.commands.administrator.*;
import com.bwfcwalshy.flarebot.commands.general.*;
import com.bwfcwalshy.flarebot.commands.music.*;
import com.bwfcwalshy.flarebot.commands.secret.*;
import com.bwfcwalshy.flarebot.github.GithubListener;
import com.bwfcwalshy.flarebot.music.QueueListener;
import com.bwfcwalshy.flarebot.objects.PlayerCache;
import com.bwfcwalshy.flarebot.permissions.PerGuildPermissions;
import com.bwfcwalshy.flarebot.permissions.Permissions;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import com.bwfcwalshy.flarebot.util.Welcome;
import com.bwfcwalshy.flarebot.web.ApiFactory;
import com.google.gson.*;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sun.management.OperatingSystemMXBean;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.commons.cli.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class FlareBot {

    private static FlareBot instance;
    public static String passwd;
    private static String youtubeApi;

    static {
        new File("latest.log").delete();
    }

    private static final Map<String, Logger> LOGGERS = new ConcurrentHashMap<>();
    public static final Logger LOGGER = getLog(FlareBot.class);
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

    public static void main(String[] args) throws ClassNotFoundException, UnknownBindingException, InterruptedException, UnirestException {
        RestAction.DEFAULT_FAILURE = t -> LOGGER.error("RestAction failed!", t);
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

        String tkn;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine parsed = parser.parse(options, args);
            tkn = parsed.getOptionValue("t");
            passwd = parsed.getOptionValue("sql");
            Class.forName("com.bwfcwalshy.flarebot.util.SQLController");
            if (parsed.hasOption("s"))
                FlareBot.secret = parsed.getOptionValue("s");
            if (parsed.hasOption("db"))
                FlareBot.dBotsAuth = parsed.getOptionValue("db");
            if (parsed.hasOption("web-secret"))
                FlareBot.webSecret = parsed.getOptionValue("web-secret");
            if (parsed.hasOption("debug")) {
                ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME))
                        .setLevel(Level.DEBUG);
            }
            FlareBot.youtubeApi = parsed.getOptionValue("yt");
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar FlareBot.jar", "FlareBot", options, "https://github.com/FlareBot/FlareBot", true);
            e.printStackTrace();
            return;
        }

        if (webSecret == null || webSecret.isEmpty()) apiEnabled = false;

        Thread.setDefaultUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        Thread.currentThread().setUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        (instance = new FlareBot()).init(tkn);
    }

    public static final char COMMAND_CHAR = '_';

    private String version = null;
    private JDA[] clients;

    private List<Command> commands = new CopyOnWriteArrayList<>();
    // Guild ID | List role ID
    private Map<String, CopyOnWriteArrayList<String>> autoAssignRoles;
    private File roleFile;
    private PlayerManager musicManager;
    private long startTime;
    private static String secret = null;
    private static Prefixes prefixes;

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
        clients = new JDA[Unirest.get("https://discordapp.com/api/gateway/bot")
                .header("Authorization", "Bot " + tkn)
                .asJson()
                .getBody()
                .getObject()
                .getInt("shards")];
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        latch = new CountDownLatch(clients.length);
        Events events = new Events(this);
        try {
            if (clients.length == 1) {
                while (true) {
                    try {
                        clients[0] = new JDABuilder(AccountType.BOT)
                                .addListener(events)
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
                                    .addListener(events)
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
                            if (!c.canTalk()) {
                                MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                            }
                            Track track = player.getPlayingTrack();
                            c.sendMessage(MessageUtils.getEmbed()
                                    .addField("Now Playing: ", SongCommand.getLink(track), false).build()).queue();
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

        musicManager.getPlayerCreateHooks().register(player -> player.getQueueHookManager().register(new QueueListener()));

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
        registerCommand(new SearchCommand());
        registerCommand(new JoinCommand());
        registerCommand(new LeaveCommand());
        registerCommand(new InfoCommand());
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
        registerCommand(new LogsCommands());
        registerCommand(new LoopCommand());
        registerCommand(new LoadCommand());
        registerCommand(new SaveCommand());
        registerCommand(new DeleteCommand());
        registerCommand(new PlaylistsCommand());
        registerCommand(new Purge());
        registerCommand(new Eval());
        registerCommand(new MusicAnnounceCommand());
        registerCommand(new SetPrefixCommand());
        registerCommand(new AvatarCommand());
        registerCommand(new RandomCommand());
        registerCommand(new UserInfoCommand());

        ApiFactory.bind();

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
        new FlarebotTask("PostData" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (FlareBot.dBotsAuth != null) {
                    try {
                        Unirest.post("https://bots.discord.pw/api/bots/" + clients[0].getSelfUser().getId() + "/stats")
                                .header("Authorization", FlareBot.dBotsAuth)
                                .header("Content-Type", "application/json")
                                .body(new JSONObject().put("server_count", getGuilds().size()))
                                .asString();
                    } catch (Exception e1) {
                        FlareBot.LOGGER.error("Could not POST data to DBots", e1);
                    }
                }
            }
        }.repeat(10, 600000);

        new FlarebotTask("UpdateWebsite" + System.currentTimeMillis()) {
            @Override
            public void run() {
                sendData();
            }
        }.repeat(10, 30000);

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
        data.addProperty("num_queued_songs", getGuilds().stream().mapToInt(guild -> musicManager.getPlayer(guild.getId()).getPlaylist().size()).sum());
        data.addProperty("ram", (((runtime.totalMemory() - runtime.freeMemory()) / 1024) / 1024) + "MB");
        data.addProperty("cpu", ((int) (ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class).getSystemCpuLoad() * 10000)) / 100f + "%");
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
            if (prefixes.getPrefixes().containsKey(guild.getId()))
                object.addProperty("prefix", prefixes.getPrefixes().get(guild.getId()));
            else
                object.addProperty("prefix", FlareBot.COMMAND_CHAR);
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

    public void postToApi(String action, String property, JsonElement data) {
        if (!apiEnabled) return;
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
                    LOGGER.info(code + " - " + obj.get("message").getAsString());
                } else {
                    LOGGER.error("Error updating site! " + obj.get("error").getAsString());
                }
                con.disconnect();
            } catch (IOException e) {
                FlareBot.LOGGER.error("Could not make POST request!\n\nDetails:\nAction: " + action + "\nProperty: " + property + "\nData: " + data.toString(), e);
            }
        });
    }

    public void quit(boolean update) {
        if (update) {
            LOGGER.debug("Updating bot!");
            try {
                File git = new File("FlareBot" + File.separator);
                if (!git.exists() || !git.isDirectory()) {
                    ProcessBuilder clone = new ProcessBuilder("git", "clone", "https://github.com/FlareBot/FlareBot.git", git.getAbsolutePath());
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
                ProcessBuilder maven = new ProcessBuilder("mvn", "clean", "package", "-e");
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
                File current = new File(URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")); // pfft this will go well..
                Files.copy(current.toPath(), Paths.get(current.getPath().replace(".jar", ".backup.jar")), StandardCopyOption.REPLACE_EXISTING);
                File built = new File(git, "target" + File.separator + "FlareBot-jar-with-dependencies.jar");
                Files.copy(built.toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.exit(0);
            } catch (InterruptedException | IOException e) {
                LOGGER.error("Could not update!", e);
                setupUpdate();
                UpdateCommand.UPDATING.set(false);
            }
        } else
            LOGGER.debug("Exiting.");
        stop();
        System.exit(0);
    }

    protected void stop() {
        LOGGER.debug("Saving data.");
        saveRoles();
        saveWelcomes();
        sendData();
        try {
            permissions.save();
        } catch (IOException e) {
            LOGGER.error("Perms save failed on stop!", e);
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
                    List<String> roles = autoAssignRoles.computeIfAbsent(guild.get("guildId").getAsString(), key -> new CopyOnWriteArrayList<>());
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

    public User getUserByID(String id) {
        return Arrays.stream(clients).map(jda -> jda.getUserById(id))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }
}

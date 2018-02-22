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
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.requests.RestAction;
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
import stream.flarebot.flarebot.audio.PlayerListener;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandManager;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.database.RedisController;
import stream.flarebot.flarebot.music.QueueListener;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.scheduler.FlareBotTask;
import stream.flarebot.flarebot.scheduler.FutureAction;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.MigrationHandler;
import stream.flarebot.flarebot.util.ShardUtils;
import stream.flarebot.flarebot.util.WebUtils;
import stream.flarebot.flarebot.util.buttons.ButtonUtil;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.objects.ButtonGroup;
import stream.flarebot.flarebot.web.ApiFactory;
import stream.flarebot.flarebot.web.DataInterceptor;

public class FlareBot {

    public static final Logger LOGGER;
    public static final Gson GSON = new GsonBuilder().create();
    public static final AtomicBoolean RUNNING = new AtomicBoolean(false);
    public static final AtomicBoolean EXITING = new AtomicBoolean(false);
    public static final AtomicBoolean UPDATING = new AtomicBoolean(false);
    public static final AtomicBoolean NOVOICE_UPDATING = new AtomicBoolean(false);
    private static final Map<String, Logger> LOGGERS;
    private static FlareBot instance;
    private static String youtubeApi;
    private static JSONConfig config;
    private static boolean apiEnabled = true;
    private static boolean testBot = false;
    private static OkHttpClient client =
            new OkHttpClient.Builder().connectionPool(new ConnectionPool(4, 10, TimeUnit.SECONDS))
                    .addInterceptor(new DataInterceptor()).build();
    private static String version = null;

    static {
        handleLogArchive();
        LOGGERS = new ConcurrentHashMap<>();
        LOGGER = getLog(FlareBot.class.getName());
    }

    private FlareBotManager manager;
    private Map<String, PlayerCache> playerCache = new ConcurrentHashMap<>();
    private Events events;
    private ShardManager shardManager;
    private PlayerManager musicManager;
    private long startTime;
    private Runtime runtime = Runtime.getRuntime();
    private WebhookClient importantHook;
    private CommandManager commandManager;

    private AnalyticsHandler analyticsHandler;

    private Set<FutureAction> futureActions;

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

    public static OkHttpClient getOkHttpClient() {
        return client;
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

    public static String getYoutubeKey() {
        return youtubeApi;
    }

    private static Logger getLog(String name) {
        return LOGGERS.computeIfAbsent(name, LoggerFactory::getLogger);
    }

    public static Logger getLog(Class<?> clazz) {
        return getLog(clazz.getName());
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

    public static String getVersion() {
        if (version == null) {
            Properties p = new Properties();
            try {
                p.load(FlareBot.class.getClassLoader().getResourceAsStream("version.properties"));
            } catch (IOException e) {
                LOGGER.error("There was an error trying to load the version!", e);
                return null;
            }
            version = (String) p.get("version");
        }
        return version;
    }

    public static String getInvite() {
        return String.format("https://discordapp.com/oauth2/authorize?client_id=%s&scope=bot&permissions=%s",
                Getters.getSelfUser().getId(), Permission.getRaw(Permission.MESSAGE_WRITE, Permission.MESSAGE_READ,
                        Permission.MANAGE_ROLES, Permission.MESSAGE_MANAGE, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK,
                        Permission.VOICE_MOVE_OTHERS, Permission.KICK_MEMBERS, Permission.BAN_MEMBERS,
                        Permission.MANAGE_CHANNEL, Permission.MESSAGE_EMBED_LINKS, Permission.NICKNAME_CHANGE,
                        Permission.MANAGE_PERMISSIONS, Permission.VIEW_AUDIT_LOGS, Permission.MESSAGE_HISTORY,
                        Permission.MANAGE_WEBHOOKS, Permission.MANAGE_SERVER, Permission.MESSAGE_ADD_REACTION));
    }

    public static FlareBot instance() {
        return instance;
    }

    public Events getEvents() {
        return events;
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
            commandManager = new CommandManager();
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
                    PlayerManager.getPlayerManager(LibraryFactory.getLibrary(new JDAMultiShard(Getters.getShardManager())));
        } catch (UnknownBindingException e) {
            LOGGER.error("Failed to initialize musicManager", e);
        }
        musicManager.getPlayerCreateHooks()
                .register(player -> player.getQueueHookManager().register(new QueueListener()));

        // Any migration
        MigrationHandler migrationHandler = new MigrationHandler();

        LOGGER.info("Loaded " + commandManager.count() + " commands!");

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
     * This will always return the main shard or just the client itself.
     * For reference the main shard will always be shard 0 - the shard responsible for DMs
     *
     * @return The main shard or actual client in the case of only 1 shard.
     */
    public JDA getClient() {
        return shardManager.getShards().get(0);
    }


    private void loadFutureTasks() {
        futureActions = new ConcurrentHashSet<>();
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
                WebUtils.asyncRequest(request.post(body));
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
                WebUtils.asyncRequest(request.post(body));

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

    private void sendData() {
        JSONObject data = new JSONObject()
                .put("guilds", Getters.getGuilds().size())
                //.put("loaded_guilds", FlareBotManager.instance().getGuilds().size())
                .put("official_guild_users", Getters.getGuildById(Constants.OFFICIAL_GUILD).getMembers().size())
                .put("text_channels", Getters.getChannels().size())
                .put("voice_channels", Getters.getVoiceChannels().size())
                .put("connected_voice_channels", Getters.getConnectedVoiceChannels())
                .put("active_voice_channels", Getters.getActiveVoiceChannels())
                .put("num_queued_songs", Getters.getGuilds().stream()
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
        for (Command cmd : commandManager.getCommands()) {
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
                        UPDATING.set(false);
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
                        UPDATING.set(false);
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
                    UPDATING.set(false);
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
                UPDATING.set(false);
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
        Constants.getImportantLogChannel().sendMessage("Average load time of this session: " + manager.getGuildWrapperLoader().getLoadTimes()
                .stream().mapToLong(v -> v).average().orElse(0) + "\nTotal loads: " + manager.getGuildWrapperLoader().getLoadTimes().size() + "\nButton info: " + getButtonInfo())
                .complete();
        for (ScheduledFuture<?> scheduledFuture : Scheduler.getTasks().values())
            scheduledFuture.cancel(false); // No tasks in theory should block this or cause issues. We'll see
        for (JDA client : shardManager.getShards())
            client.removeEventListener(events); //todo: Make a replacement for the array
        sendData();
        manager.getGuilds().invalidateAll();
        shardManager.shutdown();
        LOGGER.info("Finished saving!");
        for (JDA client : shardManager.getShards())
            client.shutdown();
    }

    public String getButtonInfo() {
        Iterator<Map.Entry<String, ButtonGroup>> it = ButtonUtil.getButtonMessages().entrySet().iterator();
        int total = 0;
        StringBuilder groupsBuilder = new StringBuilder();
        while (it.hasNext()) {
            int groupTotal = 0;
            Map.Entry<String, ButtonGroup> pair = it.next();
            String messageIdString = pair.getKey();
            Long messageId = Long.valueOf(messageIdString);
            ButtonGroup buttonGroup = pair.getValue();
            StringBuilder buttonsBuilder = new StringBuilder();
            for (ButtonGroup.Button button : buttonGroup.getButtons()) {
                StringBuilder buttonBuilder = new StringBuilder();
                if(button.getUnicode() != null) {
                    buttonBuilder.append("\tUnicode: ").append(button.getUnicode());
                } else {
                    buttonBuilder.append("\tEmote Id: ").append(button.getEmoteId());
                }
                buttonBuilder.append(" Clicks: ").append(button.getClicks());
                groupTotal += button.getClicks();
                buttonsBuilder.append(buttonBuilder.toString()).append("\n");
            }
            double average = 0;
            if(events.getButtonClicksPerSec().containsKey(messageId)) {
                List<Double> clicks = events.getButtonClicksPerSec().get(messageId);
                double combine = 0.0;
                for (double clicksPerSec : clicks) {
                    combine += clicksPerSec;
                }
                average = combine / (double) clicks.size();
            }

            groupsBuilder.append("Button Clicks on message: ").append(messageId).append(" Clicks: ").append(groupTotal).append(" Average clicks/sec: ")
                    .append(average).append(" Max clicks/sec: ").append(events.getMaxButtonClicksPerSec().get(messageId))
                    .append("\n").append(buttonsBuilder.toString()).append("\n");
            total += groupTotal;
            it.remove();
        }
        return MessageUtils.paste("Total clicks: " + total + "\n" +groupsBuilder.toString());
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


    public FlareBotManager getManager() {
        return this.manager;
    }

    public PlayerCache getPlayerCache(String userId) {
        this.playerCache.computeIfAbsent(userId, k -> new PlayerCache(userId, null, null, null));
        return this.playerCache.get(userId);
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

    public ShardManager getShardManager() {
        return shardManager;
    }

    public boolean isApiDisabled() {
        return !apiEnabled;
    }

    private WebhookClient getImportantWebhook() {
        if (!config.getString("bot.importantHook").isPresent())
            return null;
        if (importantHook == null)
            importantHook = new WebhookClientBuilder(config.getString("bot.importantHook").get()).build();
        return importantHook;
    }

    public void runTasks() {
        new FlareBotTask("FixThatStatus") {
            @Override
            public void run() {
                if (!UPDATING.get())
                    setStatus("_help | _invite");
            }
        }.repeat(10, TimeUnit.SECONDS.toMillis(32));

        new FlareBotTask("PostDbotsData") {
            @Override
            public void run() {
                if (config.getString("botlists.discordBots").isPresent()) {
                    postToBotList(config.getString("botlists.discordBots").get(), String
                            .format("https://bots.discord.pw/api/bots/%s/stats", Getters.getSelfUser().getId()));
                }
            }
        }.repeat(10, TimeUnit.MINUTES.toMillis(10));

        new FlareBotTask("PostBotlistData") {
            @Override
            public void run() {
                if (config.getString("botlists.botlist").isPresent()) {
                    postToBotList(config.getString("botlists.botlist").get(), String
                            .format("https://discordbots.org/api/bots/%s/stats", Getters.getSelfUser().getId()));
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
                                        .put("servercount", Getters.getGuilds().size())
                                        .put("shardcount", Getters.getShards().size())
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
                events.clearButtons();
            }
        }.repeat(TimeUnit.SECONDS.toMillis(3), TimeUnit.SECONDS.toMillis(3));

        new FlareBotTask("DeadShard-Checker") {
            @Override
            public void run() {
                if (getImportantWebhook() == null) {
                    LOGGER.warn("No webhook for the important-log channel! Due to this the dead shard checker has been disabled!");
                    cancel();
                    return;
                }
                if (Getters.getShards().size() == 1) {
                    LOGGER.warn("Single sharded bot, the DeadShard-Checker has been disabled!");
                    cancel();
                    return;
                }
                // 10 mins without an event... this son bitch is dead.
                if (Getters.getShards().stream().anyMatch(shard -> ShardUtils.isDead(shard, TimeUnit.MINUTES.toMillis(10)))) {
                    Getters.getShards().stream().filter(shard -> ShardUtils.isDead(shard, TimeUnit.MINUTES.toMillis(10)))
                            .forEach(shard -> {
                                getImportantWebhook().send("Restarting " + ShardUtils.getShardId(shard)
                                        + " as it seems to be dead.");
                                shardManager.restart(ShardUtils.getShardId(shard));
                            });
                }

                Set<Integer> deadShards =
                        Getters.getShards().stream().filter(ShardUtils::isDead).map(ShardUtils::getShardId)
                                .collect(Collectors.toSet());

                if (!deadShards.isEmpty()) {
                    getImportantWebhook().send("Found " + deadShards.size() + " possibly dead shards! Shards: " +
                            deadShards.toString());
                }
            }
        }.repeat(TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(5));

        new FlareBotTask("GuildCleanup") {
            @Override
            public void run() {
                FlareBotManager.instance().getGuilds().cleanUp();
            }
        }.repeat(0, TimeUnit.SECONDS.toMillis(30));

        new FlareBotTask("ActivityChecker") {
            @Override
            public void run() {
                for (VoiceChannel channel : Getters.getConnectedVoiceChannelList()) {
                    if (channel.getMembers().stream().noneMatch(member -> member.getUser().isFake() || member.getUser().isBot()))
                        return;
                    if (manager.getLastActive().containsKey(channel.getGuild().getIdLong())) {
                        if (System.currentTimeMillis() >= (manager.getLastActive().get(channel.getGuild().getIdLong()) + TimeUnit.MINUTES.toMillis(10)))
                            channel.getGuild().getAudioManager().closeAudioConnection();
                    } else
                        manager.getLastActive().put(channel.getGuild().getIdLong(), System.currentTimeMillis());
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

    public static CommandManager getCommandManager() {
        return FlareBot.instance().commandManager;
    }

    public void migrations() {
        CassandraController.runTask(session -> {
            session.execute("CREATE TABLE IF NOT EXISTS flarebot.announces (" +
                    "guild_id varchar PRIMARY KEY," +
                    "channel_id varchar)");
            ResultSet set = session.execute("SELECT * FROM flarebot.announces");
            Row row;
            while ((row = set.one()) != null) {
                FlareBotManager.instance().getGuild(row.getString(0)).setMusicAnnounceChannelId(row.getString(1));
            }
            session.execute("CREATE TABLE IF NOT EXISTS flarebot.prefixes (" +
                    "guild_id varchar PRIMARY KEY, " +
                    "prefix varchar" +
                    ")");
            set = session.execute("SELECT * FROM flarebot.prefixes;");
            while ((row = set.one()) != null) {
                FlareBotManager.instance().getGuild(row.getString(0)).setPrefix(row.getString(1).charAt(0));
            }
        });
    }

    public Set<FutureAction> getFutureActions() {
        return futureActions;
    }
}

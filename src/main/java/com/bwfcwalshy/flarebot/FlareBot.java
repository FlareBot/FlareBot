package com.bwfcwalshy.flarebot;

import com.arsenarsen.githubwebhooks4j.GithubWebhooks4J;
import com.arsenarsen.githubwebhooks4j.WebhooksBuilder;
import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.commands.administrator.*;
import com.bwfcwalshy.flarebot.commands.general.*;
import com.bwfcwalshy.flarebot.commands.music.*;
import com.bwfcwalshy.flarebot.commands.secret.Eval;
import com.bwfcwalshy.flarebot.commands.secret.LogsCommands;
import com.bwfcwalshy.flarebot.commands.secret.QuitCommand;
import com.bwfcwalshy.flarebot.commands.secret.UpdateCommand;
import com.bwfcwalshy.flarebot.github.GithubListener;
import com.bwfcwalshy.flarebot.permissions.PerGuildPermissions;
import com.bwfcwalshy.flarebot.permissions.Permissions;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import com.bwfcwalshy.flarebot.util.Welcome;
import com.google.gson.*;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.apache.commons.cli.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.*;

import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class FlareBot {

    private static FlareBot instance;
    public static String passwd;
    private static String youtubeApi;

    //    private static String[] args;
    static {
        new File("latest.log").delete();
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(FlareBot.class);
    public static final PrintStream STDERR = System.err;
    private static String dBotsAuth;
    public static final HttpClient HTPP_CLIENT = HttpClientBuilder.create()
            .setRedirectStrategy(new LaxRedirectStrategy()).disableCookieManagement().build();
    private Permissions permissions;
    private GithubWebhooks4J gitHubWebhooks;
    @SuppressWarnings("FieldCanBeLocal")
    public static final File PERMS_FILE = new File("perms.json");

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Welcomes welcomes = new Welcomes();
    private File welcomeFile;

    public static void main(String[] args) throws ClassNotFoundException, UnknownBindingException {
//        FlareBot.args = args;
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
            FlareBot.youtubeApi = parsed.getOptionValue("yt");
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar FlareBot.jar", "FlareBot", options, "https://github.com/ArsenArsen/FlareBot", true);
            e.printStackTrace();
            return;
        }
        Thread.setDefaultUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        Thread.currentThread().setUncaughtExceptionHandler(((t, e) -> LOGGER.error("Uncaught exception in thread " + t, e)));
        new FlareBot().init(tkn);
    }

    public static final char COMMAND_CHAR = '_';

    private String version = null;
    private IDiscordClient client;
    private List<Command> commands;
    // Guild ID | List role ID
    private Map<String, List<String>> autoAssignRoles;
    private File roleFile;
    private PlayerManager musicManager;
    private long startTime;
    private static String secret = null;


    public Permissions getPermissions() {
        return permissions;
    }

    public PerGuildPermissions getPermissions(IChannel channel) {
        return this.permissions.getPermissions(channel);
    }

    public void init(String tkn) throws UnknownBindingException {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        try {
            client = new ClientBuilder().withToken(tkn).login();
            client.getDispatcher().registerListener(new Events(this));
            Discord4J.disableChannelWarnings();
            commands = new ArrayList<>();
            musicManager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(client));
            musicManager.registerHook(player -> player.addEventListener(new AudioEventAdapter() {
                @Override
                public void onTrackStart(AudioPlayer aplayer, AudioTrack atrack) {
                    if (MusicAnnounceCommand.getAnnouncements().containsKey(player.getGuildId())) {
                        IChannel c =
                                client.getChannelByID(MusicAnnounceCommand.getAnnouncements().get(player.getGuildId()));
                        if (c != null) {
                            EnumSet<sx.blah.discord.handle.obj.Permissions> perms = c.getModifiedPermissions(client.getOurUser());
                            if (!perms.contains(sx.blah.discord.handle.obj.Permissions.ADMINISTRATOR)) {
                                if (!perms.contains(sx.blah.discord.handle.obj.Permissions.SEND_MESSAGES)) {
                                    MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                                    return;
                                }
                                if (!perms.contains(sx.blah.discord.handle.obj.Permissions.EMBED_LINKS)) {
                                    MusicAnnounceCommand.getAnnouncements().remove(player.getGuildId());
                                    return;
                                }
                            }
                            Track track = player.getPlayingTrack();
                            MessageUtils.sendMessage(MessageUtils.getEmbed()
                                    .appendField("Now Playing: ", SongCommand.getLink(track), false)
                                    .build(), c);
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
                gitHubWebhooks = new WebhooksBuilder().withSecret(secret).addListener(new GithubListener()).forRequest("/payload").onPort(8080).build();
            } catch (IOException e) {
                LOGGER.error("Could not set up webhooks!", e);
            }

            instance = this;
        } catch (DiscordException e) {
            LOGGER.error("Could not log in!", e);
        }
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        })); // No operation STDERR. Will not do much of anything, except to filter out some Jsoup spam
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
        registerCommand(new InfoCommand(this));
        registerCommand(new PlayCommand(this));
        registerCommand(new PauseCommand(this));
        registerCommand(new StopCommand(this));
        registerCommand(new SkipCommand(this));
        registerCommand(new ShuffleCommand(this));
        registerCommand(new VolumeCommand());
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

        startTime = System.currentTimeMillis();
        LOGGER.info("FlareBot v" + getVersion() + " booted!");

        try {
            getClient().changeAvatar(Image.forStream("png", getClass().getClassLoader().getResourceAsStream("avatar.png")));
        } catch (DiscordException | RateLimitException e) {
            LOGGER.error("Could not change avatar!", e);
        }

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
                if (!UpdateCommand.updating.get())
                    client.changeStatus(Status.game(COMMAND_CHAR + "commands"));
            }
        }.repeat(10, 32000);
        new FlarebotTask("PostData" + System.currentTimeMillis()) {
            @Override
            public void run() {
                if (FlareBot.dBotsAuth != null) {
                    try {
                        HttpPost req =
                                new HttpPost("https://bots.discord.pw/api/bots/" + getClient().getOurUser().getID() + "/stats");
                        StringEntity ent = new StringEntity("{\n" +
                                "\t\"server_count\": " + getClient().getGuilds().size() + "\n" +
                                "}");
                        req.setEntity(ent);
                        req.setHeader("Authorization", FlareBot.dBotsAuth);
                        req.setHeader("Content-Type", "application/json");
                        HTPP_CLIENT.execute(req);
                    } catch (IOException e1) {
                        FlareBot.LOGGER.error("Could not POST data to DBots", e1);
                    }
                }
            }
        }.repeat(10, 600000);

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

    public void quit(boolean update) {
        if (update) {
            LOGGER.debug("Updating bot!");
            try {
                File git = new File("FlareBot" + File.separator);
                if (!git.exists() || !git.isDirectory()) {
                    ProcessBuilder clone = new ProcessBuilder("git", "clone", "https://github.com/ArsenArsen/FlareBot.git", git.getAbsolutePath());
                    clone.redirectErrorStream(true);
                    Process p = clone.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String out = "";
                    while (p.isAlive()) {
                        String line;
                        if ((line = reader.readLine()) != null) {
                            out += line + '\n';
                        }
                    }
                    if (p.exitValue() != 0) {
                        LOGGER.error("Could not update!!!!\n" + out);
                        UpdateCommand.updating.set(false);
                        return;
                    }
                } else {
                    ProcessBuilder builder = new ProcessBuilder("git", "pull");
                    builder.directory(git);
                    Process p = builder.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String out = "";
                    while (p.isAlive()) {
                        String line;
                        if ((line = reader.readLine()) != null) {
                            out += line + '\n';
                        }
                    }
                    if (p.exitValue() != 0) {
                        LOGGER.error("Could not update!!!!\n" + out);
                        UpdateCommand.updating.set(false);
                        return;
                    }
                }
                ProcessBuilder maven = new ProcessBuilder("mvn", "clean", "package", "-e");
                maven.directory(git);
                Process p = maven.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String out = "";
                while (p.isAlive()) {
                    String line;
                    if ((line = reader.readLine()) != null) {
                        out += line + '\n';
                    }
                }
                if (p.exitValue() != 0) {
                    LOGGER.error(Markers.NO_ANNOUNCE, "Could not update!!!!\n" + out);
                    String finalOut = out;
                    UpdateCommand.updating.set(false);
                    RequestBuffer.request(() -> {
                        try {
                            getUpdateChannel().sendFile("Could not build!", false, new ByteArrayInputStream(finalOut.getBytes()), "buildlog.txt");
                        } catch (DiscordException | MissingPermissionsException e) {
                            LOGGER.error("Could not send build failure report!", e);
                        }
                    });
                    return;
                }
                File current = new File(URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8")); // pfft this will go well..
                Files.copy(current.toPath(), Paths.get(current.getPath().replace(".jar", ".backup.jar")), StandardCopyOption.REPLACE_EXISTING);
                File built = new File(git, "target" + File.separator + "FlareBot-jar-with-dependencies.jar");
                Files.copy(built.toPath(), current.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.exit(0);
            } catch (IOException e) {
                LOGGER.error("Could not update!", e);
                UpdateCommand.updating.set(false);
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
        try {
            permissions.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerCommand(Command command) {
        this.commands.add(command);
    }

    public IDiscordClient getClient() {
        return this.client;
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

    public Map<String, List<String>> getAutoAssignRoles() {
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
                    List<String> roles = autoAssignRoles.computeIfAbsent(guild.get("guildId").getAsString(), key -> new ArrayList<>());
                    for (JsonElement e : guild.get("roles").getAsJsonArray()) {
                        if (!roles.contains(e.getAsString()))
                            roles.add(e.getAsString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                roleFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveRoles() {
        if (!roleFile.exists()) {
            try {
                roleFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
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
            e.printStackTrace();
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

    public Welcome getWelcomeForGuild(IGuild guild) {
        for (Welcome welcome : welcomes) {
            if (welcome.getGuildId().equals(guild.getID()))
                return welcome;
        }
        return null;
    }

    public String getInvite() {
        return new BotInviteBuilder(client).withPermissions(EnumSet.of(
                sx.blah.discord.handle.obj.Permissions.CHANGE_NICKNAME,
                sx.blah.discord.handle.obj.Permissions.VOICE_CONNECT,
                sx.blah.discord.handle.obj.Permissions.VOICE_SPEAK,
                sx.blah.discord.handle.obj.Permissions.SEND_MESSAGES,
                sx.blah.discord.handle.obj.Permissions.READ_MESSAGE_HISTORY,
                sx.blah.discord.handle.obj.Permissions.READ_MESSAGES,
                sx.blah.discord.handle.obj.Permissions.EMBED_LINKS,
                sx.blah.discord.handle.obj.Permissions.MANAGE_ROLES,
                sx.blah.discord.handle.obj.Permissions.MANAGE_PERMISSIONS,
                sx.blah.discord.handle.obj.Permissions.VOICE_USE_VAD,
                sx.blah.discord.handle.obj.Permissions.MANAGE_MESSAGES // Optional
        )).build();
    }

    public static class Welcomes extends CopyOnWriteArrayList<Welcome> {
    }

    public IChannel getUpdateChannel() {
        return getClient().getChannelByID("226786557862871040");
    }

    public IChannel getGuildLogChannel() {
        return getClient().getChannelByID("260401007685664768");
    }

    public static String getYoutubeKey() {
        return youtubeApi;
    }
}

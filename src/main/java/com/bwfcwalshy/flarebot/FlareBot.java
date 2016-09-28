package com.bwfcwalshy.flarebot;

import com.arsenarsen.githubwebhooks4j.GithubWebhooks4J;
import com.arsenarsen.githubwebhooks4j.WebhooksBuilder;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.commands.administrator.*;
import com.bwfcwalshy.flarebot.commands.general.*;
import com.bwfcwalshy.flarebot.commands.music.*;
import com.bwfcwalshy.flarebot.github.GithubListener;
import com.bwfcwalshy.flarebot.music.MusicManager;
import com.bwfcwalshy.flarebot.permissions.PerGuildPermissions;
import com.bwfcwalshy.flarebot.permissions.Permissions;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import com.bwfcwalshy.flarebot.util.Welcome;
import com.google.gson.*;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FlareBot {

    private static FlareBot instance;
    public static final Logger LOGGER = LoggerFactory.getLogger(FlareBot.class);
    private Permissions permissions;
    private GithubWebhooks4J gitHubWebhooks;
    @SuppressWarnings("FieldCanBeLocal")
    public static final File PERMS_FILE = new File("perms.json");

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<Welcome> welcomes = new ArrayList<>();
    private File welcomeFile;

    public static void main(String[] args) {
        Options options = new Options();

        Option token = new Option("t", true, "Bot log in token");
        token.setArgName("token");
        token.setLongOpt("token");
        token.setRequired(true);
        options.addOption(token);

        Option secret = new Option("s", true, "Webhooks secret");
        secret.setArgName("secret");
        secret.setLongOpt("secret");
        secret.setRequired(false);
        options.addOption(secret);

        String tkn;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine parsed = parser.parse(options, args);
            tkn = parsed.getOptionValue("t");
            if(parsed.hasOption("s"))
                FlareBot.secret = parsed.getOptionValue("s");
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar FlareBot.jar", "FlareBot", options, "https://github.com/bwfcwalshyPluginDev/FlareBot", true);
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
    private MusicManager musicManager;
    private long startTime;
    private static String secret = null;

    public Permissions getPermissions() {
        return permissions;
    }

    public PerGuildPermissions getPermissions(IChannel channel) {
        return this.permissions.getPermissions(channel);
    }

    public void init(String tkn) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> quit(false)));

        try {
            client = new ClientBuilder().withToken(tkn).login();
            client.getDispatcher().registerListener(new Events(this));
            Discord4J.disableChannelWarnings();
            commands = new ArrayList<>();
            musicManager = new MusicManager(this);

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
    }

    private void loadPerms() {
        if (PERMS_FILE.exists()) {
            try {
                permissions = GSON.fromJson(new FileReader(PERMS_FILE), Permissions.class);
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
        registerCommand(new HelpCommand(this));
        registerCommand(new SearchCommand());
        registerCommand(new JoinCommand());
        registerCommand(new LeaveCommand());
        registerCommand(new InfoCommand(this));
        registerCommand(new PlayCommand(this));
        registerCommand(new PauseCommand(this));
        registerCommand(new StopCommand(this));
        registerCommand(new SkipCommand(this));
        registerCommand(new ShuffleCommand(this));
        registerCommand(new VolumeCommand(this));
        registerCommand(new PlaylistCommand(this));
        registerCommand(new SongCommand(this));
        registerCommand(new InviteCommand());
        registerCommand(new AutoAssignCommand(this));
        registerCommand(new QuitCommand());
        registerCommand(new RolesCommand());
//        registerCommand(new WebhooksCommand(this));
        registerCommand(new WelcomeCommand(this));
        registerCommand(new PermissionsCommand());
        registerCommand(new UpdateCommand());

        client.changeStatus(Status.game(COMMAND_CHAR + "commands"));

        startTime = System.currentTimeMillis();
        LOGGER.info("FlareBot v" + getVersion() + " booted!");

        Scanner scanner = new Scanner(System.in);
        if (scanner.next().equalsIgnoreCase("exit")) {
            quit(false);
        }else if(scanner.next().equalsIgnoreCase("update")){
            quit(true);
        }

        new FlarebotTask("PermissionSaver" + System.currentTimeMillis()){
            @Override
            public void run() {
                try {
                    getPermissions().save();
                } catch (IOException e) {
                    LOGGER.error("Could not save permissions!", e);
                }
            }
        }.repeat(300000, 300000);
    }

    public void quit(boolean update) {
        if (!client.isReady()) return;
        LOGGER.debug("Stopping bot.");
        if (client.getConnectedVoiceChannels() != null && !client.getConnectedVoiceChannels().isEmpty())
            client.getConnectedVoiceChannels().forEach(IVoiceChannel::leave);
        try {
            client.logout();
        } catch (RateLimitException e) {
            e.printStackTrace();
        } catch (DiscordException e) {
            if (!e.getMessage().contains("CloudFlare"))
                e.printStackTrace();
        }

        stop();
        if(update){
            LOGGER.debug("Updating bot!");
            ProcessBuilder builder = new ProcessBuilder("java", "-jar", "Update.jar");
            try {
                Process process = builder.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (process.isAlive()) {
                    String line = br.readLine();
                    System.out.println(line);
                    if (line.equalsIgnoreCase("Built jar"))
                        break;
                }
            }catch(IOException e){
                LOGGER.error("Error while updating!", e);
            }
        }else
            LOGGER.debug("Exiting.");
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
        LOGGER.debug("Saved.");
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
        int seconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
        int s = seconds % 60;
        int minutes = (seconds / 60) % 60;
        int hours = (minutes / 60) % 24;
        return (hours < 10 ? "0" + hours : hours) + "h " + (minutes < 10 ? "0" + minutes : minutes) + "m " + (s < 10 ? "0" + s : s) + "s";
    }

    public MusicManager getMusicManager() {
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
                BufferedReader br;
                try {
                    br = new BufferedReader(new FileReader(welcomeFile));
                } catch (FileNotFoundException e) {
                    LOGGER.error("Could not load welcomes!", e);
                    return;
                }
                if (br.readLine() == null) return;
                JsonParser parser = new JsonParser();
                JsonObject parsed = parser.parse(new FileReader(welcomeFile)).getAsJsonObject();
                JsonArray guilds = parsed.getAsJsonArray("guilds");
                for (JsonElement o : guilds) {
                    JsonObject guild = o.getAsJsonObject();
                    this.welcomes.add(new Welcome(guild.get("guildId").getAsString(), guild.get("channelId").getAsString()).setMessage(guild.get("message").getAsString()));
                }
            } else {
                welcomeFile.createNewFile();
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
        JsonObject obj = new JsonObject();
        JsonArray guildsArray = new JsonArray();

        for (Welcome welcome : welcomes) {
            JsonObject guildObj = new JsonObject();
            guildObj.addProperty("guildId", welcome.getGuildId());
            guildObj.addProperty("channelId", welcome.getChannelId());
            guildObj.addProperty("message", welcome.getMessage());
            guildsArray.add(guildObj);
        }
        obj.add("guilds", guildsArray);
        try {
//            try (Writer fw = new FileWriter(welcomeFile)) {
//                GSON.toJson(obj, fw);
//            }
            FileWriter fw = new FileWriter(welcomeFile);
            fw.write(GSON.toJson(obj));
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
}

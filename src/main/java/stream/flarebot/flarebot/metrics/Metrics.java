package stream.flarebot.flarebot.metrics;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Metrics {

    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private static Metrics instance;

    public static void setup() {
        logger.info("Setup metrics {}!", instance().toString());
    }

    public static Metrics instance() {
        if (instance == null)
            instance = new Metrics();
        return instance;
    }

    public final JdaEventMetricsListener jdaEventMetricsListener = new JdaEventMetricsListener();

    public Metrics() {
        DefaultExports.initialize();
    }

    // Conventions:
    // Names: https://prometheus.io/docs/practices/naming/
    // Labels: https://prometheus.io/docs/practices/instrumentation/#use-labels

    /*
     * JDA
     */
    public static final Counter jdaEvents = Counter.build()
            .name("flarebot_jda_events_total")
            .help("Total number of JDA events fired")
            .labelNames("class") // Use the simple name of the event class eg GuildMessageReceivedEvent, DisconnectEvent
            .register();

    public static final Counter failedRestActions = Counter.build()
            .name("flarebot_rest_actions_total")
            .help("Total number of failed restactions executed by FlareBot")
            .labelNames("error_response_code") // Use the error response code eg 50001, 50007 etc
            .register();

    /*
     * FlareBot
     */
    // General
    public static final Gauge guilds = Gauge.build()
            .name("flarebot_guild_total")
            .help("The amount of guilds we're in")
            .register();

    public static final Counter blocksGivenOut = Counter.build()
            .name("flarebot_guild_blocked_total")
            .help("Total number of times we've blocked guilds")
            .labelNames("guild_id")
            .register();

    public static final Counter buttonsPressed = Counter.build()
            .name("flarebot_buttons_pressed_total")
            .help("Total number of times a button was pressed")
            .labelNames("button")
            .register();

    // Commands
    public static final Counter commandsReceived = Counter.build()
            .name("flarebot_commands_received_total")
            .help("Total amount of commands ran by users")
            .labelNames("class")
            .register();

    public static final Counter commandsExecuted = Counter.build()
            .name("flarebot_commands_executed_total")
            .help("Total amount of commands that we executed")
            .labelNames("class")
            .register();

    public static final Histogram commandExecutionTime = Histogram.build()
            .name("flarebot_command_execution_duration_seconds")
            .help("Command execution time in seconds")
            .labelNames("class")
            .register();

    public static final Counter commandExceptions = Counter.build()
            .name("flarebot_command_exceptions_total")
            .help("Total uncaught exceptions thrown by the command")
            .labelNames("class")
            .register();

    // Music
    // TODO

    public static final Counter voiceChannelsCleanedUp = Counter.build()
            .name("flarebot_music_voicechannels_cleanedup_total")
            .help("Total inactive voice channels that were cleaned up")
            .register();

    /*
     * HTTP
     */
    public static Counter httpRequestCounter = Counter.build()
            .name("flarebot_okhttp_requests_total")
            .help("Total OkHttp requests made and the requester")
            .labelNames("request_sender", "event")
            .register();
}

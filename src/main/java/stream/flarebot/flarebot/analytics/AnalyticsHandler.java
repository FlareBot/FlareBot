package stream.flarebot.flarebot.analytics;

import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.scheduler.FlareBotTask;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.WebUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnalyticsHandler {

    private final Set<AnalyticSender> analytics = new HashSet<>();
    private final Map<String, JSONObject> latestCache = new HashMap<>();

    /**
     * Register a AnalyticSender, this will handle analytic data sending and also latest caching so that it can be
     * retrieved if needed.
     *
     * @param sender The {@link AnalyticSender} to register.
     */
    public void registerAnalyticSender(AnalyticSender sender) {
        this.analytics.add(sender);
    }

    /**
     * This will start all analyticSender's on this specific handler instance.
     * More than one instance can be made for convenience purposes.
     * <p>
     * If you wish to have a specific delay on the first run of the analytics then you can pass that as the first parameter.
     * Set this to -1 for it to use the default analytic send time.
     *
     * @param delay Delay for the analytics to be first sent.
     */
    public void run(long delay) {
        if (FlareBot.getInstance().isApiDisabled()) {
            MessageUtils.sendWarningMessage("API and Analytics disabled", FlareBot.getInstance().getErrorLogChannel());
            return;
        }
        for (AnalyticSender sender : analytics) {
            new FlareBotTask("Analytic-" + sender.endpoint()) {
                @Override
                public void run() {
                    JSONObject data = sender.processData();
                    try {
                        FlareBot.getLog(this.getClass()).debug("Sending analytics to " + sender.endpoint()
                                + " with body: `" + data.toString() + "`");
                        WebUtils.post(sender.apiUrl() + sender.endpoint(), WebUtils.APPLICATION_JSON, data.toString(), true);

                        latestCache.put(sender.getClass().getSimpleName(), data);
                    } catch (IOException e) {
                        FlareBot.getLog(this.getClass()).error("Failed to send analytic data!!" +
                                "\nAPI URL: " + sender.apiUrl() +
                                "\nEndpoint: " + sender.endpoint() +
                                "\nData: " + data.toString() +
                                "\nMessage: " + e.getMessage() + "\n"
                        );
                    }
                }
            }.repeat(delay == -1 ? sender.dataDeliveryFrequency() : delay, sender.dataDeliveryFrequency());
        }
        FlareBot.getLog(this.getClass()).info("Started analytic handler with " + analytics.size() + " senders.");
    }

    /**
     * Get the latest cached data from a specific analytic sender.
     * This accepts either the class name (eg ActivityAnalytics) or the endpoint (eg /activity).
     *
     * @param analyticSender The analytic class name or endpoint.
     * @return The latest JSON data as a string (Non-formatted).
     */
    public String getLatestData(String analyticSender) {
        for (AnalyticSender sender : analytics) {
            if (analyticSender.equalsIgnoreCase(sender.getClass().getSimpleName()) || analyticSender.equalsIgnoreCase(sender.endpoint()))
                return latestCache.get(analyticSender.getClass().getSimpleName()).toString();
        }
        return "null";
    }
}

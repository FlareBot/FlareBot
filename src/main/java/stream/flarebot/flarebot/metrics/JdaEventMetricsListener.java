package stream.flarebot.flarebot.metrics;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 * This class is used in extension of the Metrics class, this will collect all the JDA events used and allow us to see
 * which fires are firing the most.
 */
public class JdaEventMetricsListener implements EventListener {

    @Override
    public void onEvent(Event event) {
        Metrics.jdaEvents.labels(event.getClass().getSimpleName()).inc();
    }
}

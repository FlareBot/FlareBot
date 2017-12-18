package stream.flarebot.flarebot.util.errorhandling;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public class Filter extends ch.qos.logback.core.filter.Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMarker() == Markers.NO_ANNOUNCE) return FilterReply.DENY;
        return FilterReply.NEUTRAL;
    }
}

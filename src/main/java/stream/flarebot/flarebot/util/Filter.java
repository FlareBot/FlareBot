package stream.flarebot.flarebot.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public class Filter extends ch.qos.logback.core.filter.Filter<ILoggingEvent> {
    private static final String[] blacklist = {
            "nulldm;kadsdndnakdndnlasdnadnasdnsdnsdnadadnasddn"
            // Nothing yet :D
    };

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getFormattedMessage();
        if (msg == null)
            msg = "null";
        for (String prefix : blacklist) {
            if (msg.startsWith(prefix)) {
                return FilterReply.DENY;
            }
        }
        return FilterReply.NEUTRAL;
    }
}

package stream.flarebot.flarebot.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Markers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Catch those dem errorz
 * <br>
 * Created by Arsen on 19.9.16..
 */
public class ErrorCatcher extends Filter<ILoggingEvent> {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getFormattedMessage();
        if (msg == null)
            msg = "null";
        if (event.getMarker() != Markers.NO_ANNOUNCE
                && FlareBot.getInstance() != null
                && FlareBot.getInstance().isReady()
                && event.getLevel() == Level.ERROR) {
            String finalMsg = msg;
            EXECUTOR.submit(() -> {
                Throwable throwable = null;
                if (event.getThrowableProxy() != null && event.getThrowableProxy() instanceof ThrowableProxy) {
                    throwable = ((ThrowableProxy) event.getThrowableProxy()).getThrowable();
                }
                if (throwable != null) {
                    MessageUtils.sendException(finalMsg, throwable, FlareBot.getInstance().getUpdateChannel());
                } else FlareBot.getInstance().getUpdateChannel().sendMessage(finalMsg).queue();
            });
        }
        return FilterReply.NEUTRAL;
    }
}

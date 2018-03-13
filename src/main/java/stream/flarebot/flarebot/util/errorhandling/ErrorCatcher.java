package stream.flarebot.flarebot.util.errorhandling;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.MessageUtils;

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
                && event.getLevel() == Level.ERROR || event.getLevel() == Level.WARN) {
            String finalMsg = msg;
            if (event.getThreadName().startsWith("lava-daemon-pool")) {
                return FilterReply.NEUTRAL;
            }
            EXECUTOR.submit(() -> {
                Throwable throwable = null;
                if (event.getThrowableProxy() != null && event.getThrowableProxy() instanceof ThrowableProxy) {
                    throwable = ((ThrowableProxy) event.getThrowableProxy()).getThrowable();
                }
                if (event.getLevel() == Level.WARN) {
                    // Warnings should not have a throwable!
                    MessageUtils.sendWarningMessage(finalMsg, FlareBot.getInstance().getErrorLogChannel());
                    return;
                }
                if (throwable != null) {
                    if (event.getMarker() == Markers.TAG_DEVELOPER)
                        MessageUtils.sendFatalException(finalMsg, throwable, FlareBot.getInstance().getErrorLogChannel());
                    else
                        MessageUtils.sendException(finalMsg, throwable, FlareBot.getInstance().getErrorLogChannel());
                } else {
                    if (event.getMarker() == Markers.TAG_DEVELOPER)
                        MessageUtils.sendFatalErrorMessage(finalMsg, FlareBot.getInstance().getErrorLogChannel());
                    else
                        MessageUtils.sendErrorMessage(finalMsg, FlareBot.getInstance().getErrorLogChannel());
                }
            });
        }
        return FilterReply.NEUTRAL;
    }
}

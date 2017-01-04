package com.bwfcwalshy.flarebot;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Catch those dem errorz
 * <br>
 * Created by Arsen on 19.9.16..
 */
public class ErrorCatcher extends Filter<ILoggingEvent> {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getFormattedMessage();
        if (msg.startsWith("Received 40")) {
            return FilterReply.DENY;
        }
        if (msg.startsWith("Attempt to send message on closed")) {
            return FilterReply.DENY;
        }
        if (event.getMarker() != Markers.NO_ANNOUNCE
                && FlareBot.getInstance().getClient() != null
                && FlareBot.getInstance().getClient().isReady()
                && event.getLevel() == Level.ERROR) {
            EXECUTOR.submit(() -> {
                Throwable throwable = null;
                if (event.getThrowableProxy() != null && event.getThrowableProxy() instanceof ThrowableProxy) {
                    throwable = ((ThrowableProxy) event.getThrowableProxy()).getThrowable();
                }
                if (throwable != null) {
                    MessageUtils.sendException(msg, throwable, FlareBot.getInstance().getUpdateChannel());
                } else MessageUtils.sendMessage(msg, FlareBot.getInstance().getUpdateChannel());
            });
        }
        return FilterReply.NEUTRAL;
    }
}

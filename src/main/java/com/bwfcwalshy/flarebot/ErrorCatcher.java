package com.bwfcwalshy.flarebot;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Catch those dem errorz
 * <br>
 * Created by Arsen on 19.9.16..
 */
public class ErrorCatcher extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getMarker() != Markers.NO_ANNOUNCE && FlareBot.getInstance().getClient().isReady() && event.getLevel() == Level.ERROR) {
            String msg = MessageFormatter.format(event.getFormattedMessage(), event.getArgumentArray()).getMessage();
            if(msg.startsWith("Received 404 error, please notify the developer and include the URL (https://discordapp.com/api/channels/")){
                return FilterReply.NEUTRAL;
            }
            if(event.getThrowableProxy() != null && event.getThrowableProxy() instanceof ThrowableProxy){
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                Throwable throwable = ((ThrowableProxy) event.getThrowableProxy()).getThrowable();
                if(throwable != null) {
                    msg += ' ';
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    msg += sw.toString();
                    pw.close();
                }
            }
            MessageUtils.sendMessage(FlareBot.getInstance().getClient().getChannelByID("226786557862871040"), msg);
        }
        return FilterReply.NEUTRAL;
    }
}

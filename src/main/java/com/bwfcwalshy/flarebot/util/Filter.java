package com.bwfcwalshy.flarebot.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

public class Filter extends ch.qos.logback.core.filter.Filter<ILoggingEvent> {
    private String[] blacklist = {
            "Received: {\"t\":\"MESSAGE_RECIEVE\"",
            "Received: {\"t\":\"PRESENCE_UPDATE\"",
            "Received: {\"t\":\"GUILD_MEMBERS_CHUNK\"",
            "Received: {\"t\":\"TYPING_START\"",
            "Received: {\"t\":\"MESSAGE_CREATE\"",
            "Received: {\"t\":\"MESSAGE_DELETE\"",
            "Sending: {\"t\":null,\"s\":null,\"op\":2,\"d\":{\"token\":\"Bot ",
            "New guild has been created/joined!",
            "User ",
            "DiscordClientImpl Keep Alive",
            "Registered IListener",
            "Message from: ",
            "Unregistered IListener"
    };

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String msg = event.getFormattedMessage();
        if (msg == null)
            msg = "null";
        for(String prefix : blacklist){
            if (msg.startsWith(prefix)) {
                return FilterReply.DENY;
            }
        }
        return FilterReply.NEUTRAL;
    }
}

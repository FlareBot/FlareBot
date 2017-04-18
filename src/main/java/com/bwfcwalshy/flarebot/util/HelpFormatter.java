package com.bwfcwalshy.flarebot.util;

import com.bwfcwalshy.flarebot.FlareBot;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.regex.Matcher;

public class HelpFormatter {
    public static String on(TextChannel channel, String description) {
        return description.replaceAll("(?<!\\\\)%p", Matcher.quoteReplacement(String.valueOf(get(channel))));
    }

    private static char get(TextChannel channel) {
        if (channel == null)
            return FlareBot.getPrefixes().get(null);
        if (channel.getGuild() != null) {
            return FlareBot.getPrefixes().get(channel.getGuild().getId());
        }
        return FlareBot.getPrefixes().get(null);
    }
}

package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.entities.TextChannel;
import stream.flarebot.flarebot.FlareBot;

import java.util.regex.Matcher;

public class HelpFormatter {
    public static String on(TextChannel channel, String description) {
        return description.replaceAll("(?<!\\\\)%p", Matcher.quoteReplacement(String.valueOf(get(channel))));
    }

    private static char get(TextChannel channel) {
        if (channel.getGuild() != null) {
            return FlareBot.getPrefixes().get(channel.getGuild().getId());
        }
        return FlareBot.getPrefixes().get(null);
    }

    public static String formatCommandUsage(TextChannel channel, String usage) {
        return get(channel) + usage;
    }
}

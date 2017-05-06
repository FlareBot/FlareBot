package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.entities.TextChannel;
import stream.flarebot.flarebot.FlareBot;

public class HelpFormatter {

    private static char get(TextChannel channel) {
        if (channel.getGuild() != null) {
            return FlareBot.getPrefixes().get(channel.getGuild().getId());
        }
        return FlareBot.getPrefixes().get(null);
    }

    public static String formatCommandPrefix(TextChannel channel, String usage) {
        String prefix = String.valueOf(get(channel));
        return usage.replaceAll("\\{%\\}", prefix);
    }
}

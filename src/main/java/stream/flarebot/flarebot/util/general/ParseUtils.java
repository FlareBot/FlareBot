package stream.flarebot.flarebot.util.general;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.Getters;

import javax.annotation.Nullable;
import java.util.regex.Matcher;

public class ParseUtils {

    @Nullable
    public static TextChannel parseChannel(Guild guild, String arg) {
        Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(arg);
        if (matcher.matches()) {
            return guild.getTextChannelById(matcher.group(1));
        }

        for (TextChannel tc : guild.getTextChannelCache())
            if (tc.getName().equalsIgnoreCase(arg))
                return tc;

        try {
            return guild.getTextChannelById(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    public static User parseUser(Guild guild, String arg) {
        Matcher matcher = Message.MentionType.USER.getPattern().matcher(arg);
        if (matcher.matches()) {
            return Getters.getUserById(matcher.group(1));
        }

        for (Member m : guild.getMemberCache())
            if (m.getUser().getName().equalsIgnoreCase(arg)
                    || (m.getNickname() != null && m.getNickname().equalsIgnoreCase(arg)))
                return m.getUser();

        try {
            return Getters.getUserById(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    public static Role parseRole(Guild guild, String arg) {
        Matcher matcher = Message.MentionType.ROLE.getPattern().matcher(arg);
        if (matcher.matches()) {
            return guild.getRoleById(matcher.group(1));
        }

        for (Role r : guild.getRoleCache())
            if (r.getName().equalsIgnoreCase(arg))
                return r;

        try {
            return guild.getRoleById(arg);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

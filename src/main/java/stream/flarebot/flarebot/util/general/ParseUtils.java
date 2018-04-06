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

    /**
     * Parse user input to find a text channel.
     *
     * Accepts:
     * * Mention
     * * Name
     * * ID
     *
     * @param guild The guild to find the channel from.
     * @param input The input of which to search.
     * @param searchGlobally If we should search the entirety of FlareBot or just the guild.
     * @return The TextChannel if found, null otherwise.
     */
    @Nullable
    public static TextChannel parseChannel(Guild guild, String input, boolean searchGlobally) {
        Matcher matcher = Message.MentionType.CHANNEL.getPattern().matcher(input);
        if (matcher.matches()) {
            return guild.getTextChannelById(matcher.group(1));
        }

        for (TextChannel tc : guild.getTextChannelCache())
            if (tc.getName().equalsIgnoreCase(input))
                return tc;

        if (searchGlobally) {
            for (TextChannel tc : Getters.getTextChannelCache())
                if (tc.getName().equalsIgnoreCase(input))
                    return tc;
        }

        try {
            return guild.getTextChannelById(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse user input to find a user.
     *
     * Accepts:
     * * Mention
     * * Name
     * * ID
     *
     * @param guild The guild to find the user from.
     * @param input The input of which to search.
     * @param searchGlobally If we should search the entirety of FlareBot or just the guild.
     * @return The User if found, null otherwise.
     */
    @Nullable
    public static User parseUser(Guild guild, String input, boolean searchGlobally) {
        Matcher matcher = Message.MentionType.USER.getPattern().matcher(input);
        if (matcher.matches()) {
            return Getters.getUserById(matcher.group(1));
        }

        for (Member m : guild.getMemberCache()) {
            if (m.getUser().getName().equalsIgnoreCase(input)
                    || (m.getNickname() != null && m.getNickname().equalsIgnoreCase(input))
                    || input.contains("#")
                    && (m.getUser().getName() + '#' + m.getUser().getDiscriminator()).equalsIgnoreCase(input))
                return m.getUser();
        }

        if (searchGlobally) {
            for (User u : Getters.getUserCache())
                if (u.getName().equalsIgnoreCase(input) ||
                        input.contains("#") && (u.getName() + '#' + u.getDiscriminator()).equalsIgnoreCase(input))
                    return u;
        }

        try {
            return Getters.getUserById(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parse user input to find a role.
     *
     * Accepts:
     * * Mention
     * * Name
     * * ID
     *
     * @param guild The guild to find the role from.
     * @param input The input of which to search.
     * @param searchGlobally If we should search the entirety of FlareBot or just the guild.
     * @return The Role if found, null otherwise.
     */
    @Nullable
    public static Role parseRole(Guild guild, String input, boolean searchGlobally) {
        Matcher matcher = Message.MentionType.ROLE.getPattern().matcher(input);
        if (matcher.matches()) {
            return guild.getRoleById(matcher.group(1));
        }

        for (Role r : guild.getRoleCache())
            if (r.getName().equalsIgnoreCase(input))
                return r;

        if (searchGlobally) {
            for (Role r : Getters.getShardManager().getRoleCache())
                if (r.getName().equalsIgnoreCase(input))
                    return r;
        }

        try {
            return guild.getRoleById(input);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

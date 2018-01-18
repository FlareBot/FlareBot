package stream.flarebot.flarebot.util.general;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GuildUtils {
    private static final Pattern userDiscrim = Pattern.compile(".+#[0-9]{4}");
    private static final int LEVENSHTEIN_DISTANCE = 8;
    /**
     * Gets the prefix for the specified {@link Guild}
     *
     * @param guild The {@link Guild} to get a prefix from
     * @return A char that is the guilds prefix
     */
    public static char getPrefix(Guild guild) {
        return guild == null ? Constants.COMMAND_CHAR : FlareBotManager.instance().getGuild(guild.getId()).getPrefix();
    }

    /**
     * Gets the prefix from the {@link GuildWrapper}
     *
     * @param guild The {@link GuildWrapper} that represents the guild that you want to get the prefix from
     * @return A char that is the guilds prefix
     */
    public static char getPrefix(GuildWrapper guild) {
        return guild == null ? Constants.COMMAND_CHAR : guild.getPrefix();
    }

    /**
     * Gets the number of users that the guild has
     *
     * @param guild The {@link Guild} to get the user count from
     * @return An int of the number of users
     */
    public static int getGuildUserCount(Guild guild) {
        int i = 0;
        for (Member member : guild.getMembers()) {
            if (!member.getUser().isBot()) {
                i++;
            }
        }
        return i;
    }

    /**
     * Gets a list of {@link Role} from a string
     *
     * @param string The String to get a role from
     * @param guild The {@link Guild} to get the role from
     * @return null if the role doesn't, otherwise a list of roles matching the string
     */
    public static List<Role> getRole(String string, Guild guild) {
        return guild.getRolesByName(string, true);
    }

    /**
     * Gets a {@link Role} from a string
     *
     * @param s The String to get a role from
     * @param guildId The id of the {@link Guild} to get the role from
     * @return null if the role doesn't, otherwise a list of roles matching the string
     */
    public static Role getRole(String s, String guildId) {
        return getRole(s, guildId, null);
    }

    /**
     * Gets a {@link Role} from a string
     *
     * @param s The String to get a role from
     * @param guildId The id of the {@link Guild} to get the role from
     * @param channel The channel to use for finding the role.
     * @return null if the role doesn't, otherwise a list of roles matching the string
     */
    public static Role getRole(String s, String guildId, TextChannel channel) {
        Guild guild = Getters.getGuildById(guildId);
        Role role = guild.getRoles().stream()
                .filter(r -> r.getName().equalsIgnoreCase(s))
                .findFirst().orElse(null);
        if (role != null) return role;
        try {
            role = guild.getRoleById(Long.parseLong(s.replaceAll("[^0-9]", "")));
            if (role != null) return role;
        } catch (NumberFormatException | NullPointerException ignored) {
        }
        if (channel != null) {
            if (guild.getRolesByName(s, true).isEmpty()) {
                String closest = null;
                int distance = LEVENSHTEIN_DISTANCE;
                for (Role role1 : guild.getRoles().stream().filter(role1 -> FlareBotManager.instance().getGuild(guildId).getSelfAssignRoles()
                        .contains(role1.getId())).collect(Collectors.toList())) {
                    int currentDistance = StringUtils.getLevenshteinDistance(role1.getName(), s);
                    if (currentDistance < distance) {
                        distance = currentDistance;
                        closest = role1.getName();
                    }
                }
                MessageUtils.sendErrorMessage("That role does not exist! "
                        + (closest != null ? "Maybe you mean `" + closest + "`" : ""), channel);
                return null;
            } else {
                return guild.getRolesByName(s, true).get(0);
            }
        }
        return null;
    }
    //User

    /**
     * Gets a {@link User} from a string
     *
     * @param s the string to get the user from
     * @return null if the user wasn't found otherwise a {@link User}
     */
    public static User getUser(String s) {
        return getUser(s, null);
    }

    /**
     * Gets a {@link User} from a string
     *
     * @param s The string to get the user from
     * @param guildId The id of the {@link Guild} to get the user from
     * @return null if the user wasn't found otherwise a {@link User}
     */
    public static User getUser(String s, String guildId) {
        return getUser(s, guildId, false);
    }

    /**
     * Gets a {@link User} from a string.
     *
     * @param s The string to get the user from
     * @param forceGet If you want to get the user from discord instead of from a guild
     * @return null if the user wasn't found otherwise a {@link User}
     */
    public static User getUser(String s, boolean forceGet) {
        return getUser(s, null, forceGet);
    }

    /**
     * Gets a {@link User} from a string.
     *
     * @param s  The string to get the user from
     * @param guildId The id of the {@link Guild} to get the user from
     * @param forceGet If you want to get the user from discord instead of from a guild
     * @return null if the user wasn't found otherwise a {@link User}
     */
    public static User getUser(String s, String guildId, boolean forceGet) {
        if (userDiscrim.matcher(s).find()) {
            if (guildId == null || guildId.isEmpty()) {
                return Getters.getUsers().stream()
                        .filter(user -> (user.getName() + "#" + user.getDiscriminator()).equalsIgnoreCase(s))
                        .findFirst().orElse(null);
            } else {
                try {
                    return Getters.getGuildById(guildId).getMembers().stream()
                            .map(Member::getUser)
                            .filter(user -> (user.getName() + "#" + user.getDiscriminator()).equalsIgnoreCase(s))
                            .findFirst().orElse(null);
                } catch (NullPointerException ignored) {
                }
            }
        } else {
            User tmp;
            if (guildId == null || guildId.isEmpty()) {
                tmp = Getters.getUsers().stream().filter(user -> user.getName().equalsIgnoreCase(s))
                        .findFirst().orElse(null);
            } else {
                if (Getters.getGuildById(guildId) != null) {
                    tmp = Getters.getGuildById(guildId).getMembers().stream()
                            .map(Member::getUser)
                            .filter(user -> user.getName().equalsIgnoreCase(s))
                            .findFirst().orElse(null);
                } else
                    tmp = null;
            }
            if (tmp != null) return tmp;
            try {
                long l = Long.parseLong(s.replaceAll("[^0-9]", ""));
                if (guildId == null || guildId.isEmpty()) {
                    tmp = Getters.getUserById(l);
                } else {
                    Member temMember = Getters.getGuildById(guildId).getMemberById(l);
                    if (temMember != null) {
                        tmp = temMember.getUser();
                    }
                }
                if (tmp != null) {
                    return tmp;
                } else if (forceGet) {
                    return Getters.retrieveUserById(l);
                }
            } catch (NumberFormatException | NullPointerException ignored) {
            }
        }
        return null;
    }
    //Channel

    /**
     * Gets a {@link TextChannel} from a string
     *
     * @param arg The string to get the channel from
     * @return null if the channel couldn't be found otherwise a {@link TextChannel}
     */
    public static TextChannel getChannel(String arg) {
        return getChannel(arg, null);
    }

    /**
     * Gets a {@link TextChannel} from a string
     *
     * @param channelArg The string to get the channel from
     * @param wrapper The Guild wrapper for the {@link Guild} that you want to get the channel from
     * @return null if the channel couldn't be found otherwise a {@link TextChannel}
     */
    public static TextChannel getChannel(String channelArg, GuildWrapper wrapper) {
        try {
            long channelId = Long.parseLong(channelArg.replaceAll("[^0-9]", ""));
            return wrapper != null ? wrapper.getGuild().getTextChannelById(channelId) : Getters.getChannelById(channelId);
        } catch (NumberFormatException e) {
            if (wrapper != null) {
                List<TextChannel> tcs = wrapper.getGuild().getTextChannelsByName(channelArg, true);
                if (!tcs.isEmpty()) {
                    return tcs.get(0);
                }
            }
            return null;
        }
    }
    //Emote

    /**
     * Gets an {@link Emote}
     *
     * @param l the id of the emote
     * @return null if the id is invalid otherwise a {@link Emote}
     */
    public static Emote getEmoteById(long l) {
        return Getters.getGuilds().stream().map(g -> g.getEmoteById(l))
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Gets weather or not the bot can change nick
     *
     * @param guildId The guild to check if we can change nick
     * @return if we change change nick
     */
    public static boolean canChangeNick(String guildId) {
        return Getters.getGuildById(guildId) != null &&
                (Getters.getGuildById(guildId).getSelfMember().hasPermission(Permission.NICKNAME_CHANGE) ||
                        Getters.getGuildById(guildId).getSelfMember().hasPermission(Permission.NICKNAME_MANAGE));
    }

    /**
     * Joins the bot to a channel.
     *
     * @param channel The chanel to join the bot to.
     * @param member The member requesting the join.
     */
    public static void joinChannel(TextChannel channel, Member member) {
        if (channel.getGuild().getSelfMember()
                .hasPermission(member.getVoiceState().getChannel(), Permission.VOICE_CONNECT) &&
                channel.getGuild().getSelfMember()
                        .hasPermission(member.getVoiceState().getChannel(), Permission.VOICE_SPEAK)) {
            if (member.getVoiceState().getChannel().getUserLimit() > 0 && member.getVoiceState().getChannel()
                    .getMembers().size()
                    >= member.getVoiceState().getChannel().getUserLimit() && !member.getGuild().getSelfMember()
                    .hasPermission(member
                            .getVoiceState()
                            .getChannel(), Permission.MANAGE_CHANNEL)) {
                MessageUtils.sendErrorMessage("We can't join :(\n\nThe channel user limit has been reached and we don't have the 'Manage Channel' permission to " +
                        "bypass it!", channel);
                return;
            }
            channel.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
        } else {
            MessageUtils.sendErrorMessage("I do not have permission to " + (!channel.getGuild().getSelfMember()
                    .hasPermission(member.getVoiceState()
                            .getChannel(), Permission.VOICE_CONNECT) ?
                    "connect" : "speak") + " in your voice channel!", channel);
        }
    }
}

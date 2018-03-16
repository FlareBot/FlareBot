package stream.flarebot.flarebot.commands.informational;

import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.FormatUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class UserInfoCommand implements Command {

    private FlareBot flareBot = FlareBot.instance();

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        User user;
        if (args.length == 0)
            user = sender;
        else {
            if (getPermissions(channel).hasPermission(member, Permission.USERINFO_OTHER))
                user = GuildUtils.getUser(MessageUtils.getMessage(args, 0), true);
            else {
                MessageUtils.sendErrorMessage("You need the " + Permission.USERINFO_OTHER + " permission to userinfo other users!",
                        channel);
                return;
            }
        }

        if (user == null) {
            MessageUtils.sendErrorMessage("We cannot find that user!", channel);
            return;
        }
        String id = user.getId();
        Member targetMember = null;
        if (channel.getGuild().getMember(user) != null)
            targetMember = channel.getGuild().getMember(user);

        PlayerCache cache = flareBot.getPlayerCache(id);
        channel.sendMessage(MessageUtils.getEmbed(sender)
                .addField("User Info", "User: " + user.getName() + "#" + user.getDiscriminator()
                        + "\nID: " + user.getId() + "\n" +
                        "Avatar: " + (user.getEffectiveAvatarUrl() != null ? "[`link`](" + user
                        .getEffectiveAvatarUrl() + ')' : "None") + "\n" +
                        "Default Avatar: [`link`](" + MessageUtils
                        .getDefaultAvatar(sender) + ')', true)
                .addField("General Info",
                        "Servers: " + Getters.getGuildCache().stream()
                                .filter(g -> g.getMemberById(id) != null)
                                .count() + " shared\n" +
                                "Roles: " + (targetMember == null ? "The user is not in this server." :
                                targetMember.getRoles().stream()
                                        .map(Role::getName).collect(Collectors.joining(", "))) +
                                (targetMember != null && targetMember.getGame() != null ? "\nStatus" +
                                        (targetMember.getUser()
                                                .isBot() ? " (Current Shard)" : "") + ": " +
                                        (targetMember.getGame().getUrl() == null ? "`" + targetMember
                                                .getGame().getName() + "`" :
                                                String.format("[`%s`](%s)", targetMember.getGame()
                                                                .getName(),
                                                        targetMember.getGame().getUrl())) : ""), true)
                .addField("Time Data", "Created: " + FormatUtils
                        .formatTime(LocalDateTime.from(user.getCreationTime())) + "\n" +
                        "Joined: " + (targetMember == null ? "The user is not in this server."
                        : FormatUtils.formatTime(LocalDateTime
                        .from(channel.getGuild().getMember(user).getJoinDate()))) + "\n" +
                        "Last Seen: " + (cache.getLastSeen() == null ? "Unknown" : FormatUtils
                        .formatTime(cache.getLastSeen())) + "\n" +
                        "Last Spoke: " + (cache.getLastMessage() == null ? "Unknown" : FormatUtils
                        .formatTime(cache.getLastMessage())), false)
                .setThumbnail(MessageUtils.getAvatar(user)).build()).queue();
    }

    @Override
    public String getCommand() {
        return "userinfo";
    }

    @Override
    public String getDescription() {
        return "Get information about a user";
    }

    @Override
    public String getUsage() {
        return "`{%}userinfo [userID]` - Views your user info [or info for another user].";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"stalk"};
    }
}

package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.PlayerCache;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class UserInfoCommand implements Command {

    private FlareBot flareBot = FlareBot.getInstance();

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        User user;
        if (args.length == 0)
            user = sender;
        else {
            if (getPermissions(channel).hasPermission(member, "flarebot.userinfo.other"))
                user = GeneralUtils.getUser(MessageUtils.getMessage(args, 0), true);
            else {
                MessageUtils.sendErrorMessage("You need the `flarebot.userinfo.other` permission to userinfo other users!",
                        channel);
                return;
            }
        }

        if (user == null) {
            MessageUtils.sendErrorMessage("We cannot find that user!", channel);
            return;
        }
        String id = user.getId();
        if (channel.getGuild().getMember(user) == null) {
            Guild memberGuild = FlareBot.getInstance().getGuilds().stream().filter(g -> g.getMemberById(user.getId()) != null)
                    .findFirst().orElse(null);
            if (memberGuild == null)
                member = null;
            else
                memberGuild.getMember(user);
        }
        PlayerCache cache = flareBot.getPlayerCache(id);
        channel.sendMessage(MessageUtils.getEmbed(sender)
                .addField("User Info", "User: " + user.getName() + "#" + user.getDiscriminator()
                        + "\nID: " + user.getId() + "\n" +
                        "Avatar: " + (user.getEffectiveAvatarUrl() != null ? "[`link`](" + user
                        .getEffectiveAvatarUrl() + ')' : "None") + "\n" +
                        "Default Avatar: [`link`](" + MessageUtils
                        .getDefaultAvatar(sender) + ')', true)
                .addField("General Info",
                        "Servers: " + FlareBot.getInstance().getGuilds().stream()
                                .filter(g -> g.getMemberById(id) != null)
                                .count() + " shared\n" +
                                "Roles: " + (channel.getGuild()
                                .getMemberById(id) == null ? "The user is not in this server." : channel
                                .getGuild().getMember(user).getRoles().stream()
                                .map(Role::getName).collect(Collectors.joining(", "))) +
                                (member != null && member.getGame() != null ? "\nStatus" +
                                        (member.getUser()
                                                .isBot() ? " (Current Shard)" : "") + ": " +
                                        (member.getGame().getUrl() == null ? "`" + member
                                                .getGame().getName() + "`" :
                                                String.format("[`%s`](%s)", member.getGame()
                                                                .getName(),
                                                        member.getGame().getUrl())) : ""), true)
                .addField("Time Data", "Created: " + GeneralUtils
                        .formatTime(LocalDateTime.from(user.getCreationTime())) + "\n" +
                        "Joined: " + (channel.getGuild()
                        .getMember(user) == null ? "The user is not in this server."
                        : GeneralUtils.formatTime(LocalDateTime
                        .from(channel.getGuild().getMember(user).getJoinDate()))) + "\n" +
                        "Last Seen: " + (cache.getLastSeen() == null ? "Unknown" : GeneralUtils
                        .formatTime(cache.getLastSeen())) + "\n" +
                        "Last Spoke: " + (cache.getLastMessage() == null ? "Unknown" : GeneralUtils
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

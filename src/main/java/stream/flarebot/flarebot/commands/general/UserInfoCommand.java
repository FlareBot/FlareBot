package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.PlayerCache;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class UserInfoCommand implements Command {

    private FlareBot flareBot = FlareBot.getInstance();

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        User user;
        if (args.length == 0)
            user = sender;
        else
            user = MessageUtils.getUser(FlareBot.getMessage(args, 0));

        if (user == null) {
            MessageUtils.sendErrorMessage("We cannot find that user!", channel);
            return;
        }
        String id = user.getId();
        member = (channel.getGuild().getMember(user) != null ? channel.getGuild().getMember(user) :
                FlareBot.getInstance().getGuilds().stream().filter(g -> g.getMemberById(user.getId()) != null)
                        .findFirst().orElse(null).getMember(user));
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
                                (member.getGame() != null ? "\nStatus" +
                                        (member.getUser()
                                                .isBot() ? " (Current Shard)" : "") + ": " +
                                        (member.getGame().getUrl() == null ? "`" + member
                                                .getGame().getName() + "`" :
                                                String.format("[`%s`](%s)", member.getGame()
                                                                .getName(),
                                                        member.getGame().getUrl())) : ""), true)
                .addField("Time Data", "Created: " + flareBot
                        .formatTime(LocalDateTime.from(user.getCreationTime())) + "\n" +
                        "Joined: " + (channel.getGuild()
                        .getMember(user) == null ? "The user is not in this server."
                        : flareBot.formatTime(LocalDateTime
                        .from(channel.getGuild().getMember(user).getJoinDate()))) + "\n" +
                        "Last Seen: " + (cache.getLastSeen() == null ? "Unknown" : flareBot
                        .formatTime(cache.getLastSeen())) + "\n" +
                        "Last Spoke: " + (cache.getLastMessage() == null ? "Unknown" : flareBot
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
        return "`{%}userinfo [userID]` - Views your user info [or info for another user]";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

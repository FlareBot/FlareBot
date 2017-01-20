package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.objects.PlayerCache;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class UserInfoCommand implements Command {

    private FlareBot flareBot = FlareBot.getInstance();
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMMM yyyy HH:mm:ss");

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        String id;
        if (args.length != 1)
            id = sender.getID();
        else id = args[0].replaceAll("[^0-9]", "");
        IUser user = FlareBot.getInstance().getClient().getUserByID(id);
        if (user == null) {
            MessageUtils.sendErrorMessage("We cannot find that user!", channel);
            return;
        }
        PlayerCache cache = flareBot.getPlayerCache(id);
        try {
            String finalId = id;
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender).appendField("User Info", "User: " + user.getName() + "#" + user.getDiscriminator()
                    + "\nID: " + user.getID() + "\n" +
                    "Avatar: " + (user.getAvatar() != null ? "[`link`](" + user.getAvatarURL() + ')' : "None") + "\n" +
                    "Default Avatar: [`link`](" + MessageUtils.getDefaultAvatar(sender) + ')', true)
                    .appendField("General Info",
                            "Servers: " + FlareBot.getInstance().getClient().getGuilds().stream().filter(guild -> guild.getUserByID(finalId) != null).count() + " shared\n" +
                                    "Roles: " + user.getRolesForGuild(channel.getGuild()).stream()
                                    .map(IRole::getName).collect(Collectors.joining(", ")), true)
                    .appendField("Time Data", "Created: " + formatTime(user.getCreationDate()) + "\n" +
                            "Joined: " + formatTime(channel.getGuild().getJoinTimeForUser(user)) + "\n" +
                            "Last Seen: " + (cache.getLastSeen() == null ? "Unknown" : formatTime(cache.getLastSeen())) + "\n" +
                            "Last Spoke: " + (cache.getLastMessage() == null ? "Unknown" : formatTime(cache.getLastMessage())), false)
                    .withThumbnail(MessageUtils.getAvatar(user)), channel);
        } catch (DiscordException e) {
            FlareBot.LOGGER.error("Error in UserInfoCommand!", e); // do a printStackTrace one more time and ill kill you
        }
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
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public String getPermission() {
        return null;
    }

    private String formatTime(LocalDateTime time) {
        return time.getDayOfMonth() + getDayOfMonthSuffix(time.getDayOfMonth()) + " " + time.format(timeFormat) + " UTC";
    }

    private String getDayOfMonthSuffix(final int n) {
        if (n < 1 || n > 31) throw new IllegalArgumentException("illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }
}

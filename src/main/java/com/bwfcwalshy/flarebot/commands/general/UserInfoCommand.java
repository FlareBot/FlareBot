package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.objects.PlayerCache;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserInfoCommand implements Command {

    private FlareBot flareBot = FlareBot.getInstance();
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMMM yyyy HH:mm:ss");

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length != 1) {
            MessageUtils.sendErrorMessage("Incorrect usage! Usage: " + FlareBot.getPrefix(channel.getGuild().getID()) + "userinfo (user)", channel);
        } else {
            if (args[0].matches("\\d+") || args[0].matches("<@\\d+>")) {
                String id = args[0];
                if (args[0].matches("<@\\d+>") || args[0].matches("<@!\\d+>"))
                    id = id.replace("<@", "").replace(">", "");
                IUser user = FlareBot.getInstance().getClient().getUserByID(id);
                if (user == null) {
                    MessageUtils.sendErrorMessage("We cannot find that user!", channel);
                    return;
                }

                String finalId = id;
                PlayerCache cache = flareBot.getPlayerCache(finalId);
                try {
                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender).appendField("User Info", "User: " + user.getName() + "#" + user.getDiscriminator()
                            + "\nID: " + user.getID() + "\nAvatar: " + user.getAvatarURL(), true)
                            .appendField("General Info", "Servers: " + FlareBot.getInstance().getClient().getGuilds().stream().filter(guild -> guild.getUserByID(finalId) != null).count()
                                    + " shared\nRoles: " + StringUtils.join(user.getRolesForGuild(channel.getGuild()), ", ").trim(), true)
                            .appendField("Time Data", "Created: " + formatTime(user.getCreationDate()) + "\nJoined: " + formatTime(channel.getGuild().getJoinTimeForUser(user))
                                    + "\nLast Seen: " + (cache.getLastSeen() == null ? "Unknown" : formatTime(cache.getLastSeen())) + "\nLast Spoke: "
                                    + (cache.getLastMessage() == null ? "Unknown" : formatTime(cache.getLastMessage())), false).withThumbnail(user.getAvatarURL()), channel);
                } catch (DiscordException e) {
                    e.printStackTrace();
                }
            }
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

    private String formatTime(LocalDateTime time){
        return time.getDayOfMonth() + getDayOfMonthSuffix(time.getDayOfMonth()) + " " + time.format(timeFormat) + " UTC";
    }

    private String getDayOfMonthSuffix(final int n) {
        if (n < 1 || n > 31) throw new IllegalArgumentException("illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}

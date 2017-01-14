package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;

public class UserInfoCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length != 1) {
            MessageUtils.sendErrorMessage("Incorrect usage! Usage: " + FlareBot.getPrefix(channel.getGuild().getID()) + "userinfo (user)", channel);
        } else {
            if (args[0].matches("\\d+") || args[0].matches("<@\\d+>")) {
                String id = args[0];
                if (args[0].matches("<@\\d+>"))
                    id = id.replace("<@", "").replace(">", "");
                IUser user = channel.getGuild().getUserByID(id);
                if (user == null) {
                    MessageUtils.sendErrorMessage("That user does not exist!", channel);
                    return;
                }

                String finalId = id;
                try {
                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender).appendField("User Info", "User: " + user.getName() + "#" + user.getDiscriminator()
                            + "\nID: " + user.getID() + "\nAvatar: " + user.getAvatarURL(), true)
                            .appendField("General Info", "Servers: " + FlareBot.getInstance().getClient().getGuilds().stream().filter(guild -> guild.getUserByID(finalId) != null).count()
                                    + " shared\nRoles: " + StringUtils.join(user.getRolesForGuild(channel.getGuild()), ", ").trim(), true)
                            .appendField("Time Data", "Created: " + user.getCreationDate().toString() + " UTC\nJoined: " + channel.getGuild().getJoinTimeForUser(user).toString()
                                    + " UTC\nLast Seen: Unknown\nLast Spoke: Unknown", false).withThumbnail(user.getAvatarURL()), channel);
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
}

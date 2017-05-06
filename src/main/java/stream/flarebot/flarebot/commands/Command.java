package stream.flarebot.flarebot.commands;

import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;

public interface Command {

    void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member);

    String getCommand();

    String getDescription();

    String getUsage();

    CommandType getType();

    default String getPermission() {
        return "flarebot." + getCommand();
    }

    default String[] getAliases() {
        return new String[]{};
    }

    default PerGuildPermissions getPermissions(MessageChannel chan) {
        return FlareBot.getInstance().getPermissions(chan);
    }

    default boolean isDefaultPermission() {
        return (getPermission() != null && getType() != CommandType.HIDDEN && getType() != CommandType.MODERATION);
    }

    default boolean deleteMessage() {
        return true;
    }

    default char getPrefix(Guild guild) {
        return FlareBot.getPrefix(guild.getId());
    }
}

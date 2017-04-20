package stream.flarebot.flarebot.commands;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import net.dv8tion.jda.core.entities.*;

public interface Command {

    void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member);

    String getCommand();

    String getDescription();

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
        return (getPermission() != null);
    }

    default boolean deleteMessage() {
        return true;
    }

    default char getPrefix(Guild guild) {
        return FlareBot.getPrefix(guild.getId());
    }
}

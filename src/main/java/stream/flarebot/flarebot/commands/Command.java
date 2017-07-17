package stream.flarebot.flarebot.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;

import java.util.EnumSet;

public interface Command {

    void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member);

    String getCommand();

    String getDescription();

    String getUsage();

    CommandType getType();

    default String getPermission() {
        return "flarebot." + getCommand();
    }

    default EnumSet<Permission> getDiscordPermission() {
        return EnumSet.noneOf(Permission.class);
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

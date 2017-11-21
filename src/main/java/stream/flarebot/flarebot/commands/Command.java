package stream.flarebot.flarebot.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;

import java.util.EnumSet;

public interface Command {

    void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member);

    String getCommand();

    String getDescription();

    String getUsage();

    CommandType getType();

    default String getExtraInfo() {
        return null;
    }

    default String getPermission() {
        return getType() == CommandType.SECRET ? null : "flarebot." + getCommand();
    }

    default EnumSet<Permission> getDiscordPermission() {
        return EnumSet.noneOf(Permission.class);
    }

    default String[] getAliases() {
        return new String[]{};
    }

    default PerGuildPermissions getPermissions(TextChannel chan) {
        return FlareBotManager.getInstance().getGuild(chan.getGuild().getId()).getPermissions();
    }

    default boolean isDefaultPermission() {
        return (getPermission() != null && getType() != CommandType.SECRET && getType() != CommandType.MODERATION);
    }

    default boolean deleteMessage() {
        return true;
    }

    default boolean isBetaTesterCommand() {
        return false;
    }

    default char getPrefix(Guild guild) {
        return FlareBot.getPrefix(guild.getId());
    }
}

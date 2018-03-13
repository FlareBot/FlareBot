package stream.flarebot.flarebot.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
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

    default stream.flarebot.flarebot.permissions.Permission getPermission() {
        return stream.flarebot.flarebot.permissions.Permission.getPermission(this.getClass());
    }

    default EnumSet<Permission> getDiscordPermission() {
        return EnumSet.noneOf(Permission.class);
    }

    default String[] getAliases() {
        return new String[]{};
    }

    default PerGuildPermissions getPermissions(TextChannel chan) {
        return FlareBotManager.instance().getGuild(chan.getGuild().getId()).getPermissions();
    }

    default boolean isDefaultPermission() {
        return getPermission() != null && !getType().isInternal();
    }

    default boolean deleteMessage() {
        return true;
    }

    default boolean isBetaTesterCommand() {
        return false;
    }

    default char getPrefix(Guild guild) {
        return FlareBotManager.instance().getGuild(guild.getId()).getPrefix();
    }
}
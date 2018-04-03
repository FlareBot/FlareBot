package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PermissionOverride;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.*;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;

import java.util.EnumSet;

public class LockChatCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        String reason = null;
        TextChannel tc = channel;
        if (args.length > 0) {
            TextChannel tmp = GuildUtils.getChannel(args[args.length - 1], guild);
            if (tmp != null) {
                tc = tmp;
                if (args.length >= 2)
                    reason = MessageUtils.getMessage(args, 0, args.length - 1);
            } else
                reason = MessageUtils.getMessage(args, 0);
        }

        if (!guild.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            MessageUtils.sendErrorMessage("I can't lock the chat due to lack of permissions! " +
                    "I need the `Manage Roles` permission", channel);
            return;
        }

        PermissionOverride everyoneOvr = tc.getPermissionOverride(guild.getGuild().getPublicRole());
        boolean locking = everyoneOvr.getAllowed().contains(Permission.MESSAGE_WRITE);

        EnumSet<Permission> perm = EnumSet.of(Permission.MESSAGE_WRITE);
        EnumSet<Permission> empty = EnumSet.noneOf(Permission.class);

        tc.putPermissionOverride(guild.getGuild().getPublicRole())
                .setPermissions(locking ? empty : perm, locking ? perm : empty)
                .queue();
        tc.putPermissionOverride(guild.getGuild().getSelfMember())
                .setPermissions(locking ? perm : empty, locking ? empty : perm)
                .queue();

        if (tc.getIdLong() != channel.getIdLong())
            channel.sendMessage(new EmbedBuilder().setColor(locking ? ColorUtils.RED : ColorUtils.GREEN)
                    .setDescription(tc.getAsMention() + " has been " + (locking ? "locked" : "unlocked") + "!")
                    .build()).queue();
        if (guild.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_WRITE))
            channel.sendMessage(new EmbedBuilder().setColor(locking ? ColorUtils.RED : ColorUtils.GREEN)
                    .setDescription("The chat has been " + (locking ? "locked" : "unlocked") + " by a staff member!"
                            + (reason != null ? "\nReason: " + reason : ""))
                    .build()).queue();
    }

    @Override
    public String getCommand() {
        return "lockchat";
    }

    @Override
    public String getDescription() {
        return "Locks the chat so only moderators can talk.";
    }

    @Override
    public String getUsage() {
        return "`{%}lockchat [reason] [channel]` - Locks the chat so only moderators can talk.";
    }

    @Override
    public stream.flarebot.flarebot.permissions.Permission getPermission() {
        return stream.flarebot.flarebot.permissions.Permission.LOCKCHAT_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"chatlock", "lc", "lock"};
    }

    @Override
    public boolean isBetaTesterCommand() {
        return true;
    }
}

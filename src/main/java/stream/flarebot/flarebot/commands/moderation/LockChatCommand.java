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
import stream.flarebot.flarebot.scheduler.FlareBotTask;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.FormatUtils;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.general.ParseUtils;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class LockChatCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (!guild.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            MessageUtils.sendErrorMessage("I can't lock the chat due to lack of permissions! " +
                    "I need the `Manage Roles` permission", channel);
            return;
        }

        String reason = null;
        long time = -1;

        @Nonnull
        AtomicReference<TextChannel> tc = new AtomicReference<>(channel);
        if (args.length >= 1) {
            TextChannel tmp = ParseUtils.parseChannel(guild.getGuild(), args[0]);
            if (tmp != null)
                tc.set(tmp);

            if (tmp == null || args.length >= 2) {
                Long l = GeneralUtils.parseTime(tmp == null ? args[0] : args[1]);
                if (l == null) {
                    MessageUtils.sendErrorMessage("Invalid time format! Please use something like `1h10m`", channel);
                    return;
                }
                time = l;
            }

            if (tmp != null && time > 0)
                reason = MessageUtils.getMessage(args, 2);
            else if ((tmp == null && time > 0) || (tmp != null && time == -1))
                reason = MessageUtils.getMessage(args, 1);
            else
                reason = MessageUtils.getMessage(args, 0);

            if (reason.isEmpty()) reason = null;
        }

        PermissionOverride everyoneOvr = tc.get().getPermissionOverride(guild.getGuild().getPublicRole());
        boolean locking = !everyoneOvr.getDenied().contains(Permission.MESSAGE_WRITE);

        EnumSet<Permission> perm = EnumSet.of(Permission.MESSAGE_WRITE);
        EnumSet<Permission> empty = EnumSet.noneOf(Permission.class);

        tc.get().getPermissionOverride(guild.getGuild().getPublicRole()).getManager()
                .deny(locking ? perm : empty)
                .clear(locking ? empty : perm)
                .reason(reason + "\nDone by: " + sender.getIdLong())
                .queue();
        tc.get().putPermissionOverride(guild.getGuild().getSelfMember())
                .setPermissions(locking ? perm : empty, empty)
                .reason(reason + "\nDone by: " + sender.getIdLong())
                .queue();

        if (tc.get().getIdLong() != channel.getIdLong())
            channel.sendMessage(new EmbedBuilder().setColor(locking ? ColorUtils.RED : ColorUtils.GREEN)
                    .setDescription(tc.get().getAsMention() + " has been " + (locking ? "locked" : "unlocked") + "!")
                    .build()).queue();

        if (guild.getGuild().getSelfMember().hasPermission(tc.get(), Permission.MESSAGE_WRITE))
            channel.sendMessage(new EmbedBuilder().setColor(locking ? ColorUtils.RED : ColorUtils.GREEN)
                    .setDescription("The chat has been " + (locking ? "locked" : "unlocked") + " by a staff member"
                            + (locking && time > 0 ? " for "
                            + FormatUtils.formatTime(time, TimeUnit.MILLISECONDS, true, false) : "") + "!"
                            + (reason != null ? "\nReason: " + reason : ""))
                    .build()).queue();

        if (locking && time > 0) {
            new FlareBotTask("ChannelUnlock-" + tc.get().getIdLong()) {
                @Override
                public void run() {
                    tc.get().getPermissionOverride(guild.getGuild().getPublicRole()).getManager()
                            .clear(Permission.MESSAGE_WRITE)
                            .queue();

                    if (guild.getGuild().getSelfMember().hasPermission(tc.get(), Permission.MESSAGE_WRITE))
                        channel.sendMessage(new EmbedBuilder().setColor(ColorUtils.GREEN)
                                .setDescription("The chat has been unlocked")
                                .build()).queue();
                }
            }.delay(time);
        }
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
        return "`{%}lockchat [channel] [time] [reason]` - Locks the chat so only moderators can talk.";
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

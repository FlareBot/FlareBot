package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

public class LockChatCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        String reason = null;
        TextChannel tc = channel;
        if (args.length > 0) {
            TextChannel tmp = GeneralUtils.getChannel(args[args.length - 1], guild);
            if (tmp != null) {
                tc = tmp;
                reason = MessageUtils.getMessage(args, 0, args.length - 1);
            } else
                reason = MessageUtils.getMessage(args, 0);
        }

        if (!guild.getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
            MessageUtils.sendErrorMessage("I can't lock the chat due to lack of permissions! " +
                    "I need the `Manage Roles` permission", channel);
            return;
        }
        if (tc.getPermissionOverride(guild.getGuild().getPublicRole()).getDenied().contains(Permission.MESSAGE_WRITE)) {
            tc.getPermissionOverride(guild.getGuild().getPublicRole()).getManager().grant(Permission.MESSAGE_WRITE).queue();
            channel.sendMessage(new EmbedBuilder().setColor(ColorUtils.GREEN)
                    .setDescription("The chat has been unlocked!" + (reason != null ? "\nReason: " + reason : ""))
                    .build()).queue();
        } else {
            tc.getPermissionOverride(guild.getGuild().getPublicRole()).getManager().deny(Permission.MESSAGE_WRITE).queue();
            channel.sendMessage(new EmbedBuilder().setColor(ColorUtils.RED)
                    .setDescription("The chat has been locked by a staff member!" + (reason != null ? "\nReason: " + reason : ""))
                    .build()).queue();
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
        return "`{%}lockchat [reason] [channel]` - Locks the chat so only moderators can talk.";
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

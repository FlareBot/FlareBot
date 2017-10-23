package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.ModlogAction;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.EnumSet;

public class ForceBanCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.BAN_MEMBERS)) {
                User user = GeneralUtils.getUser(args[0], true);
                if (user == null) {
                    MessageUtils.sendErrorMessage("We cannot find that user!", channel, sender);
                    return;
                }
                String reason = null;
                if (args.length >= 2)
                    reason = MessageUtils.getMessage(args, 1);
                guild.getAutoModConfig().postToModLog(user, sender, ModlogAction.BAN.toPunishment(), reason);
                try {
                    channel.getGuild().getController().ban(user, 7, reason).queue();
                    MessageUtils.sendSuccessMessage("The ban hammer has been struck on " + user.getName() + " \uD83D\uDD28", channel, sender);
                } catch (PermissionException e) {
                    MessageUtils.sendErrorMessage(String.format("Cannot ban user **%s#%s**! I do not have permission!", user.getName(), user.getDiscriminator()), channel);
                }
            } else {
                MessageUtils.sendErrorMessage("We can't ban users! Make sure we have the `Ban Members` permission!", channel);
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    @Override
    public String getCommand() {
        return "forceban";
    }

    @Override
    public String getDescription() {
        return "Bans a user that is potentially not on the server.";
    }

    @Override
    public String getUsage() {
        return "`{%}ban <user> [reason]` - Bans a user with an optional reason";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public EnumSet<Permission> getDiscordPermission() {
        return EnumSet.of(Permission.BAN_MEMBERS);
    }
}

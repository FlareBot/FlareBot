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

public class KickCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.KICK_MEMBERS)) {
                User user = GeneralUtils.getUser(args[0]);
                if (user == null) {
                    MessageUtils.sendErrorMessage("We cannot find that user! Try their ID if you didn't already.", channel, sender);
                    return;
                }
                Member target = guild.getGuild().getMember(user);
                if (target == null) {
                    MessageUtils.sendErrorMessage("That user is not on this server therefor cannot be kicked!",
                            channel, sender);
                    return;
                }
                String reason = null;
                if (args.length >= 2)
                    reason = MessageUtils.getMessage(args, 1);
                try {
                    channel.getGuild().getController().kick(target, reason).queue();
                    guild.getAutoModConfig().postToModLog(user, sender, ModlogAction.KICK.toPunishment(), reason);
                    MessageUtils.sendSuccessMessage(user.getName() + " has been kicked from the server! (`" + reason.replaceAll("`", "'") + "`)", channel, sender);
                } catch (PermissionException e) {
                    MessageUtils.sendErrorMessage(String.format("Cannot kick player **%s#%s**! I do not have permission!", user.getName(), user.getDiscriminator()), channel);
                }
            } else {
                MessageUtils.sendErrorMessage("We can't kick users! Make sure we have the `Kick Members` permission!", channel, sender);
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    @Override
    public String getCommand() {
        return "kick";
    }

    @Override
    public String getDescription() {
        return "Kicks a user";
    }

    @Override
    public String getUsage() {
        return "`{%}kick <user> [reason]` - Kicks a user with an optional reason";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public EnumSet<Permission> getDiscordPermission() {
        return EnumSet.of(Permission.KICK_MEMBERS);
    }
}

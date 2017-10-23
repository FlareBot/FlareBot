package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.ModlogAction;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

public class MuteCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            User user = GeneralUtils.getUser(args[0], guild.getGuildId());
            if (user == null) {
                MessageUtils.sendErrorMessage("Invalid user!!", channel);
                return;
            }
            if (guild.getMutedRole() == null) {
                MessageUtils.sendErrorMessage("Error getting the \"Muted\" role! Check FlareBot has permissions to create and apply it!", channel);
                return;
            }
            try {
                guild.getAutoModGuild().muteUser(guild.getGuild(), guild.getGuild().getMember(user));
            } catch (HierarchyException e) {
                MessageUtils.sendErrorMessage("Cannot apply the mute role, make sure it is below FlareBot in the role hierarchy.",
                        channel);
                return;
            }
            String reason = args.length > 1 ? FlareBot.getMessage(args, 1) : null;
            guild.getAutoModConfig().postToModLog(user, sender, ModlogAction.MUTE.toPunishment(), reason);
            MessageUtils.sendSuccessMessage("Muted " + user.getAsMention() + (reason == null ? "" : " (`" + reason.replaceAll("`", "'") + "`)"),
                    channel, sender);
        }
    }

    @Override
    public String getCommand() {
        return "mute";
    }

    @Override
    public String getDescription() {
        return "Mutes people";
    }

    @Override
    public String getUsage() {
        return "`{%}mute <user>` - Mutes a user";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.modlog.ModAction;
import stream.flarebot.flarebot.mod.modlog.ModlogHandler;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;

public class SoftBanCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            User target = GuildUtils.getUser(args[0], guild.getGuildId());
            if (target == null) {
                MessageUtils.sendErrorMessage("We cannot find that user! Try their ID if you didn't already.", channel, sender);
                return;
            }
            String reason = null;
            if (args.length >= 2)
                reason = MessageUtils.getMessage(args, 1);

            ModlogHandler.getInstance().handleAction(guild, channel, sender, target, ModAction.SOFTBAN, reason);
        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    @Override
    public String getCommand() {
        return "softban";
    }

    @Override
    public String getDescription() {
        return "Ban a user to clean their messages - this unbans right away! (Basically a kick + large purge)";
    }

    @Override
    public String getUsage() {
        return "`{%}softban <user> [reason]` - Softban a user with an optional reason";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public Permission getPermission() {
        return Permission.SOFTBAN_COMMAND;
    }
}

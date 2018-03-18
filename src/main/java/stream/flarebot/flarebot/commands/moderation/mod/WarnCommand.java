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

public class WarnCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            User user = GuildUtils.getUser(args[0]);
            if (user == null) {
                MessageUtils.sendErrorMessage("We couldn't find that user!!", channel);
                return;
            }
            String reason = null;
            if (args.length >= 2) reason = MessageUtils.getMessage(args, 1);

            ModlogHandler.getInstance().handleAction(guild, channel, sender, user, ModAction.WARN, reason);
        }
    }

    @Override
    public String getCommand() {
        return "warn";
    }

    @Override
    public String getDescription() {
        return "Warns a user";
    }

    @Override
    public String getUsage() {
        return "`{%}warn <user> (reason)` - Warns a user.";
    }

    @Override
    public Permission getPermission() {
        return Permission.WARN_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

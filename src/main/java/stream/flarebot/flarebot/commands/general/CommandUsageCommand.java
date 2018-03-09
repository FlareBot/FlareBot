package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.util.MessageUtils;

public class CommandUsageCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            Command c = FlareBot.getInstance().getCommand(args[0], sender);
            if (!canRunCommand(c, sender))
                MessageUtils.sendErrorMessage("That is not a command!", channel);
            } else {
                MessageUtils.sendUsage(c, channel, sender, new String[]{});
            }
        }
    }
    
    private boolean canRunCommand(Command command, User user) {
        if (command == null) return false;
        
        if (command.getType().isInternal()) {
            Guild g = FlareBot.getInstance().getOfficialGuild();
            
            if (g.getMember(user) != null) {
                return g.getMember(user).getRoles().contains(g.getRoleById(command.getType().getRoleId()));
            } else
                return false;
        } else
            return true;
    }

    @Override
    public String getCommand() {
        return "usage";
    }

    @Override
    public String getDescription() {
        return "Allows you to view usages for other commands";
    }

    @Override
    public String getUsage() {
        return "`{%}usage <command_name>` - Displays the usage for another command.";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

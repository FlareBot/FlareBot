package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

public class CommandUsageCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            Command c = FlareBot.getInstance().getCommand(args[0]);
            if (c == null || (c.getType() == CommandType.SECRET && !getPermissions(channel).isCreator(sender))) {
                MessageUtils.sendErrorMessage("That is not a command!", channel);
            } else {
                MessageUtils.sendUsage(c, channel, sender, new String[]{});
            }
        }
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
        return "{%}usage <command_name>";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

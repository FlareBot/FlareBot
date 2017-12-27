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

public class SendMessageCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            MessageUtils.sendErrorMessage("That is not a command!", channel);
        }
    }

    @Override
    public String getCommand() {
        return "send";
    }

    @Override
    public String getDescription() {
        return "Allows you to send a message that will always fail to send.";
    }

    @Override
    public String getUsage() {
        return "`{%}send <message>` - Will send a message";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

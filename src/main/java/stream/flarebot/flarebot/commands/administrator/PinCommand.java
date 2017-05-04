package stream.flarebot.flarebot.commands.administrator;

import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class PinCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            String messageId = args[0].replaceAll("[^0-9]", "");
            Message msg = channel.getMessageById(messageId).complete();
            msg.pin().complete();
            channel.getHistory().retrievePast(1).complete().get(0).delete().queue();
        } else {
            MessageUtils.sendErrorMessage("Usage: `pin (messageId)`", channel);
        }
    }

    @Override
    public String getCommand() {
        return "pin";
    }

    @Override
    public String getDescription() {
        return "Pin a message";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}

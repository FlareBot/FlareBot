package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;

public class InviteCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        MessageUtils.sendPM(channel, sender, "You can invite me to your server using the link below!\n"
                + FlareBot.getInstance().getInvite());
    }

    @Override
    public String getCommand() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Get my invite link!";
    }

    @Override
    public String getUsage() {
        return "`{%}invite` - Gets FlareBot's invite link";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public String getPermission() {
        return null;
    }
}

package stream.flarebot.flarebot.commands.automod;

import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class WarningsCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length != 2) {
            channel.sendMessage("").queue();
            return;
        }
    }

    @Override
    public String getCommand() {
        return "warnings";
    }

    @Override
    public String getDescription() {
        return "Lists/controls warning points in your guild.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

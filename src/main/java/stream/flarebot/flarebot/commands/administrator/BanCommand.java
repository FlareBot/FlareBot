package stream.flarebot.flarebot.commands.administrator;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;

public class BanCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length >= 2){
            
        }else {
            MessageUtils.sendUsage(this, channel);
        }
    }

    @Override
    public String getCommand() {
        return "ban";
    }

    @Override
    public String getDescription() {
        return "Ban a user";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

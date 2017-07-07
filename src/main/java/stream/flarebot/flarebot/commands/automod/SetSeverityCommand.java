package stream.flarebot.flarebot.commands.automod;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

public class SetSeverityCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {

    }

    @Override
    public String getCommand() {
        return "setseverity";
    }

    @Override
    public String getDescription() {
        return "Change the severity of an action";
    }

    //TODO: Enter usage when finished
    @Override
    public String getUsage() {
        return "{%}";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"severity"};
    }
}

package stream.flarebot.flarebot.commands.automod;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

public class WarningsCommand implements Command {

    /*

        _points - Shows your points and why
        _points (user) - Shows someone elses points and why - flarebot.warnings.other
        _points set (user) - Sets a users points - This will also run the punishments if any
        _points reset (user) - Clean a users record - Sets points to 0 and removes infractions

     */

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length == 1) {

        } else if(args.length == 2) {

        }else{
            // Show their points
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
    public String getUsage() {
        // TODO: Insert when finished
        return "";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String[] getAliases() {
        return new String[] {"points"};
    }
}

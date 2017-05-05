package stream.flarebot.flarebot.commands.mod;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;

public class ReportsCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {

    }

    @Override
    public String getCommand() {
        return "reports";
    }

    @Override
    public String getDescription() {
        return "Used to report users or view reports";
    }

    @Override
    public String getUsage() {
        return "reports list: lists all reports, and gives you report counts\n" +
                "reports view: If you have permission flarebot.reports.view you an veiw all reports\n" +
                "    otherwise you can only veiw the reports you created\n" +
                "reports report <user> <reason>: Reports a user with the giving reason\n" +
                "Info: The bot will DM you if the information is correct and the report was sucesfully submitted.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

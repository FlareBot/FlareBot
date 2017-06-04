package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.Report;
import stream.flarebot.flarebot.objects.ReportStatus;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.ReportManager;

import java.sql.Timestamp;

public class ReportCommand implements Command {


    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 2) {
            User user = MessageUtils.getUser(args[0], channel.getId());
            if (user == null) {
                MessageUtils.sendErrorMessage("Invalid user: " + args[1], channel);
                return;
            }

            Report report = new Report(channel.getGuild().getId(), ReportManager.getInstance().getLastId(), MessageUtils.getMessage(args, 1), sender.getId(), user.getId(), new Timestamp(System.currentTimeMillis()), ReportStatus.OPEN);

            ReportManager.getInstance().report(channel.getGuild().getId(), report);

            MessageUtils.sendPM(channel, sender, MessageUtils.getReportEmbed(sender, report, channel).setDescription("Successfully reported the user"));
        }
    }

    @Override
    public String getCommand() {
        return "report";
    }

    @Override
    public String getDescription() {
        return "Allows users to report other members on the server";
    }

    @Override
    public String getUsage() {
        return "`{%}report <user> [reason]` - reports a user your guild moderators";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

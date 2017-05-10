package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;

import java.awt.*;

public class ReportsCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length == 0){
            MessageUtils.sendUsage(this, channel);
        } else if(args.length == 1){
            switch (args[0]){
                case "list":{
                    if(getPermissions(message.getChannel()).hasPermission(member, "flarebot.reports.list")){
                        //TODO write report stuffs.
                    } else {
                        MessageUtils.sendErrorMessage(new EmbedBuilder().setDescription("You need the permission `flarebot.reports.list` to do this."), channel);
                    }
                }
            }
        }
    }

    @Override
    public String getCommand() { return "reports"; }

    @Override
    public String getDescription() { return "Used to view reports and to report users"; }

    @Override
    public String getUsage() {
        return "{%}reports\n" +
                "{%}reports list [page]" +
                "{%}reports view <number>\n" +
                "{%}reports report <user> <reason>";
    }

    @Override
    public CommandType getType() { return CommandType.MODERATION; }
}

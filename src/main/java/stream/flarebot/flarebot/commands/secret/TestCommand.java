package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TestCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        try {
            PrintWriter out = new PrintWriter("data.json");
            out.println(FlareBot.GSON.toJson(guild));
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        sender.openPrivateChannel().complete().sendFile(new File("data.json"), new MessageBuilder().append('\u200B').build()).queue();
    }

    @Override
    public String getCommand() {
        return "test";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return "{%}test";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }
}

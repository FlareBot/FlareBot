package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONTokener;
import stream.flarebot.flarebot.api.ApiRequesterBuilder;
import stream.flarebot.flarebot.api.ApiRoute;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.stream.Collectors;

public class TestCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        // Served it's purpose once again :) Now it's free to be reused... no memes pls

        try {
            JSONObject object = new JSONObject(new JSONTokener(new FileReader(new File("test.json"))));
            Response res = new ApiRequesterBuilder(ApiRoute.TEST).setCompressed(true).setBody(object).request();
            StringBuilder sb = new StringBuilder();
            for(String s : res.headers().toMultimap().keySet())
                sb.append(s).append(": ").append(res.headers().toMultimap().get(s).stream().collect(Collectors.joining(", "))).append("\n");
            channel.sendMessage(res.code() + "\n" + res.message()).queue();
            channel.sendMessage(sb.toString()).queue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        return CommandType.SECRET;
    }
}

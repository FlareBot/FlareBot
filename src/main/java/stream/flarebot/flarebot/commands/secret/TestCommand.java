package stream.flarebot.flarebot.commands.secret;

import com.datastax.driver.mapping.Mapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import okhttp3.Response;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.api.ApiRequester;
import stream.flarebot.flarebot.api.ApiRoute;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.util.MessageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TestCommand implements Command {

    private Mapper<GuildWrapper> mapper;
    
    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if(mapper == null) mapper = CassandraController.getMappingManager().mapper(GuildWrapper.class);
        
        Message msg = channel.sendMessage("Saving...").complete();
        CassandraController.getMappingManager().mapper(GuildWrapper.class).save(guild);
        msg.editMessage("Saved!").complete();
        
        channel.sendMessage(MessageUtils.hastebin(FlareBot.GSON.toJson(guild))).queue();
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

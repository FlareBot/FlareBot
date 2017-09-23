package stream.flarebot.flarebot.commands.secret;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class TestCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        JsonArray array = new JsonArray();
        for (Command cmd : FlareBot.getInstance().getCommands()) {
            JsonObject cmdObj = new JsonObject();
            cmdObj.addProperty("command", cmd.getCommand());
            cmdObj.addProperty("description", cmd.getDescription());
            cmdObj.addProperty("permission", cmd.getPermission() == null ? "" : cmd.getPermission());
            cmdObj.addProperty("type", cmd.getType().toString());
            JsonArray aliases = new JsonArray();
            for (String s : cmd.getAliases())
                aliases.add(s);
            cmdObj.add("aliases", aliases);
            array.add(cmdObj);
        }
        Response res = ApiRequester.request(ApiRoute.COMMANDS, array);
        channel.sendMessage(res.code() + "\n" + res.message()).queue();

        //
        // Testing plain message, embed and embed with images
        //

        /*long a = System.currentTimeMillis();
        long finalA = a;
        channel.sendMessage("Test command\n**Header**\nSub info\nBlah blah\n\nRequested by " + sender.getName() + "#" + sender.getDiscriminator())
                .complete();
        System.out.println("Sent in " + (System.currentTimeMillis() - finalA) + "ms");

        a = System.currentTimeMillis();
        channel.sendMessage(new EmbedBuilder().setTitle("Test command", null).addField("Sub info", "Blah blah", true)
                .setFooter("Requested by " + sender.getName() + "#" + sender.getDiscriminator(), null).build())
                .complete();
        System.out.println("Sent in " + (System.currentTimeMillis() - a) + "ms");

        a = System.currentTimeMillis();
        channel.sendMessage(new EmbedBuilder().setTitle("Test command", null).setAuthor("Test command", null, FlareBot.getInstance().getClients()[0].getSelfUser().getEffectiveAvatarUrl())
                .addField("Sub info", "Blah blah", true)
                .setThumbnail(FlareBot.getInstance().getClients()[0].getSelfUser().getEffectiveAvatarUrl())
                .setFooter("Requested by " + sender.getName() + "#" + sender.getDiscriminator(), sender.getEffectiveAvatarUrl()).build())
                .complete();
        System.out.println("Sent in " + (System.currentTimeMillis() - a) + "ms");*/

        //
        // Testing different embeds
        //

        /*long a = System.currentTimeMillis();
        channel.sendMessage(new EmbedBuilder().setTitle("Test command", null).setAuthor("Test command", null, null)
                .addField("Sub info", "Blah blah", true)
                .setFooter("Requested by " + sender.getName() + "#" + sender.getDiscriminator(), null).build())
                .complete();
        System.out.println("Sent in " + (System.currentTimeMillis() - finalA) + "ms");

        a = System.currentTimeMillis();
        long finalA1 = a;
        channel.sendMessage(new EmbedBuilder().setTitle("Test command", null).setAuthor("Test command", null, FlareBot.getInstance().getClients()[0].getSelfUser().getEffectiveAvatarUrl())
                .addField("Sub info", "Blah blah", true)
                .setThumbnail(FlareBot.getInstance().getClients()[0].getSelfUser().getEffectiveAvatarUrl())
                .setFooter("Requested by " + sender.getName() + "#" + sender.getDiscriminator(), null).build())
                .complete();
        System.out.println("Sent in " + (System.currentTimeMillis() - finalA1) + "ms");

        a = System.currentTimeMillis();
        long finalA2 = a;
        channel.sendMessage(new EmbedBuilder().setTitle("Test command", null).setAuthor("Test command", null, FlareBot.getInstance().getClients()[0].getSelfUser().getEffectiveAvatarUrl())
                .addField("Sub info", "Blah blah", true)
                .setFooter("Requested by " + sender.getName() + "#" + sender.getDiscriminator(), sender.getEffectiveAvatarUrl()).build())
                .complete();
        System.out.println("Sent in " + (System.currentTimeMillis() - finalA2) + "ms");

        a = System.currentTimeMillis();
        long finalA3 = a;
        channel.sendMessage(new EmbedBuilder().setTitle("Test command", null).setAuthor("Test command", null, null)
                .addField("Sub info", "Blah blah", true)
                .setFooter("Requested by " + sender.getName() + "#" + sender.getDiscriminator(), sender.getEffectiveAvatarUrl()).build())
                .complete();
        System.out.println("Sent in " + (System.currentTimeMillis() - finalA3) + "ms");*/
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

package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        // Served it's purpose once again :) Now it's free to be reused... no memes pls
        List<Long> average = new ArrayList<>();
        String command = args.length > 0 ? args[0] : "avatar";
        channel.sendMessage("Warning up the JVM").complete();
        for (int i = 0; i < 1_000_000; i++) {
            long a = System.nanoTime();
            Command cmd = FlareBot.getInstance().getCommand(command, sender);
            long b = System.nanoTime();
            average.add((b - a));
        }
        channel.sendMessage("Warm-up complete - Average: " + average.stream().mapToLong(Long::longValue).average().getAsDouble() + "ns").complete();
        average.clear();

        channel.sendMessage("Method 1 (getCommand)").complete();
        for(int i = 0; i < 1000; i++) {
            long a = System.nanoTime();
            Command cmd = FlareBot.getInstance().getCommand(command, sender);
            long b = System.nanoTime();
            average.add((b - a));
        }
        channel.sendMessage("Method 1 complete - Average: " + average.stream().mapToLong(Long::longValue).average().getAsDouble() + "ns").complete();
        average.clear();

        channel.sendMessage("Method 2 (getCommand2)").complete();
        for(int i = 0; i < 1000; i++) {
            long a = System.nanoTime();
            Command cmd = FlareBot.getInstance().getCommand2(command, sender);
            long b = System.nanoTime();
            average.add((b - a));
        }
        channel.sendMessage("Method 2 complete - Average: " + average.stream().mapToLong(Long::longValue).average().getAsDouble() + "ns").complete();
        average.clear();
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
        return CommandType.USEFUL;
    }
}

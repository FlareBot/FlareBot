package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ShardInfoCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        channel.sendMessage(buildTable(FlareBot.getInstance().getClients())).queue();
    }

    @Override
    public String getCommand() {
        return "shardinfo";
    }

    @Override
    public String getDescription() {
        return "Shows info about a shard";
    }

    @Override
    public String getUsage() {
        return "{%}shardsinfo";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    public String buildTable(JDA[] jdas) {
        List<String> headers = new ArrayList<>();
        headers.add("Shard ID");
        headers.add("Status");
        headers.add("Guild Count");

        List<List<String>> table = new ArrayList<>();
        for (JDA jda : jdas) {
            List<String> row = new ArrayList<>();
            row.add(MessageUtils.getShardId(jda));
            row.add(WordUtils.capitalizeFully(jda.getStatus().toString().replace("_", " ")));
            row.add(String.valueOf(jda.getGuilds().size()));
            table.add(row);
        }

        return MessageUtils.makeAsciiTable(headers, table, null, "swift");
    }
}

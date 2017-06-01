package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang.WordUtils;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.MessageUtils;

public class ShardInfoCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        StringBuilder builder = new StringBuilder(String.format("```diff\nTotal: %d\n", FlareBot.getInstance().getClients().length));
        for (JDA jda : FlareBot.getInstance().getClients()) {
            switch (jda.getStatus()) {
                case CONNECTED:
                    builder.append(String.format("+ Shard ID: %s - %s\n" +
                            "+     Guild Count: %d\n", MessageUtils.getShardId(jda), WordUtils.capitalizeFully(jda.getStatus().toString().replace("_", " ")), jda.getGuilds().size()));
                    break;
                default:
                    builder.append(String.format("- Shard ID: %s - %s\n" +
                            "+     Guild Count: %d\n", MessageUtils.getShardId(jda), WordUtils.capitalizeFully(jda.getStatus().toString().replace("_", " ")), jda.getGuilds().size()));
            }
        }
        builder.append("```");
        channel.sendMessage(builder.toString()).queue();
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
        return CommandType.HIDDEN;
    }
}

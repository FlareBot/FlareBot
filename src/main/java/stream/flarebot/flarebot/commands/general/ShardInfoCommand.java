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
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ShardInfoCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        List<String> headers = new ArrayList<>();
        headers.add("Shard ID");
        headers.add("Status");
        headers.add("Ping");
        headers.add("Guild Count");
        headers.add("VCs");

        List<List<String>> table = new ArrayList<>();
        for (JDA jda : FlareBot.getInstance().getClients()) {
            List<String> row = new ArrayList<>();
            row.add(GeneralUtils.getShardId(jda) + (GeneralUtils.getShardIdAsInt(channel.getJDA()) == GeneralUtils.getShardIdAsInt(jda) ? " (You)" : ""));
            row.add(WordUtils.capitalizeFully(jda.getStatus().toString().replace("_", " ")));
            row.add(String.valueOf(jda.getPing()));
            row.add(String.valueOf(jda.getGuilds().size()));
            row.add(String.valueOf(jda.getVoiceChannels().stream().filter(vc -> vc.getMembers().contains(vc.getGuild().getSelfMember())).count()));
            table.add(row);
        }

        channel.sendMessage(MessageUtils.makeAsciiTable(headers, table, null, "swift")).queue();
    }

    @Override
    public String getCommand() {
        return "shardinfo";
    }

    @Override
    public String getDescription() {
        return "Shows info about the shards";
    }

    @Override
    public String getUsage() {
        return "`{%}shardinfo` - Shows info about the shards";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

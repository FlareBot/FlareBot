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
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.ShardUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShardInfoCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        List<String> headers = new ArrayList<>();
        headers.add("Shard ID");
        headers.add("Status");
        headers.add("Ping");
        headers.add("Guild Count");
        headers.add("Connected VCs");

        List<List<String>> table = new ArrayList<>();
        List<JDA> shards = new ArrayList<>(FlareBot.getInstance().getShards());
        Collections.reverse(shards);
        for (JDA jda : shards) {
            List<String> row = new ArrayList<>();
            row.add(ShardUtils.getDisplayShardId(jda) +
                    (ShardUtils.getShardId(channel.getJDA()) == ShardUtils.getShardId(jda) ? " (You)" : ""));
            row.add(WordUtils.capitalizeFully(jda.getStatus().toString().replace("_", " ")));
            row.add(String.valueOf(jda.getPing()));
            row.add(String.valueOf(jda.getGuilds().size()));
            row.add(String.valueOf(jda.getVoiceChannels().stream().filter(vc -> vc.getMembers().contains(vc.getGuild()
                    .getSelfMember())).count()));
            table.add(row);
            // TODO: Replace this hotfix with pagination
            if (table.size() == 20) {
                channel.sendMessage(MessageUtils.makeAsciiTable(headers, table, null, "swift")).queue();
                table = new ArrayList<>();
            }
        }
        if (table.size() > 0) {
            channel.sendMessage(MessageUtils.makeAsciiTable(headers, table, null, "swift")).queue();
        }
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
        return "`{%}shardinfo` - Shows info about the shards.";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

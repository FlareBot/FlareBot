package stream.flarebot.flarebot.commands.informational;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.ShardUtils;
import stream.flarebot.flarebot.util.buttons.ButtonGroupConstants;
import stream.flarebot.flarebot.util.pagination.PagedTableBuilder;
import stream.flarebot.flarebot.util.pagination.PaginationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShardInfoCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        PagedTableBuilder tb = new PagedTableBuilder();

        tb.addColumn("Shard ID");
        tb.addColumn("Status");
        tb.addColumn("Ping");
        tb.addColumn("Guild Count");
        tb.addColumn("Connected VCs");

        List<JDA> shards = new ArrayList<>(Getters.getShards());
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
            tb.addRow(row);
        }
        PaginationUtil.sendPagedMessage(channel, tb.build(), 0, sender, ButtonGroupConstants.SHARDINFO);
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
    public Permission getPermission() {
        return Permission.SHARDINFO_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

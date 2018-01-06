package stream.flarebot.flarebot.commands.secret;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.database.CassandraController;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class QueryCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        try {
            CassandraController.runUnsafeTask(conn -> {
                ResultSet set = conn.execute(MessageUtils.getMessage(args, 0));
                List<String> header = new ArrayList<>();
                List<List<String>> table = new ArrayList<>();
                int columnsCount = set.getColumnDefinitions().size();
                for (int i = 0; i < columnsCount; i++) {
                    header.add(set.getColumnDefinitions().getName(i));
                }
                for (Row setRow : set) {
                    List<String> row = new ArrayList<>();
                    for (int i = 0; i < columnsCount; i++) {
                        String value = setRow.getObject(i).toString();
                        row.add(value.substring(0, Math.min(30, value.length())));
                    }
                    table.add(row);
                }
                String output = MessageUtils.makeAsciiTable(header, table, null);
                if (output.length() < 2000) {
                    channel.sendMessage(output).queue();
                } else {
                    MessageUtils.sendErrorMessage("The query result set was very large, it has been posted to paste [here](" + MessageUtils
                            .paste(output) + ")", channel, sender);
                }
            });
        } catch (QueryExecutionException | QueryValidationException e) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Failed to execute query");
            eb.addField("Error", "```\n" + e.getMessage() + "\n```", false);
            channel.sendMessage(eb.build()).queue();
        }
    }

    @Override
    public String getCommand() {
        return "query";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return "{%}query <sql>";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }

}

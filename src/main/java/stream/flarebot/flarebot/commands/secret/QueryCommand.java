package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.SQLController;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QueryCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        try {
            SQLController.runSqlTask(conn -> {
                ResultSet set;
                try {
                    set = conn.createStatement().executeQuery(MessageUtils.getMessage(args, 0));
                } catch (SQLException e) {
                    try {
                        conn.createStatement().execute(MessageUtils.getMessage(args, 0));
                        channel.sendMessage(new EmbedBuilder().setDescription("Query was executed successfully!")
                                .setColor(Color.green).build()).queue();
                    } catch (SQLException e1) {
                        channel.sendMessage(new EmbedBuilder()
                                .setDescription("Failed to execute query! " + e.getMessage()).setColor(Color.red)
                                .build()).queue();
                    }
                    return;
                }
                List<String> header = new ArrayList<>();
                List<List<String>> table = new ArrayList<>();
                ResultSetMetaData metaData = set.getMetaData();
                int columnsCount = metaData.getColumnCount();
                for (int i = 0; i < columnsCount; i++) {
                    header.add(metaData.getColumnName(i + 1));
                }
                while (set.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 0; i < columnsCount; i++) {
                        String s = String.valueOf(set.getString(i + 1)).trim();
                        row.add(s.substring(0, Math.min(30, s.length())));
                    }
                    table.add(row);
                }
                String output = MessageUtils.makeAsciiTable(header, table, null);
                if (output.length() < 2000) {
                    channel.sendMessage(output).queue();
                } else {
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("The query result set was very large, it has been posted to hastebin [here](" + MessageUtils
                                    .hastebin(output) + ")")
                            .setColor(Color.red).build()).queue();
                }
            });
        } catch (SQLException e) {
            channel.sendMessage(new EmbedBuilder().setDescription("Could not execute query! " + e.getMessage())
                    .setColor(Color.red).build()).queue();
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
        return CommandType.HIDDEN;
    }

}

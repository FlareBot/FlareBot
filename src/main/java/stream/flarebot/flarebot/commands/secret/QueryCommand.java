package stream.flarebot.flarebot.commands.secret;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.SQLController;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        try {
            SQLController.runSqlTask(conn -> {
                ResultSet set;
                try {
                    set = conn.createStatement().executeQuery(FlareBot.getMessage(args, 0));
                }catch(SQLException e){
                    try {
                        conn.createStatement().execute(FlareBot.getMessage(args, 0));
                        channel.sendMessage(new EmbedBuilder().setDescription("Query was executed successfully!").setColor(Color.green).build()).queue();
                    }catch(SQLException e1) {
                        channel.sendMessage(new EmbedBuilder().setDescription("Failed to execute query! " + e.getMessage()).setColor(Color.red).build()).queue();
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
                String output = makeAsciiTable(header, table, null);
                if (output.length() < 2000) {
                    channel.sendMessage(output).queue();
                } else {
                    channel.sendMessage(new EmbedBuilder().setDescription("The query result set was very large, it has been posted to hastebin [here](" + MessageUtils.hastebin(output) + ")")
                            .setColor(Color.red).build()).queue();
                }
            });
        } catch (SQLException e) {
            channel.sendMessage(new EmbedBuilder().setDescription("Could not execute query! " + e.getMessage()).setColor(Color.red).build()).queue();
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
    public CommandType getType() {
        return CommandType.HIDDEN;
    }

    public static String makeAsciiTable(List<String> headers, List<List<String>> table, List<String> footer) {
        StringBuilder sb = new StringBuilder();
        int padding = 1;
        int[] widths = new int[headers.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = 0;
        }
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).length() > widths[i]) {
                widths[i] = headers.get(i).length();
                if (footer != null) {
                    widths[i] = Math.max(widths[i], footer.get(i).length());
                }
            }
        }
        for (List<String> row : table) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                if (cell.length() > widths[i]) {
                    widths[i] = cell.length();
                }
            }
        }
        sb.append("```").append("\n");
        String formatLine = "|";
        for (int width : widths) {
            formatLine += " %-" + width + "s |";
        }
        formatLine += "\n";
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        sb.append(String.format(formatLine, headers.toArray()));
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        for (List<String> row : table) {
            sb.append(String.format(formatLine, row.toArray()));
        }
        if (footer != null) {
            sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append(String.format(formatLine, footer.toArray()));
        }
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        sb.append("```");
        return sb.toString();
    }

    private static String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
        boolean first = true;
        StringBuilder ret = new StringBuilder();
        for (int size : sizes) {
            if (first) {
                first = false;
                ret.append(left).append(String.join("", Collections.nCopies(size + padding * 2, "-")));
            } else {
                ret.append(middle).append(String.join("", Collections.nCopies(size + padding * 2, "-")));
            }
        }
        return ret.append(right).append("\n").toString();
    }
}

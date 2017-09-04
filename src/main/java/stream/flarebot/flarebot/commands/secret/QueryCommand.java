package stream.flarebot.flarebot.commands.secret;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class QueryCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        CassandraController.runTask(conn -> {
            ResultSet set = conn.execute(MessageUtils.getMessage(args, 0));
            List<String> header = new ArrayList<>();
            List<List<String>> table = new ArrayList<>();
            int columnsCount = set.getColumnDefinitions().size();
            for (int i = 0; i < columnsCount; i++) {
                header.add(set.getColumnDefinitions().getName(i + 1));
            }
            Iterator<Row> setIT = set.iterator();
            while (setIT.hasNext()) {
                Row setRow = setIT.next();
                List<String> row = new ArrayList<>();
                for (int i = 0; i < columnsCount; i++) {
                    String s = String.valueOf(setRow.getString(i + 1)).trim();
                    row.add(s.substring(0, Math.min(30, s.length())));
                }
                table.add(row);
                setIT.remove();
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

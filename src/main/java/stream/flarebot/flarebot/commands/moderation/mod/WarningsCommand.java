package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WarningsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            List<String> header = new ArrayList<>();
            header.add("User");
            header.add("Warnings");
            List<List<String>> body = new ArrayList<>();
            Iterator<Map.Entry<String, List<String>>> it = guild.getWarningsMap().entrySet().iterator();
            while (it.hasNext()) {
                List<String> part = new ArrayList<>();
                Map.Entry<String, List<String>> pair = it.next();
                User user = guild.getGuild().getMemberById(pair.getKey()).getUser();
                part.add(user.getName() + "(" + user.getId() + ")");
                part.add(String.valueOf(pair.getValue().size()));
                body.add(part);
            }
            String table = MessageUtils.makeAsciiTable(header, body, null);
            channel.sendMessage(table).queue();
        } else {
            User user = GeneralUtils.getUser(args[0]);
            if (user == null) {
                MessageUtils.sendErrorMessage("Invalid user!!", channel);
                return;
            }
            StringBuilder sb = new StringBuilder();
            List<String> warnings = guild.getUserWarnings(user);
            int i = 1;
            for (String warning : warnings) {
                sb.append(i + ". " + warning + "\n");
                i++;
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Warnings for " + user.getName());
            eb.addField("Warning count", String.valueOf(warnings.size()), true);
            eb.addField("Warnings", "```md\n" + sb.toString() + "```", false);
            eb.setColor(Color.CYAN);
            channel.sendMessage(eb.build()).queue();
        }
    }

    @Override
    public String getCommand() {
        return "warnings";
    }

    @Override
    public String getDescription() {
        return "Checks the warnings for a guild/user";
    }

    @Override
    public String getUsage() {
        return "`{%}warnings [users]` - check the warnings on a guild or the warnings of a user";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

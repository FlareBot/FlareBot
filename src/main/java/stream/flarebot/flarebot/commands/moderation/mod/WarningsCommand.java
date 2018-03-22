package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class WarningsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("stats")) {
                Map.Entry<String, List<String>> highestEntry = Collections.max(guild.getWarningsMap().entrySet(),
                        Comparator.comparingInt(entry -> entry.getValue().size()));
                User mostWarned = GuildUtils.getUser(highestEntry.getKey(), guild.getGuildId(), true);
                channel.sendMessage(new EmbedBuilder().setTitle("Warning stats", null)
                        .addField("Total Warnings", String.valueOf(
                                guild.getWarningsMap().values().stream().mapToLong(List::size).sum()), true)
                        .addField("Users warned", String.valueOf(guild.getWarningsMap().size()), true)
                        .addField("Most warned user", MessageUtils.getTag(mostWarned)
                                + " - " + highestEntry.getValue().size() + " warnings", true)
                        .setColor(Color.CYAN).build()).queue();
            } else {
                User user = GuildUtils.getUser(args[0]);
                if (user == null) {
                    MessageUtils.sendErrorMessage("That user could not be found!!", channel);
                    return;
                }
                StringBuilder sb = new StringBuilder();
                List<String> tmp = guild.getUserWarnings(user);
                List<String> warnings = tmp.subList(Math.max(tmp.size() - 5, 0), tmp.size());
                int i = 1;
                for (String warning : warnings) {
                    sb.append(i).append(". ").append(warning.substring(0, Math.min(725, warning.length()))).append(warning.length() > 725 ? "..." : "").append("\n");
                    i++;
                }
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("Warnings for " + user.getName())
                        .addField("Warning count", String.valueOf(tmp.size()), true)
                        .addField("Last 5 warnings", "```md\n" + sb.toString().trim() + "\n```", false)
                        .setColor(Color.CYAN);
                channel.sendMessage(eb.build()).queue();
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
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
        return "`{%}warnings [user]` - Check the warnings on a guild or the warnings of a user.\n" +
                "`{%}warnings stats` - Check warning stats for this guild.";
    }

    @Override
    public Permission getPermission() {
        return Permission.WARNINGS_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

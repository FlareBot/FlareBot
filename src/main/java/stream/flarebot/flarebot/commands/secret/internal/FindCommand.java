package stream.flarebot.flarebot.commands.secret.internal;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.commands.InternalCommand;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.ParseUtils;
import stream.flarebot.flarebot.util.pagination.PagedEmbedBuilder;
import stream.flarebot.flarebot.util.pagination.PaginationList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FindCommand implements InternalCommand {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 2) {
            String input = MessageUtils.getMessage(args, 1);

            @Nullable
            User parsedUser = ParseUtils.parseUser(guild.getGuild(), input, true);
            @Nullable
            Guild parsedGuild = null;
            if (parsedUser == null) {
                for (Guild g : Getters.getGuildCache()) {
                    if (g.getName().equalsIgnoreCase(input) || g.getId().equals(input)) {
                        parsedGuild = g;
                        break;
                    }
                }
            }

            if (parsedUser == null && parsedGuild == null) {
                channel.sendMessage("Supply a valid user or guild!").queue();
            }

            if (args[0].equalsIgnoreCase("server") || args[0].equalsIgnoreCase("guild")) {
                if (parsedGuild != null) {
                    channel.sendMessage("Guild: " + parsedGuild.getName()
                            + "\nID: " + parsedGuild.getId()
                            + "\nOwner: " + parsedGuild.getOwner()
                    ).queue();
                } else if (parsedUser != null) {
                    List<Guild> guilds = new ArrayList<>();
                    for (Guild g : parsedUser.getMutualGuilds())
                        if (g.getOwner().getUser().getIdLong() == parsedUser.getIdLong()) guilds.add(g);

                    StringBuilder sb = new StringBuilder();
                    for (Guild g : guilds)
                        sb.append("`").append(MessageUtils.escapeMarkdown(g.getName())).append("` - ").append(g.getId())
                                .append("\n");

                    if (!guilds.isEmpty())
                        channel.sendMessage("Found guilds:\n" + sb.toString()).queue();
                    else
                        channel.sendMessage("Didn't find any guilds").queue();
                } else
                    channel.sendMessage("Couldn't find a server from that input").queue();
            }
        } else
            MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "find";
    }

    @Override
    public String getDescription() {
        return "Find a guild.";
    }

    @Override
    public String getUsage() {
        return "`{%}find server <input>` - Find a server.";
    }
}

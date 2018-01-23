package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.PaginationUtil;

import java.util.List;

public class RolesCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length <= 1) {
            int page = 1;
            if (args.length == 1) {
                try {
                    page = Integer.valueOf(args[0]);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage("Invalid page number: " + args[0] + ".", channel);
                    return;
                }
            }

            List<Role> roles = guild.getGuild().getRoles();

            if (roles.isEmpty()) {
                MessageUtils.sendInfoMessage("There are no roles in this guild!", channel, sender);
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (Role r : roles)
                sb.append(r.getName()).append(" (").append(r.getId()).append(")\n");
            PaginationUtil.sendEmbedPagedMessage(channel, PaginationUtil.splitStringToList(sb.toString(), PaginationUtil.SplitMethod.NEW_LINES, 20), page - 1, true, "Roles");
        } else {
            MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    @Override
    public String getCommand() {
        return "roles";
    }

    @Override
    public String getDescription() {
        return "Get roles on the server";
    }

    @Override
    public String getUsage() {
        return "`{%}roles [page]` - Gets the roles for the current server.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

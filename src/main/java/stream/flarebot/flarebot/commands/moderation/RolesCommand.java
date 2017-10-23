package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class RolesCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length <= 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("**Server Roles**\n```json\n");
            List<Role> roles = new ArrayList<>(channel.getGuild().getRoles());
            roles.remove(channel.getGuild().getRoleById(channel.getGuild().getId()));
            int pageSize = 20;
            int pages =
                    roles.size() < pageSize ? 1 : (roles.size() / pageSize) + (roles.size() % pageSize != 0 ? 1 : 0);
            int start;
            int end;
            int page = 1;
            if (args.length == 1) {
                try {
                    page = Integer.valueOf(args[0]);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage("Invalid page number: " + args[0] + ".", channel);
                    return;
                }
            }
            start = pageSize * (page - 1);
            end = Math.min(start + pageSize, roles.size());
            if (page > pages || page < 0) {
                MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
                return;
            } else {
                List<Role> subRoles = roles.subList(start, end);
                if (roles.isEmpty()) {
                    MessageUtils.sendInfoMessage("There are no roles in this guild!", channel, sender);
                    return;
                } else {
                    for (Role role : subRoles) {
                        if (role.getId().equals(guild.getGuildId())) {
                            continue;
                        }
                        sb.append(role.getName()).append(" (").append(role.getId()).append(")\n");
                    }
                }
            }

            sb.append("```\n").append("**Page ").append(GeneralUtils.getPageOutOfTotal(page, roles, pageSize)).append("**");
            MessageUtils.sendInfoMessage(sb.toString(), channel, sender);
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
        return "`{%}roles [page]` - Gets the roles for the current server";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.pagination.PagedEmbedBuilder;
import stream.flarebot.flarebot.util.pagination.PaginationUtil;

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

            PaginationUtil.sendEmbedPagedMessage(new PagedEmbedBuilder<>(PaginationUtil.splitStringToList(sb.toString(),
                    PaginationUtil.SplitMethod.NEW_LINES, 20))
                    .setTitle("Roles")
                    .setCodeBlock("js")
                    .build(), page, channel, sender);
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
    public Permission getPermission() {
        return Permission.ROLES_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

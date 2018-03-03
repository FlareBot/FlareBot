package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class AutoAssignCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                listRoles(guild, 1, channel, sender);
            } else if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                        .setDescription(sender
                                .getAsMention() + " Usage: " + FlareBot
                                .getPrefixes().get(channel.getGuild()
                                        .getId()) + "autoassign " + args[0] + " <role>"), channel);
            } else {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                        .getAsMention() + " Invalid argument!"), channel);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            int page;
            try {
                page = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage("Invalid page number: " + args[1] + ".", channel);
                return;
            }
            listRoles(guild, page, channel, sender);
        } else {
            StringBuilder passedRole = new StringBuilder();
            for (int i = 1; i < args.length; i++)
                passedRole.append(args[i]).append(' ');
            passedRole = new StringBuilder(passedRole.toString().trim());
            if (args[0].equalsIgnoreCase("add")) {
                Role role = getRole(channel.getGuild(), passedRole.toString());
                if (role == null) {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                            .getAsMention() + " That is not a valid role!"), channel);
                    return;
                }
                if (!guild.getAutoAssignRoles().contains(role.getId())) {
                    guild.getAutoAssignRoles().add(role.getId());
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("Added " + role
                                    .getName() + " to your auto assigned roles!").build())
                            .queue();
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(role
                            .getName() + " is already being assigned!"), channel);
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                Role role = getRole(channel.getGuild(), passedRole.toString());
                if (role == null) {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                            .getAsMention() + " That is not a valid role!"), channel);
                    return;
                }
                if (!guild.getAutoAssignRoles().isEmpty()) {
                    if (guild.getAutoAssignRoles().contains(role.getId())) {
                        guild.getAutoAssignRoles().remove(role.getId());
                        channel.sendMessage(MessageUtils.getEmbed(sender)
                                .setDescription("Removed " + role.getName() + " from your auto assigned roles")
                                .build()).queue();
                    } else {
                        MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                                .setDescription("That role is not being auto assigned!"), channel);
                    }
                } else {
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                            .setDescription("This server has no roles being assigned."), channel);
                }
            } else {
                MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(sender
                        .getAsMention() + " Invalid argument!"), channel);
            }
        }
    }

    @Override
    public String getCommand() {
        return "autoassign";
    }

    @Override
    public String getDescription() {
        return "Auto assign roles to users when they join.";
    }

    @Override
    public String getUsage() {
        return "`{%}autoassign add|remove <role>` - Add or Remove roles from AutoAssign.\n"
                + "`{%}autoassign list` - List roles that are currently AutoAssigned.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"autorole"};
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

    private Role getRole(Guild guild, String role) {
        if (!role.contains(" ") && role.startsWith("<@&") && role.endsWith(">")) {
            return guild.getRoleById(role.replaceAll("[^0-9]+", ""));
        }
        
        // Since I know it is valid I can get the id from the name or just return the id since that will be the only other thing passed.
        for (Role iRole : guild.getRoles()) {
            if (iRole.getId().equals(role) || iRole.getName().equalsIgnoreCase(role))
                return iRole;
        }
        return null;
    }

    private void listRoles(GuildWrapper wrapper, int page, TextChannel channel, User sender) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Autoassign List**\n```json\n");
        List<String> roles = new ArrayList<>(wrapper.getAutoAssignRoles());
        int pageSize = 20;
        int pages = roles.size() < pageSize ? 1 : (roles.size() / pageSize) + (roles.size() % pageSize != 0 ? 1 : 0);
        int start;
        int end;
        start = pageSize * (page - 1);
        end = Math.min(start + pageSize, roles.size());
        if (page > pages || page < 0) {
            MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
            return;
        } else {
            List<String> subRoles = roles.subList(start, end);
            if (roles.isEmpty()) {
                MessageUtils.sendInfoMessage("There are no role being autoassigned in this guild!", channel, sender);
                return;
            } else {
                for (String role : subRoles) {
                    if (wrapper.getGuild().getRoleById(role) == null) {
                        wrapper.getAutoAssignRoles().remove(role);
                        continue;
                    }
                    sb.append(wrapper.getGuild().getRoleById(role).getName()).append(" (").append(role).append(")\n");
                }
            }
        }

        sb.append("```\n").append("**Page ").append(GeneralUtils.getPageOutOfTotal(page, roles, pageSize)).append("**");
        MessageUtils.sendInfoMessage(sb.toString(), channel, sender);
    }
}

package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.apache.commons.lang3.StringUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.Iterator;
import java.util.stream.Collectors;

public class SelfAssignCommand implements Command {

    private final int LEVENSHTEIN_DISTANCE = 8;

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("add")) {
                if (guild.getPermissions().hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendUsage(this, channel, sender);
                } else {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.sendErrorMessage("You need `flarebot.selfassign.admin` in order to do this!", channel, sender), 5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (guild.getPermissions().hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendUsage(this, channel, sender);
                } else {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.sendErrorMessage("You need `flarebot.selfassign.admin` in order to do this!", channel, sender),
                            5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                if (guild.getSelfAssignRoles().isEmpty()) {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.sendErrorMessage("There are no self-assignable roles!", channel, sender), 5000, channel);
                    return;
                }
                StringBuilder base = new StringBuilder("**Self assignable roles**\n```\n");
                Iterator<String> iter = guild.getSelfAssignRoles().iterator();
                while (iter.hasNext()) {
                    String roleId = iter.next();
                    if (roleId.isEmpty()) {
                        iter.remove();
                        continue;
                    }
                    if (channel.getGuild().getRoleById(roleId) != null)
                        base.append(channel.getGuild().getRoleById(roleId).getName()).append(" (").append(roleId).append(")\n");
                    else iter.remove();
                }
                base.append("```");
                channel.sendMessage(base.toString()).queue();
            } else {
                long roleId;
                try {
                    roleId = Long.parseLong(args[0]);
                } catch (NumberFormatException e) {
                    if (handleRoleName(args[0], guild, channel) == null) return;
                    roleId = channel.getGuild().getRolesByName(args[0], true).get(0).getIdLong();
                }

                if (guild.getSelfAssignRoles().contains(String.valueOf(roleId))) {
                    handleRole(member, channel, roleId);
                } else {
                    MessageUtils.sendErrorMessage("You cannot self-assign that role! Do `" + getPrefix(channel
                                    .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                            channel);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (!guild.getPermissions()
                        .hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.sendErrorMessage("You need `flarebot.selfassign.admin` in order to do this!", channel, sender),
                            5000, channel);
                    return;
                }

                long roleId;
                try {
                    roleId = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage("Make sure to use the role ID!", channel);
                    return;
                }
                if (channel.getGuild().getRoleById(roleId) != null) {
                    guild.getSelfAssignRoles().add(String.valueOf(roleId));
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("Added `" + channel.getGuild().getRoleById(roleId)
                                    .getName() + "` to the self-assign list!").build())
                            .queue();
                } else
                    MessageUtils.sendErrorMessage("That role does not exist!", channel);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!guild.getPermissions()
                        .hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.sendErrorMessage("You need `flarebot.selfassign.admin` in order to do this!", channel, sender),
                            5000, channel);
                    return;
                }
                String roleId = args[1];
                if (channel.getGuild().getRoleById(roleId) != null) {
                    guild.getSelfAssignRoles().remove(roleId);
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("Removed `" + channel.getGuild().getRoleById(roleId)
                                    .getName() + "` from the self-assign list!").build())
                            .queue();
                } else
                    MessageUtils.sendErrorMessage("That role does not exist!", channel);
            } else {
                if (handleRoleName(MessageUtils.getMessage(args, 0), guild, channel) == null) return;
                long roleId = channel.getGuild().getRolesByName(MessageUtils.getMessage(args, 0), true).get(0).getIdLong();

                // TODO: Move these to Long
                if (guild.getSelfAssignRoles().contains(String.valueOf(roleId))) {
                    handleRole(member, channel, roleId);
                } else {
                    MessageUtils.sendErrorMessage("You cannot self-assign that role! Do `" + getPrefix(channel
                                    .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                            channel);
                }
            }
        } else {
            if (handleRoleName(MessageUtils.getMessage(args, 0), guild, channel) == null) return;
            long roleId = channel.getGuild().getRolesByName(MessageUtils.getMessage(args, 0), true).get(0).getIdLong();

            // TODO: Move these to Long
            if (guild.getSelfAssignRoles().contains(String.valueOf(roleId))) {
                handleRole(member, channel, roleId);
            } else {
                MessageUtils.sendErrorMessage("You cannot self-assign that role! Do `" + getPrefix(channel
                                .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                        channel);
            }
        }
    }

    private void handleRole(Member member, TextChannel channel, long roleId) {
        try {
            if (!member.getRoles().contains(channel.getGuild().getRoleById(roleId))) {
                channel.getGuild().getController().addRolesToMember(member, channel.getGuild().getRoleById(roleId)).queue();
                MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append(member.getAsMention())
                        .setEmbed(new EmbedBuilder().setDescription("You have been assigned `" + channel.getGuild()
                        .getRoleById(roleId).getName() + "` to yourself!").setColor(Color.green).build()).build(), 30_000, channel);
            } else {
                channel.getGuild().getController().removeRolesFromMember(member, channel.getGuild().getRoleById(roleId)).queue();
                MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append(member.getAsMention())
                        .setEmbed(new EmbedBuilder().setDescription("You have removed the role `" + channel.getGuild()
                        .getRoleById(roleId).getName() + "` from yourself!").setColor(Color.orange).build()).build(), 30_000, channel);
            }
        } catch (PermissionException e) {
            MessageUtils.sendErrorMessage(e.getMessage() + "\nContact a server administrator!", channel);
        }
    }

    private Role handleRoleName(String message, GuildWrapper guild, TextChannel channel) {
        if (guild.getGuild().getRolesByName(message, true).isEmpty()) {
            String closest = null;
            int distance = LEVENSHTEIN_DISTANCE;
            for (Role role : guild.getGuild().getRoles().stream().filter(role -> guild.getSelfAssignRoles()
                    .contains(role.getId())).collect(Collectors.toList())) {
                int currentDistance = StringUtils.getLevenshteinDistance(role.getName(), message);
                if (currentDistance < distance) {
                    distance = currentDistance;
                    closest = role.getName();
                }
            }
            MessageUtils.sendErrorMessage("That role does not exist! "
                    + (closest != null ? "Maybe you mean `" + closest + "`" : ""), channel);
            return null;
        }else
            return guild.getGuild().getRolesByName(message, true).get(0);
    }

    @Override
    public String getCommand() {
        return "selfassign";
    }

    @Override
    public String getDescription() {
        return "Self assign a role to yourself!\nTo add roles to selfassign do `selfassign add (userId)`";
    }

    @Override
    public String getUsage() {
        return "`{%}selfassign <roleID/name>` - Adds a role to yourself\n"
                + "`{%}selfassign <add/remove> <roleID/name>` - Allows admins to add roles to the self assign list\n"
                + "`{%}selfassign list` - Lists roles that are self-assignable";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

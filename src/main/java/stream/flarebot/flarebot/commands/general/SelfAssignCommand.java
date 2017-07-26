package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.Iterator;

public class SelfAssignCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel, sender).queue();
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("add")) {
                if (guild.getPermissions().hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.getUsage(this, channel, sender).queue();
                } else {
                    MessageUtils.sendAutoDeletedMessage(new EmbedBuilder()
                                    .setDescription("You need `flarebot.selfassign.admin` in order to do this!")
                                    .setColor(Color.red).build(),
                            5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (guild.getPermissions().hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.getUsage(this, channel, sender).queue();
                } else {
                    MessageUtils.sendAutoDeletedMessage(new EmbedBuilder()
                                    .setDescription("You need `flarebot.selfassign.admin` in order to do this!")
                                    .setColor(Color.red).build(),
                            5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                if (guild.getSelfAssignRoles().isEmpty()) {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.RED).setDescription("There are no self-assignable roles!").build(), 5000, channel);
                    return;
                }
                String base = "**Self assignable roles**\n```\n";
                Iterator<String> iter = guild.getSelfAssignRoles().iterator();
                while (iter.hasNext()) {
                    String roleId = iter.next();
                    if (roleId.isEmpty()) {
                        iter.remove();
                        continue;
                    }
                    if (channel.getGuild().getRoleById(roleId) != null)
                        base += channel.getGuild().getRoleById(roleId).getName() + " (" + roleId + ")\n";
                    else iter.remove();
                }
                base += "```";
                channel.sendMessage(base).queue();
            } else {
                String roleId;
                try {
                    Long.parseLong(args[0]);
                    roleId = args[0];
                } catch (NumberFormatException e) {
                    if (channel.getGuild().getRolesByName(args[0], true).isEmpty()) {
                        MessageUtils.sendErrorMessage("That role does not exist!", channel);
                        return;
                    } else
                        roleId = channel.getGuild().getRolesByName(args[0], true).get(0).getId();
                }

                if (guild.getSelfAssignRoles().contains(roleId)) {
                    handleRole(member, channel, roleId);
                } else {
                    MessageUtils.sendErrorMessage("You cannot auto-assign that role! Do `" + getPrefix(channel
                                    .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                            channel);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (!guild.getPermissions()
                        .hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendAutoDeletedMessage(new EmbedBuilder()
                                    .setDescription("You need `flarebot.selfassign.admin` in order to do this!")
                                    .setColor(Color.red).build(),
                            5000, channel);
                    return;
                }

                String roleId = args[1];
                try {
                    Long.parseLong(roleId);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage("Make sure to use the role ID!", channel);
                    return;
                }
                if (channel.getGuild().getRoleById(roleId) != null) {
                    guild.getSelfAssignRoles().add(roleId);
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("Added `" + channel.getGuild().getRoleById(roleId)
                                    .getName() + "` to the self-assign list!").build())
                            .queue();
                } else
                    MessageUtils.sendErrorMessage("That role does not exist!", channel);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!guild.getPermissions()
                        .hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendAutoDeletedMessage(new EmbedBuilder()
                                    .setDescription("You need `flarebot.selfassign.admin` in order to do this!")
                                    .setColor(Color.red).build(),
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
                String roleId;
                if (channel.getGuild().getRolesByName(MessageUtils.getMessage(args, 0), true).isEmpty()) {
                    MessageUtils.sendErrorMessage("That role does not exist!", channel);
                    return;
                } else
                    roleId = channel.getGuild().getRolesByName(MessageUtils.getMessage(args, 0), true).get(0).getId();

                if (guild.getSelfAssignRoles().contains(roleId)) {
                    handleRole(member, channel, roleId);
                } else {
                    MessageUtils.sendErrorMessage("You cannot auto-assign that role! Do `" + getPrefix(channel
                                    .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                            channel);
                }
            }
        } else {
            String roleId;
            if (channel.getGuild().getRolesByName(MessageUtils.getMessage(args, 0), true).isEmpty()) {
                MessageUtils.sendErrorMessage("That role does not exist!", channel);
                return;
            } else
                roleId = channel.getGuild().getRolesByName(MessageUtils.getMessage(args, 0), true).get(0).getId();

            if (guild.getSelfAssignRoles().contains(roleId)) {
                handleRole(member, channel, roleId);
            } else {
                MessageUtils.sendErrorMessage("You cannot auto-assign that role! Do `" + getPrefix(channel
                                .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                        channel);
            }
        }
    }

    private void handleRole(Member member, TextChannel channel, String roleId) {
        try {
            if (!member.getRoles().contains(channel.getGuild().getRoleById(roleId))) {
                channel.getGuild().getController().addRolesToMember(member, channel.getGuild().getRoleById(roleId)).queue();
                MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append(member.getAsMention()).setEmbed(new EmbedBuilder().setDescription("You have been assigned `" + channel.getGuild()
                        .getRoleById(roleId).getName() + "` to yourself!").setColor(Color.green).build()).build(), 30_000, channel);
            } else {
                channel.getGuild().getController().removeRolesFromMember(member, channel.getGuild().getRoleById(roleId)).queue();
                MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append(member.getAsMention()).setEmbed(new EmbedBuilder().setDescription("You have removed the role `" + channel.getGuild()
                        .getRoleById(roleId).getName() + "` from yourself!").setColor(Color.orange).build()).build(), 30_000, channel);
            }
        } catch (PermissionException e) {
            MessageUtils.sendErrorMessage(e.getMessage() + "\nContact a server administrator!", channel);
        }
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

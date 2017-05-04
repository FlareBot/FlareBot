package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.FlareBotManager;

import java.awt.*;

public class SelfAssignCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendErrorMessage("Usage: `" + getPrefix(channel
                    .getGuild()) + "selfassign (roleId/name)`\n\nIf you don't know how to find the role ID, do "
                    + getPrefix(channel.getGuild()) + "roles", channel);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("add")) {
                if (FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendErrorMessage("Usage: `" + getPrefix(channel
                                    .getGuild()) + "selfassign add (roleId/name)`\n\nIf you don't know how to find the role ID, do _roles",
                            channel);
                } else {
                    MessageUtils.sendAutoDeletedMessage(new EmbedBuilder()
                                    .setDescription("You need `flarebot.selfassign.admin` in order to do this!")
                                    .setColor(Color.red).build(),
                            5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendErrorMessage("Usage: `" + getPrefix(channel
                                    .getGuild()) + "selfassign remove (roleId/name)`\n\nIf you don't know how to find the role ID, do _roles",
                            channel);
                } else {
                    MessageUtils.sendAutoDeletedMessage(new EmbedBuilder()
                                    .setDescription("You need `flarebot.selfassign.admin` in order to do this!")
                                    .setColor(Color.red).build(),
                            5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                StringBuilder sb = new StringBuilder();
                sb.append("**Self assignable roles**").append("\n```\n");
                FlareBotManager.getInstance().getSelfAssignRoles(channel.getGuild().getId())
                               .forEach(role -> sb.append(channel.getGuild().getRoleById(role).getName()).append(" (")
                                                  .append(role).append(")\n"));
                sb.append("```");
                channel.sendMessage(sb.toString()).queue();
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

                if (FlareBotManager.getInstance().getSelfAssignRoles(channel.getGuild().getId()).contains(roleId)) {
                    handleRole(member, channel, roleId);
                } else {
                    MessageUtils.sendErrorMessage("You cannot auto-assign that role! Do `" + getPrefix(channel
                                    .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                            channel);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                if (!FlareBot.getInstance().getPermissions(channel)
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
                    FlareBotManager.getInstance().getSelfAssignRoles(channel.getGuild().getId()).add(roleId);
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("Added `" + channel.getGuild().getRoleById(roleId)
                                                               .getName() + "` to the self-assign list!").build())
                           .queue();
                } else
                    MessageUtils.sendErrorMessage("That role does not exist!", channel);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!FlareBot.getInstance().getPermissions(channel)
                             .hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendAutoDeletedMessage(new EmbedBuilder()
                                    .setDescription("You need `flarebot.selfassign.admin` in order to do this!")
                                    .setColor(Color.red).build(),
                            5000, channel);
                    return;
                }
                String roleId = args[1];
                if (channel.getGuild().getRoleById(roleId) != null) {
                    FlareBotManager.getInstance().getSelfAssignRoles(channel.getGuild().getId()).remove(roleId);
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("Removed `" + channel.getGuild().getRoleById(roleId)
                                                                 .getName() + "` from the self-assign list!").build())
                           .queue();
                } else
                    MessageUtils.sendErrorMessage("That role does not exist!", channel);
            } else {
                String roleId;
                if (channel.getGuild().getRolesByName(FlareBot.getMessage(args, 0), true).isEmpty()) {
                    MessageUtils.sendErrorMessage("That role does not exist!", channel);
                    return;
                } else
                    roleId = channel.getGuild().getRolesByName(FlareBot.getMessage(args, 0), true).get(0).getId();

                if (FlareBotManager.getInstance().getSelfAssignRoles(channel.getGuild().getId()).contains(roleId)) {
                    handleRole(member, channel, roleId);
                } else {
                    MessageUtils.sendErrorMessage("You cannot auto-assign that role! Do `" + getPrefix(channel
                                    .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                            channel);
                }
            }
        } else {
            String roleId;
            if (channel.getGuild().getRolesByName(FlareBot.getMessage(args, 0), true).isEmpty()) {
                MessageUtils.sendErrorMessage("That role does not exist!", channel);
                return;
            } else
                roleId = channel.getGuild().getRolesByName(FlareBot.getMessage(args, 0), true).get(0).getId();

            if (FlareBotManager.getInstance().getSelfAssignRoles(channel.getGuild().getId()).contains(roleId)) {
                handleRole(member, channel, roleId);
            } else {
                MessageUtils.sendErrorMessage("You cannot auto-assign that role! Do `" + getPrefix(channel
                                .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                        channel);
            }
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
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    private void handleRole(Member member, TextChannel channel, String roleId) {
        if (!member.getRoles().contains(channel.getGuild().getRoleById(roleId))) {
            channel.getGuild().getController().addRolesToMember(member, channel.getGuild().getRoleById(roleId)).queue();
            MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append(member.getAsMention())
                                                                    .setEmbed(new EmbedBuilder()
                                                                            .setDescription("You have been assigned `" + channel
                                                                                    .getGuild()
                                                                                    .getRoleById(roleId)
                                                                                    .getName() + "` to yourself!")
                                                                            .setColor(Color.green).build())
                                                                    .build(), 30_000, channel);
        } else {
            channel.getGuild().getController().removeRolesFromMember(member, channel.getGuild().getRoleById(roleId))
                   .queue();
            MessageUtils.sendAutoDeletedMessage(new MessageBuilder().append(member.getAsMention())
                                                                    .setEmbed(new EmbedBuilder()
                                                                            .setDescription("You have removed the role `" + channel
                                                                                    .getGuild()
                                                                                    .getRoleById(roleId)
                                                                                    .getName() + "` from yourself!")
                                                                            .setColor(Color.orange).build())
                                                                    .build(), 30_000, channel);
        }
    }
}

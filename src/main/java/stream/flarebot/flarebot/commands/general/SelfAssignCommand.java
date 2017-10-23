package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.Iterator;

public class SelfAssignCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("add")) {
                if (guild.getPermissions().hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendUsage(this, channel, sender, args);
                } else {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.sendErrorMessage("You need `flarebot.selfassign.admin` in order to do this!", channel, sender), 5000, channel);
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (guild.getPermissions().hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendUsage(this, channel, sender, args);
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
                Role role = GeneralUtils.getRole(args[0], guild.getGuildId(), channel);
                if(role == null) return;

                if (guild.getSelfAssignRoles().contains(role.getId())) {
                    handleRole(member, channel, role.getIdLong());
                } else {
                    MessageUtils.sendErrorMessage("You cannot self-assign that role! Do `" + getPrefix(channel
                                    .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                            channel);
                }
            }
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                if (!guild.getPermissions()
                        .hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.sendErrorMessage("You need `flarebot.selfassign.admin` in order to do this!", channel, sender),
                            5000, channel);
                    return;
                }

                Role role = GeneralUtils.getRole(MessageUtils.getMessage(args, 1), guild.getGuildId(), channel);
                if (role != null) {
                    guild.getSelfAssignRoles().add(role.getId());
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("Added `" + role.getName() + "` to the self-assign list!").build())
                            .queue();
                }
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!guild.getPermissions()
                        .hasPermission(member, "flarebot.selfassign.admin")) {
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.sendErrorMessage("You need `flarebot.selfassign.admin` in order to do this!", channel, sender),
                            5000, channel);
                    return;
                }
                Role role = GeneralUtils.getRole(MessageUtils.getMessage(args, 1), guild.getGuildId(), channel);
                if (role != null) {
                    guild.getSelfAssignRoles().remove(role.getId());
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("Removed `" + role.getName() + "` from the self-assign list!").build())
                            .queue();
                }
            } else {
                Role role = GeneralUtils.getRole(MessageUtils.getMessage(args, 0), guild.getGuildId(), channel);
                if(role == null) return;
                // TODO: Move these to Long
                if (guild.getSelfAssignRoles().contains(role.getId())) {
                    handleRole(member, channel, role.getIdLong());
                } else {
                    MessageUtils.sendErrorMessage("You cannot self-assign that role! Do `" + getPrefix(channel
                                    .getGuild()) + "selfassign list` to see what you can assign to yourself!",
                            channel);
                }
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
                + "`{%}selfassign add|remove <roleID/name>` - Allows admins to add roles to the self assign list\n"
                + "`{%}selfassign list` - Lists roles that are self-assignable";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

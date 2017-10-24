package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Group;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class PermissionsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("group")) {
                String groupString = args[1];
                Group group = getPermissions(channel).getGroup(groupString);
                if (group == null) {
                    MessageUtils.sendErrorMessage("That group doesn't exist! You can create it with `" + getPrefix(channel.getGuild()) + "permissions group " + groupString + " create`", channel);
                    return;
                }
                if (args[2].equals("add")) {
                    if (args.length == 4) {
                        if (!GeneralUtils.validPerm(args[3])) {
                            MessageUtils.sendErrorMessage("That is an invalid permission! Permissions start with `flarebot.` followed with a command name!\n" +
                                    "**Example:** `flarebot.play`", channel);
                            return;
                        }
                        if (group.addPermission(args[3])) {
                            MessageUtils.sendSuccessMessage("Successfully added the permission `" + args[3] + "` to the group `" + groupString + "`", channel, sender);
                            return;
                        } else {
                            MessageUtils.sendErrorMessage("Couldn't add the permission (it probably already exists)", channel);
                            return;
                        }

                    }
                } else if (args[2].equals("remove")) {
                    if (args.length == 4) {
                        if (group.removePermission(args[3])) {
                            MessageUtils.sendSuccessMessage("Successfully removed the permission `" + args[3] + "` from the group `" + groupString + "`", channel, sender);
                            return;
                        } else {
                            MessageUtils.sendErrorMessage("Couldn't remove the permission (it probably didn't exist)", channel);
                            return;
                        }
                    }
                } else if (args[2].equals("create")) {
                    if (args.length == 3) {
                        if (getPermissions(channel).addGroup(groupString)) {
                            MessageUtils.sendSuccessMessage("Successfully created group: `" + groupString + "`", channel, sender);
                            return;
                        } else {
                            MessageUtils.sendErrorMessage("That group already exists!!", channel);
                            return;
                        }
                    }
                } else if (args[2].equals("delete")) {
                    if (args.length == 3) {
                        getPermissions(channel).deleteGroup(groupString);
                        return;
                    }
                } else if (args[2].equals("link")) {
                    if (args.length == 4) {
                        Role role = GeneralUtils.getRole(args[3], guild.getGuildId());
                        if (role != null) {
                            group.linkRole(role.getId());
                            MessageUtils.sendSuccessMessage("Successfully linked the group `" + groupString + "` to the role `" + role.getName() + "`", channel, sender);
                            return;
                        } else {
                            MessageUtils.sendErrorMessage("That role doesn't exist!", channel);
                            return;
                        }
                    }
                } else if (args[2].equals("unlink")) {
                    if (args.length == 3) {
                        Role role = guild.getGuild().getRoleById(group.getRoleId());
                        if (role == null) {
                            MessageUtils.sendErrorMessage("Cannot unlink if a role isn't linked!!", channel);
                            return;
                        } else {
                            group.linkRole(null);
                            MessageUtils.sendSuccessMessage("Successfully unlinked the role " + role.getName() + " from the group " + group.getName(), channel, sender);
                            return;
                        }
                    }
                } else if (args[2].equals("list")) {
                    if (args.length == 3 || args.length == 4) {
                        int page = args.length == 4 ? Integer.valueOf(args[3]) : 1;
                        Set<String> perms = group.getPermissions();
                        List<String> permList = GeneralUtils.orderList(perms);

                        String list = getStringList(permList, page);
                        EmbedBuilder eb = MessageUtils.getEmbed(sender);
                        eb.addField("Perms", list, false);
                        eb.addField("Current page", String.valueOf(page), true);
                        int pageSize = 20;
                        int pages =
                                perms.size() < pageSize ? 1 : (perms.size() / pageSize) + (perms.size() % pageSize != 0 ? 1 : 0);
                        eb.addField("Pages", String.valueOf(pages), true);
                        eb.setColor(Color.CYAN);
                        channel.sendMessage(eb.build()).queue();
                        return;

                    }
                } else if (args[2].equals("massadd")) {
                    if (args.length == 4) {
                        List<Member> roleMembers;
                        String roleName = "";
                        if (args[3].equals("@everyone")) {
                            roleMembers = guild.getGuild().getMembers();
                            roleName = "everyone";
                        } else if (args[3].equals("@here")) {
                            roleMembers = channel.getMembers();
                            roleName = "here";
                        } else {
                            Role role = GeneralUtils.getRole(args[3], guild.getGuildId());
                            if (role != null) {
                                roleMembers = guild.getGuild().getMembersWithRoles(role);
                            } else {
                                MessageUtils.sendErrorMessage("That role doesn't exist!!", channel);
                                return;
                            }
                        }
                        for (Member user : roleMembers) {
                            getPermissions(channel).getUser(user).addGroup(group);
                        }
                        MessageUtils.sendSuccessMessage("Successfully added the group `" + groupString + "` to everyone in the role @" + roleName, channel, sender);
                        return;

                    }
                }
            } else if (args[0].equalsIgnoreCase("user")) {
                String userString = args[1];
                User user = GeneralUtils.getUser(userString, guild.getGuildId());
                if (user == null) {
                    MessageUtils.sendErrorMessage("That user doesn't exist!!", channel);
                    return;
                }
                stream.flarebot.flarebot.permissions.User permUser =
                        getPermissions(channel).getUser(guild.getGuild().getMember(user));
                if (args[2].equals("group")) {
                    if (args.length >= 4) {
                        if (args[3].equals("add")) {
                            if (args.length == 5) {
                                String groupString = args[4];
                                Group group = getPermissions(channel).getGroup(groupString);
                                if (group == null) {
                                    MessageUtils.sendErrorMessage("That group doesn't exists!! You can create it with `" + getPrefix(channel.getGuild()) + "permissions group " + groupString + " create`", channel);
                                    return;
                                }
                                permUser.addGroup(group);
                                MessageUtils.sendSuccessMessage("Successfully added the group `" + groupString + "` to " + user.getAsMention(), channel, sender);
                                return;
                            }
                        } else if (args[3].equals("remove")) {
                            if (args.length == 5) {
                                String groupString = args[4];
                                Group group = getPermissions(channel).getGroup(groupString);
                                if (group == null) {
                                    MessageUtils.sendErrorMessage("That group doesn't exists!!", channel);
                                    return;
                                }
                                if (permUser.removeGroup(group)) {
                                    MessageUtils.sendSuccessMessage("Successfully removed the group `" + groupString + "` from " + user.getAsMention(), channel, sender);
                                    return;
                                } else {
                                    MessageUtils.sendErrorMessage("The user doesn't have that group!!", channel);
                                    return;
                                }
                            }
                        } else if (args[3].equals("list")) {
                            int page = args.length == 5 ? Integer.valueOf(args[4]) : 1;
                            Set<String> groups = permUser.getGroups();
                            List<String> groupList = GeneralUtils.orderList(groups);

                            String list = getStringList(groupList, page);
                            EmbedBuilder eb = MessageUtils.getEmbed(sender);
                            eb.addField("Groups for " + MessageUtils.getTag(user), list, false);
                            eb.addField("Current page", String.valueOf(page), true);
                            int pageSize = 20;
                            int pages =
                                    groups.size() < pageSize ? 1 : (groups.size() / pageSize) + (groups.size() % pageSize != 0 ? 1 : 0);
                            eb.addField("Pages", String.valueOf(pages), true);
                            eb.setColor(Color.CYAN);
                            channel.sendMessage(eb.build()).queue();
                            return;
                        }
                    }
                } else if (args[2].equals("permission")) {
                    if (args.length >= 4) {
                        if (args[3].equals("add")) {
                            if (args.length == 5) {
                                if (!GeneralUtils.validPerm(args[4])) {
                                    MessageUtils.sendErrorMessage("That is an invalid permission! Permissions start with `flarebot.` followed with a command name!\n" +
                                            "**Example:** `flarebot.play`", channel);
                                    return;
                                }
                                if (permUser.addPermission(args[4])) {
                                    MessageUtils.sendSuccessMessage("Successfully added the permission `" + args[4] + "` to " + user.getAsMention(), channel, sender);
                                    return;
                                } else {
                                    MessageUtils.sendErrorMessage("The user doesn't have that permission!!", channel);
                                    return;
                                }
                            }
                        } else if (args[3].equals("remove")) {
                            if (args.length == 5) {
                                if (permUser.removePermission(args[4])) {
                                    MessageUtils.sendSuccessMessage("Successfully removed the permission `" + args[4] + "` from " + user.getAsMention(), channel, sender);
                                    return;
                                } else {
                                    MessageUtils.sendErrorMessage("The user already has that permission!!", channel);
                                    return;
                                }
                            }
                        } else if (args[3].equals("list")) {
                            int page = args.length == 5 ? Integer.valueOf(args[4]) : 1;
                            Set<String> perms = permUser.getPermissions();
                            List<String> permList = GeneralUtils.orderList(perms);

                            String list = getStringList(permList, page);
                            EmbedBuilder eb = MessageUtils.getEmbed(sender);
                            eb.addField("Perms", list, false);
                            eb.addField("Current page", String.valueOf(page), true);
                            int pageSize = 20;
                            int pages =
                                    perms.size() < pageSize ? 1 : (perms.size() / pageSize) + (perms.size() % pageSize != 0 ? 1 : 0);
                            eb.addField("Pages", String.valueOf(pages), true);
                            eb.setColor(Color.CYAN);
                            channel.sendMessage(eb.build()).queue();
                            return;
                        }
                    }
                }
            }
        } else if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("groups")) {
                if (this.getPermissions(channel).getListGroups().isEmpty()) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setColor(Color.RED)
                            .setDescription("There are no groups for this guild!")
                            .build()).queue();
                    return;
                } else {
                    int page = args.length == 2 ? Integer.valueOf(args[1]) : 1;
                    Set<String> groups = this.getPermissions(channel).getGroups().keySet();
                    List<String> groupList = GeneralUtils.orderList(groups);

                    String list = getStringList(groupList, page);
                    EmbedBuilder eb = MessageUtils.getEmbed(sender);
                    eb.addField("Groups", list, false);
                    eb.addField("Current page", String.valueOf(page), true);
                    int pageSize = 20;
                    int pages =
                            groups.size() < pageSize ? 1 : (groups.size() / pageSize) + (groups.size() % pageSize != 0 ? 1 : 0);
                    eb.addField("Pages", String.valueOf(pages), true);
                    eb.setColor(Color.CYAN);
                    channel.sendMessage(eb.build()).queue();
                    return;
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                guild.setPermissions(new PerGuildPermissions());
                MessageUtils.sendSuccessMessage("Successfully reset perms", channel, sender);
                return;
            } else if (args[0].equalsIgnoreCase("restoredefault")) {
                guild.getPermissions().createDefaultGroup();
                MessageUtils.sendSuccessMessage("Successfully restored the Default group", channel, sender);
                return;
            }
        }
        MessageUtils.sendUsage(this, channel, sender, args);
    }

    private String getStringList(Collection<String> perms, int page) {
        int pageSize = 20;
        int pages = perms.size() < pageSize ? 1 : (perms.size() / pageSize) + (perms.size() % pageSize != 0 ? 1 : 0);
        int start;
        int end;

        start = pageSize * (page - 1);
        end = Math.min(start + pageSize, perms.size());
        if (page > pages || page < 0) {
            return null;
        }
        String[] permsList = new String[perms.size()];
        permsList = perms.toArray(permsList);
        permsList = Arrays.copyOfRange(permsList, start, end);
        StringBuilder sb = new StringBuilder();
        sb.append("```\n");
        for (String perm : permsList) {
            sb.append(perm).append("\n");
        }
        sb.append("```");
        return sb.toString();
    }

    @Override
    public String getCommand() {
        return "permissions";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"perm", "perms"};
    }

    @Override
    public String getDescription() {
        return "Manages server-wide permissions for FlareBot.";
    }


    //TODO: Pagination
    @Override
    public String getUsage() {
        return "`{%}permissions group <group> add|remove <perm>` - Adds or removes a permission to a group\n" +
                "`{%}permissions group <group> create|delete` - Creates or deletes a group\n" +
                "`{%}permissions group <group> link <role>` - Links the group to a discord role\n" +
                "`{%}permissions group <group> unlink` - Unlinks the group from a role\n" +
                "`{%}permissions group <group> list [page]` - lists the permissions this group has\n" +
                "`{%}permissions group <group> massadd <@everyone/@here/role>` - Puts everyone with the giving role into the group\n\n" +
                "`{%}permissions user <user> group add|remove <group>` - Adds or removes a group from this user\n" +
                "`{%}permissions user <user> group list [page]` - Lists the groups this user is in\n" +
                "`{%}permissions user <user> permissions add|remove <perm>` - Adds or removes a permissions from this user\n" +
                "`{%}permissions user <user> permissions list [page]` - list the permmissions this user has (Excluding those obtained from groups)\n\n" +
                "`{%}permissions groups` - Lists all the groups in a server\n" +
                "`{%}permissions reset|restoredefault` - Resets all of the guilds perms or resets the default group permissions";
    }


    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

    @Override
    public EnumSet<Permission> getDiscordPermission() {
        return EnumSet.of(Permission.MANAGE_PERMISSIONS);
    }
}

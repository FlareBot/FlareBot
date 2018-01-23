package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
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
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.pagination.PaginationUtil;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PermissionsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("group")) {
                String groupString = args[1];
                Group group = getPermissions(channel).getGroup(groupString);
                if (args.length >= 3) {
                    if (group == null && !args[2].equalsIgnoreCase("create")) {
                        MessageUtils.sendErrorMessage("That group doesn't exist! You can create it with `{%}permissions group " + groupString + " create`", channel);
                        return;
                    }
                    if (args[2].equalsIgnoreCase("add")) {
                        if (args.length == 4) {
                            if (!Permission.isValidPermission(args[3])) {
                                MessageUtils.sendErrorMessage("That is an invalid permission! Permissions start with `flarebot.` followed with a command name!\n" +
                                        "**Example:** `flarebot.play`\n" +
                                        "See `_permissions list` for a full list!", channel);
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
                    } else if (args[2].equalsIgnoreCase("remove")) {
                        if (args.length == 4) {
                            if (group.removePermission(args[3])) {
                                MessageUtils.sendSuccessMessage("Successfully removed the permission `" + args[3] + "` from the group `" + groupString + "`", channel, sender);
                                return;
                            } else {
                                MessageUtils.sendErrorMessage("Couldn't remove the permission (it probably didn't exist)", channel);
                                return;
                            }
                        }
                    } else if (args[2].equalsIgnoreCase("create")) {
                        if (getPermissions(channel).addGroup(groupString)) {
                            MessageUtils.sendSuccessMessage("Successfully created group: `" + groupString + "`", channel, sender);
                            return;
                        } else {
                            MessageUtils.sendErrorMessage("That group already exists!!", channel);
                            return;
                        }
                    } else if (args[2].equalsIgnoreCase("delete")) {
                        getPermissions(channel).deleteGroup(groupString);
                        return;
                    } else if (args[2].equalsIgnoreCase("link")) {
                        if (args.length == 4) {
                            Role role = GuildUtils.getRole(args[3], guild.getGuildId());
                            if (role != null) {
                                group.linkRole(role.getId());
                                MessageUtils.sendSuccessMessage("Successfully linked the group `" + groupString + "` to the role `" + role.getName() + "`", channel, sender);
                                return;
                            } else {
                                MessageUtils.sendErrorMessage("That role doesn't exist!", channel);
                                return;
                            }
                        }
                    } else if (args[2].equalsIgnoreCase("unlink")) {
                        Role role;
                        if (group.getRoleId() == null || (role =
                                guild.getGuild().getRoleById(group.getRoleId())) == null) {
                            MessageUtils.sendErrorMessage("Cannot unlink if a role isn't linked!!", channel);
                            return;
                        } else {
                            group.linkRole(null);
                            MessageUtils.sendSuccessMessage("Successfully unlinked the role " + role.getName() + " from the group " + group.getName(), channel, sender);
                            return;
                        }
                    } else if (args[2].equalsIgnoreCase("list")) {
                        if (args.length <= 4) {
                            int page = args.length == 4 ? Integer.valueOf(args[3]) : 1;
                            Set<String> perms = group.getPermissions();
                            List<String> permList = GeneralUtils.orderList(perms);

                            String list = permList.stream().collect(Collectors.joining("\n"));
                            PaginationUtil.sendEmbedPagedMessage(channel,
                                    PaginationUtil.splitStringToList(list, PaginationUtil.SplitMethod.NEW_LINES, 25),
                                    page - 1, true, group.getName() + " Permissions");
                            return;
                        }
                    } else if (args[2].equalsIgnoreCase("massadd")) {
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
                                Role role = GuildUtils.getRole(args[3], guild.getGuildId());
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
                    } else if (args[2].equalsIgnoreCase("clear")) {
                        group.getPermissions().clear();
                        MessageUtils.sendSuccessMessage("Cleared all permissions from the group: " + group.getName(), channel);
                        return;
                    }
                }
            } else if (args[0].equalsIgnoreCase("user")) {
                String userString = args[1];
                User user = GuildUtils.getUser(userString, guild.getGuildId());
                if (user == null) {
                    MessageUtils.sendErrorMessage("That user doesn't exist!!", channel);
                    return;
                }
                stream.flarebot.flarebot.permissions.User permUser =
                        getPermissions(channel).getUser(guild.getGuild().getMember(user));
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("group")) {
                        if (args.length >= 4) {
                            if (args[3].equalsIgnoreCase("add")) {
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
                            } else if (args[3].equalsIgnoreCase("remove")) {
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
                            } else if (args[3].equalsIgnoreCase("list")) {
                                int page = args.length == 5 ? Integer.valueOf(args[4]) : 1;
                                Set<String> groups = permUser.getGroups();
                                List<String> groupList = GeneralUtils.orderList(groups);

                                String list = groupList.stream().collect(Collectors.joining("\n"));
                                PaginationUtil.sendEmbedPagedMessage(channel,
                                        PaginationUtil.splitStringToList(list, PaginationUtil.SplitMethod.NEW_LINES, 25),
                                        page - 1, true, MessageUtils.getTag(user) + " Groups");
                                return;
                            }
                        }
                    } else if (args[2].equalsIgnoreCase("permission")) {
                        if (args.length >= 4) {
                            if (args[3].equalsIgnoreCase("add")) {
                                if (args.length == 5) {
                                    if (!Permission.isValidPermission(args[4])) {
                                        MessageUtils.sendErrorMessage("That is an invalid permission! Permissions start with `flarebot.` followed with a command name!\n" +
                                                "**Example:** `flarebot.play`\n" +
                                                "See `_permissions list` for a full list!", channel);
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
                            } else if (args[3].equalsIgnoreCase("remove")) {
                                if (args.length == 5) {
                                    if (permUser.removePermission(args[4])) {
                                        MessageUtils.sendSuccessMessage("Successfully removed the permission `" + args[4] + "` from " + user.getAsMention(), channel, sender);
                                        return;
                                    } else {
                                        MessageUtils.sendErrorMessage("The user already has that permission!!", channel);
                                        return;
                                    }
                                }
                            } else if (args[3].equalsIgnoreCase("list")) {
                                int page = args.length == 5 ? Integer.valueOf(args[4]) : 1;
                                Set<String> perms = permUser.getPermissions();
                                List<String> permList = GeneralUtils.orderList(perms);

                                String list = permList.stream().collect(Collectors.joining("\n"));
                                PaginationUtil.sendEmbedPagedMessage(channel,
                                        PaginationUtil.splitStringToList(list, PaginationUtil.SplitMethod.NEW_LINES, 25),
                                        page - 1, true, MessageUtils.getTag(user) + " Permissions");
                                return;
                            }
                        }
                    } else if (args[2].equalsIgnoreCase("check")) {
                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setTitle("Permissions for " + user.getName());
                        StringBuilder stringBuilder = new StringBuilder();
                        if (getPermissions(channel).hasPermission(guild.getGuild().getMember(user), Permission.ALL_PERMISSIONS)) {
                            stringBuilder.append("**All Permissions!**");
                        } else {
                            for (Permission perm : Permission.VALUES) {
                                if (getPermissions(channel).hasPermission(guild.getGuild().getMember(user), perm)) {
                                    stringBuilder.append("`").append(perm).append("`\n");
                                }
                            }
                        }
                        builder.setDescription(stringBuilder.toString());
                        channel.sendMessage(builder.build()).queue();
                        return;
                    } else if (args[2].equalsIgnoreCase("clear")) {
                        permUser.getPermissions().clear();
                        MessageUtils.sendSuccessMessage("Cleared all permissions from: " + MessageUtils.getTag(user), channel);
                        return;
                    }
                }
            }
        } else if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("groups")) {
                if (this.getPermissions(channel).getGroups().isEmpty()) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setColor(Color.RED)
                            .setDescription("There are no groups for this guild!")
                            .build()).queue();
                    return;
                } else {
                    int page = args.length == 2 ? Integer.valueOf(args[1]) : 1;
                    Set<String> groups =
                            this.getPermissions(channel).getGroups().stream().map(Group::getName).collect(Collectors.toSet());
                    List<String> groupList = GeneralUtils.orderList(groups);

                    String list = groupList.stream().collect(Collectors.joining("\n"));
                    PaginationUtil.sendEmbedPagedMessage(channel,
                            PaginationUtil.splitStringToList(list, PaginationUtil.SplitMethod.NEW_LINES, 20),
                            page - 1, true, "Groups");
                    return;
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                // TODO: Implement page system and embed here
                StringBuilder m = new StringBuilder("**Permissions List**\n");
                for (Permission p : Permission.values()) {
                    m.append("`").append(p).append("` ").append(p.isDefaultPerm()).append("\n");
                }
                channel.sendMessage(new EmbedBuilder().setDescription(m.toString()).build()).queue();
                return;
            } else if (args[0].equalsIgnoreCase("reset")) {
                guild.setPermissions(new PerGuildPermissions());
                MessageUtils.sendSuccessMessage("Successfully reset perms", channel, sender);
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
        return "`{%}permissions group <group> add|remove <perm>` - Adds or removes a permission to a group.\n" +
                "`{%}permissions group <group> create|delete` - Creates or deletes a group.\n" +
                "`{%}permissions group <group> link <role>` - Links the group to a discord role.\n" +
                "`{%}permissions group <group> unlink` - Unlinks the group from a role.\n" +
                "`{%}permissions group <group> list [page]` - lists the permissions this group has.\n" +
                "`{%}permissions group <group> massadd <@everyone/@here/role>` - Puts everyone with the giving role into the group.\n" +
                "`{%}permissions group <group> clear` - Removes all permissions from this group!\n\n" +
                "`{%}permissions user <user> group add|remove <group>` - Adds or removes a group from this user.\n" +
                "`{%}permissions user <user> group list [page]` - Lists the groups this user is in.\n" +
                "`{%}permissions user <user> permission add|remove <perm>` - Adds or removes a permissions from this user.\n" +
                "`{%}permissions user <user> permission list [page]` - list the permmissions this user has (Excluding those obtained from groups).\n" +
                "`{%}permissions user <user> check` - Returns all permissions a user has access to\n" +
                "`{%}permissions user <user> clear` - Removes all permissions from this user!\n\n" +
                "`{%}permissions groups` - Lists all the groups in a server.\n" +
                "`{%}permissions reset|restoredefault` - Resets all of the guilds perms or resets the default group permissions.";
    }


    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public EnumSet<net.dv8tion.jda.core.Permission> getDiscordPermission() {
        return EnumSet.of(net.dv8tion.jda.core.Permission.MANAGE_PERMISSIONS);
    }
}

package stream.flarebot.flarebot.commands.administrator;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.permissions.Group;
import stream.flarebot.flarebot.permissions.User;
import stream.flarebot.flarebot.util.Parser;

import java.io.IOException;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class PermissionsCommand implements Command {

    @Override
    public void onCommand(net.dv8tion.jda.core.entities.User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel).queue();
            return;
        }
        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 3) {
                    MessageUtils
                            .sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(getDescription()), channel);
                    return;
                }
                Member user = Parser.mention(args[1], channel.getGuild());
                if (user == null) {
                    MessageUtils
                            .sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("No such user!"), channel);
                    return;
                }
                if (!getPermissions(channel).hasGroup(args[2])) {
                    MessageUtils
                            .sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("No such group!"), channel);
                    return;
                }
                Group group = getPermissions(channel).getGroup(args[2]);
                if (getPermissions(channel).getUser(user).addGroup(group))
                    channel.sendMessage("Success").queue();
                else
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                            .setDescription("User already had that group!"), channel);
                break;
            case "revoke":
                if (args.length < 3) {
                    MessageUtils
                            .sendErrorMessage(MessageUtils.getEmbed(sender).setDescription(getDescription()), channel);
                    return;
                }
                Member user2 = Parser.mention(args[1], channel.getGuild());
                if (user2 == null) {
                    MessageUtils
                            .sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("No such user!"), channel);
                    return;
                }
                if (!getPermissions(channel).hasGroup(args[2])) {
                    MessageUtils
                            .sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("No such group!"), channel);
                    return;
                }
                Group group2 = getPermissions(channel).getGroup(args[2]);
                if (getPermissions(channel).getUser(user2).removeGroup(group2))
                    channel.sendMessage("Success").queue();
                else
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                            .setDescription("User never had that group!"), channel);
                break;
            case "groups":
                if (args.length == 1) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("Groups: " + getPermissions(channel).getGroups()
                                    .keySet()
                                    .stream()
                                    .collect(Collectors
                                            .joining(", ", "`", "`")))
                            .build()).queue();
                    return;
                }
                Member iUser = Parser.mention(args[1], channel.getGuild());
                if (iUser == null) {
                    MessageUtils
                            .sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("No such user!"), channel);
                    return;
                }
                User toList = getPermissions(channel).getUser(iUser);
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .addField("User", iUser.getAsMention(), true)
                        .setDescription("Groups: " + toList.getGroups().stream()
                                .collect(Collectors
                                        .joining(", ", "`", "`")))
                        .build()).queue();
                break;
            case "add":
                if (args.length < 3) {
                    channel.sendMessage(getDescription()).queue();
                    return;
                }
                Group group3 = getPermissions(channel).getGroup(args[1]);
                if (getPermissions(channel).addPermission(group3.getName(), args[2]))
                    channel.sendMessage("Success").queue();
                else
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                            .setDescription("Group already had that permission"), channel);
                break;
            case "remove":
                if (args.length < 3) {
                    channel.sendMessage(getDescription()).queue();
                    return;
                }
                Group group4 = getPermissions(channel).getGroup(args[1]);
                if (getPermissions(channel).removePermission(group4.getName(), args[2]))
                    channel.sendMessage("Success").queue();
                else
                    MessageUtils.sendErrorMessage(MessageUtils.getEmbed(sender)
                            .setDescription("Group never had that permission"), channel);
                break;
            case "list":
                if (args.length < 2) {
                    channel.sendMessage(getDescription()).queue();
                    return;
                }
                if (!getPermissions(channel).hasGroup(args[1])) {
                    MessageUtils
                            .sendErrorMessage(MessageUtils.getEmbed(sender).setDescription("No such group!"), channel);
                    return;
                }
                Group group5 = getPermissions(channel).getGroup(args[1]);
                StringBuilder perms = new StringBuilder("**Permissions for group ").append(group5.getName())
                        .append("**\n```fix\n");
                group5.getPermissions().forEach(perm -> perms.append(perm).append('\n'));
                perms.append("\n```");
                channel.sendMessage(perms.toString()).queue();
                break;
            case "save":
                if (getPermissions(channel).isCreator(sender))
                    try {
                        FlareBot.getInstance().getPermissions().save();
                    } catch (IOException e) {
                        MessageUtils.sendException("Could not save permissions!", e, channel);
                    }
                break;
            default:
                MessageUtils.sendUsage(this, channel).queue();
                break;
        }
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

    @Override
    public String getUsage() {
        return "`{%}permissions <give/revoke> <user> <group>` - Add/Remove a user from a group\n"
                + "`{%}permissions <list> <group>` - Lists permissions for a group\n"
                + "`{%}permissions <add/remove> <group> <permission>` - Add/Remove Permissions From group\n"
                + "`{%}permissions groups [user]` - List groups [for a user]";
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

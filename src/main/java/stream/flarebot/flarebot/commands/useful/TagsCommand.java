package stream.flarebot.flarebot.commands.useful;

import net.dv8tion.jda.core.entities.*;
import org.apache.commons.lang3.StringUtils;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.VariableUtils;

import java.awt.Color;
import java.util.Arrays;

public class TagsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            if (guild.getTags().isEmpty()) {
                MessageUtils.sendErrorMessage("There are currently no tags for this guild!\n" +
                        "Add a tag via `{%}tags add <tag_name> <tag_message>`." +
                        "\n\nView the usage for variables you can use!", channel);
                return;
            }
            channel.sendMessage(MessageUtils.getEmbed(sender).setTitle("Guild Tags", null).setColor(Color.CYAN)
                    .setDescription("```\n" + StringUtils.join(guild.getTags().keySet(), ", ") + "\n```")
                    .build()).queue();
        } else if (args.length == 1) {
            if (guild.getTags().containsKey(args[0].toLowerCase()))
                sendTag(guild, args[0].toLowerCase(), sender, channel, null);
            else
                MessageUtils.sendErrorMessage("This tag doesn't exist!", channel);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                MessageUtils.sendErrorMessage("This seems to be invalid ¯\\_(ツ)_/¯\n"
                        + "Usage: `{%}tags add " + args[1] + " <tag_message>`" +
                        "\n\nView the usage for variables you can use!", channel);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (!getPermissions(channel).hasPermission(member, Permission.TAGS_ADMIN)) {
                    MessageUtils.sendErrorMessage("You need the permission `" + Permission.TAGS_ADMIN + "` to do this!", channel, sender);
                    return;
                }
                if (guild.getTags().containsKey(args[1].toLowerCase())) {
                    guild.getTags().remove(args[1]);
                    MessageUtils.sendSuccessMessage("You successfully removed the tag `" + args[1] + "`!",
                            channel, sender);
                } else {
                    MessageUtils.sendErrorMessage("This tag doesn't exist!", channel);
                }
            } else if (args[0].equalsIgnoreCase("edit")) {
                MessageUtils.sendErrorMessage("This seems to be invalid ¯\\_(ツ)_/¯\n"
                        + "Usage: `{%}tags edit " + args[1] + " <tag_message>`" +
                        "\n\nView the usage for variables you can use!", channel);
            } else if (args[0].equalsIgnoreCase("raw")) {
                if (guild.getTags().containsKey(args[1].toLowerCase())) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setColor(Color.CYAN)
                            .addField("Tag", args[1].toLowerCase(), false)
                            .addField("Content", "```" + guild.getTags().get(args[1].toLowerCase()) + "```", false)
                            .build()).queue();
                } else {
                    MessageUtils.sendErrorMessage("This tag doesn't exist!", channel);
                }
            } else {
                if (guild.getTags().containsKey(args[0].toLowerCase()))
                    sendTag(guild, args[0].toLowerCase(), sender, channel,
                            Arrays.copyOfRange(args, 1, args.length));
                else
                    MessageUtils.sendErrorMessage("This tag doesn't exist!", channel);
            }
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                if (!getPermissions(channel).hasPermission(member, Permission.TAGS_ADMIN)) {
                    MessageUtils.sendErrorMessage("You need the permission `" + Permission.TAGS_ADMIN + "` to do this!", channel, sender);
                    return;
                }
                if (guild.getTags().containsKey(args[1].toLowerCase())) {
                    MessageUtils.sendErrorMessage("This tag already exists!", channel);
                    return;
                }

                guild.getTags().put(args[1].toLowerCase(), MessageUtils.getMessage(args, 2));
                MessageUtils.sendSuccessMessage("You successfully added the tag `" + args[1] + "`!", channel,
                        sender);
            } else if (args[0].equalsIgnoreCase("edit")) {
                if (!getPermissions(channel).hasPermission(member, Permission.TAGS_ADMIN)) {
                    MessageUtils.sendErrorMessage("You need the permission `" + Permission.TAGS_ADMIN + "` to do this!", channel, sender);
                    return;
                }
                if (!guild.getTags().containsKey(args[1].toLowerCase())) {
                    MessageUtils.sendErrorMessage("This tag doesn't exist!", channel);
                    return;
                }

                guild.getTags().put(args[1].toLowerCase(), MessageUtils.getMessage(args, 2));
                MessageUtils.sendSuccessMessage("You successfully edited the tag `" + args[1] + "`!", channel,
                        sender);
            } else {
                if (guild.getTags().containsKey(args[0].toLowerCase()))
                    sendTag(guild, args[0].toLowerCase(), sender, channel,
                            Arrays.copyOfRange(args, 1, args.length));
                else
                    MessageUtils.sendErrorMessage("This tag doesn't exist!", channel);
            }
        }
    }

    private void sendTag(GuildWrapper wrapper, String tag, User user, TextChannel channel, String[] args) {
        String msg = parseTag(wrapper.getGuild(), tag, wrapper.getTags().get(tag), user, channel, args);
        channel.sendMessage(msg).queue();
    }

    private String parseTag(Guild guild, String tag, String message, User user, TextChannel channel, String[] args) {
        String parsed = VariableUtils.parseVariables(message, FlareBotManager.instance().getGuild(guild.getId()),
                channel, user, args);
        if (message.contains("%user%") || message.contains("%mention%") || message.contains("{%}")
                || message.contains("%prefix%") || message.contains("%tag%")) {
            MessageUtils.sendPM(guild.getOwner().getUser(),
                    "Your tag '" + tag + "' contains deprecated variables! Please check the docs at the link below " +
                            "for a list of all the variables you can use!\n" +
                            "https://docs.flarebot.stream/variables");
        }
        // Deprecated values
        parsed = parsed
                .replace("%user%", user.getName())
                .replace("%mention%", user.getAsMention())
                .replace("{%}", String.valueOf(getPrefix(guild)))
                .replace("%prefix%", String.valueOf(getPrefix(guild)))
                .replace("%tag%", tag);
        return parsed;
    }

    @Override
    public String getCommand() {
        return "tags";
    }

    @Override
    public String getDescription() {
        return "Allows user to save chunks of texts as 'tags' and view them in the future";
    }

    @Override
    public String getUsage() {
        return "`{%}tags` - Lists the existing tags.\n" +
                "`{%}tags <tag_name>` - Sends the tag message.\n" +
                "`{%}tags raw <tag_name>` - Displays the raw content of a tag.\n" +
                "`{%}tags add <tag_name> <tag_message>` - Adds a tag to the guild.\n" +
                "`{%}tags edit <tag_name> <tag_message>` - Edits an already existing tag.\n" +
                "`{%}tags remove <tag_name>` - Removes a tag from the guild.";
    }

    @Override
    public Permission getPermission() {
        return Permission.TAGS_COMMAND;
    }

    @Override
    public String getExtraInfo() {
        return "**Variables**\n" +
                "`%user%` - Display the username of the person the tag is directed at, you can put a user in the second " +
                "argument to direct a tag at them. Example `_tag helpful_tag Walshy#9060`\n" +
                "`%mention%` - Just like %user% but instead it will tag the user.\n" +
                "`%prefix%` and `{%}` - These will put the server prefix, so you could so something like `Run {%}song " +
                "to see what song is currently playing!` and it would print something like `Run !song to see...`\n" +
                "`%tag%` - This will put what tag is being used, if you want to make it easier for users to know what " +
                "tag is what you can put this anywhere to show what tag was run.";
    }

    @Override
    public CommandType getType() {
        return CommandType.USEFUL;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"tag", "t"};
    }
}

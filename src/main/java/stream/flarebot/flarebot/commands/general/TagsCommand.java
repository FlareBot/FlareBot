package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringUtils;
import stream.flarebot.flarebot.Lang;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class TagsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            if (guild.getTags().isEmpty()) {
                MessageUtils.sendErrorMessage(Lang.get(guild, this, "no_tags"), channel);
                return;
            }
            channel.sendMessage(MessageUtils.getEmbed(sender).setTitle(Lang.get(guild, this, "guild_tags"), null)
                    .setColor(Color.CYAN).setDescription("```\n" + StringUtils.join(guild.getTags().keySet(), ", ")
                            + "\n```")
                    .build()).queue();
        } else if (args.length == 1) {
            if (guild.getTags().containsKey(args[0].toLowerCase()))
                channel.sendMessage(guild.getTags().get(args[0].toLowerCase())).queue();
            else
                MessageUtils.sendErrorMessage(Lang.get(guild, this, "tag_does_not_exist"), channel);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                MessageUtils.sendErrorMessage(Lang.get(guild, this, "invalid", args[1]), channel);
            } else if (args[0].equalsIgnoreCase("remove")) {
                if (guild.getTags().containsKey(args[1].toLowerCase())) {
                    guild.getTags().remove(args[1]);
                    MessageUtils.sendSuccessMessage(Lang.get(guild, this, "removed_tag", args[1]),
                            channel, sender);
                } else
                    MessageUtils.sendErrorMessage(Lang.get(guild, this, "tag_does_not_exist"), channel);
            } else
                MessageUtils.sendErrorMessage(Lang.get(guild, "global.invalid_argument"), channel);
        } else {
            if (args[0].equalsIgnoreCase("add")) {
                if (guild.getTags().containsKey(args[1].toLowerCase())) {
                    MessageUtils.sendErrorMessage(Lang.get(guild, this, "tag_exists"), channel);
                    return;
                }

                guild.getTags().put(args[1].toLowerCase(), MessageUtils.getMessage(args, 2));
                MessageUtils.sendSuccessMessage(Lang.get(guild, this, "added_tag", args[1]), channel,
                        sender);
            } else
                MessageUtils.sendErrorMessage(Lang.get(guild, "global.invalid_argument"), channel);
        }
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
        return "`{%}tags` - Lists the existing tags\n" +
                "`{%}tags <tag_name>` - Sends the tag message\n" +
                "`{%}tags add <tag_name> <tag_message>` - Adds a tag to the guild\n" +
                "`{%}tags remove <tag_name>` - Removes a tag from the guild";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"tag"};
    }

    @Override
    public boolean isBetaTesterCommand() {
        return true;
    }
}

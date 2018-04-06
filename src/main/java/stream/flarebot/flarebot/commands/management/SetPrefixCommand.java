package stream.flarebot.flarebot.commands.management;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.EnumSet;

public class SetPrefixCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reset")) {
                guild.setPrefix('_');
                MessageUtils.sendInfoMessage("Reset the prefix back to `_`!", channel, sender);
            } else if (args[0].length() == 1) {
                guild.setPrefix(args[0].charAt(0));
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription(String.format("Set the prefix to `%s`", args[0])).build())
                        .queue();
            } else {
                MessageUtils.sendErrorMessage("Cannot set the prefix to be more that one character long!", channel, sender);
            }
        } else {
            channel.sendMessage(MessageUtils.getEmbed(sender)
                    .setDescription(String.format("Current guild prefix is `%s`!", guild.getPrefix())).build()).queue();
        }
    }

    @Override
    public String getCommand() {
        return "prefix";
    }

    @Override
    public String getDescription() {
        return "Sets the prefix in this guild!";
    }

    @Override
    public String getUsage() {
        return "`{%}prefix reset` - Resets the prefix for FlareBot commands.\n" +
                "`{%}prefix <prefix>` - Sets the prefix for FlareBot commands.";
    }

    @Override
    public stream.flarebot.flarebot.permissions.Permission getPermission() {
        return stream.flarebot.flarebot.permissions.Permission.SETPREFIX_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"setprefix"};
    }
}

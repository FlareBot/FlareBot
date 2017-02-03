package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class SetPrefixCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reset")) {
                FlareBot.getPrefixes().set(channel.getGuild().getId(), '_');
            } else if (args[0].length() == 1) {
                FlareBot.getPrefixes().set(channel.getGuild().getId(), args[0].charAt(0));
            } else {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("Cannot set the prefix to be more that one character long!"), channel);
                return;
            }
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                    .setDescription(String.format("Set the prefix to `%s`", args[0])), channel);
        } else {
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                    .setDescription(String.format("Current guild prefix is `%s`!", FlareBot.getPrefix(channel.getGuild().getId()))), channel);
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
    public CommandType getType() {
        return CommandType.ADMINISTRATIVE;
    }

    @Override
    public String getPermission() {
        return "flarebot.prefix";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"setprefix"};
    }
}

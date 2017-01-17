package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

import java.util.stream.Collectors;

public class HelpCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 1) {
            CommandType type;
            try {
                type = CommandType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException ignored) {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("No such category!").build(), channel);
                return;
            }
            if (type != CommandType.HIDDEN) {
                EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
                embedBuilder.withDesc("***FlareBot " + type + " commands!***");
                MessageUtils.sendMessage(embedBuilder.appendField(type.toString(), type.getCommands()
                        .stream()
                        .map(command -> get(channel) + command.getCommand() + " - " + command.getDescription() + '\n')
                        .collect(Collectors.joining("")), false).build(), channel);
            } else {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("No such category!").build(), channel);
            }
        } else {
            sendCommands(channel, sender);
        }
    }

    private char get(IChannel channel) {
        if (channel.getGuild() != null) {
            return FlareBot.getPrefixes().get(channel.getGuild().getID());
        }
        return FlareBot.getPrefixes().get(null);
    }

    private void sendCommands(IChannel channel, IUser sender) {
        EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
        for (CommandType c : CommandType.getTypes()) {
            String help = c.getCommands()
                    .stream()
                    .map(command -> get(channel) + command.getCommand() + " - " + command.getDescription() + '\n')
                    .collect(Collectors.joining(""));
            embedBuilder.appendField(c.toString(), help, false);
        }
        MessageUtils.sendMessage(embedBuilder.build(), channel);
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"commands"};
    }

    @Override
    public String getDescription() {
        return "See a list of all commands.";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

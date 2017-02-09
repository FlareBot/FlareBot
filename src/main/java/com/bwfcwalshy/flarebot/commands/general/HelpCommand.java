package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.stream.Collectors;

public class HelpCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            CommandType type;
            try {
                type = CommandType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException ignored) {
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("No such category!").build()).queue();
                return;
            }
            if (type != CommandType.HIDDEN) {
                EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
                embedBuilder.setDescription("***FlareBot " + type + " commands!***");
                channel.sendMessage(embedBuilder.addField(type.toString(), type.getCommands()
                        .stream()
                        .map(command -> get(channel) + command.getCommand() + " - " + command.getDescription() + '\n')
                        .collect(Collectors.joining("")), false).build()).queue();
            } else {
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("No such category!").build()).queue();
            }
        } else {
            sendCommands(channel, sender);
        }
    }

    private char get(TextChannel channel) {
        if (channel.getGuild() != null) {
            return FlareBot.getPrefixes().get(channel.getGuild().getId());
        }
        return FlareBot.getPrefixes().get(null);
    }

    private void sendCommands(TextChannel channel, User sender) {
        EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
        for (CommandType c : CommandType.getTypes()) {
            String help = c.getCommands()
                    .stream()
                    .map(command -> get(channel) + command.getCommand() + " - " + command.getDescription() + '\n')
                    .collect(Collectors.joining(""));
            embedBuilder.addField(c.toString(), help, false);
        }
        channel.sendMessage(embedBuilder.build()).queue();
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

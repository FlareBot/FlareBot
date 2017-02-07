package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.File;
import java.io.IOException;

public class LogsCommands implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (getPermissions(channel).isCreator(sender)) {
            try {
                channel.sendFile(new File("latest.log"), new MessageBuilder().append('\u200B').build()).queue();
            } catch (IOException e) {
                FlareBot.LOGGER.error("Could not send logs", e);
            }
        }
    }

    @Override
    public String getCommand() {
        return "logs";
    }

    @Override
    public String getDescription() {
        return "Gets the logs";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }
}

package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class LeaveCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        channel.getGuild().getAudioManager().closeAudioConnection();
    }

    @Override
    public String getCommand() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Tell me to leave the voice channel.";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public String getPermission() {
        return "flarebot.leave";
    }
}

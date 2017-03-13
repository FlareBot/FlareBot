package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class QuitCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (sender.getId().equals("158310004187725824") || sender.getId().equals("155954930191040513")) {
            FlareBot.getInstance().quit(false);
        }
    }

    @Override
    public String getCommand() {
        return "quit";
    }

    @Override
    public String getDescription() {
        return "Dev only command";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }

    @Override
    public boolean isDefaultPermission(){
        return false;
    }
}

package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class QuitCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (sender.getID().equals("158310004187725824") || sender.getID().equals("155954930191040513")) {
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
}

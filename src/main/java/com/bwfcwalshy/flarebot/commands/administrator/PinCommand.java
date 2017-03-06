package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public class PinCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length == 1){
            if(FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.pin")){
                String messageId = args[0].replaceAll("[^0-9]", "");
                Message msg = channel.getMessageById(messageId).complete();
                msg.pin().queue();
                channel.getHistory().retrievePast(1).complete().get(0).delete().queue();
            }
        }else{
            if(FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.pin")){
                MessageUtils.sendErrorMessage("Usage: `pin (messageId)`", channel);
            }
        }
    }

    @Override
    public String getCommand() {
        return "pin";
    }

    @Override
    public String getDescription() {
        return "Pin a message";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

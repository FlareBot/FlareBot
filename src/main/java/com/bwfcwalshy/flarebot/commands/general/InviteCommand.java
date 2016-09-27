package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class InviteCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        MessageUtils.sendPM(sender, "You can invite me to your server using the link below!" +
                "\nhttps://discordapp.com/oauth2/authorize?client_id=225652110493089792&scope=bot&&permissions=271584256");
    }

    @Override
    public String getCommand() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Get my invite link!";
    }

    @Override
    public CommandType getType() { return CommandType.GENERAL; }
}

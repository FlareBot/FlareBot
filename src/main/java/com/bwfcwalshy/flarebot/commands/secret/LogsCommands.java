package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.io.File;
import java.io.IOException;

public class LogsCommands implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(getPermissions(channel).isCreator(sender)){
            RequestBuffer.request(() -> {
                try {
                    channel.sendFile("Latest log:", new File("latest.log"));
                } catch (IOException | MissingPermissionsException | DiscordException e) {
                    MessageUtils.sendException("**Could not upload the log file!**", e, channel);
                }
            });
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

package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.List;
import java.util.stream.Collectors;

public class Purge implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length == 1 && args[0].matches("\\d+")){
            int count = Math.min(Integer.parseInt(args[0]), 100);
            if(count < 2){
                MessageUtils.sendMessage(MessageUtils
                        .getEmbed(sender).withDesc("Can't purge less than 2 messages!").build(), channel);
            }
            RequestBuffer.request(() -> {
                channel.getMessages().setCacheCapacity(count);
                boolean loaded  = channel.getMessages().load(count);
                if(loaded){
                    try {
                        List<IMessage> list = channel.getMessages().stream().limit(99)
                                .collect(Collectors.toList());
                        channel.getMessages().bulkDelete(list);
                        channel.getMessages().setCacheCapacity(0);
                        MessageUtils.sendMessage(MessageUtils
                                .getEmbed(sender).withDesc(":+1: Deleted!")
                                .appendField("Message Count: ", String.valueOf(list.size()), true).build(), channel);
                    } catch (DiscordException e) {
                        FlareBot.LOGGER.error("Could not bulk delete!", e);
                        MessageUtils.sendMessage(MessageUtils
                                .getEmbed(sender).withDesc("Could not bulk delete! Error occured!").build(), channel);
                    } catch (MissingPermissionsException e) {
                        MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                                .withDesc("I do not have the `Manage Messages` permission!\n"+e.getMessage()).build(),
                                channel);
                    }
                } else MessageUtils.sendMessage(MessageUtils
                        .getEmbed(sender).withDesc("Could not load in messages!").build(), channel);
            });
        }
    }

    @Override
    public String getCommand() {
        return "purge";
    }

    @Override
    public String getDescription() {
        return "Removes last X messages. Usage: `_purge MESSAGES`";
    }

    @Override
    public CommandType getType() {
        return CommandType.ADMINISTRATIVE;
    }

    @Override
    public String getPermission() {
        return "flarebot.purge";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"clean"};
    }
}

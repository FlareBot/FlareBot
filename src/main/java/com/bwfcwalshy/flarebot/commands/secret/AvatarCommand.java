package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.Image;
import sx.blah.discord.util.RateLimitException;

public class AvatarCommand implements Command {
    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 0) {
            if (!message.getAttachments().isEmpty()) {
                IMessage.Attachment attachment = message.getAttachments().get(0);
                try {
                    sender.getClient().changeAvatar(Image.forUrl(
                            attachment.getFilename().substring(attachment.getFilename().lastIndexOf('.') + 1),
                            attachment.getUrl()));
                } catch (DiscordException | RateLimitException e) {
                    MessageUtils.sendException("Could not update avatar!", e, channel);
                    return;
                }
            } else {
                MessageUtils.sendMessage("You must either attach an image or link one!", channel);
                return;
            }
        } else {
            try {
                sender.getClient().changeAvatar(Image.forUrl(
                        args[0].substring(args[0].lastIndexOf('.') + 1),
                        args[0]));
            } catch (DiscordException | RateLimitException e) {
                MessageUtils.sendException("Could not update avatar!", e, channel);
                return;
            }
        }
        MessageUtils.sendMessage("Done, I think ;)", channel);
    }

    @Override
    public String getCommand() {
        return "avatar";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }
}

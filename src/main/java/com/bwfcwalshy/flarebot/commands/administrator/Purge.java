package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Purge implements Command {
    private Map<String, Long> cooldowns = new HashMap<>();
    private static final long cooldown = 60000;

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (channel.isPrivate()) {
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("Cannot purge in DMs!").build(), channel);
            return;
        }
        if (args.length == 1 && args[0].matches("\\d+")) {
            if(!FlareBot.getInstance().getPermissions(channel).hasPermission(sender, "flarebot.purge.bypass")) {
                long calmitdood = cooldowns.computeIfAbsent(channel.getGuild().getID(), n -> 0L);
                if (System.currentTimeMillis() - calmitdood < cooldown) {
                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                            .withDesc(String.format("You are on a cooldown! %s seconds left!",
                                    (cooldown - (System.currentTimeMillis() - calmitdood)) / 1000)).build(), channel);
                    return;
                }
            }
            int count = Integer.parseInt(args[0]) + 1;
            if (count < 2 || count > 200) {
                MessageUtils.sendMessage(MessageUtils
                        .getEmbed(sender).withDesc("Can't purge less than 2 messages or more than 200!").build(), channel);
                return;
            }
            if(!FlareBot.getInstance().getPermissions(channel).hasPermission(sender, "flarebot.purge.bypass"))
                cooldowns.put(channel.getGuild().getID(), System.currentTimeMillis());
            RequestBuffer.request(() -> {
                channel.getMessages().setCacheCapacity(count);
                boolean loaded = true;
                while (loaded && channel.getMessages().size() < count)
                    loaded = channel.getMessages().load(Math.min(count, 100));
                if (loaded) {
                    List<IMessage> list = new ArrayList<>(channel.getMessages());
                    List<IMessage> toDelete = new ArrayList<>();
                    for (IMessage msg : list) {
                        if (toDelete.size() > 99) {
                            bulk(toDelete, channel, sender);
                            toDelete.clear();
                        }
                        toDelete.add(msg);
                    }
                    bulk(toDelete, channel, sender);
                    channel.getMessages().setCacheCapacity(0);
                    IMessage msg = MessageUtils.sendMessage(MessageUtils
                            .getEmbed(sender).withDesc(":+1: Deleted!")
                            .appendField("Message Count: ", String.valueOf(count - 1), true).build(), channel);
                    new FlarebotTask("Delete message " + msg.getChannel().toString() + msg.getID()) {
                        @Override
                        public void run() {
                            RequestBuffer.request(() -> {
                                try {
                                    msg.delete();
                                } catch (MissingPermissionsException | DiscordException ignored) {
                                }
                            });
                        }
                    }.delay(10000);
                } else MessageUtils.sendMessage(MessageUtils
                        .getEmbed(sender).withDesc("Could not load in messages!").build(), channel);
            });
        } else {
            MessageUtils.sendMessage(MessageUtils
                    .getEmbed(sender).withDesc("Bad arguments!\n" + getDescription()).build(), channel);
        }
    }

    private void bulk(List<IMessage> toDelete, IChannel channel, IUser sender) {
        RequestBuffer.request(() -> {
            try {
                channel.getMessages().bulkDelete(toDelete);
            } catch (DiscordException e) {
                FlareBot.LOGGER.error("Could not bulk delete!", e);
                MessageUtils.sendMessage(MessageUtils
                        .getEmbed(sender).withDesc("Could not bulk delete! Error occured!").build(), channel);
            } catch (MissingPermissionsException e) {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                                .withDesc("I do not have the `Manage Messages` permission!").build(), channel);
            }
        });
    }

    @Override
    public String getCommand() {
        return "purge";
    }

    @Override
    public String getDescription() {
        return "Removes last X messages. Usage: `purge MESSAGES`";
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

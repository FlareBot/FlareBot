package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class MusicAnnounceCommand implements Command {
    private static final Pattern ARGS_PATTERN = Pattern.compile("(here)|(off)", Pattern.CASE_INSENSITIVE);
    private static Map<String, String> announcements = new ConcurrentHashMap<>();

    public static Map<String, String> getAnnouncements() {
        return announcements;
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 1 && ARGS_PATTERN.matcher(args[0]).matches()) {
            if (args[0].equalsIgnoreCase("here")) {
                announcements.put(channel.getGuild().getID(), channel.getID());
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .withDesc("Set music announcements to appear in " + channel).build(), channel);
            } else {
                announcements.remove(channel.getGuild().getID());
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .withDesc(String.format("Disabled announcements for `%s`", channel.getGuild().getName()))
                        .build(), channel);
            }
        } else {
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                    .withDesc("Bad syntax! Must have either `HERE` or `OFF` as your first, and only, argument." +
                            "\nCase insensitive.").build(), channel);
        }
    }

    @Override
    public String getCommand() {
        return "announce";
    }

    @Override
    public String getDescription() {
        return "Announces a track start in a text channel. Usage: `_announce HERE|OFF`";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

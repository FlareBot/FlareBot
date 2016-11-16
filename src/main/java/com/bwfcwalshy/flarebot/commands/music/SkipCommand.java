package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.MusicManager;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.Map;

public class SkipCommand implements Command {

    private MusicManager musicManager;
    private Map<String, Map<String, Vote>> votes = new HashMap<>();

    public SkipCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (channel.getGuild().getConnectedVoiceChannel() == null || !musicManager.hasPlayer(channel.getGuild().getID())
                || musicManager.getPlayer(channel.getGuild().getID()).getCurrentTrack() == null) {
            MessageUtils.sendMessage(channel, "I am not streaming!");
            return;
        }
        if (!sender.getConnectedVoiceChannels().contains(channel.getGuild().getConnectedVoiceChannel())) {
            MessageUtils.sendMessage(channel, "You must be in the channel in order to skip songs!");
        }
        if (args.length != 1) {
            if (votes.containsKey(channel.getGuild().getID())) {
                MessageUtils.sendMessage(channel, "Can't start a vote right now! " +
                        "Another one in progress! Please use _skip YES|NO to vote!");
                return;
            }
        }
        Map<String, Vote> mvotes = this.votes.computeIfAbsent(channel.getGuild().getID(), s -> {
            new FlarebotTask("Vote " + s) {

                @Override
                public void run() {
                    String res = "";
                    boolean skip = votes.get(s).entrySet().stream()
                            .filter(e -> e.getValue() == Vote.YES)
                            .count() > (votes.size() / 2.0f);
                    MessageUtils.sendMessage(channel, "The votes are in!!!\nResult: " + (skip ? "Skip!" : "Keep!"));
                    if (skip)
                        musicManager.skip(s);
                    votes.remove(s);
                }
            }.delay(20000);
            MessageUtils.sendMessage(channel, "The vote to skip **" +
                    musicManager.getPlayer(channel.getGuild().getID()).getCurrentTrack().getMetadata().get("name")
                    + "** has started!\nUse _skip YES|NO to vote!");
            return new HashMap<>();
        });
        if (mvotes.containsKey(sender.getID())) {
            MessageUtils.sendMessage(channel, "You already voted!");
            return;
        }
        if (args.length == 1)
            try {
                Vote vote = Vote.valueOf(args[0].toUpperCase());
                mvotes.put(sender.getID(), vote);
            } catch (IllegalArgumentException e) {
                MessageUtils.sendMessage(channel, "Please use yes or no!");
            }
    }

    @Override
    public String getCommand() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Starts a skip voting, or if one is happening, marks a vote. _skip YES|NO to pass a vote.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.skip";
    }

    private enum Vote {
        YES,
        NO
    }
}

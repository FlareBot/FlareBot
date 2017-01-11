package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.scheduler.FlarebotTask;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SkipCommand implements Command {

    private PlayerManager musicManager;
    private Map<String, Map<String, Vote>> votes = new HashMap<>();
    private Map<String, Boolean> skips = new HashMap<>();

    public SkipCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (channel.getGuild().getConnectedVoiceChannel() == null
                || musicManager.getPlayer(channel.getGuild().getID()).getPlayingTrack() == null) {
            MessageUtils.sendMessage("I am not streaming!", channel);
            return;
        }
        if (!sender.getConnectedVoiceChannels().contains(channel.getGuild().getConnectedVoiceChannel())) {
            MessageUtils.sendMessage("You must be in the channel in order to skip songs!", channel);
        }
        if (args.length != 1) {
            if (votes.containsKey(channel.getGuild().getID())) {
                String yes = String.valueOf(votes.get(channel.getGuild().getID()).values().stream().filter(vote -> vote == Vote.YES)
                        .count());
                String no = String.valueOf(votes.get(channel.getGuild().getID()).values().stream().filter(vote -> vote == Vote.NO)
                        .count());
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withColor(229, 45, 39)
                        .withDesc("Can't start a vote right now! " +
                                "Another one in progress! Please use _skip YES|NO to vote!")
                        .appendField("Votes for YES:", yes, true).appendField("Votes for NO:", no, true), channel);
            } else getVotes(channel, sender);
        } else {
            if (args[0].equalsIgnoreCase("force")) {
                if (getPermissions(channel).hasPermission(sender, "flarebot.skip.force")) {
                    musicManager.getPlayer(channel.getGuild().getID()).skip();
                    skips.put(channel.getGuild().getID(), true);
                } else {
                    MessageUtils.sendMessage("You are missing the permission ``flarebot.skip.force`` which is required for use of this command!", channel
                    );
                }
                return;
            }
            Map<String, Vote> mvotes = getVotes(channel, sender);
            if (mvotes == null)
                return;
            if (mvotes.containsKey(sender.getID())) {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withColor(229, 45, 39)
                        .withDesc("***\u26A0 You already voted! \u26A0***")
                        .appendField("Your vote: ", mvotes.get(sender.getID()).toString(), true), channel);
                return;
            }
            try {
                Vote vote = Vote.valueOf(args[0].toUpperCase());
                mvotes.put(sender.getID(), vote);
            } catch (IllegalArgumentException e) {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .withColor(229, 45, 39).withDesc("***\u26A0 Use YES|NO! \u26A0***"), channel);
            }
        }
    }

    private Map<String, Vote> getVotes(IChannel channel, IUser sender) {
        return this.votes.computeIfAbsent(channel.getGuild().getID(), s -> {
            AtomicBoolean bool = new AtomicBoolean(false);
            sender.getConnectedVoiceChannels().stream().filter(c -> c.getGuild().equals(channel.getGuild()))
                    .findFirst().ifPresent(c -> {
                if (c.getConnectedUsers().size() == 2
                        && c.getConnectedUsers().contains(FlareBot.getInstance().getClient().getOurUser())) {
                    bool.set(true);
                    musicManager.getPlayer(s).skip();
                }
            });
            if (bool.get()) {
                MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                        .withDescription("You were the only person in the channel.\nSkipping!"), channel);
                return null;
            }
            new FlarebotTask("Vote " + s) {

                @Override
                public void run() {
                    if (skips.getOrDefault(s, false)) {
                        skips.remove(s);
                        votes.remove(s);
                        return;
                    }
                    String res = "";
                    boolean skip = votes.get(s).entrySet().stream()
                            .filter(e -> e.getValue() == Vote.YES)
                            .count() > (votes.size() / 2.0f);
                    MessageUtils.sendMessage(MessageUtils.getEmbed()
                            .withDesc("The votes are in!")
                            .appendField("Results: ", (skip ? "Skip!" : "Keep!"), false), channel);
                    if (skip)
                        musicManager.getPlayer(s).skip();
                    votes.remove(s);
                }
            }.delay(20000);
            MessageUtils.sendMessage(MessageUtils.getEmbed(sender).withDesc("The vote to skip **" +
                    musicManager.getPlayer(channel.getGuild().getID()).getPlayingTrack().getTrack().getInfo().title
                    + "** has started!\nUse _skip YES|NO to vote!"), channel);
            return new HashMap<>();
        });
    }

    @Override
    public String getCommand() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Starts a skip voting, or if one is happening, marks a vote. `skip YES|NO` to pass a vote. To force skip use `skip force`";
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
        NO;

        @Override
        public String toString() {
            return Character.toUpperCase(name().charAt(0)) + name().substring(1);
        }
    }
}

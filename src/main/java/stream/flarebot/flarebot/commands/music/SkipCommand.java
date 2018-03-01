package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.player.Track;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.FlareBotTask;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SkipCommand implements Command {

    private Map<String, Map<String, Vote>> votes = new HashMap<>();
    private Map<String, Boolean> skips = new HashMap<>();

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        PlayerManager musicManager = FlareBot.getInstance().getMusicManager();
        if (!channel.getGuild().getAudioManager().isConnected() ||
                musicManager.getPlayer(channel.getGuild().getId()).getPlayingTrack() == null) {
            channel.sendMessage("I am not playing anything!").queue();
            return;
        }
        if (member.getVoiceState().inVoiceChannel() && !channel.getGuild().getSelfMember().getVoiceState().getChannel()
                .getId().equals(member.getVoiceState().getChannel().getId())
                && !getPermissions(channel).hasPermission(member, "flarebot.skip.force")) {
            channel.sendMessage("You must be in the channel in order to skip songs!").queue();
            return;
        }
        Track currentTrack = musicManager.getPlayer(guild.getGuildId()).getPlayingTrack();
        if (args.length == 0 && currentTrack.getMeta().get("requester").equals(sender.getId())) {
            channel.sendMessage("Skipped your own song!").queue();
            musicManager.getPlayer(guild.getGuildId()).skip();
            return;
        }
        
        if (args.length != 1) {
            if (votes.containsKey(channel.getGuild().getId())) {
                String yes = String.valueOf(votes.get(channel.getGuild().getId()).values().stream()
                        .filter(vote -> vote == Vote.YES)
                        .count());
                String no = String
                        .valueOf(votes.get(channel.getGuild().getId()).size() - Long.valueOf(yes));
                channel.sendMessage(MessageUtils.getEmbed(sender).setColor(new Color(229, 45, 39))
                        .setDescription("Can't start a vote right now! " +
                                "Another one in progress! Please use " + FlareBot
                                .getPrefix(channel.getGuild().getId()) + "skip YES|NO to vote!")
                        .addField("Votes for YES:", yes, true)
                        .addField("Votes for NO:", no, true).build()).queue();
            } else getVotes(channel, member);
        } else {
            if (args[0].equalsIgnoreCase("force")) {
                if (getPermissions(channel).hasPermission(member, "flarebot.skip.force")) {
                    musicManager.getPlayer(channel.getGuild().getId()).skip();
                    skips.put(channel.getGuild().getId(), true);
                } else {
                    channel.sendMessage("You are missing the permission ``flarebot.skip.force`` which is required for use of this command!")
                            .queue();
                }
                return;
            } else if (args[0].equalsIgnoreCase("cancel")) {

                if (getPermissions(channel).hasPermission(member, "flarebot.skip.cancel")) {
                    skips.put(channel.getGuild().getId(), true);
                } else {
                    channel.sendMessage("You are missing the permission ``flarebot.skip.cancel`` which is required for use of this command!")
                            .queue();
                }
                return;
            }
            Map<String, Vote> mvotes = getVotes(channel, member);
            if (mvotes == null)
                return;
            if (mvotes.containsKey(sender.getId())) {
                channel.sendMessage(MessageUtils.getEmbed(sender).setColor(new Color(229, 45, 39))
                        .setDescription("***\u26A0 You already voted! \u26A0***")
                        .addField("Your vote: ", mvotes.get(sender.getId()).toString(), true)
                        .build()).queue();
                return;
            }
            try {
                Vote vote = Vote.valueOf(args[0].toUpperCase());
                mvotes.put(sender.getId(), vote);
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription(String.format("***Voted for %s***", vote))
                        .addField("Current votes for " + vote, String
                                .valueOf(votes.get(channel.getGuild().getId())
                                        .values().stream().filter(v -> v == vote)
                                        .count()), true).build()).queue();
            } catch (IllegalArgumentException e) {
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setColor(new Color(229, 45, 39))
                        .setDescription("***\u26A0 Use YES|NO! \u26A0***").build()).queue();
            }
        }
    }

    private Map<String, Vote> getVotes(TextChannel channel, Member sender) {
        PlayerManager musicManager = FlareBot.getInstance().getMusicManager();
        return this.votes.computeIfAbsent(channel.getGuild().getId(), s -> {
            AtomicBoolean bool = new AtomicBoolean(false);
            channel.getGuild().getVoiceChannels().stream().filter(c -> c.equals(sender.getVoiceState().getChannel()))
                    .findFirst().ifPresent(c -> {
                if (c.getMembers().size() == 2
                        && c.equals(channel.getGuild().getSelfMember().getVoiceState().getChannel())) {
                    bool.set(true);
                    musicManager.getPlayer(s).skip();
                }
            });
            if (bool.get()) {
                channel.sendMessage(MessageUtils.getEmbed(sender.getUser())
                        .setDescription("You were the only person in the channel.\nSkipping!")
                        .build()).queue();
                return null;
            }
            new FlareBotTask("Vote " + s) {

                @Override
                public void run() {
                    if (skips.getOrDefault(s, false)) {
                        skips.remove(s);
                        votes.remove(s);
                        return;
                    }
                    long yesCount = votes.get(s).entrySet().stream()
                            .filter(e -> e.getValue() == Vote.YES)
                            .count();
                    boolean skip = yesCount > (votes.get(s).size() - yesCount);
                    channel.sendMessage(MessageUtils.getEmbed()
                            .setDescription("The votes are in!")
                            .addField("Results: ", (skip ? "Skip!" : "Keep!"), false).build())
                            .queue();
                    if (skip)
                        musicManager.getPlayer(s).skip();
                    votes.remove(s);
                }
            }.delay(TimeUnit.SECONDS.toMillis(20));
            channel.sendMessage(MessageUtils.getEmbed(sender.getUser()).setDescription("The vote to skip **" +
                    musicManager.getPlayer(channel.getGuild().getId()).getPlayingTrack().getTrack().getInfo().title
                    + "** has started!\nUse " + FlareBot.getPrefix(channel.getGuild().getId()) + "skip YES|NO to vote!")
                    .build()).queue();
            return new HashMap<>();
        });
    }

    @Override
    public String getCommand() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Starts a skip voting, or if one is happening, marks a vote. `skip YES|NO` to pass a vote. To force skip use `skip force`." +
                " You can also use `skip cancel` to cancel the current vote as an admin.";
    }

    @Override
    public String getUsage() {
        return "`{%}skip` - Starts a vote to skip the song.\n" +
                "`{%}skip yes|no` - Vote yes or no to skip the current song.\n" +
                "`{%}skip force` - Forces FlareBot to skip the current song.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
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

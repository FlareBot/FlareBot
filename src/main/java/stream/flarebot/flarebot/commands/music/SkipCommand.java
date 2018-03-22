package stream.flarebot.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.player.Track;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.FormatUtils;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.objects.ButtonGroup;
import stream.flarebot.flarebot.util.votes.VoteGroup;
import stream.flarebot.flarebot.util.votes.VoteUtil;

import static stream.flarebot.flarebot.commands.music.SongCommand.getLink;

public class SkipCommand implements Command {
    private Map<String, Boolean> skips = new HashMap<>();

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        boolean songMessage = message.getAuthor().getIdLong() == Getters.getSelfUser().getIdLong();
        PlayerManager musicManager = FlareBot.instance().getMusicManager();
        if (!channel.getGuild().getAudioManager().isConnected() ||
                musicManager.getPlayer(channel.getGuild().getId()).getPlayingTrack() == null) {
            channel.sendMessage("I am not playing anything!").queue();
            return;
        }
        if (member.getVoiceState().inVoiceChannel() && !channel.getGuild().getSelfMember().getVoiceState().getChannel()
                .getId()
                .equals(member.getVoiceState().getChannel().getId())
                && !getPermissions(channel).hasPermission(member, Permission.SKIP_FORCE)) {
            channel.sendMessage("You must be in the channel in order to skip songs!").queue();
            return;
        }
        Track currentTrack = musicManager.getPlayer(guild.getGuildId()).getPlayingTrack();
        if (args.length == 0 && currentTrack.getMeta().get("requester").equals(sender.getId())) {
            channel.sendMessage("Skipped your own song!").queue();
            musicManager.getPlayer(guild.getGuildId()).skip();
            if(songMessage)
                editSong(sender, message, channel);
            return;
        }

        if (args.length != 1) {
            if (!channel.getGuild().getMember(sender).getVoiceState().inVoiceChannel() ||
                    channel.getGuild().getMember(sender).getVoiceState().getChannel().getIdLong() != channel.getGuild().getSelfMember().getVoiceState().getChannel().getIdLong()) {
                MessageUtils.sendErrorMessage("You cannot skip if you aren't listening to it!", channel);
                return;
            }
            if (VoteUtil.contains("Skip current song", guild.getGuild()))
                MessageUtils.sendWarningMessage("Their is already a vote to skip current song! Vote with `{%}skip yes | no`", channel, sender);
            else {
                VoteGroup group = new VoteGroup("Skip current song");
                List<User> users = new ArrayList<>();
                for(Member inChannelMember : channel.getGuild().getSelfMember().getVoiceState().getChannel().getMembers()) {
                    if(channel.getGuild().getSelfMember().getUser().getIdLong() != inChannelMember.getUser().getIdLong()) {
                        users.add(inChannelMember.getUser());
                    }
                }
                group.limitUsers(users);
                VoteUtil.sendVoteMessage((vote) -> {
                    if(vote.equals(VoteGroup.Vote.NONE) || vote.equals(VoteGroup.Vote.NO)) {
                        MessageUtils.sendMessage("Results are in: Keep!", channel);
                    } else {
                        MessageUtils.sendMessage("Skipping!", channel);
                        if(songMessage)
                            editSong(sender, message, channel);
                        skips.put(guild.getGuildId(), true);
                    }
                }, group, TimeUnit.MINUTES.toMillis(1), channel, sender, new ButtonGroup.Button("\u23ED", (owner, user, message1) -> {
                    if (getPermissions(channel).hasPermission(channel.getGuild().getMember(user), Permission.SKIP_FORCE)) {
                        musicManager.getPlayer(channel.getGuild().getId()).skip();
                        if(songMessage) {
                            editSong(user, message1, channel);
                        }
                        skips.put(channel.getGuild().getId(), true);
                        VoteUtil.remove("Skip current song", guild.getGuild());
                    } else {
                        channel.sendMessage("You are missing the permission `" + Permission.SKIP_FORCE + "` which is required for use of this button!")
                                .queue();
                    }
                }));
            }
        } else {
            if (args[0].equalsIgnoreCase("force")) {
                if (getPermissions(channel).hasPermission(member, Permission.SKIP_FORCE)) {
                    musicManager.getPlayer(channel.getGuild().getId()).skip();
                    if(songMessage)
                        editSong(sender, message, channel);
                    if(VoteUtil.contains("Skip current song", guild.getGuild()))
                        VoteUtil.remove("Skip current song", guild.getGuild());
                    skips.put(channel.getGuild().getId(), true);
                } else {
                    channel.sendMessage("You are missing the permission `" + Permission.SKIP_FORCE + "` which is required for use of this command!")
                            .queue();
                }
                return;
            } else if (args[0].equalsIgnoreCase("cancel")) {

                if (getPermissions(channel).hasPermission(member, Permission.SKIP_CANCEL)) {
                    skips.put(channel.getGuild().getId(), true);
                } else {
                    channel.sendMessage("You are missing the permission `" + Permission.SKIP_CANCEL + "` which is required for use of this command!")
                            .queue();
                }
                return;
            }
            if (!channel.getGuild().getMember(sender).getVoiceState().inVoiceChannel() ||
                    channel.getGuild().getMember(sender).getVoiceState().getChannel().getIdLong() != channel.getGuild().getSelfMember().getVoiceState().getChannel().getIdLong()) {
                MessageUtils.sendWarningMessage("You cannot vote to skip if you aren't listening to it!", channel);
                return;
            }
            VoteGroup.Vote vote = VoteGroup.Vote.parseVote(args[0]);
            if (vote != null) {
                if(!VoteUtil.contains("Skip current song", guild.getGuild()))
                    MessageUtils.sendWarningMessage("We don't have a vote running!", channel, sender);
                else
                    VoteUtil.getVoteGroup("Skip current song", guild.getGuild()).addVote(vote, sender);
            } else
                MessageUtils.sendUsage(this, channel, sender, args);
        }
    }

    public void editSong(User sender, Message message, TextChannel channel) {
        Track track = FlareBot.instance().getMusicManager().getPlayer(channel.getGuild().getId()).getPlayingTrack();
        if(track == null)
            return;
        EmbedBuilder eb = MessageUtils.getEmbed(sender)
                .addField("Current Song", getLink(track), false)
                .setThumbnail("https://img.youtube.com/vi/" + track.getTrack().getIdentifier() + "/hqdefault.jpg");
        if (track.getTrack().getInfo().isStream)
            eb.addField("Amount Played", "Issa livestream ;)", false);
        else
            eb.addField("Amount Played", GeneralUtils.getProgressBar(track), true)
                    .addField("Time", String.format("%s / %s", FormatUtils.formatDuration(track.getTrack().getPosition()),
                            FormatUtils.formatDuration(track.getTrack().getDuration())), false);
        message.editMessage(eb.build()).queue();
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
    public Permission getPermission() {
        return Permission.SKIP_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

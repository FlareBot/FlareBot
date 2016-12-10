package com.bwfcwalshy.flarebot.commands.general;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

public class JoinCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(!sender.getConnectedVoiceChannels().isEmpty()){
            IVoiceChannel voiceChannel = sender.getConnectedVoiceChannels().get(0);
            if(!FlareBot.getInstance().getMusicManager().hasPlayer(channel.getGuild().getID())){
                Player player = FlareBot.getInstance().getMusicManager().getPlayer(voiceChannel.getGuild().getID());
                player.addEventListener(new AudioEventAdapter() {
                    @Override
                    public void onTrackEnd(AudioPlayer aplayer, AudioTrack track, AudioTrackEndReason endReason) {
                        if(player.getPlaylist().isEmpty())
                            voiceChannel.leave();
                    }
                });
            } else FlareBot.getInstance().getMusicManager().getPlayer(voiceChannel.getGuild().getID()).setPaused(false);
            try {
                voiceChannel.join();
            } catch (MissingPermissionsException e) {
                MessageUtils.sendMessage(channel, sender.mention() + " I cannot join that voice channel!");
            }
        }
    }

    @Override
    public String getCommand() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Tell me to join your voice channel.";
    }

    @Override
    public CommandType getType() { return CommandType.GENERAL; }
}

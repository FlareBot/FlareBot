package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class LeaveCommand implements Command {

    private IDiscordClient client;

    public LeaveCommand(){
        client = FlareBot.getInstance().getClient();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        client.getConnectedVoiceChannels().stream()
                .filter((IVoiceChannel voiceChannel) -> voiceChannel.getGuild().equals(message.getGuild()))
                .findFirst().ifPresent(IVoiceChannel::leave);
        FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getID()).setPaused(true);
    }

    @Override
    public String getCommand() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Tell me to leave the voice channel.";
    }

    @Override
    public CommandType getType() { return CommandType.GENERAL; }
}

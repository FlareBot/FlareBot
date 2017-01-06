package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class PlayCommand implements Command {

    private PlayerManager musicManager;

    public PlayCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length > 0){
            MessageUtils.sendErrorMessage(MessageUtils.getEmbed().withDesc("To search for a song by term or URL do "
                    + FlareBot.getPrefixes().get(channel.getGuild().getID()) + "search <tern/URL>"), channel);
        }else
            musicManager.getPlayer(channel.getGuild().getID()).play();
    }

    @Override
    public String getCommand() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Resumes your song and playlist.";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"resume"};
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

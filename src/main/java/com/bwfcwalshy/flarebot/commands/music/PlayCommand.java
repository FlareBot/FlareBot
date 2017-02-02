package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class PlayCommand implements Command {

    private PlayerManager musicManager;

    public PlayCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length > 0) {
            MessageUtils.sendErrorMessage(MessageUtils.getEmbed().setDescription("To search for a song by term or URL do "
                    + FlareBot.getPrefixes().get(channel.getGuild().getId()) + "search <tern/URL>"), channel);
        } else
            musicManager.getPlayer(channel.getGuild().getId()).play();
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

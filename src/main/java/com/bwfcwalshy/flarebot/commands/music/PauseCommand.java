package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class PauseCommand implements Command {

    private PlayerManager musicManager;

    public PauseCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        musicManager.getPlayer(channel.getGuild().getId()).setPaused(true);
    }

    @Override
    public String getCommand() {
        return "pause";
    }

    @Override
    public String getDescription() {
        return "Pause your song. Opposite of play";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class ShuffleCommand implements Command {

    private PlayerManager musicManager;

    public ShuffleCommand(FlareBot bot) {
        this.musicManager = bot.getMusicManager();
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        this.musicManager.getPlayer(channel.getGuild().getId()).shuffle();
    }

    @Override
    public String getCommand() {
        return "shuffle";
    }

    @Override
    public String getDescription() {
        return "Shuffle up the order of the songs";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}

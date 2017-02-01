package com.bwfcwalshy.flarebot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class LoopCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        Player player = FlareBot.getInstance().getMusicManager().getPlayer(channel.getGuild().getID());
        if (!player.getLooping()) {
            player.setLooping(true);
            MessageUtils.sendMessage("Looping: **ON**", channel);
        } else {
            player.setLooping(false);
            MessageUtils.sendMessage("Looping: **OFF**", channel);
        }
    }

    @Override
    public String getCommand() {
        return "loop";
    }

    @Override
    public String getDescription() {
        return "Toggles looping of the current playlist";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

    @Override
    public String getPermission() {
        return "flarebot.loop";
    }
}

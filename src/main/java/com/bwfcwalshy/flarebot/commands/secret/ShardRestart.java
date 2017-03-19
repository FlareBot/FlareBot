package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

public class ShardRestart implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (getPermissions(channel).isCreator(sender)) {
            int shard = Integer.parseInt(args[0]);
            try {
                FlareBot.getInstance().getClients()[shard] = new JDABuilder(AccountType.BOT)
                                .addListener(FlareBot.getInstance().getEvents())
                                .useSharding(shard, FlareBot.getInstance().getClients().length)
                                .setToken(FlareBot.getToken())
                                .setAudioSendFactory(new NativeAudioSendFactory())
                                .buildAsync();
            } catch (LoginException | RateLimitedException e) {
                MessageUtils.sendException("", e, channel);
            }
        }
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CommandType getType() {
        return null;
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}

package com.bwfcwalshy.flarebot.commands.automod;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.mod.StrikeCounter;
import com.bwfcwalshy.flarebot.util.HelpFormatter;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class ModlogCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length == 1 && args[0].matches("<#\\d+>")){
            TextChannel t = channel.getGuild().getTextChannelById(args[0].replaceAll("[^0-9]", ""));
            StrikeCounter.setChannel(t, channel.getGuild());
            channel.sendMessage(String.format("Set the modlog channel to %s!", t.getAsMention())).queue();
        } else channel.sendMessage(HelpFormatter.on(channel, "Usage: `%pmodlog #channel`")).queue();
    }

    @Override
    public String getCommand() {
        return "modlog";
    }

    @Override
    public String getDescription() {
        return "Sets the modlog channel.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

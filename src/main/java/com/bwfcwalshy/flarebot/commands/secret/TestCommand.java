package com.bwfcwalshy.flarebot.commands.secret;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.commands.FlareBotManager;
import com.bwfcwalshy.flarebot.mod.AutoModGuild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class TestCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        AutoModGuild guild = FlareBotManager.getInstance().getAutoModGuild(channel.getGuild().getId());
        sender.openPrivateChannel().complete().sendMessage(FlareBotManager.GSON.toJson(guild)).queue();
    }

    @Override
    public String getCommand() {
        return "test";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }
}

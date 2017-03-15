package com.bwfcwalshy.flarebot.commands;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.permissions.PerGuildPermissions;
import net.dv8tion.jda.core.entities.*;

public interface Command {

    void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member);

    String getCommand();

    String getDescription();

    CommandType getType();

    default String getPermission() {
        return "flarebot." + getCommand();
    }

    default String[] getAliases() {
        return new String[]{};
    }

    default PerGuildPermissions getPermissions(MessageChannel chan) {
        return FlareBot.getInstance().getPermissions(chan);
    }

    default boolean isDefaultPermission() {
        return true;
    }

    default boolean deleteMessage() {
        return true;
    }
}

package com.bwfcwalshy.flarebot.commands;

import com.bwfcwalshy.flarebot.FlareBot;

import java.util.List;

public enum CommandType {

    GENERAL,
    MODERATION,
    MUSIC,
    HIDDEN;

    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public static CommandType[] getTypes() {
        return new CommandType[]{GENERAL, MODERATION, MUSIC};
    }

    public List<Command> getCommands() {
        return FlareBot.getInstance().getCommandsByType(this);
    }

    public String formattedName() {
        return toString();
    }
}

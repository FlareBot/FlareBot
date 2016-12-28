package com.bwfcwalshy.flarebot.commands;

import com.bwfcwalshy.flarebot.FlareBot;

import java.util.List;

public enum CommandType {

    GENERAL,
    ADMINISTRATIVE(false),
    MUSIC(false),
    HIDDEN,
    OWNER;

    private boolean dms;

    CommandType(){
        this(true);
    }

    CommandType(boolean dms) {
        this.dms = dms;
    }

    public String toString(){
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public static CommandType[] getTypes(){
        return new CommandType[] {GENERAL, ADMINISTRATIVE, MUSIC};
    }

    public List<Command> getCommands() {
        return FlareBot.getInstance().getCommandsByType(this);
    }

    public boolean usableInDMs() {
        return dms;
    }

    public String formattedName() {
        return Character.toUpperCase(name().charAt(0)) + name().substring(1).toLowerCase();
    }
}

package com.bwfcwalshy.flarebot.commands;

public enum CommandType {

    GENERAL,
    ADMINISTRATIVE,
    MUSIC,
    HIDDEN;

    public String toString(){
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public static CommandType[] getTypes(){
        return new CommandType[] {GENERAL, ADMINISTRATIVE, MUSIC};
    }
}

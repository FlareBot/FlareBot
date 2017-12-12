package stream.flarebot.flarebot.commands;

import stream.flarebot.flarebot.FlareBot;

import java.util.List;

public enum CommandType {

    GENERAL,
    MODERATION,
    MUSIC,
    INTERNAL,
    USEFUL,
    CURRENCY,
    RANDOM,
    SECRET;

    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public static CommandType[] getTypes() {
        return new CommandType[]{GENERAL, MODERATION, MUSIC};
    }

    public List<Command> getCommands() {
        return FlareBot.getInstance().getCommandsByType(this);
    }
}

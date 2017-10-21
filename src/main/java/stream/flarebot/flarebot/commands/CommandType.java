package stream.flarebot.flarebot.commands;

import stream.flarebot.flarebot.FlareBot;

import java.util.List;

public enum CommandType {

    GENERAL,
    MUSIC,
    CONFIGURATION,
    MODERATION,
    SECRET;

    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public static CommandType[] getTypes() {
        return new CommandType[]{GENERAL, MUSIC, CONFIGURATION, MODERATION};
    }

    public List<Command> getCommands() {
        return FlareBot.getInstance().getCommandsByType(this);
    }
}

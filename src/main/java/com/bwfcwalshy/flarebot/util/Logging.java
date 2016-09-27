package com.bwfcwalshy.flarebot.util;

import com.bwfcwalshy.flarebot.FlareBot;
import sx.blah.discord.handle.obj.IDiscordObject;
import sx.blah.discord.handle.obj.IGuild;

public class Logging {

    public static void error(String message, Exception error, IDiscordObject cause){
        FlareBot.LOGGER.error(message + " Cause ID: " + cause.getID() + " Which is: " + cause.getClass().getSimpleName(), error);
    }

    public static void error(String message, Exception error, IGuild guild){
        FlareBot.LOGGER.error(message + "\n" +
                "Guild: " + guild.getName() + " (" + guild.getID() + ")\n", error);
    }
}

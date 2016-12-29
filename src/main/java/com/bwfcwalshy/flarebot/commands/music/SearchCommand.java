package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.music.VideoThread;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class SearchCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 0) {
            MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "search <term>");
        } else if (args.length >= 1) {
            if (args[0].startsWith("http") || args[0].startsWith("www.")) {
                VideoThread.getThread(args[0], channel, sender).start();
            } else {
                // Due to YouTube limits the video length should never be long enough to need a StringBuilder.
                // EDIT BY Arsen: StringBuilder is there too when you look at bytecode. I think. kek
                String term = "";
                for (String s : args) {
                    term += s + " ";
                }
                // Remove that last space. Not a good way to do this but it works for now.
                // EDIT BY Arsen: That's why trim() exists
                term = term.trim();
                VideoThread.getSearchThread(term, channel, sender).start();
            }
        }
    }

    @Override
    public String getCommand() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "Search for a song on YouTube. Usage: `_search URL` or `_search WORDS`";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }

}

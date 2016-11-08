package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.music.VideoThread;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

public class SearchCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (args.length == 0) {
            MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "search <term>");
        } else if (args.length >= 1) {
            if (args[0].startsWith("http") || args[0].startsWith("www.")) {
                if (args[0].matches(VideoThread.ANY_YT_URL)) {
                    new VideoThread(args[0], sender, channel, true, (args[0].startsWith("https://youtu.be/") || args[0].startsWith("http://youtu.be")));
                } else MessageUtils.sendMessage(channel, "Not a YouTube link!");
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
                new VideoThread(term, sender, channel);
            }
            RequestBuffer.request(() -> {
                try {
                    message.delete();
                } catch (MissingPermissionsException e) {
                    // Ignore
                } catch (DiscordException e) {
                    FlareBot.LOGGER.error("Could not erase message!", e);
                }
            });
        }

    }

    @Override
    public String getCommand() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "Search for a song on YouTube.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MUSIC;
    }
}

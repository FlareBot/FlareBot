package com.bwfcwalshy.flarebot.commands.music;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.VideoThread;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class SearchCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length == 0){
            MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "search <term>");
        }else if(args.length >= 1){
            if(args[0].startsWith("http") || args[0].startsWith("www.")){
                if(args[0].contains("youtube.com") || args[0].contains("youtu.be")){
                    new VideoThread(args[0], sender, channel, true, (args[0].startsWith("https://youtu.be/") || args[0].startsWith("http://youtu.be")));
                }
            }else {

                // Due to YouTube limits the video length should never be long enough to need a StringBuilder.
                String term = "";
                for (String s : args) {
                    term += s + " ";
                }
                // Remove that last space. Not a good way to do this but it works for now.
                term = term.substring(0, term.length() - 1);

                new VideoThread(term, sender, channel);
            }
            try {
                message.delete();
            } catch (MissingPermissionsException e) {
                // Ignore
            } catch (RateLimitException e) {
                e.printStackTrace();
            } catch (DiscordException e) {
                e.printStackTrace();
            }
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
    public CommandType getType() { return CommandType.MUSIC; }
}

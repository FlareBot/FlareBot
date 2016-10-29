package com.bwfcwalshy.flarebot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MessageUtils {

    public static IMessage sendMessage(IChannel channel, CharSequence message){
        RequestBuffer.RequestFuture<IMessage> future = RequestBuffer.request(() -> {
            try {
                return channel.sendMessage(message.toString().substring(0, Math.min(message.length(), 1999)));
            } catch (MissingPermissionsException e) {
                FlareBot.LOGGER.error("Something went wrong!", e);
            } catch (DiscordException e) {
                sendMessage(channel, message);
            }
            return null;
        });
        return future.get();
    }

    public static void sendPM(IUser user, String message){
        RequestBuffer.request(() -> {
            try {
                user.getOrCreatePMChannel().sendMessage(message.substring(0, Math.min(message.length(), 1999)));
            } catch (MissingPermissionsException | DiscordException e) {
                FlareBot.LOGGER.error("Uh oh!", e);
            }
        });
    }

    public static String escapeFile(String s){
        return s.replaceAll("[/\\\\*:?\"<>|]", "");
    } // Jesus christ

    public static IMessage sendException(String s, Exception e, IChannel channel) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        pw.close();
        return sendMessage(channel, s + ' ' + trace);
    }

    public static void editMessage(IMessage message, String content){
        RequestBuffer.request(() -> {
            try {
                message.edit(content);
            } catch (MissingPermissionsException | DiscordException e1) {
                FlareBot.LOGGER.error("Edit own message!", e1);
            }
        });
    }
}

package com.bwfcwalshy.flarebot;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.io.ByteArrayInputStream;
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
                return sendMessage(channel, message);
            }
            return null;
        });
        return future.get();
    }

    public static void sendPM(IUser user, CharSequence message){
        RequestBuffer.request(() -> {
            try {
                user.getOrCreatePMChannel().sendMessage(message.toString().substring(0, Math.min(message.length(), 1999)));
            } catch (MissingPermissionsException | DiscordException e) {
                FlareBot.LOGGER.error("Uh oh!", e);
            }
        });
    }

    public static String escapeFile(String s){
        return s.replaceAll("[/\\\\*:?\"<>|]", "");
    } // Jesus christ

    public static IMessage sendException(String s, Throwable e, IChannel channel) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        pw.close();
        if(trace.length() > (1999 - s.length())){
            return sendFile(channel, s, trace, "trace.txt");
        }
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

    public static IMessage sendFile(IChannel channel, String s, String fileContent, String filename) {
        ByteArrayInputStream stream = new ByteArrayInputStream(fileContent.getBytes());
        return RequestBuffer.request(() -> {
            try {
                return channel.sendFile(s, false, stream, filename);
            } catch (MissingPermissionsException e1) {
                FlareBot.LOGGER.error("Could not send stack trace!", e1);
                return null;
            } catch (DiscordException e1) {
                return sendFile(channel, s, fileContent, filename);
            }
        }).get();
    }
}

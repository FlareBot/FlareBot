package com.bwfcwalshy.flarebot;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.*;

import java.io.*;
import java.nio.charset.Charset;

public class MessageUtils {

    private static String[] defaults = {
            "6debd47ed13483642cf09e832ed0bc1b",
            "322c936a8c8be1b803cd94861bdfa868",
            "dd4dbc0016779df1378e7812eabaa04d",
            "0e291f67c9274a1abdddeb3fd919cbaa",
            "1cbd08c76f8af6dddce02c5138971129"
    };

    public static IMessage sendMessage(IChannel channel, CharSequence message) {
        RequestBuffer.RequestFuture<IMessage> future = RequestBuffer.request(() -> {
            try {
                return channel.sendMessage(message.toString().substring(0, Math.min(message.length(), 1999)));
            } catch (DiscordException | MissingPermissionsException e) {
                FlareBot.LOGGER.error("Something went wrong!", e);
            }
            return null;
        });
        return future.get();
    }

    public static void sendPM(IUser user, CharSequence message) {
        RequestBuffer.request(() -> {
            try {
                return user.getOrCreatePMChannel().sendMessage(message.toString().substring(0, Math.min(message.length(), 1999)));
            } catch (MissingPermissionsException | DiscordException e) {
                FlareBot.LOGGER.error("Uh oh!", e);
            }
            return null;
        }).get();
    }

    public static void sendPM(IUser user, EmbedObject message) {
        RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(FlareBot.getInstance().getClient()).withEmbed(message)
                        .withChannel(user.getOrCreatePMChannel()).withContent("\u200B").send();
            } catch (MissingPermissionsException | DiscordException e) {
                FlareBot.LOGGER.error("Uh oh!", e);
            }
            return null;
        }).get();
    }

    public static IMessage sendException(String s, Throwable e, IChannel channel) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        pw.close();
        return sendMessage(channel, s + "\n**Stack trace**: " + hastebin(trace));
    }

    public static String hastebin(String trace) {
        try {
            HttpPost req = new HttpPost("https://hastebin.com/documents");
            req.addHeader("Content-Type", "text/plain");
            req.addHeader("User-Agent", "Mozilla/5.0 FlareBot");
            StringEntity ent = new StringEntity(trace);
            req.setEntity(ent);
            HttpResponse response = FlareBot.HTPP_CLIENT.execute(req);
            if (response.getStatusLine().getStatusCode() != 200) {
                FlareBot.LOGGER.error("HasteBin API did not respond with a correct code! Code was: {}! Response: {}", response.getStatusLine().getStatusCode(),
                        IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset()));
                return null;
            }
            return "https://hastebin.com/" + FlareBot.GSON.fromJson(new InputStreamReader(response.getEntity().getContent()), HastebinResponse.class).key;
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            FlareBot.LOGGER.error("Could not make POST request to hastebin!", e);
            return null;
        }
    }

    public static void editMessage(IMessage message, String content) {
        RequestBuffer.request(() -> {
            try {
                return message.edit(content);
            } catch (MissingPermissionsException | DiscordException e1) {
                FlareBot.LOGGER.error("Could not edit own message!", e1);
            }
            return message;
        }).get();
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

    public static EmbedBuilder getEmbed() {
        return new EmbedBuilder()
                .withAuthorIcon(getAvatar(FlareBot.getInstance().getClient().getOurUser()))
                .withAuthorUrl("https://github.com/ArsenArsen/FlareBot")
                .withAuthorName(getTag(FlareBot.getInstance().getClient().getOurUser()));
    }

    public static String getTag(IUser user) {
        return user.getName() + '#' + user.getDiscriminator();
    }

    public static EmbedBuilder getEmbed(IUser user) {
        return getEmbed().withFooterIcon(getAvatar(user))
                .withFooterText("Requested by @" + getTag(user));
    }

    public static String getAvatar(IUser user) {
        return user.getAvatar() != null ? user.getAvatarURL() : getDefaultAvatar(user);
    }

    public static String getDefaultAvatar(IUser user) {
        int discrim = Integer.parseInt(user.getDiscriminator());
        discrim %= defaults.length;
        return "https://discordapp.com/assets/" + defaults[discrim] + ".png";
    }

    public static IMessage sendMessage(EmbedObject embedObject, IChannel channel) {
        RequestBuffer.RequestFuture<IMessage> future = RequestBuffer.request(() -> {
            try {
                return new MessageBuilder(FlareBot.getInstance().getClient()).withEmbed(embedObject)
                        .withChannel(channel).withContent("\u200B").send();
            } catch (DiscordException | MissingPermissionsException e) {
                FlareBot.LOGGER.error("Something went wrong!", e);
            }
            return null;
        });
        return future.get();
    }

    public static void editMessage(EmbedObject embed, IMessage message) {
        editMessage(message.getContent(), embed, message);
    }

    public static void editMessage(String s, EmbedObject embed, IMessage message) {
        RequestBuffer.request(() -> {
            try {
                message.edit(s, embed);
            } catch (MissingPermissionsException | DiscordException e) {
                FlareBot.LOGGER.error("Could not edit own message + embed!", e);
            }
        });

    }

    private static class HastebinResponse {
        public String key;
    }
}

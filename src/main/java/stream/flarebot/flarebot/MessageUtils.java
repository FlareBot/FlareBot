package stream.flarebot.flarebot;

import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.scheduler.FlarebotTask;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final String DEBUG_CHANNEL = "226786557862871040";
    private static final Pattern INVITE_REGEX = Pattern.compile("(?:https?://)?discord(?:app\\.com/invite|\\.gg)/(\\S+?)");
    private static final Pattern LINK_REGEX = Pattern.compile("((http(s)?://)?(www\\.)?)[a-zA-Z0-9-]+\\.[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?/?(.+)?");
    private static final Pattern YOUTUBE_LINK_REGEX = Pattern.compile("(http(s)?://)?(www\\.)?youtu(be\\.com)?(\\.be)?/(watch\\?v=)?[a-zA-Z0-9-_]+");

    public static <T> Consumer<T> noOpConsumer() {
        return t -> {
        };
    }

    public static int getLength(EmbedBuilder embed) {
        int len = 0;
        MessageEmbed e = embed.build();
        if (e.getTitle() != null)
            len += e.getTitle().length();
        if (e.getDescription() != null)
            len += e.getDescription().length();
        if (e.getAuthor() != null)
            len += e.getAuthor().getName().length();
        if (e.getFooter() != null)
            len += e.getFooter().getText().length();
        if (e.getFields() != null) {
            for (MessageEmbed.Field f : e.getFields()) {
                len += f.getName().length() + f.getValue().length();
            }
        }
        return len;
    }

    public static Message sendPM(User user, CharSequence message) {
        return user.openPrivateChannel().complete().sendMessage(message.toString().substring(0, Math.min(message
                .length(), 1999))).complete();
    }

    public static Message sendPM(User user, EmbedBuilder message) {
        return user.openPrivateChannel().complete().sendMessage(new MessageBuilder().setEmbed(message.build())
                .append("\u200B")
                .build()).complete();
    }

    public static Message sendException(String s, Throwable e, MessageChannel channel) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        pw.close();
        return sendErrorMessage(getEmbed().setDescription(s + "\n**Stack trace**: " + hastebin(trace)), channel);
    }

    public static String hastebin(String trace) {
        try {
            return "https://hastebin.com/" + Unirest.post("https://hastebin.com/documents")
                    .header("User-Agent", "Mozilla/5.0 FlareBot")
                    .header("Content-Type", "text/plain")
                    .body(trace)
                    .asJson()
                    .getBody()
                    .getObject().getString("key");
        } catch (UnirestException e) {
            FlareBot.LOGGER.error(Markers.NO_ANNOUNCE, "Could not make POST request to hastebin!", e);
            return null;
        }
    }

    public static void editMessage(Message message, String content) {
        message.editMessage(content).queue();
    }

    public static Message sendFile(MessageChannel channel, String s, String fileContent, String filename) {
        ByteArrayInputStream stream = new ByteArrayInputStream(fileContent.getBytes());
        return channel.sendFile(stream, filename, new MessageBuilder().append(s).build()).complete();
    }

    public static EmbedBuilder getEmbed() {
        return new EmbedBuilder()
                .setAuthor("FlareBot",
                        "https://github.com/FlareBot/FlareBot",
                        FlareBot.getInstance().getClients()[0].getSelfUser().getEffectiveAvatarUrl());
    }

    public static String getTag(User user) {
        return user.getName() + '#' + user.getDiscriminator();
    }

    public static EmbedBuilder getEmbed(User user) {
        return getEmbed().setFooter("Requested by @" + getTag(user), user.getEffectiveAvatarUrl());
    }

    public static String getAvatar(User user) {
        return user.getEffectiveAvatarUrl();
    }

    public static String getDefaultAvatar(User user) {
        return user.getDefaultAvatarUrl();
    }

    public static Message sendErrorMessage(EmbedBuilder builder, MessageChannel channel) {
        return channel.sendMessage(builder.setColor(Color.red).build()).complete();
    }

    public static Message sendErrorMessage(String message, MessageChannel channel) {
        return channel.sendMessage(MessageUtils.getEmbed().setColor(Color.red).setDescription(message).build())
                .complete();
    }

    public static void editMessage(EmbedBuilder embed, Message message) {
        editMessage(message.getRawContent(), embed, message);
    }

    public static void editMessage(String s, EmbedBuilder embed, Message message) {
        if (message != null)
            message.editMessage(new MessageBuilder().append(s).setEmbed(embed.build()).build()).queue();
    }

    public static boolean hasInvite(Message message) {
        return INVITE_REGEX.matcher(message.getRawContent()).find();
    }

    public static boolean hasInvite(String message) {
        return INVITE_REGEX.matcher(message).find();
    }

    public static boolean hasLink(Message message) {
        return LINK_REGEX.matcher(message.getRawContent()).find();
    }

    public static boolean hasLink(String message) {
        return LINK_REGEX.matcher(message).find();
    }

    public static boolean hasYouTubeLink(Message message) {
        return YOUTUBE_LINK_REGEX.matcher(message.getRawContent()).find();
    }

    public static void sendAutoDeletedMessage(MessageEmbed messageEmbed, long delay, MessageChannel channel) {
        sendAutoDeletedMessage(new MessageBuilder().setEmbed(messageEmbed).build(), delay, channel);
    }

    public static void sendAutoDeletedMessage(Message message, long delay, MessageChannel channel) {
        Message msg = channel.sendMessage(message).complete();
        new FlarebotTask("AutoDeleteTask") {
            @Override
            public void run() {
                msg.delete().queue();
            }
        }.delay(delay);
    }

    public static void sendUsage(Command command, TextChannel channel){
        channel.sendMessage(new EmbedBuilder().setTitle(command.getCommand() + " Usage", null).addField("Usage", "Insert", false)
            .addField("Permission", command.getPermission() + "\nDefault permission: " + command.isDefaultPermission(), false).setColor(Color.red).build()).queue();
    }
}

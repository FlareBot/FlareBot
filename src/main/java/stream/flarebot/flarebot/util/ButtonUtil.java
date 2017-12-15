package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import stream.flarebot.flarebot.util.objects.ButtonGroup;

import java.util.HashMap;
import java.util.Map;

public class ButtonUtil {
    private static Map<String, ButtonGroup> buttonMessages = new HashMap<>();

    public static void sendButtonedMessage(TextChannel channel, MessageEmbed embed, ButtonGroup buttons) {

        channel.sendMessage(embed).queue(message -> {
            if (!channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)) {
                MessageUtils.sendErrorMessage("We don't have permission to add reactions to messages so buttons have been disabled", channel);
                return;
            }
            if (!channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                MessageUtils.sendErrorMessage("We don't have permission to manage reactions so you won't be getting the best experience with buttons", channel);
            }
            for (String emote : buttons.getButtonEmotes()) {
                message.addReaction(emote).queue();
            }
            buttonMessages.put(message.getId(), buttons);
        });
    }

    public static void sendButtonedMessage(TextChannel channel, String text, ButtonGroup buttons) {
        channel.sendMessage(text).queue(message -> {
            if (!channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)) {
                MessageUtils.sendErrorMessage("We don't have permission to add reactions to messages so buttons have been disabled", channel);
                return;
            }
            if (!channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                MessageUtils.sendErrorMessage("We don't have permission to manage reactions so you won't be getting the best experience with buttons", channel);
            }
            for (String emote : buttons.getButtonEmotes()) {
                message.addReaction(emote).queue();
            }
            buttonMessages.put(message.getId(), buttons);
        });
    }

    public static Map<String, ButtonGroup> getButtonMessages() {
        return buttonMessages;
    }

    public static boolean isButtonMessage(String id) {
        return buttonMessages.containsKey(id);
    }

    public static ButtonGroup getButtonGroup(String id) {
        return buttonMessages.get(id);
    }
}

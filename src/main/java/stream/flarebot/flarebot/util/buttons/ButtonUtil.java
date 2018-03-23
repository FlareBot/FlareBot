package stream.flarebot.flarebot.util.buttons;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.objects.ButtonGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ButtonUtil {

    private static Map<String, ButtonGroup> buttonMessages = new ConcurrentHashMap<>();

    /**
     * Sends an embed button message with a set of buttons.
     *
     * @param channel The TextChannel to send it to.
     * @param embed   The embed to send.
     * @param buttons The buttons to display.
     */
    public static void sendButtonedMessage(TextChannel channel, MessageEmbed embed, ButtonGroup buttons) {
        channel.sendMessage(embed).queue(message -> handleSuccessConsumer(channel, message, buttons));
    }

    /**
     * Sends an embed button message with a set of buttons, and returns the message.
     *
     * @return The message we sent to discord.
     *
     * @param channel The {@link TextChannel} to send it to.
     * @param embed   The {@link MessageEmbed} to send.
     * @param buttons The buttons to display.
     */
    public static Message sendReturnedButtonedMessage(TextChannel channel, MessageEmbed embed, ButtonGroup buttons) {
        Message message = channel.sendMessage(embed).complete();
        handleSuccessConsumer(channel, message, buttons);
        return message;
    }

    /**
     * Sends a string message with a set of buttons.
     *
     * @param channel The {@link TextChannel} to send it to.
     * @param text    The message to send.
     * @param buttons The buttons to display.
     */
    public static void sendButtonedMessage(TextChannel channel, String text, ButtonGroup buttons) {
        channel.sendMessage(text).queue(message -> handleSuccessConsumer(channel, message, buttons));
    }

    private static void handleSuccessConsumer(TextChannel channel, Message message, ButtonGroup buttonGroup) {
        if (!channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_ADD_REACTION)) {
            MessageUtils.sendErrorMessage("We don't have permission to add reactions to messages so buttons have been " +
                    "disabled", channel);
            return;
        }
        if (!channel.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            MessageUtils.sendErrorMessage("We don't have permission to manage reactions so you won't be getting the best experience with buttons", channel);
        }
        for (ButtonGroup.Button button : buttonGroup.getButtons()) {
            button.addReaction(message);
        }
        buttonMessages.put(message.getId(), buttonGroup);
    }

    /**
     * Gets all the button Messages along with their buttons.
     *
     * @return A map containing what message id corresponds to what buttons.
     */
    public static Map<String, ButtonGroup> getButtonMessages() {
        return buttonMessages;
    }

    /**
     * Gets weather or not the message has buttons.
     *
     * @param id The Message id.
     * @return If the message has buttons.
     */
    public static boolean isButtonMessage(String id) {
        return buttonMessages.containsKey(id);
    }

    /**
     * Gets the buttons for the specified message.
     *
     * @param id The Message id.
     * @return The ButtonGroup for that message.
     */
    public static ButtonGroup getButtonGroup(String id) {
        return buttonMessages.get(id);
    }
}

package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.Color;

public class PaginationUtil {

    /**
     *
     *
     * @param channel
     * @param fullContent
     * @param method
     * @param amount
     */
    public static void sendPagedMessage(TextChannel channel, String fullContent, SplitMethod method, int amount) {

    }

    /**
     *
     *
     * @param channel
     * @param fullContent
     * @param method
     * @param amount
     * @param reactions
     */
    public static void sendPagedMessage(TextChannel channel, String fullContent, SplitMethod method, int amount,
                                        boolean reactions) {

    }

    /**
     *
     *
     * @param channel
     * @param fullContent
     * @param splitMethod
     * @param splitAmount
     * @param currentPage
     * @param reactions
     */
    public static void sendPagedMessage(TextChannel channel, String fullContent, SplitMethod splitMethod, int splitAmount,
                                        int currentPage, boolean reactions) {
        int maxPages = 1;
        String subContent = fullContent;
        if(splitMethod == SplitMethod.CHAR_COUNT) {
            maxPages = Math.max((int) Math.ceil((double) fullContent.length() / splitAmount), 1);
            // TODO: Take into account new lines so it doesn't get cut half way
            subContent = fullContent.substring((currentPage-1) * splitAmount, Math.min(currentPage * splitAmount,
                    fullContent.length())).substring(0, fullContent.lastIndexOf("\n"));
        }

        channel.sendMessage(new EmbedBuilder().setColor(Color.ORANGE).setDescription("```\n" + subContent + "```\n\n**Page "
                + currentPage + "/" + maxPages + "**").build()).queue(message -> {
                    if(reactions) {
                        message.addReaction("\u23EE").queue(); // :track_previous: ⏮
                        message.addReaction("\u23EA").queue(); // :rewind: ⏪
                        message.addReaction("\u23E9").queue(); // :fast_forward: ⏩
                        message.addReaction("\u23ED").queue(); // :track_next: ⏭
                    }
        });
    }

    /**
     * This is a sub-enum used to determine how the content will be split and displayed in pages.
     */
    public enum SplitMethod {
        CHAR_COUNT,
        NEW_LINES,
        TABLE_ROW_COUNT
    }
}

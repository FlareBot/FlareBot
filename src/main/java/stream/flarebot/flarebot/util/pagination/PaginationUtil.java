package stream.flarebot.flarebot.util.pagination;

import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.ArrayUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.buttons.ButtonUtil;
import stream.flarebot.flarebot.util.objects.ButtonGroup;

import java.util.ArrayList;
import java.util.List;

public class PaginationUtil {

    /**
     * Splits a string into a {@link PaginationList}
     *
     * @param content     The string to split
     * @param splitMethod the method by witch to split
     * @param splitAmount The amount to split
     * @return {@link PaginationList}
     */
    public static PaginationList<String> splitStringToList(String content, SplitMethod splitMethod, int splitAmount) {
        List<String> pages = new ArrayList<>();
        if (splitMethod == SplitMethod.CHAR_COUNT) {
            int pagesCount = Math.max((int) Math.ceil((double) content.length() / splitAmount), 1);
            String workingString = content;
            for (int i = 0; i < pagesCount; i++) {
                String substring = workingString.substring(0, Math.min(splitAmount, workingString.length()));
                int splitIndex = substring.lastIndexOf("\n") == -1 ? substring.length() : substring.lastIndexOf("\n");
                pages.add(substring.substring(0, splitIndex));
                if(i != (pagesCount - 1)) {
                    workingString = workingString.substring(splitIndex + 1, workingString.length());
                }
            }

        } else if (splitMethod == SplitMethod.NEW_LINES) {
            String[] lines = content.split("\n");
            int pagesCount = Math.max((int) Math.ceil((double) lines.length / splitAmount), 1);
            for (int i = 0; i < pagesCount; i++) {
                String[] page = ArrayUtils.subarray(lines, splitAmount * i, (splitAmount * i) + splitAmount);
                StringBuilder sb = new StringBuilder();
                for (String line : page) {
                    sb.append(line).append("\n");
                }
                pages.add(sb.toString());
            }
        }
        return new PaginationList<>(pages);
    }

    /**
     * Sends a paged message
     *
     * @param textChannel The channel to send it to
     * @param list        The {@link PaginationList} to use
     * @param page        The starting page
     */
    public static void sendPagedMessage(TextChannel textChannel, PaginationList list, int page) {
        if(page < 0 || page > list.getPages() - 1) {
            MessageUtils.sendErrorMessage("Invalid page: " + (page + 1) + " Total Pages: " + list.getPages(), textChannel);
            return;
        }
        Integer[] pages = new Integer[]{page};
        if(list.getPages() > 1) {
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.addButton(new ButtonGroup.Button("\u23EE", (user, message) -> {
                //Start
                pages[0] = 0;
                message.editMessage(list.getPage(pages[0])).queue();
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23EA", (user, message) -> {
                //Prev
                if (pages[0] != 0) {
                    pages[0] -= 1;
                    message.editMessage(list.getPage(pages[0])).queue();
                }
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23E9", (user, message) -> {
                //Next
                if (pages[0] + 1 != list.getPages()) {
                    pages[0] += 1;
                    message.editMessage(list.getPage(pages[0])).queue();
                }
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23ED", (user, message) -> {
                //Last
                pages[0] = list.getPages() - 1;
                message.editMessage(list.getPage(pages[0])).queue();
            }));
            ButtonUtil.sendButtonedMessage(textChannel, list.getPage(page), buttonGroup);
        } else {
            textChannel.sendMessage(list.getPage(page)).queue();
        }
    }

    /**
     * Sends an embed paged message the to specified channel.
     * You can build with Embed with {@link PagedEmbedBuilder}.
     *
     * @param pagedEmbed The {@link stream.flarebot.flarebot.util.pagination.PagedEmbedBuilder.PagedEmbed} to use.
     * @param page The page to start on.
     * @param channel The channel to send the paged message to.
     */
    public static void sendEmbedPagedMessage(PagedEmbedBuilder.PagedEmbed pagedEmbed, int page, TextChannel channel) {
        if(page < 0 || page > pagedEmbed.getPageTotal() - 1) {
            MessageUtils.sendErrorMessage("Invalid page: " + (page + 1) + " Total Pages: " + pagedEmbed.getPageTotal(), channel);
            return;
        }
        if (!pagedEmbed.isSinglePage()) {
            ButtonGroup buttonGroup = new ButtonGroup();
            Integer[] pages = new Integer[]{page};
            buttonGroup.addButton(new ButtonGroup.Button("\u23EE", (user, message) -> {
                //Start
                pages[0] = 0;
                message.editMessage(pagedEmbed.getEmbed(pages[0])).queue();
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23EA", (user, message) -> {
                //Prev
                if (pages[0] != 0) {
                    pages[0] -= 1;
                    message.editMessage(pagedEmbed.getEmbed(pages[0])).queue();
                }
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23E9", (user, message) -> {
                //Next
                if (pages[0] + 1 != pagedEmbed.getPageTotal()) {
                    pages[0] += 1;
                    message.editMessage(pagedEmbed.getEmbed(pages[0])).queue();
                }
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23ED", (user, message) -> {
                //Last
                pages[0] = pagedEmbed.getPageTotal() - 1;
                message.editMessage(pagedEmbed.getEmbed(pages[0])).queue();
            }));
            ButtonUtil.sendButtonedMessage(channel, pagedEmbed.getEmbed(page), buttonGroup);
        } else {
            channel.sendMessage(pagedEmbed.getEmbed(page)).queue();
        }
    }

    /**
     * This is a sub-enum used to determine how the content will be split and displayed in pages.
     */
    public enum SplitMethod {
        CHAR_COUNT,
        NEW_LINES
    }
}

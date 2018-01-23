package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.ArrayUtils;
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
     * Sends a paged message in an embed.
     * This will automatically handle page counts for you as well.
     *
     * @param textChannel The channel to send it to.
     * @param list        The {@link PaginationList} to use.
     * @param page        The starting page.
     * @param codeBlock   Weather or not to wrap the data in a code block.
     * @param title       The title to give the embed.
     */
    public static void sendEmbedPagedMessage(TextChannel textChannel, PaginationList<String> list, int page, boolean codeBlock, String title) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.addField("Info", (codeBlock ? "```" : "") + list.getPage(page) + (codeBlock ? "```" : ""), false);
        if(list.getPages() > 1) {
            embedBuilder.addField("Page", String.valueOf(page + 1), true);
            embedBuilder.addField("Total Pages", String.valueOf(list.getPages()), true);
            ButtonGroup buttonGroup = new ButtonGroup();
            Integer[] pages = new Integer[]{page};
            buttonGroup.addButton(new ButtonGroup.Button("\u23EE", (user, message) -> {
                //Start
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(title);
                builder.addField("Info", (codeBlock ? "```" : "") + list.getPage(0) + (codeBlock ? "```" : ""), false);
                builder.addField("Page", String.valueOf(1), true);
                builder.addField("Total Pages", String.valueOf(list.getPages()), true);
                pages[0] = 0;
                message.editMessage(builder.build()).queue();
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23EA", (user, message) -> {
                //Prev
                if (pages[0] != 0) {
                    pages[0] -= 1;
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle(title);
                    builder.addField("Info", (codeBlock ? "```" : "") + list.getPage(pages[0]) + (codeBlock ? "```" : ""), false);
                    builder.addField("Page", String.valueOf(pages[0] + 1), true);
                    builder.addField("Total Pages", String.valueOf(list.getPages()), true);
                    message.editMessage(builder.build()).queue();
                }
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23E9", (user, message) -> {
                //Next
                if (pages[0] + 1 != list.getPages()) {
                    pages[0] += 1;
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle(title);
                    builder.addField("Info", (codeBlock ? "```" : "") + list.getPage(pages[0]) + (codeBlock ? "```" : ""), false);
                    builder.addField("Page", String.valueOf(pages[0] + 1), true);
                    builder.addField("Total Pages", String.valueOf(list.getPages()), true);
                    message.editMessage(builder.build()).queue();
                }
            }));
            buttonGroup.addButton(new ButtonGroup.Button("\u23ED", (user, message) -> {
                //Last
                pages[0] = list.getPages() - 1;
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle(title);
                builder.addField("Info", (codeBlock ? "```" : "") + list.getPage(pages[0]) + (codeBlock ? "```" : ""), false);
                builder.addField("Page", String.valueOf(pages[0] + 1), true);
                builder.addField("Total Pages", String.valueOf(list.getPages()), true);
                message.editMessage(builder.build()).queue();
            }));
            ButtonUtil.sendButtonedMessage(textChannel, embedBuilder.build(), buttonGroup);
        } else {
            textChannel.sendMessage(embedBuilder.build()).queue();
        }
    }

    /**
     * Builds a {@link PaginationList} as a table
     *
     * @param headers The column headers
     * @param table   The table it's self
     * @param rows    The number of rows that should be on each page
     * @return {@link PaginationList}
     */
    public static PaginationList<String> buildPagedTable(List<String> headers, List<List<String>> table, int rows) {
        int padding = 1;
        int[] widths = new int[headers.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = 0;
        }
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).length() > widths[i]) {
                widths[i] = headers.get(i).length();
            }
        }
        for (List<String> row : table) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                if (cell.length() > widths[i]) {
                    widths[i] = cell.length();
                }
            }
        }
        StringBuilder formatLine = new StringBuilder("|");
        for (int width : widths) {
            formatLine.append(" %-").append(width).append("s |");
        }
        formatLine.append("\n");
        String headerString = String.format(formatLine.toString(), headers.toArray());
        StringBuilder bodyBuilder = new StringBuilder();
        for (List<String> row : table) {
            bodyBuilder.append(String.format(formatLine.toString(), row.toArray()));
        }
        String bodyString = bodyBuilder.toString();
        List<String> pages = new ArrayList<>();
        String[] lines = bodyString.split("\n");
        int pagesCount = Math.max((int) Math.ceil((double) lines.length / rows), 1);
        for (int i = 0; i < pagesCount; i++) {
            String[] page = ArrayUtils.subarray(lines, rows * i, (rows * i) + rows);
            StringBuilder sb = new StringBuilder();
            sb.append("```\n");
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append(headerString);
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            for (String line : page) {
                sb.append(line).append("\n");
            }
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append(MessageUtils.getFooter("Page: " + (i + 1) + "/" + pagesCount, padding, widths));
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append("\n```");
            pages.add(sb.toString());
        }
        return new PaginationList<>(pages);
    }

    /**
     * This is a sub-enum used to determine how the content will be split and displayed in pages.
     */
    public enum SplitMethod {
        CHAR_COUNT,
        NEW_LINES
    }
}

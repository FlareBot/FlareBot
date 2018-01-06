package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.ArrayUtils;
import stream.flarebot.flarebot.util.buttons.ButtonUtil;
import stream.flarebot.flarebot.util.objects.ButtonGroup;

import java.util.ArrayList;
import java.util.List;

public class PaginationUtil {

    /*public static void sendPagedMessage(TextChannel channel, String fullContent, SplitMethod splitMethod, int splitAmount,
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
        });*/

    public static PaginationList<String> splitStringToList(String content, SplitMethod splitMethod, int splitAmount) {
        List<String> pages = new ArrayList<>();
        if (splitMethod == SplitMethod.CHAR_COUNT) {
            int pagesCount = Math.max((int) Math.ceil((double) content.length() / splitAmount), 1);
            for (int i = 0; i <= pagesCount; i++) {
                pages.add(content.substring((i) * splitAmount, Math.min((i + 1) * splitAmount,
                        content.length())).substring(0, content.lastIndexOf("\n")));
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

    public static void sendPagedMessage(TextChannel textChannel, PaginationList list, int page) {
        Integer[] pages = new Integer[]{page};
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
    }

    public static void sendEmbedPagedMessage(TextChannel textChannel, PaginationList<String> list, int page) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.addField("Info", list.getPage(page), false);
        embedBuilder.addField("Page", String.valueOf(page + 1), true);
        embedBuilder.addField("Total Pages", String.valueOf(list.getPages()), true);
        ButtonGroup buttonGroup = new ButtonGroup();
        Integer[] pages = new Integer[]{page};
        buttonGroup.addButton(new ButtonGroup.Button("\u23EE", (user, message) -> {
            //Start
            EmbedBuilder builder = new EmbedBuilder();
            builder.addField("Info", list.getPage(0), false);
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
                builder.addField("Info", list.getPage(pages[0]), false);
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
                builder.addField("Info", list.getPage(pages[0]), false);
                builder.addField("Page", String.valueOf(pages[0] + 1), true);
                builder.addField("Total Pages", String.valueOf(list.getPages()), true);
                message.editMessage(builder.build()).queue();
            }
        }));
        buttonGroup.addButton(new ButtonGroup.Button("\u23ED", (user, message) -> {
            //Last
            pages[0] = list.getPages() - 1;
            EmbedBuilder builder = new EmbedBuilder();
            builder.addField("Info", list.getPage(pages[0]), false);
            builder.addField("Page", String.valueOf(pages[0] + 1), true);
            builder.addField("Total Pages", String.valueOf(list.getPages()), true);
            message.editMessage(builder.build()).queue();
        }));
        ButtonUtil.sendButtonedMessage(textChannel, embedBuilder.build(), buttonGroup);
    }

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
        for (java.util.List<String> row : table) {
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
        for (java.util.List<String> row : table) {
            bodyBuilder.append(String.format(formatLine.toString(), row.toArray()));
        }
        String bodyString = bodyBuilder.toString();
        List<String> pages = new ArrayList<>();
        String[] lines = bodyString.split("\n");
        int pagesCount = Math.max((int) Math.ceil((double) lines.length / rows), 1);
        for (int i = 0; i < pagesCount; i++) {
            String[] page = ArrayUtils.subarray(lines, rows * i, (rows * i) + rows);
            StringBuilder sb = new StringBuilder();
            sb.append("```");
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append(headerString);
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            for (String line : page) {
                sb.append(line).append("\n");
            }
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append(MessageUtils.getFooter("Page: " + (i + 1) + "/" + pagesCount, padding, widths));
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append("```");
            pages.add(sb.toString());
        }
        return new PaginationList<>(pages);
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

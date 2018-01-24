package stream.flarebot.flarebot.util.pagination;

import org.apache.commons.lang3.ArrayUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for Building Paged Tables.
 */
public class PagedTableBuilder {
    private List<String> header;
    private List<List<String>> body;
    private int rows;
    private boolean pageCounts;

    public PagedTableBuilder() {
        body = new ArrayList<>();
        rows = 15;
        pageCounts = true;
    }

    /**
     * Sets the column values
     *
     * @param columns A list of the column values to use for this table
     * @return this
     */
    public PagedTableBuilder setColumns(List<String> columns) {
        header = columns;
        return this;
    }

    /**
     * Adds a row to the table.
     *
     * @param row A list of the role values to add
     * @return this
     */
    public PagedTableBuilder addRow(List<String> row) {
        body.add(row);
        return this;
    }

    /**
     * Sets the amount of rows per page.
     * This default to 15.
     *
     * @param rows The amount of rows per page.
     * @return this.
     */
    public PagedTableBuilder setRowCount(int rows) {
        this.rows = rows;
        return this;
    }

    /**
     * Sets weather or not to show the page count in the table.
     * This defaults to true.
     *
     * @param pageCounts weather or not to show the page count in the table.
     * @return this.
     */
    public PagedTableBuilder setShowPageCounts(boolean pageCounts) {
        this.pageCounts = pageCounts;
        return this;
    }

    /**
     * Builds the table and creates a {@link PaginationList}
     * for use in sending paged message.
     *
     * @return {@link PaginationList} for use in sending paged message.
     */
    public PaginationList<String> build() {
        int padding = 1;
        int[] widths = new int[header.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = 0;
        }
        for (int i = 0; i < header.size(); i++) {
            if (header.get(i).length() > widths[i]) {
                widths[i] = header.get(i).length();
            }
        }
        for (List<String> row : body) {
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
        String headertring = String.format(formatLine.toString(), header.toArray());
        StringBuilder bodyBuilder = new StringBuilder();
        for (List<String> row : body) {
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
            sb.append(headertring);
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            for (String line : page) {
                sb.append(line).append("\n");
            }
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            if (pageCounts) {
                sb.append(MessageUtils.getFooter("Page: " + (i + 1) + "/" + pagesCount, padding, widths));
            }
            sb.append(MessageUtils.appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append("\n```");
            pages.add(sb.toString());
        }
        return new PaginationList<>(pages);
    }
}

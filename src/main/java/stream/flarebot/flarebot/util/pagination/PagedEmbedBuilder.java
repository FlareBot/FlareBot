package stream.flarebot.flarebot.util.pagination;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.awt.Color;

public class PagedEmbedBuilder<T> {

    private String title;
    private String codeBlock;
    private PaginationList<T> list;
    private boolean hasCodeBlock = false;
    private Color color;

    /**
     * Instantiates the builder with a {@link PaginationList}.
     *
     * @param list The {@link PaginationList} to use as the pages.
     */
    public PagedEmbedBuilder(PaginationList<T> list) {
        this.list = list;
    }

    /**
     * Sets the title for the Embed.
     *
     * @param title The title.
     * @return this.
     */
    public PagedEmbedBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the language to use.
     *
     * @param codeBlock The string representing the code block.
     * @return this
     */
    public PagedEmbedBuilder setCodeBlock(String codeBlock) {
        this.codeBlock = codeBlock;
        enableCodeBlock();
        return this;
    }

    /**
     * Enables code blocks and uses no language syntax, to set a language syntax you can use {@link #setCodeBlock(String)}
     * If you use {@link PagedEmbedBuilder#setCodeBlock(String)} it auto enables.
     *
     * @return this
     */
    public PagedEmbedBuilder enableCodeBlock() {
        this.hasCodeBlock = true;
        return this;
    }

    /**
     * Builds a {@link PagedEmbed} for use in embed pages.
     *
     * @return {@link PagedEmbed}.
     */
    public PagedEmbed build() {
        boolean pageCounts = false;
        if (list.getPages() > 1)
            pageCounts = true;
        return new PagedEmbed(title, codeBlock, hasCodeBlock, list, pageCounts, color);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public class PagedEmbed {

        private String title;
        private String codeBlock;
        private boolean hasCodeBlock;
        private PaginationList<T> list;
        private boolean pageCounts;
        private int pageTotal;
        private Color color;

        public PagedEmbed(String title, String codeBlock, boolean hasCodeBlock, PaginationList<T> list, boolean pageCounts, Color color) {
            this.title = title;
            this.pageCounts = pageCounts;
            pageTotal = list.getPages();
            this.codeBlock = codeBlock;
            this.hasCodeBlock = hasCodeBlock;
            this.list = list;
            this.color = color;
        }

        /**
         * Gets the {@link MessageEmbed} for a specified page.
         *
         * @param page The page to get an embed.
         * @return the {@link MessageEmbed} page.
         */
        public MessageEmbed getEmbed(int page) {
            EmbedBuilder pageEmbed = new EmbedBuilder();
            if (title != null)
                pageEmbed.setTitle(title);
            pageEmbed.setDescription((hasCodeBlock ? "```" + codeBlock + "\n" : "") + list.getPage(page) + (hasCodeBlock ? "\n```" : ""));
            if (pageCounts) {
                pageEmbed.addField("Page", String.valueOf(page + 1), true);
                pageEmbed.addField("Total Pages", String.valueOf(list.getPages()), true);
            }
            pageEmbed.setColor(color);
            return pageEmbed.build();
        }

        /**
         * Gets weather or not this is single paged.
         *
         * @return if it's single paged.
         */
        public boolean isSinglePage() {
            return (pageTotal <= 1);
        }

        /**
         * Gets the total amount of paged.
         *
         * @return The total amount of paged.
         */
        public int getPageTotal() {
            return pageTotal;
        }
    }
}

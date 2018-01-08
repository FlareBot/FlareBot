package stream.flarebot.flarebot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PaginationList<T> {

    private List<T> list;
    private List<PageGroup> groups;

    /**
     * Creates a PagnationList without groups
     *
     * @param list The list of pages
     */
    public PaginationList(List<T> list) {
        Objects.requireNonNull(list);
        this.list = list;
    }

    /**
     * Creates a PagnationList with groups
     *
     * @param list The list of pages
     * @param groupSize the size of the group
     */
    public PaginationList(List<T> list, int groupSize) {
        Objects.requireNonNull(list);
        this.list = list;
        createGroups(groupSize);
    }

    /**
     * Gets the amount of pages we have
     *
     * @return the amount of pages we have
     */
    public int getPages() {
        return list.size();
    }

    /**
     * creates page groups
     *
     * @param groupSize The amount of pages in the group to put.
     * @throws IllegalArgumentException
     */
    public void createGroups(int groupSize) throws IllegalArgumentException {
        if (groupSize > getPages()) {
            throw new IllegalArgumentException("Can't create groups bigger then the page amount");
        }
        List<PageGroup> groups = new ArrayList<>();
        for (int i = 0; i < groupSize; i++) {
            int start = groupSize * (i - 1);
            int end = Math.min(start + groupSize, list.size());
            groups.add(new PageGroup(list.subList(start, end)));
        }
        this.groups = groups;
    }

    /**
     * Gets the specified page as a string
     *
     * @param page the position of the page to get
     * @return The page as a string
     */
    public String getPage(int page) {
        return list.get(page).toString();
    }

    /**
     * Tells if this list has groups
     *
     * @return if this list has groups
     */
    public boolean hasGroups() {
        return groups != null;
    }

    /**
     * Gets the groups that this list has
     *
     * @return The groups
     */
    public List<PageGroup> getGroups() {
        return groups;
    }

    /**
     * Gets a group at the specified position
     *
     * @param pos The position of the group
     * @return The PageGroup
     */
    public PageGroup getGroup(int pos) {
        return groups.get(pos);
    }

    public class PageGroup {
        private List<T> group;

        /**
         * Makes a new PageGroup
         *
         * @param group The list of the group.
         */
        public PageGroup(List<T> group) {
            this.group = group;
        }

        /**
         * Gets the group as a string
         * Useful for if you want to use the groups as pages
         *
         * @return The group as a string.
         */
        public String getGroupAsString() {
            StringBuilder builder = new StringBuilder();
            for (T page : group) {
                builder.append(page.toString()).append("\n");
            }
            return builder.toString();
        }

        /**
         * Gets the page in the group as a String
         *
         * @param pos The position of the page
         * @return The page as a String
         */
        public String getPageInGroup(int pos) {
            return list.get(pos).toString();
        }
    }
}

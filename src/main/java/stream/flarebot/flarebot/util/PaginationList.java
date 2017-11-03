package stream.flarebot.flarebot.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PaginationList<T> implements Iterable<List<T>> {

    private int pageSize = 10;
    private List<T> list;
    private List<List<T>> pagesList;
    private boolean updated = false;

    public PaginationList(List<T> list) {
        Objects.requireNonNull(list);
        this.list = list;
        generatePages();
    }

    public PaginationList(List<T> list, int pageSize) {
        this.list = list;
        this.pageSize = pageSize;
        generatePages();
    }

    @Override
    public Iterator<List<T>> iterator() {
        generatePages();
        return pagesList.iterator();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    protected void generatePages() {
        if (this.updated) return;
        Objects.requireNonNull(this.list);
        if (this.pagesList == null) {
            this.pagesList = new ArrayList<>();
        } else {
            this.pagesList.clear();
        }
        int remaining = this.list.size();
        int cursor = 0;
        List<List<T>> pagesList = new ArrayList<>();
        while (remaining > 0) {
            List<T> list = this.list.subList(cursor, Math.min(cursor + this.pageSize, cursor + remaining));
            pagesList.add(list);
            remaining -= list.size();
            cursor += list.size();
        }
        this.pagesList.addAll(pagesList);
        this.updated = true;
    }

    public List<T> getPage(int index) {
        return this.pagesList.get(index);
    }

    public boolean hasPage(int index) {
        return (this.pagesList.size() >= index) || index < 0;
    }

    public int getPageAmount() {
        generatePages();
        return this.pagesList.size();
    }

    public List<List<T>> getPages() {
        generatePages();
        return this.pagesList;
    }

    public void setList(List<T> list) {
        this.updated = false;
        this.list = list;
    }
}

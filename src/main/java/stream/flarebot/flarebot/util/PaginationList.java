package stream.flarebot.flarebot.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PaginationList<T> implements Iterable<List<T>> {

    private int pageSize = 10;
    private List<T> list;

    public PaginationList(List<T> list) {
        Objects.requireNonNull(list);
        this.list = list;
        list.stream();
    }

    public PaginationList(List<T> list, int pageSize) {
        this.list = list;
        this.pageSize = pageSize;
    }

    public int getPages() {
        return this.list.size() < this.pageSize ?
                1 : (this.list.size() / this.pageSize) + (this.list.size() % this.pageSize != 0 ? 1 : 0);
    }

    public Pair<Integer, Integer> getIndexes(int page) {
        if (page >= getPages())
            throw new NoSuchElementException();
        int start = this.pageSize * (page);
        int end = Math.min(start + this.pageSize, this.list.size());
        return new Pair<>(start, end);
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Iterator<List<T>>() {
            int cursor;

            @Override
            public boolean hasNext() {
                return !(cursor >= getPages());
            }

            @Override
            public List<T> next() {
                int i = cursor;
                if (i >= getPages())
                    throw new NoSuchElementException();
                Pair<Integer, Integer> indexes = getIndexes(i);
                List<T> elementData = PaginationList.this.list.subList(indexes.getKey(), indexes.getValue());
                if (i >= getPages())
                    throw new ConcurrentModificationException();
                cursor = i + 1;
                return elementData;
            }

            @Override
            public void forEachRemaining(Consumer<? super List<T>> consumer) {
                Objects.requireNonNull(consumer);
                final int size = getPages();
                int i = cursor;

                if (i >= getPages())
                    throw new ConcurrentModificationException();
                while (i != size) {
                    Pair<Integer, Integer> indexes = getIndexes(i++);
                    List<T> elementData = PaginationList.this.list.subList(indexes.getKey(), indexes.getValue());
                    consumer.accept(elementData);
                }
                cursor = i;
            }

        };
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


    public List<T> getPage(int index) {
        Pair<Integer, Integer> indexes = getIndexes(index);
        return PaginationList.this.list.subList(indexes.getKey(), indexes.getValue());
    }

    public boolean hasPage(int index) {
        return (getPages() >= index) && !(index < 0);
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Stream<List<T>> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    public String[] convertToStringPage(Function<? super T, ? extends String> mapper) {
        return this.stream().map(l -> l.stream().map(mapper).collect(Collectors.joining("\n"))).toArray(String[]::new);
    }

}

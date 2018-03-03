package stream.flarebot.flarebot.util.errorhandling;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Iterator;

/**
 * <br>
 * Created by Arsen on 1.10.16..
 */
@SuppressWarnings("deprecation")
public enum Markers implements Marker {
    NO_ANNOUNCE,
    TAG_DEVELOPER;

    final Marker marker;

    Markers() {
        marker = MarkerFactory.getMarker(this.toString());
    }

    @Override
    public String getName() {
        return marker.getName();
    }

    @Override
    public void add(Marker reference) {
        marker.add(reference);
    }

    @Override
    public boolean remove(Marker reference) {
        return marker.remove(reference);
    }

    @Override
    public boolean hasChildren() {
        return marker.hasChildren();
    }

    @Override
    public boolean hasReferences() {
        return marker.hasReferences();
    }

    @Override
    public Iterator<Marker> iterator() {
        return marker.iterator();
    }

    @Override
    public boolean contains(Marker other) {
        return marker.contains(other);
    }

    @Override
    public boolean contains(String name) {
        return marker.contains(name);
    }
}

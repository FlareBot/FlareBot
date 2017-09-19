package stream.flarebot.flarebot.util.objects.expiringmap;

public abstract class ExpiredEvent<K, V> {

    private boolean cancelled = false;

    /**
     * Ran when a item in the ExpiringMap has expired!
     *
     * @param k              Key of the map.
     * @param v              Value of the map.
     * @param expired        Time it expired.
     * @param last_retrieved The time the data was last retrieved with a `get` method.
     */
    public abstract void run(K k, V v, long expired, long last_retrieved);

    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}

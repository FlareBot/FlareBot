package stream.flarebot.flarebot.util.objects;

import stream.flarebot.flarebot.commands.Command;

public class RunnableWrapper {

    private final Runnable action;
    private final Class<? extends Command> origin;

    public RunnableWrapper(Runnable action, Class<? extends Command> origin) {
        this.action = action;
        this.origin = origin;
    }

    public Runnable getAction() {
        return action;
    }

    public Class<? extends Command> getOrigin() {
        return origin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestActionWrapper wrapper = (RestActionWrapper) o;

        return getAction().equals(wrapper.getAction()) && getOrigin().equals(wrapper.getOrigin());
    }

    @Override
    public int hashCode() {
        int result = getAction().hashCode();
        result = 31 * result + getOrigin().hashCode();
        return result;
    }

}

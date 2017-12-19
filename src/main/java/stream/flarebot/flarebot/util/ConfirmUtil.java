package stream.flarebot.flarebot.util;

import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.util.objects.RunnableWrapper;
import stream.flarebot.flarebot.util.objects.expiringmap.ExpiringMap;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ConfirmUtil {

    private static final ExpiringMap<String, Set<RunnableWrapper>> confirmMap =
            new ExpiringMap<>(TimeUnit.MINUTES.toMillis(1));

    public static void pushAction(String userID, RunnableWrapper action) {
        if (confirmMap.containsKey(userID)) {
            Set<RunnableWrapper> actions = confirmMap.get(userID);
            if (actions.stream().noneMatch(wrapper -> wrapper.getOrigin().equals(action.getOrigin()))) {
                actions.add(action);
            }
        } else {
            Set<RunnableWrapper> actions = new ConcurrentHashSet<>();
            actions.add(action);
            confirmMap.put(userID, actions);
        }
    }

    public static void run(String userID, Class<? extends Command> command) {
        Runnable runnable = get(userID, command);
        if (runnable != null) {
            runnable.run();
        }
    }

    public static Runnable get(String userID, Class<? extends Command> command) {
        if (confirmMap.containsKey(userID)) {
            Set<RunnableWrapper> wrappers = confirmMap.get(userID);
            Optional<RunnableWrapper> wrapper =
                    wrappers.stream().filter(w -> w.getOrigin().equals(command)).findFirst();
            if (wrapper.isPresent()) {
                return wrapper.get().getAction();
            }
        }
        return null;
    }

    public static void removeUser(String userID) {
        if (confirmMap.containsKey(userID)) {
            confirmMap.remove(userID);
        }
    }

    public static boolean checkExists(String userID, Class<? extends Command> command) {
        if (confirmMap.containsKey(userID)) {
            Set<RunnableWrapper> wrappers = confirmMap.get(userID);
            return wrappers.stream().anyMatch(wrapper -> wrapper.getOrigin().equals(command));
        }
        return false;
    }

    public static void clearConfirmMap() {
        ConfirmUtil.clearConfirmMap(false);
    }

    public static void clearConfirmMap(boolean force) {
        confirmMap.purge(force);
    }


    public static void remove(String id, Class<? extends Command> command) {
        if (checkExists(id, command)) {
            confirmMap.get(id).removeIf(restActionWrapper -> restActionWrapper.getOrigin().equals(command));
        }
    }

}

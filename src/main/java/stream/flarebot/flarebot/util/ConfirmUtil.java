package stream.flarebot.flarebot.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.util.objects.RunnableWrapper;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ConfirmUtil {

    private static final Cache<String, Set<RunnableWrapper>> confirmCache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(1, TimeUnit.MINUTES)
                    .build();

    public static void pushAction(String userID, RunnableWrapper action) {
        Set<RunnableWrapper> actions = confirmCache.getIfPresent(userID);
        if (actions != null) {
            if (actions.stream().noneMatch(wrapper -> wrapper.getOrigin().equals(action.getOrigin()))) {
                actions.add(action);
            }
        } else {
            Set<RunnableWrapper> newActions = new ConcurrentHashSet<>();
            newActions.add(action);
            confirmCache.put(userID, newActions);
        }
    }

    public static void run(String userID, Class<? extends Command> command) {
        Runnable runnable = get(userID, command);
        if (runnable != null) {
            runnable.run();
            remove(userID, command);
        }
    }

    public static Runnable get(String userID, Class<? extends Command> command) {
        if (confirmCache.getIfPresent(userID) != null) {
            Set<RunnableWrapper> wrappers = confirmCache.getIfPresent(userID);
            Optional<RunnableWrapper> wrapper =
                    wrappers.stream().filter(w -> w.getOrigin().equals(command)).findFirst();
            if (wrapper.isPresent()) {
                return wrapper.get().getAction();
            }
        }
        return null;
    }

    public static void removeUser(String userID) {
        confirmCache.invalidate(userID);
    }

    public static boolean checkExists(String userID, Class<? extends Command> command) {
        if (confirmCache.getIfPresent(userID) != null) {
            Set<RunnableWrapper> wrappers = confirmCache.getIfPresent(userID);
            return wrappers.stream().anyMatch(wrapper -> wrapper.getOrigin().equals(command));
        }
        return false;
    }


    public static void remove(String id, Class<? extends Command> command) {
        if (checkExists(id, command)) {
            confirmCache.getIfPresent(id).removeIf(restActionWrapper -> restActionWrapper.getOrigin().equals(command));
        }
    }

}

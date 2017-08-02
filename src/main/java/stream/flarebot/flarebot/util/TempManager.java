package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.requests.RestAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TempManager {

    private static Map<Integer, List<RestAction>> scheduledActions = new ConcurrentHashMap<>();

    public static synchronized void executeAtCurrent() {
        int time = Math.round(System.currentTimeMillis() / 1000);
        if (scheduledActions.get(time) != null) {
            Iterator<RestAction> actions = scheduledActions.get(time).iterator();
            while (actions.hasNext()) {
                actions.next().queue();
                actions.remove();
            }
            if (scheduledActions.get(time).isEmpty()) {
                scheduledActions.remove(scheduledActions.get(time));
            }
        }
    }

    public static synchronized void executeInPast() {
        int time = Math.round(System.currentTimeMillis() / 1000);
        Iterator<Map.Entry<Integer, List<RestAction>>> iterator = scheduledActions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, List<RestAction>> entry = iterator.next();
            if (entry.getKey() <= time) {
                Iterator<RestAction> actions = entry.getValue().iterator();
                while (actions.hasNext()) {
                    actions.next().queue();
                    actions.remove();
                }
                if (entry.getValue().isEmpty()) {
                    iterator.remove();
                }
            }
        }
    }

    public static synchronized void add(RestAction action, Long timeToExecute) {
        int time = Math.round(timeToExecute / 1000);
        if (scheduledActions.get(time) != null) {
            scheduledActions.get(time).add(action);
        } else {
            List<RestAction> actions = new ArrayList<>();
            actions.add(action);
            scheduledActions.put(time, actions);
        }
    }

    public static Map<Integer, List<RestAction>> getScheduledActions(){
        return scheduledActions;
    }

}

package stream.flarebot.flarebot.scheduler;

import org.joda.time.Period;
import stream.flarebot.flarebot.FlareBot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private static final ScheduledExecutorService timer = Executors
            .newScheduledThreadPool(10, r -> new Thread(r, "FlareBot Scheduled Task"));

    private static final Map<String, ScheduledFuture<?>> tasks = new HashMap<>();
    private static final Map<String, ScheduledFuture<?>> persistentTasks = new HashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(timer::shutdownNow));
    }

    public static boolean scheduleRepeating(Runnable task, String taskName, long delay, long interval) {
        if (tasks.containsKey(taskName)) {
            return false;
        }
        tasks.put(taskName,
                timer.scheduleAtFixedRate(() -> {
                    try {
                        task.run();
                    } catch (Exception e) {
                        FlareBot.LOGGER.error("Error in " + taskName + " task scheduler!", e);
                    }
                }, delay, interval, TimeUnit.MILLISECONDS));
        return true;
    }

    public static void delayTask(Runnable task, String taskName, long delay) {
        tasks.put(taskName, timer.schedule(task, delay, TimeUnit.MILLISECONDS));
    }

    public static void delayTask(FutureAction action) {
        persistentTasks.put(action.toString() + System.currentTimeMillis(), timer.schedule(action::execute,
                action.getExpires().getMillis(), TimeUnit.MILLISECONDS));
    }

    public static boolean cancelTask(String taskName) {
        Iterator<Map.Entry<String, ScheduledFuture<?>>> i = tasks.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, ScheduledFuture<?>> next = i.next();
            if (next.getKey() == null) continue;
            if (next.getKey().equals(taskName)) {
                next.getValue().cancel(false);
                i.remove();
                return true;
            }
        }
        return false;
    }

    public static Map<String, ScheduledFuture<?>> getTasks() {
        return tasks;
    }

    public static Map<String, ScheduledFuture<?>> getPersistentTasks() {
        return persistentTasks;
    }

    public static void queueFutureAction(long guuildId, long channelId, long responsible, long target, String reason,
                                         Period delay, FutureAction.Action action) {
        new FutureAction(guuildId, channelId, responsible, target, reason, delay, action).queue();
    }

    public static void queueFutureAction(long guuildId, long channelId, long responsible, String reason,
                                         Period delay, FutureAction.Action action) {
        new FutureAction(guuildId, channelId, responsible, reason, delay, action).queue();
    }
}

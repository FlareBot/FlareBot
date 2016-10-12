package com.bwfcwalshy.flarebot.scheduler;

import com.bwfcwalshy.flarebot.FlareBot;

import java.util.*;

/**
 * ayy it repeats
 * <br>
 * Created by Arsen on 20.9.16..
 */
public class Scheduler {
    private static final Timer timer = new Timer();
    private static final Map<String, TimerTask> tasks = new HashMap<>();

    public static boolean scheduleRepeating(Runnable task, String taskName, long delay, long interval) {
        if (tasks.containsKey(taskName)) {
            return false;
        }
        TimerTask toPut = new TimerTask() {
            @Override
            public void run() {
                FlareBot.LOGGER.info("Running task '" + taskName + '\'');
                task.run();
            }
        };
        timer.schedule(toPut, delay, interval);
        tasks.put(taskName, toPut);
        return true;
    }

    public static void delayTask(Runnable task, long delay) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        }, delay);
    }

    public static boolean cancelTask(String taskName) {
        Iterator<Map.Entry<String, TimerTask>> i = tasks.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, TimerTask> next = i.next();
            if (next.getKey().equals(taskName)) {
                next.getValue().cancel();
                i.remove();
                return true;
            }
        }
        return false;
    }
}

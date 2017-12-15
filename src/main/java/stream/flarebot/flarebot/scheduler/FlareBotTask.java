package stream.flarebot.flarebot.scheduler;

public abstract class FlareBotTask implements Runnable {

    private String taskName;

    private FlareBotTask() {
    }

    public FlareBotTask(String taskName) {
        this.taskName = taskName;
    }

    public boolean repeat(long delay, long interval) {
        return Scheduler.scheduleRepeating(this, taskName, delay, interval);
    }

    public void delay(long delay) {
        Scheduler.delayTask(this, taskName, delay);
    }

    public boolean cancel() {
        return Scheduler.cancelTask(taskName);
    }
}

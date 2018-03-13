package stream.flarebot.flarebot.scheduler;

public abstract class FlareBotTask implements Runnable {

    private String taskName;

    private FlareBotTask() {
    }

    public FlareBotTask(String taskName) {
        this.taskName = taskName;
    }

    public FlareBotTask(String taskName, long delay) {
        this.taskName = taskName;
        delay(delay);
    }

    public FlareBotTask(String taskName, long delay, long interval) {
        this.taskName = taskName;
        repeat(delay, interval);
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

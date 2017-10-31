package stream.flarebot.flarebot.scheduler;

public abstract class FlareBotTask implements Runnable {

    private String taskName;

    protected FlareBotTask() {
    }

    public FlareBotTask(String taskName) {
        this.taskName = taskName;
    }

    public boolean repeat(long delay, long interval) {
        return Scheduler.scheduleRepeating(this, taskName, delay, interval);
    }

    public void delay(long delay) {
        Scheduler.delayTask(this, delay);
    }

    public boolean cancel() {
        return Scheduler.cancelTask(taskName);
    }
}

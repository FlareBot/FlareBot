package stream.flarebot.flarebot.scheduler;

import net.dv8tion.jda.core.requests.RestAction;

public class RestActionTask implements Runnable {

    private String taskName;
    private RestAction restAction;

    private RestActionTask(RestAction restAction) {
        this.restAction = restAction;
    }

    public RestActionTask(RestAction restAction, String taskName) {
        this.restAction = restAction;
        this.taskName = taskName;
    }

    public boolean repeat(long delay, long interval) {
        return Scheduler.scheduleRepeating(this, taskName, delay, interval);
    }

    public void delay(long delay) {
        //Scheduler.delayTask(this, delay);
    }

    public boolean cancel() {
        return Scheduler.cancelTask(taskName);
    }

    @Override
    public void run() {
        restAction.complete();
    }
}

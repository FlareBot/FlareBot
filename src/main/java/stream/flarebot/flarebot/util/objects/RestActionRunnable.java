package stream.flarebot.flarebot.util.objects;

import net.dv8tion.jda.core.requests.RestAction;

/**
 * Created by William on 12/09/2017.
 * Project flarebot
 */
public class RestActionRunnable implements Runnable {

    private RestAction action;

    public RestActionRunnable(RestAction action) {
        this.action = action;
    }


    @Override
    public void run() {
        if (action != null) {
            action.queue();
        }
    }
}

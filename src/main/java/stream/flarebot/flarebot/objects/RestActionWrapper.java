package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.requests.RestAction;
import stream.flarebot.flarebot.commands.Command;

public class RestActionWrapper {

    private final RestAction action;
    private final Class<? extends Command> origin;

    public RestActionWrapper(RestAction action, Class<? extends Command> origin) {

        this.action = action;
        this.origin = origin;
    }

    public RestAction getAction() {
        return action;
    }

    public Class<? extends Command> getOrigin() {
        return origin;
    }
}

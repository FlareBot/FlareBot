package stream.flarebot.flarebot.mod.automod;

import stream.flarebot.flarebot.mod.modlog.ModAction;

@Deprecated
public class Punishment {

    private ModAction action;
    private long duration;

    public Punishment(ModAction action) {
        this.action = action;
    }

    public Punishment(ModAction action, long duration) {
        this.action = action;
        this.duration = duration;
    }

    public long getDuration() {
        return this.duration;
    }

    public String getName() {
        return action.name().charAt(0) + action.name().substring(1).toLowerCase().replaceAll("_", " ");
    }

    public ModAction getAction() {
        return action;
    }


}

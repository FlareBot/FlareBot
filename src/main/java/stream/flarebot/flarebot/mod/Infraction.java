package stream.flarebot.flarebot.mod;

import stream.flarebot.flarebot.annotations.DoNotUse;
import stream.flarebot.flarebot.mod.modlog.ModAction;

public class Infraction {

    private int caseId;
    private ModAction action;
    private long user;
    private long responsible;
    private String reason;
    private long duration;

    @DoNotUse(expressUse = "//TODO")
    protected Infraction(ModAction action, long user, long responsible, String reason) {
        this(action, user, responsible, reason, -1);
    }

    @DoNotUse(expressUse = "//TODO")
    protected Infraction(ModAction action, long user, long responsible, String reason, long duration) {
        this.action = action;
        this.user = user;
        this.responsible = responsible;
        this.reason = reason;
        this.duration = duration;
    }

    public int getCaseId() {
        return caseId;
    }

    public ModAction getAction() {
        return action;
    }

    public long getUser() {
        return user;
    }

    public long getResponsible() {
        return responsible;
    }

    public String getReason() {
        return reason;
    }

    public long getDuration() {
        return duration;
    }
}

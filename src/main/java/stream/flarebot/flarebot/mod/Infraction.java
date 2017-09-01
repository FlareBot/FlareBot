package stream.flarebot.flarebot.mod;

public class Infraction {

    private Action action;
    private int points;
    private Punishment solePunishment;

    public Infraction(Action action, int points) {
        this.action = action;
        this.points = points;
    }

    public Infraction(Action action, Punishment punishment) {
        this.action = action;
        this.solePunishment = punishment;
    }

    public Action getAction() {
        return this.action;
    }

    public int getPoints() {
        return this.points;
    }

    public Punishment getSolePunishment() {
        return solePunishment;
    }
}

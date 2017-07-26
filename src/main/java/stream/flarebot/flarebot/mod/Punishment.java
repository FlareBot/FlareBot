package stream.flarebot.flarebot.mod;

public class Punishment {

    private EPunishment punishment;
    private long duration;

    public Punishment(EPunishment punishment) {
        this.punishment = punishment;
    }

    public Punishment(EPunishment punishment, int duration) {
        this.punishment = punishment;
        this.duration = duration;
    }

    public long getDuration() {
        return this.duration;
    }

    public String getName() {
        return punishment.name().charAt(0) + punishment.name().substring(1).toLowerCase().replaceAll("_", " ");
    }

    public EPunishment getPunishment() {
        return punishment;
    }

    public enum EPunishment {
        PURGE,
        TEMP_MUTE,
        MUTE,
        KICK,
        TEMP_BAN,
        BAN
    }
}

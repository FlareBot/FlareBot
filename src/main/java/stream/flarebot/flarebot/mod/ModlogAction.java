package stream.flarebot.flarebot.mod;

public enum ModlogAction {

    PURGE,

    TEMP_MUTE,
    MUTE,
    UNMUTE,

    KICK,
    TEMP_BAN,
    BAN,

    WARN;

    public Punishment toPunishment() {
        return new Punishment(this);
    }

    public Punishment toPunishment(int duration) {
        return new Punishment(this, duration);
    }
}

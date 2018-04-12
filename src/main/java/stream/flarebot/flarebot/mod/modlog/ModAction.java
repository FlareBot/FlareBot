package stream.flarebot.flarebot.mod.modlog;

public enum ModAction {

    BAN(true, ModlogEvent.USER_BANNED),
    SOFTBAN(true, ModlogEvent.USER_SOFTBANNED),
    FORCE_BAN(true, ModlogEvent.USER_BANNED),
    TEMP_BAN(true, ModlogEvent.USER_TEMP_BANNED),
    UNBAN(false, ModlogEvent.USER_UNBANNED),

    KICK(true, ModlogEvent.USER_KICKED),

    TEMP_MUTE(true, ModlogEvent.USER_TEMP_MUTED),
    MUTE(true, ModlogEvent.USER_MUTED),
    UNMUTE(false, ModlogEvent.USER_UNMUTED),

    WARN(true, ModlogEvent.USER_WARNED);

    private boolean infraction;
    private ModlogEvent event;

    ModAction(boolean infraction, ModlogEvent modlogEvent) {
        this.infraction = infraction;
        this.event = modlogEvent;
    }

    public boolean isInfraction() {
        return infraction;
    }

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase().replaceAll("_", " ");
    }

    public String getLowercaseName() {
        return toString().toLowerCase();
    }

    public ModlogEvent getEvent() {
        return event;
    }
}

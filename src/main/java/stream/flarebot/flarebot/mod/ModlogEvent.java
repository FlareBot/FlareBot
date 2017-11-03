package stream.flarebot.flarebot.mod;

public enum ModlogEvent {
    MEMBER_JOIN,
    MEMBER_LEAVE,

    MEMBER_ROLE_GIVE,
    MEMBER_ROLE_REMOVE,

    MEMBER_VOICE_JOIN,
    MEMBER_VOICE_LEAVE,

    ROLE_CREATE,
    ROLE_DELETE,
    ROLE_EDIT,

    CHANNEL_CREATE,
    CHANNEL_DELETE,

    COMMAND,

    MESSAGE_EDIT,
    MESSAGE_DELETE;

    boolean compact = false;

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

}

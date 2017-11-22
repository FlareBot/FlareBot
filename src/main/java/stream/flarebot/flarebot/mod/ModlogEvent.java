package stream.flarebot.flarebot.mod;

public enum ModlogEvent {
    MEMBER_JOIN("Triggers when user joins the server."),
    MEMBER_LEAVE("Triggers when user leaves the server."),

    MEMBER_ROLE_GIVE("Triggers when a role is added to a user."),
    MEMBER_ROLE_REMOVE("Triggers when a role is taken away a user."),

    MEMBER_VOICE_JOIN("Triggers when a user joins a voice channel."),
    MEMBER_VOICE_LEAVE("Triggers when a user leaves a voice channel."),

    ROLE_CREATE("Triggers when a role is created."),
    ROLE_DELETE("Triggers when a role is deleted."),
    ROLE_EDIT("Triggers when a role is edited."),
    ROLE_MOVE("Triggers when a role is moved on the higharchy."),

    CHANNEL_CREATE("Triggers when a channel is created."),
    CHANNEL_DELETE("Triggers when a channel is deleted."),

    COMMAND("Triggers when a user runs a **FlareBot** command."),

    MESSAGE_EDIT("Triggers when a message is edited."),
    MESSAGE_DELETE("Triggers when a message is deleted.");

    boolean compact = false;

    String discription;

    ModlogEvent(String discription) {
        this.discription = discription;
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public String getDiscription() {
        return discription;
    }
}

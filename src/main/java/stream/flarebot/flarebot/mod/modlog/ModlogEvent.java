package stream.flarebot.flarebot.mod.modlog;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public enum ModlogEvent {

    MEMBER_JOIN("Join", "Triggers when user joins the server.", Color.GREEN, true),
    MEMBER_LEAVE("Leave", "Triggers when user leaves the server.", Color.RED, true),

    MEMBER_ROLE_GIVE("Role Give", "Triggers when a role is added to a user.", Color.GREEN, true),
    MEMBER_ROLE_REMOVE("Role Remove", "Triggers when a role is taken away a user.", Color.RED, true),

    MEMBER_VOICE_JOIN("Voice Join", "Triggers when a user joins a voice channel.", Color.GREEN, false),
    MEMBER_VOICE_LEAVE("Voice Leave", "Triggers when a user leaves a voice channel.", Color.RED, false),
    MEMBER_VOICE_MOVE("Voice Move", "Triggers when a user moves to a different voice channel. " +
            "Either because they moved or because they where force moved.", ColorUtils.FLAREBOT_BLUE, false),

    MEMBER_NICK_CHANGE("Nickname Change", "Triggers when a user changes their nick.", ColorUtils.FLAREBOT_BLUE, true),

    // These are events triggered mostly by our mod actions
    USER_BANNED("User Banned", "Triggers when a user is banned, either through Discord or a bot", Color.RED, true, true),
    USER_TEMP_BANNED("User Temp Banned", "Triggers when a user is temporarily banned using FlareBot", Color.RED, true, true),
    USER_UNBANNED("User Unbanned", "Triggers when a user is unbanned, either through Discord or a bot", ColorUtils.FLAREBOT_BLUE, true, true),

    USER_KICKED("User Kicked", "Triggers when a user is kicked. **This only works if they were kicked with FlareBot " +
            "since Discord doesn't give us actual kick info!!**", Color.ORANGE, true, true),

    USER_MUTED("User Muted", "Triggers when a user is muted using FlareBot", Color.ORANGE, true, true),
    USER_TEMP_MUTED("User Temp Muted", "Triggers when a user is temporary muted using FlareBot", Color.orange, true, true),
    USER_UNMUTED("User Unmuted", "Triggers when a user is unmuted", ColorUtils.FLAREBOT_BLUE, true, true),

    USER_WARNED("User Warned", "Triggers when a user is warned using FlareBot", Color.ORANGE, true, true),
    /////////////////

    ROLE_CREATE("Role Create", "Triggers when a role is created.", Color.GREEN, true),
    ROLE_DELETE("Role Delete", "Triggers when a role is deleted.", Color.RED, true),
    ROLE_EDIT("Role Edit", "Triggers when a role is edited.", ColorUtils.FLAREBOT_BLUE, false),

    CHANNEL_CREATE("Channel Create", "Triggers when a channel is created.", Color.GREEN, true),
    CHANNEL_DELETE("Channel Delete", "Triggers when a channel is deleted.", Color.RED, true),

    MESSAGE_EDIT("Message Edit", "Triggers when a message is edited.", ColorUtils.FLAREBOT_BLUE, true),
    MESSAGE_DELETE("Message Delete", "Triggers when a message is deleted.", Color.RED, true),

    GUILD_EXPLICIT_FILTER_CHANGE("Explicit Filter Change", "Triggers when the server's explicit filter level is changed.",
            Color.orange, false),
    GUILD_UPDATE("Settings Update", "Triggers when any of the server setting are changed.", Color.decode("#addfe6"), false),

    FLAREBOT_AUTOASSIGN_ROLE("FlareBot Autoassign Role", "Triggers when a role is automatically given to a user",
            Color.GREEN, true),
    FLAREBOT_COMMAND("FlareBot Command", "Triggers when a user runs a **FlareBot** command.", Color.decode("#addfe6"),
            false);

    private String title;
    private String description;
    private Color color;
    private boolean defaultEvent;
    private boolean showReason;

    // GSONs needs
    ModlogEvent(){}

    ModlogEvent(String title, String description, Color color, boolean defaultEvent) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.defaultEvent = defaultEvent;
        this.showReason = false;
    }

    ModlogEvent(String title, String description, Color color, boolean defaultEvent, boolean showReason) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.defaultEvent = defaultEvent;
        this.showReason = showReason;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    public EmbedBuilder getEventEmbed(User user, User responsible) {
        return getEventEmbed(user, responsible, null);
    }

    public EmbedBuilder getEventEmbed(User user, User responsible, String reason) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(WordUtils.capitalize(getTitle()));
        if (user != null) {
            eb.addField("User", MessageUtils.getTag(user) + " (" + user.getId() + ")", true);
        }
        if (responsible != null) {
            eb.addField("Responsible", responsible.getAsMention(), true);
        }
        if (showReason) {
            eb.addField("Reason", (reason == null || reason.isEmpty() ? "No Reason Given!" : reason), true);
        }
        eb.setColor(color);
        return eb;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDefaultEvent() {
        return defaultEvent;
    }

    public String getName() {
        return WordUtils.capitalize(name().replaceAll("_", " "));
    }

    public ModlogAction getAction(long channelId) {
        return new ModlogAction(channelId, this, false);
    }
}

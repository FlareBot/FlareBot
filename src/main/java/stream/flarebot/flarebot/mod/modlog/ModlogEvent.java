package stream.flarebot.flarebot.mod.modlog;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

public enum ModlogEvent {

    MEMBER_JOIN("Member Joined", "Triggers when user joins the server.", ColorUtils.GREEN, true),
    MEMBER_LEAVE("Member Left", "Triggers when user leaves the server.", ColorUtils.ORANGE, true),

    MEMBER_NICK_CHANGE("Nickname Change", "Triggers when a user changes their nick.", ColorUtils.FLAREBOT_BLUE, true),

    MEMBER_ROLE_GIVE("Role Give", "Triggers when a role is added to a user.", ColorUtils.GREEN, true),
    MEMBER_ROLE_REMOVE("Role Remove", "Triggers when a role is taken away a user.", ColorUtils.ORANGE, true),

    MEMBER_VOICE_JOIN("Voice Join", "Triggers when a user joins a voice channel.", ColorUtils.FLAREBOT_BLUE, false),
    MEMBER_VOICE_LEAVE("Voice Leave", "Triggers when a user leaves a voice channel.", ColorUtils.FLAREBOT_BLUE, false),
    MEMBER_VOICE_MOVE("Voice Move", "Triggers when a user moves to a different voice channel. " +
            "Either because they moved or because they where force moved.", ColorUtils.FLAREBOT_BLUE, false),

    // These are events triggered mostly by our mod actions
    USER_BANNED("User Banned", "Triggers when a user is banned, either through Discord or a bot", ColorUtils.RED, true, true),
    USER_TEMP_BANNED("User Temp Banned", "Triggers when a user is temporarily banned using FlareBot", ColorUtils.RED, true, true),
    USER_UNBANNED("User Unbanned", "Triggers when a user is unbanned, either through Discord or a bot", ColorUtils.FLAREBOT_BLUE, true, true),

    USER_KICKED("User Kicked", "Triggers when a user is kicked.", ColorUtils.ORANGE, true, true),

    USER_MUTED("User Muted", "Triggers when a user is muted using FlareBot", ColorUtils.ORANGE, true, true),
    USER_TEMP_MUTED("User Temp Muted", "Triggers when a user is temporary muted using FlareBot", ColorUtils.ORANGE, true, true),
    USER_UNMUTED("User Unmuted", "Triggers when a user is unmuted", ColorUtils.FLAREBOT_BLUE, true, true),

    USER_WARNED("User Warned", "Triggers when a user is warned using FlareBot", ColorUtils.ORANGE, true, true),

    REPORT_SUBMITTED("User Reported", "Triggers when a user is reported using FlareBot", ColorUtils.ORANGE, true, true),
    REPORT_EDITED("Report Edited", "Triggers when a report is edited", ColorUtils.FLAREBOT_BLUE, true, true),
    /////////////////

    ROLE_CREATE("Role Create", "Triggers when a role is created.", ColorUtils.GREEN, true),
    ROLE_DELETE("Role Delete", "Triggers when a role is deleted.", ColorUtils.RED, true),
    ROLE_EDIT("Role Edit", "Triggers when a role is edited.", ColorUtils.FLAREBOT_BLUE, false),

    CHANNEL_CREATE("Channel Create", "Triggers when a channel is created.", ColorUtils.GREEN, true),
    CHANNEL_DELETE("Channel Delete", "Triggers when a channel is deleted.", ColorUtils.RED, true),

    MESSAGE_EDIT("Message Edit", "Triggers when a message is edited.", ColorUtils.FLAREBOT_BLUE, true),
    MESSAGE_DELETE("Message Delete", "Triggers when a message is deleted.", ColorUtils.RED, true),

    GUILD_EXPLICIT_FILTER_CHANGE("Explicit Filter Change", "Triggers when the server's explicit filter level is changed.",
            ColorUtils.ORANGE, false),
    GUILD_UPDATE("Settings Update", "Triggers when any of the server setting are changed.", ColorUtils.FLAREBOT_BLUE, false),

    FLAREBOT_AUTOASSIGN_ROLE("Autoassign Role", "Triggers when a role is automatically given to a user",
            ColorUtils.GREEN, true),
    FLAREBOT_COMMAND("Command", "Triggers when a user runs a **FlareBot** command.", ColorUtils.FLAREBOT_BLUE,
            false),
    FLAREBOT_PURGE("Purge", "Triggers when a user does a purge with FlareBot.", ColorUtils.FLAREBOT_BLUE, false),
    
    INVITE_POSTED("Invite Posted", "Triggers when a Discord invite is posted", ColorUtils.ORANGE, false, false, true);

    public static final ModlogEvent[] values = values();
    public static final List<ModlogEvent> events = Arrays.asList(values);

    private String title;
    private String description;
    private Color color;
    private boolean defaultEvent;
    private boolean showReason;
    private boolean hidden = false;

    // GSONs needs
    ModlogEvent() {
    }

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
    
    ModlogEvent(String title, String description, Color color, boolean defaultEvent, boolean showReason, boolean hidden) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.defaultEvent = defaultEvent;
        this.showReason = showReason;
        this.hidden = hidden;
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
        if (user == null && responsible == null) {
            throw new IllegalArgumentException("User or the responsible user has to be not-null! Event: " + this.getName());
        }
        EmbedBuilder eb = new EmbedBuilder()
                .setAuthor(WordUtils.capitalize(getTitle()), null, user == null ? responsible.getEffectiveAvatarUrl()
                        : user.getEffectiveAvatarUrl());
        if (user != null)
            eb.addField("User", user.getAsMention() + " | " + MessageUtils.getTag(user), true);
        eb.setFooter("User ID: " + (user == null ? responsible.getId() : user.getId()), null)
                .setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        if (responsible != null) {
            eb.addField("Responsible", MessageUtils.getTag(responsible), true);
        }
        if (showReason) {
            eb.addField("Reason", (reason == null || reason.isEmpty() ? "No Reason Given!" : reason), true);
        }
        eb.setColor(color);

        // Custom event changes.
        if (this == ModlogEvent.FLAREBOT_PURGE)
            eb.setAuthor(user != null ? "User Purge" : "Chat Purge", null, user == null ? responsible.getEffectiveAvatarUrl()
                    : user.getEffectiveAvatarUrl());
        return eb;
    }

    public String getEventText(User user, User responsible, String reason) {
        if (user == null && responsible == null) {
            throw new IllegalArgumentException("User or the responsible user has to be not-null! Event: " + this.getName());
        }
        String title = WordUtils.capitalize(getTitle());
        // Custom event changes.
        if (this == ModlogEvent.FLAREBOT_PURGE)
            title = user != null ? "User Purge" : "Chat Purge";

        StringBuilder sb = new StringBuilder()
                .append(title)
                .append(" (").append(GeneralUtils.formatTime(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime()))
                .append(")\n");

        if (user != null)
            sb.append("**User**: `").append(MessageUtils.getTag(user)).append("` (").append(user.getId()).append(") ");
        if (responsible != null) {
            sb.append("**Responsible**: `").append(MessageUtils.getTag(responsible)).append("` (")
                    .append(responsible.getId()).append(") ");
        }
        if (showReason) {
            sb.append("**Reason**: ").append(reason == null || reason.isEmpty() ? "No Reason Given!" : reason);
        }
        return sb.toString().trim();
    }

    public String getTitle() {
        return title;
    }

    public boolean isDefaultEvent() {
        return defaultEvent;
    }

    public String getName() {
        return WordUtils.capitalize(name().toLowerCase().replaceAll("_", " "));
    }

    public ModlogAction getAction(long channelId) {
        return new ModlogAction(channelId, this, false);
    }

    public static ModlogEvent getEvent(String arg) {
        for (ModlogEvent event : values()) {
            if (event.getName().equalsIgnoreCase(arg) || event.getTitle().equalsIgnoreCase(arg)) {
                return event;
            }
        }
        return null;
    }


}

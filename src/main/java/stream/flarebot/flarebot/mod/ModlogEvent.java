package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public enum ModlogEvent {
    MEMBER_JOIN("Triggers when user joins the server.", Color.GREEN),
    MEMBER_LEAVE("Triggers when user leaves the server.", Color.RED),

    MEMBER_ROLE_GIVE("Triggers when a role is added to a user.", Color.GREEN),
    MEMBER_ROLE_REMOVE("Triggers when a role is taken away a user.", Color.RED),

    MEMBER_VOICE_JOIN("Triggers when a user joins a voice channel.", Color.GREEN),
    MEMBER_VOICE_LEAVE("Triggers when a user leaves a voice channel.", Color.RED),

    ROLE_CREATE("Triggers when a role is created.", Color.GREEN),
    ROLE_DELETE("Triggers when a role is deleted.", Color.RED),
    ROLE_EDIT("Triggers when a role is edited.", Color.decode("#addfe6")),

    CHANNEL_CREATE("Triggers when a channel is created.", Color.GREEN),
    CHANNEL_DELETE("Triggers when a channel is deleted.", Color.RED),

    COMMAND("Triggers when a user runs a **FlareBot** command.", Color.decode("#addfe6")),

    MESSAGE_EDIT("Triggers when a message is edited.", Color.decode("#addfe6")),
    MESSAGE_DELETE("Triggers when a message is deleted.", Color.RED);

    boolean compact = false;

    String description;
    Color color;

    ModlogEvent(String description, Color color) {
        this.description = description;
        this.color = color;
    }

    public boolean isCompact() {
        return compact;
    }

    public void setCompact(boolean compact) {
        this.compact = compact;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    public EmbedBuilder getEventEmbed(User user, User responsible) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(WordUtils.capitalize(name().toLowerCase().replaceAll("_", " ")));
        if (user != null) {
            eb.addField("User", MessageUtils.getTag(user), true);
        }
        if (responsible != null) {
            eb.addField("Responsible", responsible.getAsMention(), true);
        }
        eb.setColor(color);
        return eb;
    }
}

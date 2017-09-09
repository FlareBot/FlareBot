package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.awt.Color;

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

    public MessageEmbed getPunishmentEmbed(User user, User responsible, String reason) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("FlareBot ModLog");
        eb.setColor(Color.WHITE);
        eb.addField("User", "`" + user.getName() + "#" + user.getDiscriminator() + "`", true);
        eb.addField("User ID", "`" + user.getId() + "`", true);
        if(responsible != null)
            eb.addField("Responsible", responsible.getAsMention(), true);
        String action = null;
        switch (punishment) {
            case PURGE:
                action = "Purge";
                break;
            case TEMP_MUTE:
                action = "Temp Mute";
                break;
            case MUTE:
                action = "Mute";
                break;
            case KICK:
                action = "Kick";
                break;
            case TEMP_BAN:
                action = "Temp Ban";
                break;
            case BAN:
                action = "Ban";
                break;
        }
        eb.addField("Action", action, true).addField("Reason", (reason != null ? reason : "No reason given!"), true);
        return eb.build();
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

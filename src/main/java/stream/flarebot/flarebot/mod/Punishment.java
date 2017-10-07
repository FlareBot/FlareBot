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
        eb.setTitle(getName());
        eb.setColor(Color.WHITE);
        eb.addField("User", user.getName() + "#" + user.getDiscriminator() + " (" + user.getId() + ")", true);
        if(responsible != null)
            eb.addField("Responsible moderator", responsible.getAsMention(), true);
        if(responsible != null || reason != null)
            eb.addField("Reason", (reason != null ? reason : "No reason given!"), true);
        return eb.build();
    }

    public enum EPunishment {
        PURGE,
        TEMP_MUTE,
        MUTE,
        UNMUTE,
        KICK,
        TEMP_BAN,
        BAN
    }
}

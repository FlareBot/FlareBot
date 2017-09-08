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

    public MessageEmbed getPunishmentEmbed(User user, User responsible) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("FlareBot ModLog");
        eb.setColor(Color.WHITE);
        switch (punishment) {
            case PURGE:
                eb.addField("User", "`" + user.getName() + "(" + user.getId() + ")`", true);
                if(responsible != null)
                    eb.addField("Responsible", responsible.getAsMention(), true);
                eb.addField("Action", "Purge", true);
                break;
            case TEMP_MUTE:
                eb.addField("User", "`" + user.getName() + "(" + user.getId() + ")`", true);
                if(responsible != null)
                    eb.addField("Responsible", responsible.getAsMention(), true);
                eb.addField("Action", "Temp Mute", true);
                break;
            case MUTE:
                eb.addField("User", "`" + user.getName() + "(" + user.getId() + ")`", true);
                if(responsible != null)
                    eb.addField("Responsible", responsible.getAsMention(), true);
                eb.addField("Action", "Mute", true);
                break;
            case KICK:
                eb.addField("User", "`" + user.getName() + "(" + user.getId() + ")`", true);
                if(responsible != null)
                    eb.addField("Responsible", responsible.getAsMention(), true);
                eb.addField("Action", "Kick", true);
                break;
            case TEMP_BAN:
                eb.addField("User", "`" + user.getName() + "(" + user.getId() + ")`", true);
                if(responsible != null)
                    eb.addField("Responsible", responsible.getAsMention(), true);
                eb.addField("Action", "Temp Ban", true);
                break;
            case BAN:
                eb.addField("User", "`" + user.getName() + "(" + user.getId() + ")`", true);
                if(responsible != null)
                    eb.addField("Responsible", responsible.getAsMention(), true);
                eb.addField("Action", "Ban", true);
                break;
        }
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

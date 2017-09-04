package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.entities.User;

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

    public String getPunishmentMessage(User user, User responsible) {
        String msg = "";
        switch (punishment) {
            case PURGE:
                msg = user.getAsMention() + " has been purged" + (responsible != null ? " by " + responsible.getAsMention() : ".");
                break;
            case TEMP_MUTE:
                msg = user.getName() + " has been temporarily muted" + (responsible != null ? " by " + responsible.getAsMention() : ".");
                break;
            case MUTE:
                msg = user.getName() + " has been muted by" + (responsible != null ? " by " + responsible.getAsMention() : ".");
                break;
            case KICK:
                msg = user.getName() + " has been kicked by" + (responsible != null ? " by " + responsible.getAsMention() : ".");
                break;
            case TEMP_BAN:
                msg = user.getName() + " has been temporarily banned by" + (responsible != null ? " by " + responsible.getAsMention() : ".");
                break;
            case BAN:
                msg = user.getName() + " has been banned by" + (responsible != null ? " by " + responsible.getAsMention() : ".");
                break;
        }
        return msg;
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

package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.awt.Color;

public class Punishment {

    private ModlogAction action;
    private long duration;

    public Punishment(ModlogAction action) {
        this.action = action;
    }

    public Punishment(ModlogAction action, int duration) {
        this.action = action;
        this.duration = duration;
    }

    public long getDuration() {
        return this.duration;
    }

    public String getName() {
        return action.name().charAt(0) + action.name().substring(1).toLowerCase().replaceAll("_", " ");
    }

    public ModlogAction getAction() {
        return action;
    }

    public MessageEmbed getActionEmbed(User user, User responsible, String reason) {
        return getActionEmbed(user, responsible, reason, true);
    }
    
    public MessageEmbed getActionEmbed(User user, User responsible, String reason, boolean showReason) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(getName());
        eb.setColor(Color.WHITE);
        eb.addField("User", user.getName() + "#" + user.getDiscriminator() + " (" + user.getId() + ")", true);
        if(responsible != null)
            eb.addField("Responsible moderator", responsible.getAsMention(), true);
        if((responsible != null || reason != null) && showReason)
            eb.addField("Reason", (reason != null ? reason : "No reason given!"), true);
        return eb.build();
    }
}

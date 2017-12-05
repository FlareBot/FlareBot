package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.mod.modlog.ModAction;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

public class Infraction {

    private ModAction action;
    private long duration;
    private int points;

    public Infraction(ModAction action, int points) {
        this.action = action;
        this.points = points;
    }

    public int getPoints() {
        return this.points;
    }

    public MessageEmbed getActionEmbed(User user, User responsible, String reason) {
        return getActionEmbed(user, responsible, reason, true);
    }

    public MessageEmbed getActionEmbed(User user, User responsible, String reason, boolean showReason) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(action.toString());
        eb.setColor(Color.WHITE);
        eb.addField("User", user.getName() + "#" + user.getDiscriminator() + " (" + user.getId() + ")", true);
        if (responsible != null)
            eb.addField("Responsible moderator", responsible.getAsMention(), true);
        if ((responsible != null || reason != null) && showReason)
            eb.addField("Reason", (reason != null ? reason : "No reason given!"), true);
        if (action.name().startsWith("TEMP"))
            eb.addField("Duration", FlareBot.getInstance().formatTime(duration, TimeUnit.MILLISECONDS, true, false), true);
        return eb.build();
    }
}

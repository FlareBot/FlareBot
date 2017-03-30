package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Predicate;

public enum Action {

    INVITE_LINK(2, MessageUtils::hasInvite),
    SPAM(1, message -> FlareBot.getInstance().getAutoModTracker().getMessages(message.getGuild().getId(), message.getAuthor().getId())
            >= FlareBot.getInstance().getManager().getAutoModConfig(message.getGuild().getId()).getMaxMessagesPerSecond()),
    LINKS(1, message -> message.getContent().matches("((http(s)?:\\/\\/)?(www\\.)?)[a-zA-Z0-9-]+\\.[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?\\/?(.+)?")),
    PROFANITY(1, message -> FlareBot.getInstance().getManager().getProfanity().stream().filter(word -> message.getContent().toLowerCase().contains(word)).count() > 0);

    public static Action[] values = values();

    private int points;
    private Predicate<Message> check;

    Action(int points, Predicate<Message> check){
        this.points = points;
        this.check = check;
    }

    public int getDefaultPoints(){
        return this.points;
    }

    public boolean check(Message message) {
        if(!FlareBot.getInstance().getPermissions(message.getTextChannel()).hasPermission(message.getGuild().getMember(message.getAuthor()), "flarebot.automod.bypass"))
            return this.check.test(message);
        else
            return false;
    }

    public String getName() {
        return name().charAt(0) + name().substring(1).toLowerCase().replaceAll("_", " ");
    }
}

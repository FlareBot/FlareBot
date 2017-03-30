package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Predicate;

public enum Action {

    INVITE_LINK(2, MessageUtils::hasInvite),
    SPAM(1, message -> FlareBot.getInstance().getAutoModTracker().getMessages(message.getGuild().getId(), message.getAuthor().getId())
            >= FlareBot.getInstance().getManager().getAutoModConfig(message.getGuild().getId()).getMaxMessagesPerSecond()),
    LINKS(1, message -> message.getContent().matches("((http(s)?:\\/\\/)?(www\\.)?)[a-zA-Z0-9]+.[a-zA-Z0-9]+(.[a-zA-Z0-9]+)?(\\/.)?")),
    PROFANITY(1, );

    private int points;

    Action(int points, Predicate<Message> check){
        this.points = points;
    }

    public int getDefaultPoints(){
        return this.points;
    }
}

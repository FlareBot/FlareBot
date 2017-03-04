package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.MessageUtils;
import net.dv8tion.jda.core.entities.Message;

import java.util.function.Predicate;

public enum Automod {
    INVITES(MessageUtils::hasInvite, SeverityLevel.MEDIUM);

    private final Predicate<Message> test;
    private final SeverityLevel severityLevel;

    Automod(Predicate<Message> test, SeverityLevel severityLevel) {
        this.test = test;
        this.severityLevel = severityLevel;
    }

    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    public boolean tryOn(Message message) {
        if (test.test(message)) {
            SeverityProvider.getSeverityFor(message.getGuild(), this).accept(message.getGuild().getMember(message.getAuthor()));
            return true;
        }
        return false;
    }
}

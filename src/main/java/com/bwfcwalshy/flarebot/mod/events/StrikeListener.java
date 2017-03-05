package com.bwfcwalshy.flarebot.mod.events;

import net.dv8tion.jda.core.entities.Member;

import java.util.function.Consumer;

public interface StrikeListener {
    void accept(Member member);

    default Consumer<Member> asConsumer() {
        return this::accept;
    }

    int getNeededStrikes();
    String getGuild();
}

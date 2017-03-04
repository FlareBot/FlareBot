package com.bwfcwalshy.flarebot.mod;

import net.dv8tion.jda.core.entities.Member;

import java.util.function.Consumer;

public enum SeverityLevel implements Consumer<Member> {
    NONE(m -> {}),
    SMALL(StrikeCounter::strike),
    MEDIUM(m -> StrikeCounter.strike(m, 2)),
    HIGH(m -> StrikeCounter.strike(m, 3)),
    INSTABAN(m -> m.getGuild().getController().ban(m, 14));

    private Consumer<Member> operation;

    SeverityLevel(Consumer<Member> operation){
        this.operation = operation;
    }

    @Override
    public void accept(Member member) {
        operation.accept(member);
    }
}

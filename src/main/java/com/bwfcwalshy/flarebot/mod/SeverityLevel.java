package com.bwfcwalshy.flarebot.mod;

import com.bwfcwalshy.flarebot.util.TriConsumer;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.function.Consumer;

public enum SeverityLevel implements TriConsumer<Member, TextChannel, Automod> {
    NONE(m -> {}, (m, c, a) -> {}),
    SMALL(StrikeCounter::strike, (m, c, a) -> c.sendMessage(m.getAsMention() + ", you have been given a strikes for violating " + a).complete()),
    MEDIUM(m -> StrikeCounter.strike(m, 2), (m, c, a) -> c.sendMessage(m.getAsMention() + ", you have been given 2 strikes for violating " + a).complete()),
    HIGH(m -> StrikeCounter.strike(m, 3), (m, c, a) -> c.sendMessage(m.getAsMention() + ", you have been given 3 strikes for violating " + a).complete()),
    INSTABAN(m -> m.getGuild().getController().ban(m, 14), (m, c, a) ->
            m.getUser().openPrivateChannel().complete().sendMessage("You have been banned for violating automod rule " + a).queue());

    private Consumer<Member> operation;
    private TriConsumer<Member, TextChannel, Automod> warning;

    SeverityLevel(Consumer<Member> operation, TriConsumer<Member, TextChannel, Automod> warning){
        this.operation = operation;
        this.warning = warning;
    }

    @Override
    public void accept(Member member, TextChannel channel, Automod automod) {
        warning.accept(member, channel, automod);
        operation.accept(member);
    }
}

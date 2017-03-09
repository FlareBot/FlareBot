package com.bwfcwalshy.flarebot.mod;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public enum OnStrikeActions {
    BAN(m -> m.getGuild().getController().ban(m, 14).complete()),
    KICK(m -> m.getGuild().getController().kick(m).complete());

    private Map<String, Integer> guild = new ConcurrentHashMap<>();
    private Consumer<Member> action;

    OnStrikeActions(Consumer<Member> action) {
        this.action = action;
    }

    public boolean on(Member member, int i) {
        if (guild.containsKey(member.getGuild().getId())) {
            if (guild.get(member.getGuild().getId()) > i) {
                action.accept(member);
                return true;
            }
        }
        return false;
    }

    public void setNeededPoints(int i, Guild g) {
        guild.put(g.getId(), i);
    }
}

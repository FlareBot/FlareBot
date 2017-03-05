package com.bwfcwalshy.flarebot.mod.events;

import com.bwfcwalshy.flarebot.mod.StrikeCounter;
import net.dv8tion.jda.core.entities.Member;

public class KickStrikeListener implements StrikeListener {
    private int neededStrikes;
    private String guild;

    public KickStrikeListener(int neededStrikes, String guild) {
        this.neededStrikes = neededStrikes;
        this.guild = guild;
    }

    @Override
    public void accept(Member member) {
        if (member.getGuild().getId().equals(guild) && StrikeCounter.getStrikes(member) == neededStrikes)
            member.getGuild().getController().kick(member)
                    .queue(d -> {
                    }, d -> {
                    });
    }

    public int getNeededStrikes() {
        return neededStrikes;
    }

    public String getGuild() {
        return guild;
    }
}

package com.bwfcwalshy.flarebot.mod.events;

import com.bwfcwalshy.flarebot.mod.StrikeCounter;
import net.dv8tion.jda.core.entities.Member;

public class BanStrikeListener implements StrikeListener {
    private int neededStrikes;
    private String guild;

    public BanStrikeListener(int neededStrikes, String guild) {
        this.neededStrikes = neededStrikes;
        this.guild = guild;
    }

    @Override
    public void accept(Member member) {
        if (member.getGuild().getId().equals(guild) && StrikeCounter.getStrikes(member) == neededStrikes)
            member.getGuild().getController().ban(member, 14)
                    .queue(d -> StrikeCounter.resetStrikes(member), d -> {
                    });
    }

    public int getNeededStrikes() {
        return neededStrikes;
    }

    public String getGuild() {
        return guild;
    }
}

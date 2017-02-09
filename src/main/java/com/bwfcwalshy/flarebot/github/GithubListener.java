package com.bwfcwalshy.flarebot.github;

import com.arsenarsen.githubwebhooks4j.events.EventListener;
import com.arsenarsen.githubwebhooks4j.events.PushEvent;
import com.arsenarsen.githubwebhooks4j.objects.Commit;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import net.dv8tion.jda.core.EmbedBuilder;

public class GithubListener implements EventListener<PushEvent> {

    @Override
    public void handle(PushEvent e) {
        if (!FlareBot.getInstance().isReady())
            return;
        EmbedBuilder eb = MessageUtils.getEmbed();
        eb.setAuthor(e.getSender().getLogin(), e.getSender().getProfile(), e.getSender().getAvatarUrl());
        StringBuilder sb = new StringBuilder();
        for (Commit commit : e.getCommits()) {
            sb.append(String.format("`%s` in `%s` - [`%s`](%s) %s\n",
                    commit.getAuthor().getName(),
                    e.getRef().substring(e.getRef().lastIndexOf('/') + 1),
                    commit.getId().substring(0, 7),
                    commit.getUrl(),
                    commit.getMessage()));
        }
        eb.setDescription(sb.toString());
        FlareBot.getInstance().getChannelByID("229236239201468417").sendMessage(eb.build()).queue();
    }
}

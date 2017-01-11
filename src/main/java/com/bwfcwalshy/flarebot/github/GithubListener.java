package com.bwfcwalshy.flarebot.github;

import com.arsenarsen.githubwebhooks4j.events.EventListener;
import com.arsenarsen.githubwebhooks4j.events.PushEvent;
import com.arsenarsen.githubwebhooks4j.objects.Commit;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import sx.blah.discord.util.EmbedBuilder;

public class GithubListener implements EventListener<PushEvent> {

    @Override
    public void handle(PushEvent e) {
        if (!FlareBot.getInstance().getClient().isReady())
            return;
        EmbedBuilder eb = MessageUtils.getEmbed();
        eb.withAuthorIcon(e.getSender().getAvatarUrl());
        eb.withAuthorName(e.getSender().getLogin());
        eb.withAuthorUrl(e.getSender().getProfile());
        StringBuilder sb = new StringBuilder();
        for (Commit commit : e.getCommits()) {
            sb.append(String.format("`%s` in `%s` - [`%s`](%s) %s\n",
                    commit.getAuthor().getName(),
                    e.getRef(),
                    commit.getId().substring(0, 7),
                    commit.getUrl(),
                    commit.getMessage()));
        }
        eb.withDesc(sb.toString());
        MessageUtils.sendMessage(eb, FlareBot.getInstance().getClient().getChannelByID("229236239201468417"));
    }
}

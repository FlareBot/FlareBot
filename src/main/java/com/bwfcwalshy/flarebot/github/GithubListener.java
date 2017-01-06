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
        for (Commit commit : e.getCommits()) {
            eb.appendField(commit.getAuthor().getUsername()
                    + " - " + commit.getId().substring(0, 7),
                    String.format("`%s` - [`%s`](%s) %s",
                            commit.getAuthor(),
                            commit.getId().substring(0, 7),
                            commit.getUrl(),
                            commit.getMessage()), true);
        }
        MessageUtils.sendMessage(eb.build(), FlareBot.getInstance().getClient().getChannelByID("229236239201468417"));
    }
}

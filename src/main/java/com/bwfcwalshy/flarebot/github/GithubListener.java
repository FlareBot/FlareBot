package com.bwfcwalshy.flarebot.github;

import com.arsenarsen.githubwebhooks4j.events.EventListener;
import com.arsenarsen.githubwebhooks4j.events.PushEvent;
import com.arsenarsen.githubwebhooks4j.objects.Commit;
import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;

public class GithubListener implements EventListener<PushEvent> {

    private IChannel githubChannel;

    @Override
    public void handle(PushEvent e) {
        StringBuilder sb = new StringBuilder();
        sb.append("New push by **")
                .append(e.getPusher().getName())
                .append("** in branch ")
                .append("`")
                .append(e.getRef().substring(e.getRef().lastIndexOf('/') + 1))
                .append("`\n");
        for(Commit commit : e.getCommits()){
            sb.append("`")
                    .append(commit.getId()).append("` ")
                    .append(commit.getMessage());
        }
        MessageUtils.sendMessage(FlareBot.getInstance().getClient().getChannelByID("229236239201468417"), sb.toString());
    }
}

package stream.flarebot.flarebot.github;

import com.arsenarsen.githubwebhooks4j.events.EventListener;
import com.arsenarsen.githubwebhooks4j.events.PushEvent;
import com.arsenarsen.githubwebhooks4j.objects.Commit;
import net.dv8tion.jda.core.EmbedBuilder;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.MessageUtils;

public class GithubListener implements EventListener<PushEvent> {

    @Override
    public void handle(PushEvent e) {
        if (!FlareBot.getInstance().isReady())
            return;
        EmbedBuilder eb = MessageUtils.getEmbed();
        eb.setAuthor(e.getSender().getLogin(), e.getSender().getProfile(), e.getSender().getAvatarUrl());
        for (Commit commit : e.getCommits()) {
            eb.addField("Commit:", "[" +
                    commit.getId().substring(0, 7) + "](" +
                    commit.getUrl() + ")\n Branch `" +
                    e.getRef().substring(e.getRef().lastIndexOf('/') + 1) + "` " + "```" +
                    commit.getMessage() + "```", false);

            if (commit.getAdded().length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("```md\n");
                int i = 1;
                for (String filePath : commit.getAdded()) {
                    if (i++ <= 5) {
                        String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                        sb.append("* " + file + "\n\n");
                    }
                }

                if (commit.getRemoved().length >= 5) {
                    sb.append("...\n");
                }

                String added = sb.toString() + "```";
                eb.addField("Added files", added, false);
            }

            if (commit.getRemoved().length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("```md\n");
                int i = 1;
                for (String filePath : commit.getRemoved()) {
                    if (i++ <= 5) {
                        String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                        sb.append("* " + file + "\n\n");
                    }
                }

                if (commit.getRemoved().length >= 5) {
                    sb.append("...\n");
                }

                String removed = sb.toString() + "```";
                eb.addField("Removed files", removed, false);
            }

            if (commit.getModified().length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("```md\n");
                int i = 1;
                for (String filePath : commit.getModified()) {
                    if (i++ <= 5) {
                        String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                        sb.append("* " + file + "\n\n");
                    }
                }

                if (commit.getModified().length >= 5) {
                    sb.append("...\n");
                }

                String modified = sb.toString() + "```";
                eb.addField("Modified files", modified, false);
            }
        }
        FlareBot.getInstance().getChannelByID("229236239201468417").sendMessage(eb.build()).queue();
    }
}

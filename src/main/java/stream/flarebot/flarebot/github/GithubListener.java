package stream.flarebot.flarebot.github;

import com.arsenarsen.githubwebhooks4j.events.EventListener;
import com.arsenarsen.githubwebhooks4j.events.PushEvent;
import com.arsenarsen.githubwebhooks4j.objects.Commit;
import net.dv8tion.jda.core.EmbedBuilder;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.Arrays;

public class GithubListener implements EventListener<PushEvent> {

    @Override
    public void handle(PushEvent e) {
        if (!FlareBot.getInstance().isReady())
            return;
        EmbedBuilder eb = MessageUtils.getEmbed();
        eb.setAuthor(e.getSender().getLogin(), e.getSender().getProfile(), e.getSender().getAvatarUrl());
        for (Commit commit : Arrays.copyOfRange(e.getCommits(), Math.max(e.getCommits().length - 6, 0), e.getCommits().length - 1)) {
            eb.addField("Commit:", "[" +
                    commit.getId().substring(0, 7) + "](" +
                    commit.getUrl() + ")\n Branch `" +
                    e.getRef().substring(e.getRef().lastIndexOf('/') + 1) + "` " + "```" +
                    commit.getMessage().substring(0, Math.min(commit.getMessage().length(), 80) - 1) +
                    (commit.getMessage().length() > 80 ? "..." : "")
                    + "```", false);

            if (commit.getAdded().length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("```md\n");
                int i = 1;
                for (String filePath : Arrays.copyOfRange(commit.getAdded(), Math.max(commit.getAdded().length - 6, 0), commit.getAdded().length - 1)) {
                    String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                    sb.append("* ").append(file).append("\n");
                }

                if (commit.getAdded().length >= 5) {
                    sb.append("...\n");
                }

                String added = sb.toString() + "```";
                eb.addField("Added files", added, false);
            }

            if (commit.getRemoved().length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("```md\n");
                int i = 1;
                for (String filePath : Arrays.copyOfRange(commit.getRemoved(), Math.max(commit.getRemoved().length - 6, 0), commit.getRemoved().length - 1)) {
                    String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                    sb.append("* ").append(file).append("\n");
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
                for (String filePath : Arrays.copyOfRange(commit.getModified(), Math.max(commit.getModified().length - 6, 0), commit.getModified().length - 1)) {
                    String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                    sb.append("* ").append(file).append("\n");
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

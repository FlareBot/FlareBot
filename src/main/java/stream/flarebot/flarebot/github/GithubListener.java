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
        Commit[] commits = Arrays.copyOfRange(e.getCommits(), Math.max(e.getCommits().length - 6, 0), e.getCommits().length);
        for (Commit commit : commits) {
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

                String[] added = Arrays.copyOfRange(commit.getAdded(), Math.max(commit.getAdded().length - 6, 0), commit.getAdded().length - 1);
                for (String filePath : added) {
                    String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                    sb.append("* ").append(file).append("\n");
                }

                if (commit.getAdded().length >= 5) {
                    sb.append("...\n");
                }

                eb.addField("Added files", sb.toString() + "```", false);
            }

            if (commit.getRemoved().length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("```md\n");

                String[] removed = Arrays.copyOfRange(commit.getRemoved(), Math.max(commit.getRemoved().length - 6, 0), commit.getRemoved().length);
                for (String filePath : removed) {
                    String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                    sb.append("* ").append(file).append("\n");
                }

                if (commit.getRemoved().length >= 5) {
                    sb.append("...\n");
                }

                eb.addField("Removed files", sb.toString() + "```", false);
            }

            if (commit.getModified().length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("```md\n");

                String[] modified = Arrays.copyOfRange(commit.getModified(), Math.max(commit.getModified().length - 6, 0), commit.getModified().length);
                for (String filePath : modified) {
                    String file = filePath.substring(filePath.lastIndexOf("/") + 1);
                    sb.append("* ").append(file).append("\n");
                }

                if (commit.getModified().length >= 5) {
                    sb.append("...\n");
                }

                eb.addField("Modified files", sb.toString() + "```", false);
            }
        }
        FlareBot.getInstance().getChannelByID("229236239201468417").sendMessage(eb.build()).queue();
    }
}

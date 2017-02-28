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
		for (Commit commit : e.getCommits()) {
			eb.addField("Commit:", "[" + 
			commit.getId().substring(0, 7) + "](" + 
			commit.getUrl() + ")\n Branch `" + 
			e.getRef().substring(e.getRef().lastIndexOf('/') + 1) + "` " + "```" + 
			commit.getMessage() + "```", false);

			if (commit.getAdded().length > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("```Markdown\n");
				for (String file : commit.getAdded()) {
					sb.append("* " + file + "\n\n");
				}
				String added = sb.toString() + "```";
				eb.addField("Added files", added, false);
			}

			if (commit.getRemoved().length > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("```Markdown\n");
				for (String file : commit.getRemoved()) {
					sb.append("* " + file + "\n\n");
				}
				String removed = sb.toString() + "```";
				eb.addField("Removed files", removed, false);
			}
			
			if (commit.getModified().length > 0) {
				StringBuilder sb = new StringBuilder();
				sb.append("```Markdown\n");
				for (String file : commit.getModified()) {
					sb.append("* " + file + "\n\n");
				}
				String modified = sb.toString() + "```";
				eb.addField("Modified files", modified, false);
			}
		}
		FlareBot.getInstance().getChannelByID("229236239201468417").sendMessage(eb.build()).queue();
	}
}

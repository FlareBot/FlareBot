package stream.flarebot.flarebot.util.votes;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VoteGroup {

    private int yesVotes = 0;
    private int noVotes = 0;

    private final Set<Long> users = new HashSet<>();
    private Set<Long> allowedUsers = new HashSet<>();

    private final String messageDesc;

    private Message voteMessage;
    private EmbedBuilder votesEmbed;

    private UUID id;

    public VoteGroup(String messageDesc, UUID id) {
        this.messageDesc = messageDesc;
        this.id = id;
    }

    public boolean addVote(Vote vote, User user) {
        if (allowedUsers != null && voteMessage != null && !allowedUsers.contains(user.getIdLong()))
            return false;
        if (!users.contains(user.getIdLong())) {
            if (vote == Vote.NO)
                noVotes++;
            else
                yesVotes++;

            if (allowedUsers.size() == totalVotes()) {
                VoteUtil.finishNow(id, voteMessage.getGuild());
                return true;
            }

            users.add(user.getIdLong());
            if (voteMessage != null) {
                votesEmbed.clearFields();
                votesEmbed.addField("Yes", String.valueOf(yesVotes), true);
                votesEmbed.addField("No", String.valueOf(noVotes), true);
                voteMessage.editMessage(votesEmbed.build()).queue();
            }
            return true;
        } else
            return false;
    }

    public Vote won() {
        if (yesVotes > noVotes)
            return Vote.YES;
        else if (noVotes > yesVotes)
            return Vote.NO;
        else
            return Vote.NONE;
    }

    public int totalVotes() {
        return yesVotes + noVotes;
    }

    public void setVoteMessage(Message message) {
        this.voteMessage = message;
    }

    public void setVotesEmbed(EmbedBuilder embed) {
        this.votesEmbed = embed;
    }

    public Message getVoteMessage() {
        return voteMessage;
    }

    public String getMessageDesc() {
        return messageDesc;
    }

    public enum Vote {
        YES,
        NO,
        NONE;

        public static Vote parseVote(String vote) {
            for (Vote v : values()) {
                if (v.name().equalsIgnoreCase(vote))
                    return v;
            }
            return Vote.NONE;
        }
    }

    public interface VoteRunnable {
        void run(Vote vote);
    }

    public void limitUsers(Collection<? extends User> users) {
        this.allowedUsers = new HashSet<>();
        for (User user : users) {
            allowedUsers.add(user.getIdLong());
        }
    }

    public void addAllowedUser(User user) {
        allowedUsers.add(user.getIdLong());
    }

    public void remoreAllowedUser(User user) {
        allowedUsers.remove(user.getIdLong());
    }
}

package stream.flarebot.flarebot.util.votes;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.List;

public class VoteGroup {

    private int yesVotes = 0;
    private int noVotes = 0;

    private final List<Long> users = new ArrayList<>();
    private List<Long> allowedUsers = null;

    private final String messageDesc;

    private Message voteMessage;
    private EmbedBuilder votesEmbed;

    public VoteGroup(String messageDesc) {
        this.messageDesc = messageDesc;
    }

    public boolean addVote(Vote vote, User user) {
        if (allowedUsers != null && voteMessage != null && !allowedUsers.contains(user.getIdLong()))
            return false;
        if (!users.contains(user.getIdLong())) {
            if (vote == Vote.NO)
                noVotes++;
            else
                yesVotes++;
            if (allowedUsers.size() == votes()) {
                VoteUtil.finishNow(messageDesc, voteMessage.getGuild());
                return true;
            }
            users.add(user.getIdLong());
            if (voteMessage != null) {
                votesEmbed.clearFields();
                votesEmbed.addField("Yes Votes", String.valueOf(yesVotes), true);
                votesEmbed.addField("No Votes", String.valueOf(noVotes), true);
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
            Vote parsedVote = Vote.valueOf(vote.toUpperCase());
            return parsedVote == Vote.NONE ? null : parsedVote;
        }
    }

    public interface VoteRunnable {
        void run(Vote vote);
    }

    public void limitUsers() {
        limitUsers(new ArrayList<>());
    }

    public void limitUsers(List<User> users) {
        List<Long> userIds = new ArrayList<>();
        for (User user : users) {
            userIds.add(user.getIdLong());
        }
        this.allowedUsers = userIds;
    }

    public void addAllowedUser(User user) {
        allowedUsers.add(user.getIdLong());
    }

    public void remoreAllowedUser(User user) {
        allowedUsers.remove(user.getIdLong());
    }
}

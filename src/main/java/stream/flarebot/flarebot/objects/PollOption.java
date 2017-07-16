package stream.flarebot.flarebot.objects;

import java.util.ArrayList;
import java.util.List;

public class PollOption {

    private String option;
    private int votes;
    private List<String> voters = new ArrayList<>();

    public PollOption(String option) {
        this.option = option;
    }

    public void incrementVotes(String userId) {
        this.votes++;
        this.voters.add(userId);
    }

    public String getOption() {
        return option;
    }

    public int getVotes() {
        return this.votes;
    }

    public List<String> getVoters() {
        return voters;
    }
}

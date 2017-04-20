package stream.flarebot.flarebot.objects;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PollOption {

    private String option;
    private Set<String> votes = ConcurrentHashMap.newKeySet();

    public PollOption(String option) {
        this.option = option;
    }

    public void incrementVotes(String userId) {
        this.votes.add(userId);
    }

    public String getOption() {
        return option;
    }

    public int getVotes() {
        return this.votes.size();
    }
}

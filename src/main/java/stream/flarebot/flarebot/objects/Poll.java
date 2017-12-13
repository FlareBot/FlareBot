package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Poll {

    private String question;
    private List<PollOption> pollOptions;
    private PollStatus status;
    private LocalDateTime endTime;
    private Color pollColor;
    private String pollChannel;

    public Poll(String question, TextChannel channel) {
        this.pollChannel = channel.getId();
        this.question = question;
        pollOptions = new ArrayList<>();
        endTime = LocalDateTime.now().plusHours(1);
        status = PollStatus.EDITING;
    }

    public String getQuestion() {
        return this.question;
    }

    public List<PollOption> getPollOptions() {
        return this.pollOptions;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isOpen() {
        return this.status == PollStatus.OPEN;
    }

    public boolean isClosed() {
        return this.status == PollStatus.CLOSED;
    }

    public PollStatus getStatus() {
        return this.status;
    }

    public void setStatus(PollStatus status) {
        if (status.equals(this.status)) {
            throw new IllegalStateException("This poll is already " + status.name().toLowerCase() + "!");
        }
        this.status = status;
        if (status == PollStatus.OPEN) {
            FlareBot.getInstance().getChannelById(pollChannel).sendMessage(getPollEmbed("New Poll", "A new poll has been opened!").build()).queue();
        } else {
            FlareBot.getInstance().getChannelById(pollChannel).sendMessage(getClosedPollEmbed("Poll Closed!", "The poll has been closed!").build()).queue();
        }
    }

    public void setColor(Color color) {
        this.pollColor = color;
    }

    public void setChannel(String channel) {
        this.pollChannel = channel;
    }

    public EmbedBuilder getPollEmbed(String title, String description) {
        EmbedBuilder builder = new EmbedBuilder().setTitle(title, null)
                .setDescription(description)
                .addField("Question", WordUtils.capitalizeFully(this.question), false);
        getPollOptions().forEach(option -> builder.addField("Option " + (getPollOptions().indexOf(option) + 1), option.getOption() + "\nVotes: " + option.getVotes(), true));
        builder.setColor(pollColor)
                .addBlankField(false)
                .addField("End", (isClosed() ? "Closed" : "The poll will be ending at `" + GeneralUtils.formatTime(getEndTime()) + "`"), false)
                .addField("Total Votes", String.valueOf(getPollOptions().stream().mapToInt(PollOption::getVotes).sum()), true);
        return builder;
    }

    public EmbedBuilder getClosedPollEmbed(String title, String description) {
        List<PollOption> orderedOptions = new ArrayList<>(getPollOptions());
        orderedOptions.sort((o1, o2) -> o2.getVotes() - o1.getVotes());
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.RED).setTitle("Poll Closed", null)
                .setDescription("The poll has been closed!\nHere are the results: ");
        orderedOptions.forEach(option -> builder.addField(option.getOption(), "Final votes: " + option.getVotes(), false));
        builder.addBlankField(false)
                .addField("Question", WordUtils.capitalizeFully(this.question), false)
                .addField("Total Votes", String.valueOf(getPollOptions().stream().mapToInt(PollOption::getVotes).sum()), false)
                .setColor(Color.RED)
        ;
        return builder;
    }

    public enum PollStatus {
        EDITING,
        OPEN,
        CLOSED
    }
}

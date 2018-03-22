package stream.flarebot.flarebot.util.votes;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import stream.flarebot.flarebot.scheduler.FlareBotTask;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.buttons.ButtonUtil;
import stream.flarebot.flarebot.util.objects.ButtonGroup;

import java.util.HashMap;
import java.util.Map;

public class VoteUtil {

    private static Map<String, VoteGroup> groupMap = new HashMap<>();
    private static Map<String, VoteGroup.VoteRunnable> runnableMap = new HashMap<>();

    public static void sendVoteMessage(VoteGroup.VoteRunnable voteRunnable, VoteGroup group, long timeout, TextChannel channel, ButtonGroup.Button... optionalButtons) {
        EmbedBuilder votesEmbed = new EmbedBuilder();
        votesEmbed.setDescription("Vote to " + group.getMessageDesc());
        votesEmbed.addField("Yes Votes", "0", true);
        votesEmbed.addField("No Votes", "0", true);
        String messageDesc = group.getMessageDesc();
        votesEmbed.setColor(ColorUtils.FLAREBOT_BLUE);
        group.setVotesEmbed(votesEmbed);
        ButtonGroup buttonGroup = new ButtonGroup();

        groupMap.put(group.getMessageDesc() + channel.getGuild().getId(), group);
        runnableMap.put(group.getMessageDesc() + channel.getGuild().getId(), voteRunnable);

        buttonGroup.addButton(new ButtonGroup.Button(355776056092917761L, (user, message) -> {
            if (group.addVote(VoteGroup.Vote.YES, user)) {
                MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription("You voted yes on " + messageDesc).build(), 2000, channel);
            } else {
                MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription("You cannot vote on " + messageDesc).build(), 2000, channel);
            }
        }));
        buttonGroup.addButton(new ButtonGroup.Button(355776081384570881L, (user, message) -> {
            if (group.addVote(VoteGroup.Vote.NO, user)) {
                MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription("You voted no on " + messageDesc).build(), 2000, channel);
            } else {
                MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription("You cannot vote on " + messageDesc).build(), 2000, channel);
            }
        }));
        for(ButtonGroup.Button button : optionalButtons) {
            buttonGroup.addButton(button);
        }
        Message voteMessage = ButtonUtil.sendReturnedButtonedMessage(channel, votesEmbed.build(), buttonGroup);
        group.setVoteMessage(voteMessage);

        new FlareBotTask("Votes-" + voteMessage.getId()){

            @Override
            public void run() {
                voteRunnable.run(group.won());
                groupMap.remove(group.getMessageDesc() + channel.getGuild().getId());
                runnableMap.remove(group.getMessageDesc() + channel.getGuild().getId());
                channel.deleteMessageById(voteMessage.getId()).queue();
            }

        }.delay(timeout);
    }

    public static VoteGroup getVoteGroup(String messageDesc, Guild guild) {
        return groupMap.get(messageDesc + guild.getId());
    }

    public static boolean contains(String messageDesc, Guild guild) {
        return groupMap.containsKey(messageDesc + guild.getId());
    }

    public static void remove(String messageDesc, Guild guild) {
        VoteGroup group = groupMap.get(messageDesc + guild.getId());
        String message = group.getVoteMessage().getId();
        groupMap.remove(messageDesc + guild.getId());
        Scheduler.cancelTask("Vote-" + message);
        group.getVoteMessage().getChannel().deleteMessageById(group.getVoteMessage().getId()).queue();
    }

    public static void finishNow(String messageDesc, Guild guild) {
        VoteGroup group = groupMap.get(messageDesc + guild.getId());
        String message = group.getVoteMessage().getId();
        groupMap.remove(messageDesc + guild.getId());
        runnableMap.get(messageDesc + guild.getId()).run(group.won());
        runnableMap.remove(messageDesc + guild.getId());
        Scheduler.cancelTask("Vote-" + message);
        group.getVoteMessage().getChannel().deleteMessageById(group.getVoteMessage().getId()).queue();
    }
}

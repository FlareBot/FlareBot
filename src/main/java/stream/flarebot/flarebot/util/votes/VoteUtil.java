package stream.flarebot.flarebot.util.votes;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.scheduler.FlareBotTask;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.buttons.ButtonUtil;
import stream.flarebot.flarebot.util.objects.ButtonGroup;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoteUtil {

    private static Map<String, VoteGroup> groupMap = new ConcurrentHashMap<>();
    private static Map<String, VoteGroup.VoteRunnable> runnableMap = new ConcurrentHashMap<>();

    public static void sendVoteMessage(UUID id, VoteGroup.VoteRunnable voteRunnable, VoteGroup group, long timeout, TextChannel channel, User user, ButtonGroup.Button... optionalButtons) {
        EmbedBuilder votesEmbed = new EmbedBuilder();
        votesEmbed.setDescription("Vote to " + group.getMessageDesc());
        votesEmbed.addField("Yes Votes", "0", true);
        votesEmbed.addField("No Votes", "0", true);
        String messageDesc = group.getMessageDesc();
        votesEmbed.setColor(ColorUtils.FLAREBOT_BLUE);
        group.setVotesEmbed(votesEmbed);
        ButtonGroup buttonGroup = new ButtonGroup(user.getIdLong());

        groupMap.put(id + channel.getGuild().getId(), group);
        runnableMap.put(id + channel.getGuild().getId(), voteRunnable);

        buttonGroup.addButton(new ButtonGroup.Button(355776056092917761L, (owner, user1, message) -> {
            if (group.addVote(VoteGroup.Vote.YES, user1)) {
                MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription("You voted yes on " + messageDesc).build(), 2000, channel);
            } else {
                MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription("You cannot vote on " + messageDesc).build(), 2000, channel);
            }
        }));
        buttonGroup.addButton(new ButtonGroup.Button(355776081384570881L, (owner, user1, message) -> {
            if (group.addVote(VoteGroup.Vote.NO, user1)) {
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

    public static VoteGroup getVoteGroup(UUID uuid, Guild guild) {
        return groupMap.get(uuid + guild.getId());
    }

    public static boolean contains(UUID uuid, Guild guild) {
        return groupMap.containsKey(uuid + guild.getId());
    }

    public static void remove(UUID uuid, Guild guild) {
        VoteGroup group = groupMap.get(uuid + guild.getId());
        String message = group.getVoteMessage().getId();
        groupMap.remove(uuid + guild.getId());
        Scheduler.cancelTask("Vote-" + message);
        group.getVoteMessage().getChannel().deleteMessageById(group.getVoteMessage().getId()).queue();
    }

    public static void finishNow(UUID uuid, Guild guild) {
        VoteGroup group = groupMap.get(uuid + guild.getId());
        String message = group.getVoteMessage().getId();
        groupMap.remove(uuid + guild.getId());
        runnableMap.get(uuid + guild.getId()).run(group.won());
        runnableMap.remove(uuid + guild.getId());
        Scheduler.cancelTask("Vote-" + message);
        group.getVoteMessage().getChannel().deleteMessageById(group.getVoteMessage().getId()).queue();
    }
}

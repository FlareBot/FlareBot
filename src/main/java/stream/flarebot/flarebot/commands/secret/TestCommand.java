package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.VariableUtils;
import stream.flarebot.flarebot.util.votes.VoteGroup;
import stream.flarebot.flarebot.util.votes.VoteUtil;

import java.util.concurrent.TimeUnit;

public class TestCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length == 0) {
            VoteUtil.sendVoteMessage((vote) -> {
                channel.sendMessage("Vote: " + vote).queue();
            }, new VoteGroup("test"), TimeUnit.MINUTES.toMillis(1), channel);
        } else {
            if(VoteUtil.contains("test", channel.getGuild())) {
                VoteGroup.Vote vote = VoteGroup.Vote.parseVote(args[0]);
                if(vote == null) {
                    MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription(args[0] + " Isn't a valid vote").build(), 5000, channel);
                } else {
                    VoteUtil.getVoteGroup("test", channel.getGuild()).addVote(vote, sender);
                }
            }
        }
    }

    @Override
    public String getCommand() {
        return "test";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return "{%}test";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }
}

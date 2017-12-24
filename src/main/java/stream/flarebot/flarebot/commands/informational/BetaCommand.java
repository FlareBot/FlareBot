package stream.flarebot.flarebot.commands.informational;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.*;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.stream.Collectors;

public class BetaCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        String betaInfo = GeneralUtils.formatCommandPrefix(channel,FlareBot.getInstance().getCommands()
                .stream()
                .filter(Command::isBetaTesterCommand)
                .map(c -> "`{%}" + c.getCommand() + "` - " + c.getDescription())
                .collect(Collectors.joining("\n")));
        String betaMessage = guild.isBetaAccess() ?
                "Thank you for being apart of our beta program! Please report any bugs or give us suggestions over at the [support discord](" + FlareBot.INVITE_URL + ")!"
                : "Listed below are the commands you can gain access to by being apart of our beta program! To join the beta program, you will either need to win " +
                "beta access through a giveaway (Make sure you are in the [support server](" + FlareBot.INVITE_URL + ") to hear of these!) or you can become apart of the " +
                "Donator tier or higher on our [patreon page](https://www.patreon.com/flarebot)!";
        EmbedBuilder builder = MessageUtils.getEmbed(sender);
        builder.setColor(Color.CYAN);
        builder.setDescription(betaMessage);
        builder.addField("Beta Commands", betaInfo, false);
        channel.sendMessage(builder.build()).queue();
    }

    @Override
    public String getCommand() {
        return "beta";
    }

    @Override
    public String getDescription() {
        return "Shows all the beta commands";
    }

    @Override
    public String getUsage() {
        return "`{%}beta` - Shows the beta information";
    }

    @Override
    public CommandType getType() {
        return CommandType.INFORMATIONAL;
    }
}

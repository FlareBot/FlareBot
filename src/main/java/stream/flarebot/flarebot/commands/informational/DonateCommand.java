package stream.flarebot.flarebot.commands.informational;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.*;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class DonateCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        EmbedBuilder builder = MessageUtils.getEmbed(sender);
        builder.setColor(Color.CYAN);
        builder.setDescription("Thank for you taking the time to see how you can donate to the project! This project relies heavily upon user donations so every little helps! \uD83D\uDE0A");
        builder.addField("Patreon", "The most common way to donate is through our [Patreon page](https://www.patreon.com/flarebot)! Through here you can donate the amount you want and also get rewarded for it!", false);
        builder.addField("PayPal", "If you cannot donate through Patreon or don't like the idea of a monthly subscription then this option is for you! Simply join our [support server](" + FlareBot.INVITE_URL + ") and our amazing staff will assist you in doing this!\n\nShould you want to donate anonymously or you don't want to recieve an award, you can simply send money to `walshydev@gmail.com` via PayPal to donate!", false);
        channel.sendMessage(builder.build()).queue();
    }

    @Override
    public String getCommand() {
        return "donate";
    }

    @Override
    public String getDescription() {
        return "Shows users where they can donate to the project!";
    }

    @Override
    public String getUsage() {
        return "`{%}donate` - Shows donation options";
    }

    @Override
    public CommandType getType() {
        return CommandType.INFORMATIONAL;
    }
}

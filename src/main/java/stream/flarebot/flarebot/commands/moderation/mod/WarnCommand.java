package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class WarnCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel, sender).queue();
        } else {
            User user = GeneralUtils.getUser(args[0]);
            if (user == null) {
                MessageUtils.sendErrorMessage("We couldn't find that user!!", channel);
                return;
            }
            String reason = MessageUtils.getMessage(args, 1);
            guild.addWarning(user, reason);
            EmbedBuilder eb = new EmbedBuilder();
            eb.appendDescription("You have been warned in the `" + guild.getGuild().getName() + "(" + guild.getGuildId() + ")` guild");
            eb.addField("Reason", "```" + reason + "```", false);
            eb.addField("WarningsCommand", String.valueOf(guild.getUserWarnings(user).size()), true);
            eb.setColor(Color.CYAN);
            MessageUtils.sendPM(channel, user, eb);
        }
    }

    @Override
    public String getCommand() {
        return "warn";
    }

    @Override
    public String getDescription() {
        return "Warns a user";
    }

    @Override
    public String getUsage() {
        return "`{%}warn <user> <reason>` - warns a user";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

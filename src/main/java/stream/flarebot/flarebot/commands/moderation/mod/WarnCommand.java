package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.ModlogAction;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class WarnCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            User user = GeneralUtils.getUser(args[0]);
            if (user == null) {
                MessageUtils.sendErrorMessage("We couldn't find that user!!", channel);
                return;
            }
            String reason = null;
            if (args.length >= 2) reason = MessageUtils.getMessage(args, 1);
            guild.addWarning(user, (reason != null ? reason : "No reason provided - action done by " + sender.getName()));
            guild.getAutoModConfig().postToModLog(user, sender, ModlogAction.WARN.toPunishment(), reason);
            EmbedBuilder eb = new EmbedBuilder();
            eb.appendDescription("\u26A0 Warned " + MessageUtils.getTag(user)
                    + (reason != null ? " (`" + reason.replaceAll("`", "'") + "`)" : ""))
                    .setColor(Color.WHITE);
            channel.sendMessage(eb.build()).queue();
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
        return "`{%}warn <user> (reason)` - Warns a user";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

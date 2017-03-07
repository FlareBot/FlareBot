package com.bwfcwalshy.flarebot.commands.moderation;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.mod.Automod;
import com.bwfcwalshy.flarebot.mod.SeverityLevel;
import com.bwfcwalshy.flarebot.mod.SeverityProvider;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class SetSeverityCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length != 2) {
            channel.sendMessage("Two arguments required!").queue();
            return;
        }
        Automod automod;
        SeverityLevel severity;
        try {
            automod = Automod.valueOf(args[0].toUpperCase());
            severity = SeverityLevel.valueOf(args[1].toUpperCase());
        } catch (Exception e) {
            channel.sendMessage("No such automod/severity!");
            return;
        }
        SeverityProvider.setSeverityFor(channel.getGuild(), automod, severity);
        channel.sendMessage("Success!");
    }

    @Override
    public String getCommand() {
        return "setseverity";
    }

    @Override
    public String getDescription() {
        return "`%psetseverity automod-rule severity` - Sets a severity level for an automod rule. Rule list soon:tm:";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

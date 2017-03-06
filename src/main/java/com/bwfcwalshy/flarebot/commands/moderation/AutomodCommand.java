package com.bwfcwalshy.flarebot.commands.moderation;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.mod.Automod;
import com.bwfcwalshy.flarebot.util.HelpFormatter;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class AutomodCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length != 1 || args[0].toLowerCase().matches("on|off")) {
            channel.sendMessage(HelpFormatter.on(channel, getDescription())).queue();
        } else {
            Automod.setEnabled(channel.getGuild(), args[0].equalsIgnoreCase("on"));
            channel.sendMessage("Turned automod " + args[0].toLowerCase()).queue();
        }
    }

    @Override
    public String getCommand() {
        return "automod";
    }

    @Override
    public String getDescription() {
        return "%pautomod on/off - Toggles automod on or off";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String getPermission() {
        return "flarebot.automod.command";
    }
}

package com.bwfcwalshy.flarebot.commands.automod;

import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.mod.OnStrikeActions;
import com.bwfcwalshy.flarebot.util.HelpFormatter;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class ActionCommand implements Command {
    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 2) {
            int points;
            OnStrikeActions action;
            try {
                points = Integer.parseInt(args[1]);
                action = OnStrikeActions.valueOf(args[0].toUpperCase());
            } catch (Exception e) {
                channel.sendMessage(HelpFormatter.on(channel, "Usage: `%psetaction RULE STRIKES`")).queue();
                return;
            }
            action.setNeededPoints(points, channel.getGuild());
        } else {
            channel.sendMessage(HelpFormatter.on(channel, "Usage: `%psetaction RULE STRIKES`")).queue();
        }
    }

    @Override
    public String getCommand() {
        return "setaction";
    }

    @Override
    public String getDescription() {
        return "Sets an automod action";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String getPermission() {
        return "flarebot.automod.setaction";
    }
}

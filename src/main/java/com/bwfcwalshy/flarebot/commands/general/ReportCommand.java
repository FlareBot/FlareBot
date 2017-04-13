package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.commands.FlareBotManager;
import com.bwfcwalshy.flarebot.util.SQLController;
import com.bwfcwalshy.flarebot.util.SQLTask;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.Arrays;

public class ReportCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            if (FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.reports.view")) {

            } else {

            }
        } else if (args.length == 1) {
            switch (args[0]) {
                case "resolve": {
                    if (FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.reports.view")) {
                        EmbedBuilder eb = MessageUtils.getEmbed(sender);
                        eb.setDescription("Current server reports");

                    } else {
                        MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription("You need `flarebot.reports.view` in order to do this!").setColor(Color.red).build(), 500, channel);
                    }
                }
                break;
            }
        } else {
            if (FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.report")) {
                String userString = args[0];
                StringBuilder sb = new StringBuilder();
                for (String string : Arrays.copyOfRange(args, 1, args.length - 1)) {
                    sb.append(string + " ");
                }

                User user = MessageUtils.getUserFromString(userString, channel);
                String reportMessage = sb.toString();


            } else {
                MessageUtils.sendAutoDeletedMessage(new EmbedBuilder().setDescription("You need `flarebot.report` in order to do this!").setColor(Color.red).build(), 500, channel);
            }
        }
    }

    @Override
    public String getCommand() {
        return "reports";
    }

    @Override
    public String getDescription() {
        return "Reports a user.";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

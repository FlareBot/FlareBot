package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.util.Welcome;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class WelcomeCommand implements Command {

    private FlareBot flareBot;

    public WelcomeCommand(FlareBot bot) {
        this.flareBot = bot;
    }

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            channel.sendMessage(sender.getAsMention() + " Usage: " +
                    FlareBot.getPrefixes().get(channel.getGuild().getId()) + "welcome <enable/disable/set/message>").queue();
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("enable")) {
                if (flareBot.getWelcomeForGuild(channel.getGuild()) == null) {
                    flareBot.getWelcomes().add(new Welcome(channel.getGuild().getId(), channel.getId()));
                    channel.sendMessage("Welcomes **enabled**!").queue();
                } else {
                    channel.sendMessage("Welcomes already **enabled**!").queue();
                }
            } else if (args[0].equalsIgnoreCase("disable")) {
                if (flareBot.getWelcomeForGuild(channel.getGuild()) != null) {
                    flareBot.getWelcomes().remove(flareBot.getWelcomeForGuild(channel.getGuild()));
                    channel.sendMessage("Welcomes **disabled**!").queue();
                } else {
                    channel.sendMessage("Welcomes already **disabled**!").queue();
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                if (flareBot.getWelcomeForGuild(channel.getGuild()) != null) {
                    Welcome welcome = flareBot.getWelcomeForGuild(channel.getGuild());
                    welcome.setChannelId(channel.getId());
                    channel.sendMessage("Welcomes set to appear in this channel!").queue();
                } else {
                    channel.sendMessage("Welcomes are not enabled!").queue();
                }
            } else if (args[0].equalsIgnoreCase("message")) {
                channel.sendMessage(sender.getAsMention() +
                        " To set a new message do " + FlareBot.getPrefixes().get(channel.getGuild().getId()) + "welcome message (message)\n" +
                        "Known variables are:\n" +
                        "``%user%`` for the username,\n" +
                        "``%mention%`` to mention the user, and\n" +
                        "``%guild%`` for the guild name.\n" +
                        (flareBot.getWelcomeForGuild(channel.getGuild()) == null ? "" : "The current message is: ```md\n"
                                + flareBot.getWelcomeForGuild(channel.getGuild()).getMessage() + "```")).queue();
            } else {
                channel.sendMessage(sender.getAsMention()
                        + " Usage: " + FlareBot.getPrefixes().get(channel.getGuild().getId()) + "welcome <enable/disable/set/message>").queue();
            }
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("message")) {
                if (flareBot.getWelcomeForGuild(channel.getGuild()) != null) {
                    String msg = "";
                    for (int i = 1; i < args.length; i++) {
                        msg += args[i] + (i == args.length - 1 ? "" : " ");
                    }
                    Welcome welcome = flareBot.getWelcomeForGuild(channel.getGuild());
                    welcome.setMessage(msg);
                    channel.sendMessage("Set welcome message to ```" + msg + "```").queue();
                } else {
                    channel.sendMessage("Welcomes are not enabled!").queue();
                }
            } else {
                channel.sendMessage(sender.getAsMention() + " Usage: " +
                        FlareBot.getPrefixes().get(channel.getGuild().getId()) + "welcome <enable/disable/set/message>").queue();
            }
        } else {
            channel.sendMessage(sender.getAsMention() + " Usage: " +
                    FlareBot.getPrefixes().get(channel.getGuild().getId()) + "welcome <enable/disable/set/message>").queue();
        }
    }

    @Override
    public String getCommand() {
        return "welcome";
    }

    @Override
    public String getDescription() {
        return "Add welcome messages to your server!";
    }

    @Override
    public CommandType getType() {
        return CommandType.ADMINISTRATIVE;
    }

    @Override
    public String getPermission() {
        return "flarebot.welcome";
    }
}

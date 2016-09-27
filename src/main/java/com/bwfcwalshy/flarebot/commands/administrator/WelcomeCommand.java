package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.util.Welcome;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class WelcomeCommand implements Command {

    private FlareBot flareBot;
    public WelcomeCommand(FlareBot bot){
        this.flareBot = bot;
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if (FlareBot.getInstance().getPermissions(channel).hasPermission(sender, "flarebot.commands.welcome")){
            if (args.length == 0) {
                MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "welcome <enable/disable/set/message>");
            }else if(args.length == 1) {
                if(args[0].equalsIgnoreCase("enable")){
                    if(flareBot.getWelcomeForGuild(channel.getGuild()) == null){
                        flareBot.getWelcomes().add(new Welcome(channel.getGuild().getID(), channel.getID()));
                        MessageUtils.sendMessage(channel, "Welcomes **enabled**!");
                    }else{
                        MessageUtils.sendMessage(channel, "Welcomes already **enabled**!");
                    }
                }else if(args[0].equalsIgnoreCase("disable")) {
                    if (flareBot.getWelcomeForGuild(channel.getGuild()) != null) {
                        flareBot.getWelcomes().remove(flareBot.getWelcomeForGuild(channel.getGuild()));
                        MessageUtils.sendMessage(channel, "Welcomes **disabled**!");
                    } else {
                        MessageUtils.sendMessage(channel, "Welcomes already **disabled**!");
                    }
                }else if(args[0].equalsIgnoreCase("set")) {
                    if (flareBot.getWelcomeForGuild(channel.getGuild()) != null) {
                        Welcome welcome = flareBot.getWelcomeForGuild(channel.getGuild());
                        welcome.setChannelId(channel.getID());
                        MessageUtils.sendMessage(channel, "Welcomes set to appear in this channel!");
                    } else {
                        MessageUtils.sendMessage(channel, "Welcomes are not enabled!");
                    }
                }else if(args[0].equalsIgnoreCase("message")){
                    MessageUtils.sendMessage(channel, sender.mention() + " To set a new message do " + FlareBot.COMMAND_CHAR + "welcome message (message)\n" +
                            (flareBot.getWelcomeForGuild(channel.getGuild()) == null ? "" : "The current message is: ```"
                                    + flareBot.getWelcomeForGuild(channel.getGuild()).getMessage() + "```"));
                }else{
                    MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "welcome <enable/disable/set/message>");
                }

            // Don't really want 2 maps just for if they are doing it, what channel and a message. :/
            }else if(args.length >= 2){
                if(args[0].equalsIgnoreCase("message")){
                    if(flareBot.getWelcomeForGuild(channel.getGuild()) != null) {
                        String msg = "";
                        for (int i = 1; i < args.length; i++) {
                            msg += args[i] + (i == args.length - 1 ? "" : " ");
                        }
                        Welcome welcome = flareBot.getWelcomeForGuild(channel.getGuild());
                        welcome.setMessage(msg);
                        MessageUtils.sendMessage(channel, "Set welcome message to ```" + msg + "```");
                    }else{
                        MessageUtils.sendMessage(channel, "Welcomes are not enabled!");
                    }
                }else{
                    MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "welcome <enable/disable/set/message>");
                }
            }else{
                MessageUtils.sendMessage(channel, sender.mention() + " Usage: " + FlareBot.COMMAND_CHAR + "welcome <enable/disable/set/message>");
            }
        }else{
            MessageUtils.sendMessage(channel, "Missing permission `flarebot.commands.welcome`");
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
    public String getPermission(){
        return "flarebot.welcome";
    }
}

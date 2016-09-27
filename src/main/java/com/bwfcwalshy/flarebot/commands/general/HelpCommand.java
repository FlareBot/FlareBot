package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class HelpCommand implements Command {

    private FlareBot flareBot;
    public HelpCommand(FlareBot bot){
        this.flareBot = bot;
    }

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(args.length == 1){
            CommandType type = CommandType.valueOf(args[0].toUpperCase());
            if(type != null && type != CommandType.HIDDEN){
                StringBuilder sb = new StringBuilder();
                sb.append("**FlareBot _" + type.toString() + "_ Commands**\n```xl\n");
                for(Command commands : flareBot.getCommandsByType(type)){
                    sb.append("  " + FlareBot.COMMAND_CHAR).append(commands.getCommand()).append(" - ").append(commands.getDescription()).append("\n");
                }
                sb.append("```");

                MessageUtils.sendMessage(channel, sb.toString());
            }else{
                sendCommands(channel);
            }
        }else{
            sendCommands(channel);
        }
    }

    private void sendCommands(IChannel channel){
        StringBuilder sb = new StringBuilder();
        sb.append("**FlareBot commands**\n```xl\n");
        for(CommandType type : CommandType.getTypes()) {
            sb.append(type.toString()).append("\n");
            for (Command commands : flareBot.getCommandsByType(type)) {
                sb.append("  " + FlareBot.COMMAND_CHAR).append(commands.getCommand()).append(" - ").append(commands.getDescription()).append("\n");
            }
            sb.append("\n");
        }
        sb.append("```");

        MessageUtils.sendMessage(channel, sb.toString());
    }

    @Override
    public String getCommand() {
        return "commands";
    }

    @Override
    public String[] getAliases(){
        return new String[] {"help"};
    }

    @Override
    public String getDescription() {
        return "See a list of all commands.";
    }

    @Override
    public CommandType getType() { return CommandType.GENERAL; }
}

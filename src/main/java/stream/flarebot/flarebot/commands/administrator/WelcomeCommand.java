package stream.flarebot.flarebot.commands.administrator;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class WelcomeCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if(args.length == 1){
            MessageUtils.getUsage(this, channel, sender).queue();
        } else if(args.length == 2){
            if(args[1].equalsIgnoreCase("enable")){
                if(args[0].equalsIgnoreCase("dm")){
                    if(guild.getWelcome().isDmEnabled()){
                        guild.getWelcome().setDmEnabled(true);
                        channel.sendMessage("DM welcomes are now **enabled**").queue();
                    } else {
                        MessageUtils.sendErrorMessage("DM welcomes are already **enabled**", channel);
                    }
                } else if(args[0].equalsIgnoreCase("guild")){
                    if(guild.getWelcome().isGuildEnabled()){
                        guild.getWelcome().setGuildEnabled(true);
                        channel.sendMessage("Guild welcomes are now **enabled**").queue();
                    }else {
                        MessageUtils.sendErrorMessage("Guild welcomes are already **enabled**", channel);
                    }
                } else {
                    MessageUtils.getUsage(this, channel, sender).queue();
                }
            } else if(args[1].equalsIgnoreCase("disable")){
                if(args[0].equalsIgnoreCase("dm")){
                    if(!guild.getWelcome().isDmEnabled()){
                        guild.getWelcome().setDmEnabled(false);
                        channel.sendMessage("DM welcomes are now **disabled**").queue();
                    } else {
                        MessageUtils.sendErrorMessage("DM welcomes are already **disabled**", channel);
                    }
                } else if(args[0].equalsIgnoreCase("guild")){
                    if(!guild.getWelcome().isGuildEnabled()){
                        guild.getWelcome().setGuildEnabled(false);
                        channel.sendMessage("Guild welcomes are now **disabled**").queue();
                    }else {
                        MessageUtils.sendErrorMessage("Guild welcomes are already **disabled**", channel);
                    }
                } else {
                    MessageUtils.getUsage(this, channel, sender).queue();
                }
            } else {
                MessageUtils.getUsage(this, channel, sender).queue();
            }
        } else if(args.length >= 3){
            if(args[1].equalsIgnoreCase("message")){
                if(args[2].equalsIgnoreCase("list")) {
                    int page = args[3] == null ? 1 : Integer.valueOf(args[3]);
                    List<String> messages;
                    if (args[0].equalsIgnoreCase("dm")) {
                        messages = guild.getWelcome().getDmMessages();
                    } else if (args[0].equalsIgnoreCase("guild")) {
                        messages = guild.getWelcome().getGuildMessages();
                    } else {
                        MessageUtils.getUsage(this, channel, sender).queue();
                        return;
                    }
                    int messagesLength = 15;
                    int pages = messages.size() < messagesLength ? 1 : (messages.size() / messagesLength) + (messages.size() % messagesLength != 0 ? 1 : 0);
                    int start =  messagesLength * (page - 1);
                    int end = Math.min(start + messagesLength, messages.size());
                    if (page > pages || page < 0) {
                        MessageUtils.sendErrorMessage("That page doesn't exist. Current page count: " + pages, channel);
                    } else {
                        List<String> messagesSub = messages.subList(start, end);
                        List<List<String>> body = new ArrayList<>();
                        int i = 0;
                        for (String messagesMessage : messagesSub) {
                            List<String> part = new ArrayList<>();
                            part.add(String.valueOf(i));
                            part.add(messagesMessage);
                            body.add(part);
                            i++;
                        }
                        List<String> header = new ArrayList<>();
                        header.add("Id");
                        header.add("Message");
                        channel.sendMessage(MessageUtils.makeAsciiTable(header, body, " Reports Page " + GeneralUtils.getPageOutOfTotal(page, messages, messagesLength))).queue();
                    }
                }else if(args[2].equalsIgnoreCase("add")){
                    String welcomeMessage = MessageUtils.getMessage(args, 4);
                    if(args[0].equalsIgnoreCase("dm")){
                        guild.getWelcome().getDmMessages().add(welcomeMessage);
                    } else if(args[0].equalsIgnoreCase("guild")){
                        guild.getWelcome().getGuildMessages().add(welcomeMessage);
                    } else {
                        MessageUtils.getUsage(this, channel, sender).queue();
                    }
                } else if(args[2].equalsIgnoreCase("remove") && args.length == 4){
                    int id = Integer.valueOf(args[3]);
                    if(args[0].equalsIgnoreCase("dm")){
                        guild.getWelcome().getDmMessages().remove(id);
                    } else if(args[0].equalsIgnoreCase("guild")){
                        guild.getWelcome().getGuildMessages().remove(id);
                    }else {
                        MessageUtils.getUsage(this, channel, sender).queue();
                    }
                } else {
                    MessageUtils.getUsage(this, channel, sender).queue();
                }
            } else {
                MessageUtils.getUsage(this, channel, sender).queue();
            }
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
    public String getUsage() {
        return "`{%}welcome <dm/guild> <enable/disable> `- Enables/Disables welcome message for dm/guild\n" +
                "`{%}welcome <dm/guild> message add <message>` - Adds a message to the welcomes\n" +
                "`{%}welcome <dm/guild> message remove <message id>` - Removes a message from the welcomes\n" +
                "`{%}welcome <dm/guild> message list [page]` - Lists all the messages and their IDs for welcomes";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}

package com.bwfcwalshy.flarebot.commands.general;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import com.bwfcwalshy.flarebot.objects.Poll;
import com.bwfcwalshy.flarebot.objects.PollOption;
import com.bwfcwalshy.flarebot.permissions.PerGuildPermissions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PollCommand implements Command {

    private Map<String, Poll> polls = new HashMap<>();

    private FlareBot flareBot = FlareBot.getInstance();

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        PerGuildPermissions perms = flareBot.getPermissions(channel);
        String guildId = channel.getGuild().getId();
        if(args.length == 0){
            if(polls.containsKey(channel.getGuild().getId())){
                Poll poll = polls.get(channel.getGuild().getId());
                EmbedBuilder builder = new EmbedBuilder().addField("Question", poll.getQuestion(), false);
                builder.setColor(poll.getColor());
                poll.getPollOptions().forEach(option -> builder.addField("Option " + (poll.getPollOptions().indexOf(option)+1), option.getOption() + "\nVotes: " + option.getVotes(), true));
                builder.addBlankField(false);
                builder.addField("End", "The poll will be ending at `" + flareBot.formatTime(poll.getEndTime()) + "`", false)
                        .addField("Total Votes", String.valueOf(poll.getPollOptions().stream()
                        .mapToInt(PollOption::getVotes).sum()), true);
                channel.sendMessage(builder.build()).queue();
            }else{
                MessageUtils.sendErrorMessage("There are no polls running!" + (!perms.hasPermission(member, "flarebot.poll.create") ? "" :
                        " To start a poll you can use " + FlareBot.getPrefix(channel.getGuild().getId()) + "poll create (Question)"), channel);
            }
        }else if(args.length == 1){
            if(args[0].equalsIgnoreCase("create")) {
                MessageUtils.sendErrorMessage("Usage: " + FlareBot.getPrefix(channel.getGuild().getId()) + "poll create (Question)", channel);
            }else if(args[0].equalsIgnoreCase("set")) {

            }else if(args[0].equalsIgnoreCase("close")) {
                if(polls.containsKey(channel.getGuild().getId())){
                    Poll poll = polls.get(guildId);
                    if(poll.isOpen()){
                        poll.setOpen(false);
                        polls.put(guildId, poll);
                        channel.sendMessage(new EmbedBuilder().setColor(Color.red).setTitle("Poll Closed", null).setDescription("The poll has been closed!\nHere are the results: " +
                                "").build()).queue();
                        //TODO: Finish this off
                    }else{
                        MessageUtils.sendErrorMessage("The current poll isn't open!", channel);
                    }
                }else{
                    channel.sendMessage(new EmbedBuilder().setDescription("There is no poll currently open!").build()).queue();
                }
            }else if(args[0].equalsIgnoreCase("edit")) {

            }else{

            }
        }else if(args.length >= 2){
            if(args[0].equalsIgnoreCase("create")){
                if(polls.containsKey(channel.getGuild().getId())){
                    MessageUtils.sendErrorMessage("Close current poll, also make this message better", channel);
                }else{
                    String question = FlareBot.getMessage(args, 1);
                    this.polls.put(guildId, new Poll(question));
                    channel.sendMessage(new EmbedBuilder().setTitle("Poll created", null).setColor(Color.green).setAuthor(sender.getName(), null, sender.getEffectiveAvatarUrl())
                            .setDescription("Poll has been created! To close the poll use `poll close` and to edit the auto-close time of the poll do `poll set closetime (time eg 10m)`\n" +
                                    "To open the poll for people to vote on do `poll open`!").build()).queue();
                }
            }else if(args[0].equalsIgnoreCase("edit")){
                if(args[1].equalsIgnoreCase("colour") || args[1].equalsIgnoreCase("color")){
                    if(args.length < 3){
                        MessageUtils.sendErrorMessage("Usage: `poll set color (color)`", channel);
                        return;
                    }
                    if(polls.containsKey(channel.getGuild().getId())){
                        try {
                            Poll poll = polls.get(channel.getGuild().getId());
                            poll.setColor(Color.decode(args[2]));
                            this.polls.put(guildId, poll);
                            channel.sendMessage(new EmbedBuilder().setColor(Color.decode(args[2])).setDescription("Changed the color of the poll to `" + args[2] + "`").build()).queue();
                        }catch(NumberFormatException e){
                            MessageUtils.sendErrorMessage("That is not a valid color input!", channel);
                            return;
                        }
                    }else{
                        MessageUtils.sendErrorMessage("There is no poll currently, use `poll create` to make one!", channel);
                    }
                }
            }else if(args[0].equalsIgnoreCase("options")){
                if(args[1].equalsIgnoreCase("add")){
                    if(perms.hasPermission(member, "flarebot.poll.options.add")){
                        String option = FlareBot.getMessage(args, 2);
                        Poll poll = polls.get(channel.getGuild().getId());
                        poll.getPollOptions().add(new PollOption(option));
                        polls.put(guildId, poll);
                        channel.sendMessage("Added the option `" + option + "` to the poll!").queue();
                    }else
                        listOptions(channel);
                }else if(args[1].equalsIgnoreCase("remove")){

                }else{
                    listOptions(channel);
                }
            }else if(args[0].equalsIgnoreCase("vote")) {
                Poll poll = polls.get(channel.getGuild().getId());
                if(!poll.isOpen()){
                    channel.sendMessage(new EmbedBuilder().setDescription("There is no poll currently open!").build()).queue();
                }
                int option;
                try{
                    option = Integer.parseInt(args[1]);
                }catch(NumberFormatException e){
                    MessageUtils.sendErrorMessage("That is not a valid number!", channel);
                    return;
                }

                PollOption pollOption = poll.getPollOptions().get(option-1);
                if(pollOption == null){
                    MessageUtils.sendErrorMessage("That is not a valid option!", channel);
                    return;
                }
                pollOption.incrementVotes(sender.getId());
                polls.put(guildId, poll);
            }
        }
    }

    private void listOptions(TextChannel channel){

    }

    @Override
    public String getCommand() {
        return "poll";
    }

    @Override
    public String getDescription() {
        return "Create a poll for your community to vote on!";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

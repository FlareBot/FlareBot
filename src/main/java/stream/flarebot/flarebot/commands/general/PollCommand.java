package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.commands.FlareBotManager;
import stream.flarebot.flarebot.objects.Poll;
import stream.flarebot.flarebot.objects.PollOption;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;

import java.awt.*;

public class PollCommand implements Command {

    private FlareBot flareBot = FlareBot.getInstance();
    private FlareBotManager manager = flareBot.getManager();

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        PerGuildPermissions perms = flareBot.getPermissions(channel);
        String guildId = channel.getGuild().getId();
        if (args.length == 0) {
            if (manager.getPolls().containsKey(channel.getGuild().getId())) {
                Poll poll = manager.getPolls().get(channel.getGuild().getId());
                channel.sendMessage(poll.getPollEmbed("Poll", null).build()).queue();
            } else {
                MessageUtils.sendErrorMessage("There are no polls running!" + (!perms
                        .hasPermission(member, "flarebot.poll.create") ? "" :
                        " To start a poll you can use " + FlareBot
                                .getPrefix(channel.getGuild().getId()) + "poll create (Question)"), channel);
            }
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("create")) {
                MessageUtils.sendErrorMessage("Usage: " + FlareBot
                        .getPrefix(channel.getGuild().getId()) + "poll create (Question)", channel);
            } else if (args[0].equalsIgnoreCase("set")) {

            } else if (args[0].equalsIgnoreCase("close")) {
                if (manager.getPolls().containsKey(channel.getGuild().getId())) {
                    Poll poll = manager.getPolls().get(guildId);
                    if (poll.isOpen()) {
                        poll.setStatus(Poll.PollStatus.CLOSED);
                        manager.getPolls().put(guildId, poll);
                        channel.sendMessage(poll.getClosedPollEmbed("Poll Closed!", "The poll has been closed!")
                                .build()).queue();
                    } else {
                        MessageUtils.sendErrorMessage("The current poll isn't open!", channel);
                    }
                } else {
                    channel.sendMessage(new EmbedBuilder().setDescription("There is no poll currently open!").build())
                            .queue();
                }
            } else if (args[0].equalsIgnoreCase("edit")) {

            } else if (args[0].equalsIgnoreCase("open")) {
                if (manager.getPolls().containsKey(channel.getGuild().getId())) {
                    Poll poll = manager.getPolls().get(guildId);
                    poll.setChannel(channel.getId());
                    poll.setStatus(Poll.PollStatus.OPEN);
                    manager.getPolls().put(guildId, poll);
                }
            } else {

            }
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("create")) {
                if (manager.getPolls().containsKey(channel.getGuild().getId()) && manager
                        .getPollFromGuild(channel.getGuild()).isOpen()) {
                    MessageUtils.sendErrorMessage("Close current poll, also make this message better", channel);
                } else {
                    String question = MessageUtils.getMessage(args, 1);
                    manager.getPolls().put(guildId, new Poll(question));
                    channel.sendMessage(new EmbedBuilder().setTitle("Poll created", null).setColor(Color.green)
                            .setAuthor(sender.getName(), null, sender
                                    .getEffectiveAvatarUrl())
                            .setDescription("Poll has been created! To close the poll use `poll close` and to edit the auto-close time of the poll do `poll set closetime (time eg 10m)`\n" +
                                    "To open the poll for people to vote on do `poll open`!")
                            .build()).queue();
                }
            } else if (args[0].equalsIgnoreCase("edit")) {
                if (args[1].equalsIgnoreCase("colour") || args[1].equalsIgnoreCase("color")) {
                    if (args.length < 3) {
                        MessageUtils.sendErrorMessage("Usage: `poll edit colour (color)`", channel);
                        return;
                    }
                    if (manager.getPolls().containsKey(channel.getGuild().getId())) {
                        try {
                            Poll poll = manager.getPolls().get(channel.getGuild().getId());
                            poll.setColor(Color.decode(args[2]));
                            manager.getPolls().put(guildId, poll);
                            channel.sendMessage(new EmbedBuilder().setColor(Color.decode(args[2]))
                                    .setDescription("Changed the color of the poll to `" + args[2] + "`")
                                    .build()).queue();
                        } catch (NumberFormatException e) {
                            MessageUtils.sendErrorMessage("That is not a valid color input!", channel);
                            return;
                        }
                    } else {
                        MessageUtils
                                .sendErrorMessage("There is no poll currently, use `poll create` to make one!", channel);
                    }
                }
            } else if (args[0].equalsIgnoreCase("options")) {
                if (args[1].equalsIgnoreCase("add")) {
                    if (perms.hasPermission(member, "flarebot.poll.options.add")) {
                        if (args.length >= 3) {
                            String option = MessageUtils.getMessage(args, 2);
                            Poll poll = manager.getPolls().get(channel.getGuild().getId());
                            poll.getPollOptions().add(new PollOption(option));
                            manager.getPolls().put(guildId, poll);
                            channel.sendMessage("Added the option `" + option + "` to the poll!").queue();
                        } else {
                            MessageUtils.sendErrorMessage("Usage: `poll options add (option)`", channel);
                        }
                    } else
                        listOptions(channel);
                } else if (args[1].equalsIgnoreCase("remove")) {

                } else {
                    listOptions(channel);
                }
            } else if (args[0].equalsIgnoreCase("vote")) {
                Poll poll = manager.getPolls().get(channel.getGuild().getId());
                if (!poll.isOpen()) {
                    channel.sendMessage(new EmbedBuilder().setDescription("There is no poll currently open!").build())
                            .queue();
                }
                int option;
                try {
                    option = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage("That is not a valid number!", channel);
                    return;
                }

                PollOption pollOption = poll.getPollOptions().get(option - 1);
                if (pollOption == null) {
                    MessageUtils.sendErrorMessage("That is not a valid option!", channel);
                    return;
                }
                pollOption.incrementVotes(sender.getId());
                manager.getPolls().put(guildId, poll);
            }
        }
    }

    private void listOptions(TextChannel channel) {
        Poll poll = manager.getPollFromGuild(channel.getGuild());
        EmbedBuilder builder = new EmbedBuilder().setTitle("Options", null)
                .setDescription("Options for `" + poll.getPollOptions() + "`");
        poll.getPollOptions().forEach(option -> builder
                .addField("Option " + (poll.getPollOptions().indexOf(option) + 1), option
                        .getOption() + "\nVotes: " + option.getVotes(), true));
        channel.sendMessage(builder.build()).queue();
    }

    @Override
    public String getCommand() {
        return "poll";
    }

    @Override
    public String getDescription() {
        return "Create a poll for your community to vote on!";
    }

    //TODO: Finish when poll is fully done
    @Override
    public String getUsage() {
        return "`{%}poll ";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

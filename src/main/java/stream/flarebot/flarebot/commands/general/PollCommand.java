package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.Poll;
import stream.flarebot.flarebot.objects.PollOption;
import stream.flarebot.flarebot.util.ColorEnum;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class PollCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            int closedSize = guild.getPolls().stream().filter(Poll::isClosed).collect(Collectors.toList()).size();
            if (guild.getPolls().size() == 0 || (closedSize > 0 && guild.getPolls().size() == closedSize)) {
                MessageUtils.sendInfoMessage("This guild has no polls currently running!" +
                        (closedSize > 0 && getPermissions(channel).hasPermission(member, "flarebot.poll.open")
                                ? String.format("\nThis guild has %d closed " + (guild.getPolls().size() == 1 ? "poll" : "polls"), closedSize) : ""), channel, sender);
            } else {
                MessageUtils.sendInfoMessage(String.format("This guild has %d " + (guild.getPolls().size() == 1 ? "poll" : "polls"), guild.getPolls().size()), channel, sender);
            }
            return;
        } else {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length >= 2) {
                    if (guild.getPolls().size() >= 4) {
                        MessageUtils.sendErrorMessage("You cannot have more that 4 polls at any one time!", channel);
                        return;
                    }
                    String question = MessageUtils.getMessage(args, 1);
                    if (question.length() > 1024) {
                        MessageUtils.sendErrorMessage("The question you entered is too long! It has to be 1024 characters or shorter!", channel);
                        return;
                    }
                    Poll poll = new Poll(question, channel);
                    guild.getPolls().add(poll);
                    channel.sendMessage(new EmbedBuilder().setTitle("Poll created", null)
                            .setColor(Color.green).setAuthor(sender.getName(), null, sender.getEffectiveAvatarUrl())
                            .setDescription(String.format("Poll with ID %d has been created! Remember to set the close-time and the options for this poll!\n" +
                                    "To open the poll for people to vote on do `poll open`!", guild.getPolls().indexOf(poll) + 1))
                            .build()
                    ).queue();
                    return;
                }
            } else if (args[0].equalsIgnoreCase("list")) {
                EmbedBuilder builder = MessageUtils.getEmbed(sender);
                if (guild.getPolls().size() == 0) {
                    MessageUtils.sendInfoMessage("This guild has no polls currently running!", channel, sender);
                    return;
                }
                for (Poll poll : guild.getPolls()) {
                    builder.addField("Poll ID: " + (guild.getPolls().indexOf(poll) + 1)
                            , "Status: " + WordUtils.capitalizeFully(poll.getStatus().name()) + "\n" +
                                    "Poll Options: " + String.valueOf(poll.getPollOptions().size())
                            , true);
                }
                channel.sendMessage(builder.build()).queue();
                return;
            } else if (args[0].equalsIgnoreCase("close") ||
                    args[0].equalsIgnoreCase("open") ||
                    args[0].equalsIgnoreCase("remove")) {
                if (guild.getPolls().size() == 0) {
                    MessageUtils.sendInfoMessage("This guild has no polls currently running!", channel, sender);
                    return;
                } else if (args.length == 2) {
                    int index;
                    String action = "";

                    try {
                        index = Integer.parseInt(args[1]) - 1;
                        if (args[0].equalsIgnoreCase("close")) {
                            if (!getPermissions(channel).hasPermission(member, "flarebot.poll.close")) {
                                MessageUtils.sendErrorMessage("You need the permission `flarebot.poll.close` to do this!", channel);
                                return;
                            }

                            guild.getPolls().get(index).setStatus(Poll.PollStatus.CLOSED);

                            action = "closed";
                        } else if (args[0].equalsIgnoreCase("open")) {
                            if (!this.getPermissions(channel).hasPermission(member, "flarebot.poll.open")) {
                                MessageUtils.sendErrorMessage("You need the permission `flarebot.poll.open` to do this!", channel);
                                return;
                            }
                            guild.getPolls().get(index).setStatus(Poll.PollStatus.OPEN);
                            action = "opened";
                        } else {
                            if (!this.getPermissions(channel).hasPermission(member, "flarebot.poll.remove")) {
                                MessageUtils.sendErrorMessage("You need the permission `flarebot.poll.remove` to do this!", channel);
                                return;
                            }
                            guild.getPolls().remove(index);
                            action = "removed";
                        }
                    } catch (IndexOutOfBoundsException e) {
                        MessageUtils.sendErrorMessage("Please provide a valid poll ID!\n" +
                                "Use the poll list command to see the IDs!", channel);
                        return;
                    } catch (IllegalStateException e) {
                        MessageUtils.sendErrorMessage(e.getMessage(), channel);
                        return;
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage("That is not a valid poll ID!", channel);
                        return;
                    }

                    MessageUtils.sendSuccessMessage("Successfully " + action + " the poll with the ID: " + String.valueOf(index + 1), channel, sender);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                // {%}poll set <id> <property> <value>
                if (args.length == 4) {

                    Poll poll = getPollById(args[1], guild.getPolls(), channel);
                    if (poll == null) {
                        return;
                    }

                    if (args[2].equalsIgnoreCase("colour") || args[2].equalsIgnoreCase("color")) {
                        Color color;
                        try {
                            color = Color.decode(args[3]);
                        } catch (NumberFormatException e) {
                            try {
                                color = ColorEnum.getColorByName(args[3]).get();
                            } catch (NoSuchElementException ex) {
                                MessageUtils.sendErrorMessage("That is not a valid color input!", channel);
                                return;
                            }
                        }
                        channel.sendMessage(new EmbedBuilder().setColor(color)
                                .setDescription("Changed the color of the poll to `" + GeneralUtils.colourFormat(color) + "`")
                                .build())
                                .queue();
                        poll.setColor(color);
                        return;
                    }

                }
            } else if (args[0].equalsIgnoreCase("options")) {
                if (args.length >= 3) {
                    Poll poll = getPollById(args[1], guild.getPolls(), channel);
                    if (poll == null) {
                        return;
                    }

                    if (args[2].equalsIgnoreCase("view") && args.length == 4) {
                        int optionId;
                        PollOption option;

                        try {
                            optionId = Integer.parseInt(args[3]) - 1;
                            option = poll.getPollOptions().get(optionId);
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            MessageUtils.sendErrorMessage("Please provide a valid poll option ID!\n" +
                                    "Use the poll option list command to see the IDs!", channel);
                            return;
                        }

                        channel.sendMessage(MessageUtils.getEmbed(sender)
                                .setDescription("Information for option ID: " + args[1])
                                .setColor(Color.CYAN)
                                .addField("Option", GeneralUtils.truncate(1024, option.getOption(), false), false)
                                .addField("Votes", String.valueOf(option.getVotes()), false)
                                .build()).queue();
                        return;
                    } else if (args[2].equalsIgnoreCase("add") && args.length >= 4) {
                        String option = MessageUtils.getMessage(args, 3);
                        List<PollOption> options = poll.getPollOptions();
                        if (options.size() >= 20) {
                            MessageUtils.sendErrorMessage("You can only have up to 20 options on one poll!", channel);
                            return;
                        } else if (option.length() > 1024) {
                            MessageUtils.sendErrorMessage("The option you entered is too long! It has to be 1024 characters or shorter!", channel);
                            return;
                        }

                        options.add(new PollOption(option));
                        MessageUtils.sendSuccessMessage("Successfully added option: `" + GeneralUtils.truncate(50, option) + "`", channel, sender);
                        return;
                    } else if (args[2].equalsIgnoreCase("remove") && args.length == 4) {
                        int optionId;
                        List<PollOption> options;
                        options = poll.getPollOptions();
                        try {
                            optionId = Integer.parseInt(args[1]) - 1;
                            options.remove(optionId);
                        } catch (NumberFormatException | IndexOutOfBoundsException e) {
                            MessageUtils.sendErrorMessage("Please provide a valid poll option ID!\n" +
                                    "Use the poll option list command to see the IDs!", channel);
                            return;
                        }

                        MessageUtils.sendSuccessMessage("Successfully removed option with ID: " + (optionId + 1), channel, sender);
                        return;
                    } else if (args[2].equalsIgnoreCase("list")) {
                        if (poll.getPollOptions().size() == 0) {
                            MessageUtils.sendInfoMessage("This poll has no options!", channel, sender);
                            return;
                        }
                        StringBuilder builder = new StringBuilder("```md\n");
                        int i = 1;
                        for (PollOption option : poll.getPollOptions()) {
                            builder.append(String.valueOf(i)).append(". \"")
                                    .append(GeneralUtils.truncate(50, option.getOption()))
                                    .append("\" - Votes: ")
                                    .append(option.getVotes())
                                    .append("\n");
                            i++;
                        }
                        builder.append("```");
                        channel.sendMessage(MessageUtils.getEmbed(sender).addField("Options", builder.toString(), false).build()).queue();
                        return;
                    }

                }
            } else if (args[0].equalsIgnoreCase("setchannel")) {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("all")) {
                        for (Poll poll : guild.getPolls()) {
                            poll.setChannel(channel.getId());
                        }
                        MessageUtils.sendSuccessMessage("Set this channel to be the announcement channel for all polls!", channel, sender);
                        return;
                    } else {
                        Poll poll = getPollById(args[1], guild.getPolls(), channel);
                        if (poll == null) {
                            return;
                        }

                        poll.setChannel(channel.getId());
                        MessageUtils.sendSuccessMessage("Set this channel to be the announcement channel for poll ID: " + args[0] + "!", channel, sender);
                        return;
                    }
                }
            } else if (args[0].equalsIgnoreCase("vote")) {
                if ((args.length == 3 && guild.getPolls().size() >= 1) || (args.length == 2 && guild.getPolls().size() == 1)) {

                    Poll poll;
                    if (args.length == 2) {
                        poll = guild.getPolls().get(0);
                    } else {
                        poll = getPollById(args[1], guild.getPolls(), channel);
                        if (poll == null) {
                            return;
                        }
                    }

                    int id;
                    PollOption option;
                    try {
                        if (args.length == 2) {
                            id = Integer.parseInt(args[1]) - 1;
                        } else {
                            id = Integer.parseInt(args[2]) - 1;
                        }
                        option = poll.getPollOptions().get(id);
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        MessageUtils.sendErrorMessage("Please provide a valid poll option ID!\n" +
                                "Use the poll option list command to see the IDs!", channel);
                        return;
                    }

                    if (option.getVoters().contains(sender.getId())) {
                        MessageUtils.sendErrorMessage("You cannot vote twice!", channel);
                        return;
                    }

                    for (PollOption opt : poll.getPollOptions()) {
                        if (opt.getVoters().contains(sender.getId()) && !opt.equals(option)) {
                            opt.getVoters().remove(sender.getId());
                        }
                    }

                    option.incrementVotes(sender.getId());

                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("You voted for option ID: " + (id + 1))
                            .addField("Option", GeneralUtils.truncate(50, option.getOption()), false)
                            .addField("Votes", "This option has " + String.valueOf(option.getVotes()) + " votes!", false)
                            .build()).queue();
                    return;
                } else if (guild.getPolls().size() == 0) {
                    MessageUtils.sendInfoMessage("This guild has no polls currently running!", channel, sender);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                MessageUtils.sendUsage(this, channel, sender, args);
                return;
            }
        }
        MessageUtils.sendUsage(this, channel, sender, args);
    }

    private Poll getPollById(String idText, List<Poll> polls, TextChannel channel) {
        int id;
        Poll poll = null;
        try {
            id = Integer.parseInt(idText) - 1;
            poll = polls.get(id);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            MessageUtils.sendErrorMessage("Please provide a valid poll ID!\n" +
                    "Use the poll list command to see the IDs!", channel);
        }
        return poll;
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
    public String getUsage() {
        return "`{%}poll` - Shows if there are any polls running or not\n" +
                "`{%}poll help` - Shows this help message\n" +
                "`{%}poll create <question>` - Creates a poll with a specified question\n" +
                "`{%}poll list` - Lists the polls for the server\n\n" +
                "`{%}poll close|open|remove <pollID>` - Closes, opens or removes a poll with the specified ID\n" +
                "`{%}poll set <pollID> colour <colour>` - Sets a colour for a poll with the specified ID\n\n" +
                "`{%}poll options <pollID> view|remove <optionID>` - Views or Removes a specific option from a poll\n" +
                "`{%}poll options <pollID> add <option>` - Adds an option to a poll\n" +
                "`{%}poll options <pollID> list` - Lists all the options for a poll\n\n" +
                "`{%}poll setchannel <pollID/\"all\">` - Sets the poll announcement channel for all polls or a specific poll\n" +
                "`{%}poll vote [pollID] <optionID>` - Votes for an option on a poll (Poll ID is only needed if the server has more than 1 poll)";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

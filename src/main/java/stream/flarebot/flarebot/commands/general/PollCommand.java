package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.Poll;
import stream.flarebot.flarebot.objects.PollOption;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

public class PollCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            int closedSize = guild.getPolls().stream().filter(Poll::isClosed).collect(Collectors.toList()).size();
            if (guild.getPolls().size() == 0 || closedSize > 0) {
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("This guild has no polls currently running!" +
                                (closedSize > 0 && FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.poll.open")
                                        ? String.format("\nThis guild has %d closed " + (guild.getPolls().size() == 1 ? "poll" : "polls"), closedSize) : ""))
                        .setColor(Color.CYAN)
                        .build()).queue();
            } else {
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription(String.format("This guild has %d " + (guild.getPolls().size() == 1 ? "poll" : "polls"), guild.getPolls().size()))
                        .setColor(Color.CYAN)
                        .build()).queue();
            }
            return;
        } else {
            if (args[0].equalsIgnoreCase("create")) {
                if (args.length >= 2) {
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
                guild.getPolls().stream().limit(12).forEach(poll -> {
                    builder.addField("Poll ID: " + (guild.getPolls().indexOf(poll) + 1)
                            , "Status: " + WordUtils.capitalizeFully(poll.getStatus().name()) + "\n" +
                                    "Poll Options: " + String.valueOf(poll.getPollOptions().size())
                            , true);
                });
                channel.sendMessage(builder.build()).queue();
                return;
            } else if (args[0].equalsIgnoreCase("close") ||
                    args[0].equalsIgnoreCase("open") ||
                    args[0].equalsIgnoreCase("remove")) {
                if (guild.getPolls().size() == 0) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("This guild has no polls currently running!")
                            .setColor(Color.CYAN)
                            .build()).queue();
                    return;
                } else if (args.length == 2) {
                    int index;
                    try {
                        index = Integer.parseInt(args[1]) - 1;
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage("Please provide a valid poll ID!\n" +
                                "Use the poll list command to see the IDs!", channel);
                        return;
                    }

                    String action = "";

                    try {
                        if (args[0].equalsIgnoreCase("close")) {
                            if (!FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.poll.close")) {
                                MessageUtils.sendErrorMessage("You need the permission `flarebot.poll.close` to do this!", channel);
                                return;
                            }
                            try {
                                guild.getPolls().get(index).setStatus(Poll.PollStatus.CLOSED);
                            } catch (IllegalStateException e) {
                                MessageUtils.sendErrorMessage(e.getMessage(), channel);
                                return;
                            }
                            action = "closed";
                        } else if (args[0].equalsIgnoreCase("open")) {
                            if (!FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.poll.open")) {
                                MessageUtils.sendErrorMessage("You need the permission `flarebot.poll.open` to do this!", channel);
                                return;
                            }
                            try {
                                guild.getPolls().get(index).setStatus(Poll.PollStatus.OPEN);
                            } catch (IllegalStateException e) {
                                MessageUtils.sendErrorMessage(e.getMessage(), channel);
                                return;
                            }
                            action = "opened";
                        } else {
                            if (!FlareBot.getInstance().getPermissions(channel).hasPermission(member, "flarebot.poll.remove")) {
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
                    }

                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription("Successfully " + action + " the poll with the ID: " + String.valueOf(index + 1))
                            .setColor(Color.GREEN)
                            .build()
                    ).queue();
                    return;
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                // {%}poll set <id> <property> <value>
                if (args.length == 4) {
                    int id;
                    try {
                        id = Integer.parseInt(args[1]) - 1;
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage("Please provide a valid poll ID!\n" +
                                "Use the poll list command to see the IDs!", channel);
                        return;
                    }

                    Poll poll;
                    try {
                        poll = guild.getPolls().get(id);
                    } catch (IndexOutOfBoundsException e) {
                        MessageUtils.sendErrorMessage("Please provide a valid poll ID!\n" +
                                "Use the poll list command to see the IDs!", channel);
                        return;
                    }

                    if (args[2].equalsIgnoreCase("colour") || args[2].equalsIgnoreCase("color")) {
                        Color color;
                        try {
                            color = Color.decode(args[3]);
                        } catch (NumberFormatException e) {
                            try {
                                color = GeneralUtils.getColor(args[3]);
                            } catch (IllegalArgumentException ex) {
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
                    int id;
                    try {
                        id = Integer.parseInt(args[1]) - 1;
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage("Please provide a valid poll ID!\n" +
                                "Use the poll list command to see the IDs!", channel);
                        return;
                    }

                    Poll poll;
                    try {
                        poll = guild.getPolls().get(id);
                    } catch (IndexOutOfBoundsException e) {
                        MessageUtils.sendErrorMessage("Please provide a valid poll ID!\n" +
                                "Use the poll list command to see the IDs!", channel);
                        return;
                    }

                    if (args[2].equalsIgnoreCase("add") && args.length >= 4) {
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
                        channel.sendMessage(MessageUtils.getEmbed(sender)
                                .setColor(Color.GREEN)
                                .setDescription("Successfully added option: `" + option + "`")
                                .build()).queue();
                        return;
                    } else if (args[2].equalsIgnoreCase("remove") && args.length == 4) {
                        int optionId;
                        try {
                            optionId = Integer.parseInt(args[1]) - 1;
                        } catch (NumberFormatException e) {
                            MessageUtils.sendErrorMessage("Please provide a valid poll ID!\n" +
                                    "Use the poll list command to see the IDs!", channel);
                            return;
                        }

                        List<PollOption> options;
                        options = poll.getPollOptions();
                        try {
                            options.remove(optionId);
                        }catch (IndexOutOfBoundsException e) {
                            MessageUtils.sendErrorMessage("Please provide a valid option ID!\n" +
                                    "Use the poll option list command to see the IDs!", channel);
                            return;
                        }
                        channel.sendMessage(MessageUtils.getEmbed(sender)
                                .setColor(Color.GREEN)
                                .setDescription("Successfully removed option with ID: " + (optionId + 1))
                                .build()).queue();
                        return;
                    } else if (args[2].equalsIgnoreCase("list")) {
                        if (poll.getPollOptions().size() == 0) {
                            channel.sendMessage(MessageUtils.getEmbed(sender)
                                    .setColor(Color.CYAN)
                                    .setDescription("Tjos ").build());
                        }
                        StringBuilder builder = new StringBuilder("**Options:**\n```md\n");
                        int i = 1;
                        for (PollOption option : poll.getPollOptions()) {
                            builder.append(String.valueOf(i)).append(". ").append(option.getOption()).append("\n");
                            i++;
                        }
                        builder.append("```");
                        channel.sendMessage(builder.toString()).queue();
                        return;
                    }

                }
            }
        }
        MessageUtils.getUsage(this, channel, sender).queue();
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

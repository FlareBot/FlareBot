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
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
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
                            guild.getPolls().get(index).setStatus(Poll.PollStatus.CLOSED);
                            action = "closed";
                        } else if (args[0].equalsIgnoreCase("open")) {
                            guild.getPolls().get(index).setStatus(Poll.PollStatus.OPEN);
                            action = "opened";
                        } else {
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

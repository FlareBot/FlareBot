package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
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
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            //New system
            if (args[0].equalsIgnoreCase("dm")) {
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("enable")) {
                        if (!guild.getWelcome().isDmEnabled()) {
                            guild.getWelcome().setDmEnabled(true);
                            channel.sendMessage("DM welcomes are now **enabled**").queue();
                        } else {
                            MessageUtils.sendErrorMessage("DM welcomes are already **enabled**", channel);
                        }
                    } else if (args[1].equalsIgnoreCase("disable")) {
                        if (args.length == 2) {
                            if (guild.getWelcome().isDmEnabled()) {
                                guild.getWelcome().setDmEnabled(false);
                                channel.sendMessage("DM welcomes are now **disabled**").queue();
                            } else {
                                MessageUtils.sendErrorMessage("DM welcomes are already **disabled**", channel);
                            }
                        } else {
                            MessageUtils.sendUsage(this, channel, sender, args);
                        }
                    } else if (args[1].equalsIgnoreCase("message")) {
                        if (args.length >= 3) {
                            if (args.length == 3 || args.length == 4) {
                                if (args[2].equalsIgnoreCase("list")) {
                                    int page = args.length == 3 ? 1 : GeneralUtils.getInt(args[3], 1);
                                    List<String> messages = guild.getWelcome().getDmMessages();
                                    sendWelcomeTable(messages, page, channel);
                                    return;
                                } else if (args[2].equalsIgnoreCase("remove")) {
                                    if (args.length == 4) {
                                        int id = Integer.valueOf(args[3]);
                                        String welcome = guild.getWelcome().getDmMessages().get(id);
                                        guild.getWelcome().getDmMessages().remove(id);
                                        channel.sendMessage("Removed welcome message `" + welcome + "`").queue();
                                        return;
                                    } else {
                                        MessageUtils.sendUsage(this, channel, sender, args);
                                        return;
                                    }
                                } else {
                                    MessageUtils.sendUsage(this, channel, sender, args);
                                    return;
                                }
                            }
                            if (args[2].equalsIgnoreCase("add")) {
                                String welcomeMessage = MessageUtils.getMessage(args, 3);
                                guild.getWelcome().getDmMessages().add(welcomeMessage);
                                channel.sendMessage("Added welcome message `"
                                        + MessageUtils.escapeMarkdown(welcomeMessage) + "`").queue();
                            } else {
                                MessageUtils.sendUsage(this, channel, sender, args);
                            }
                        } else {
                            MessageUtils.sendUsage(this, channel, sender, args);
                        }
                    }
                } else {
                    MessageUtils.sendUsage(this, channel, sender, args);
                }
            } else if (args[0].equalsIgnoreCase("guild")) {
                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("enable")) {
                        if (!guild.getWelcome().isGuildEnabled()) {
                            guild.getWelcome().setGuildEnabled(true);
                            channel.sendMessage("Guild welcomes are now **enabled**").queue();
                        } else {
                            MessageUtils.sendErrorMessage("Guild welcomes are already **enabled**", channel);
                        }
                    } else if (args[1].equalsIgnoreCase("disable")) {
                        if (args.length == 2) {
                            if (guild.getWelcome().isGuildEnabled()) {
                                guild.getWelcome().setGuildEnabled(false);
                                channel.sendMessage("Guild welcomes are now **disabled**").queue();
                            } else {
                                MessageUtils.sendErrorMessage("Guild welcomes are already **disabled**", channel);
                            }
                        }
                    } else if (args[1].equalsIgnoreCase("message")) {
                        if (args.length >= 3) {
                            if (args.length == 3 || args.length == 4) {
                                if (args[2].equalsIgnoreCase("list")) {
                                    int page = args.length == 3 ? 1 : GeneralUtils.getInt(args[3], 1);
                                    List<String> messages = guild.getWelcome().getGuildMessages();
                                    sendWelcomeTable(messages, page, channel);
                                    return;
                                } else if (args[2].equalsIgnoreCase("remove")) {
                                    if (args.length == 4) {
                                        int id = GeneralUtils.getInt(args[3], 0);
                                        if (id >= guild.getWelcome().getGuildMessages().size()) {
                                            MessageUtils.sendErrorMessage("Invalid index!", channel);
                                            return;
                                        }
                                        String welcome = guild.getWelcome().getGuildMessages().get(id);
                                        guild.getWelcome().getGuildMessages().remove(id);
                                        channel.sendMessage("Removed welcome message `" + welcome + "`").queue();
                                        return;
                                    } else {
                                        MessageUtils.sendUsage(this, channel, sender, args);
                                        return;
                                    }
                                } else if (!args[2].equalsIgnoreCase("add")) {
                                    MessageUtils.sendUsage(this, channel, sender, args);
                                    return;
                                }
                            }
                            if (args[2].equalsIgnoreCase("add")) {
                                String welcomeMessage = MessageUtils.getMessage(args, 3);
                                guild.getWelcome().getGuildMessages().add(welcomeMessage);
                                channel.sendMessage("Added welcome message `"
                                        + MessageUtils.escapeMarkdown(welcomeMessage) + "`").queue();
                            } else {
                                MessageUtils.sendUsage(this, channel, sender, args);
                            }
                        } else {
                            MessageUtils.sendUsage(this, channel, sender, args);
                        }
                    }
                } else {
                    MessageUtils.sendUsage(this, channel, sender, args);
                }
            } else if (args[0].equalsIgnoreCase("setchannel")) {
                guild.getWelcome().setChannelId(channel.getId());
                MessageUtils.sendSuccessMessage("Set welcome channel to " + channel.getAsMention(), channel, sender);
            } else {
                MessageUtils.sendUsage(this, channel, sender, args);
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
        return "`{%}welcome dm|guild enable|disable`- Enables/Disables welcome message for dm/guild.\n" +
                "`{%}welcome dm|guild message add <message>` - Adds a message to the welcomes.\n" +
                "`{%}welcome dm|guild message remove <message_id>` - Removes a message from the welcomes.\n" +
                "`{%}welcome dm|guild message list [page]` - Lists all the messages and their IDs for welcomes.\n" +
                "`{%}welcome setchannel` - Sets the channel in the guild to have welcome in.";
    }

    @Override
    public String getExtraInfo() {
        return "**Message syntax:**\n" +
                "`%mention%` - mentions the user\n" +
                "`%user%` - the users name\n" +
                "`%guild%` - the guild name";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

    private void sendWelcomeTable(List<String> messages, int page, TextChannel channel) {
        int messagesLength = 15;
        int pages =
                messages.size() < messagesLength ? 1 : (messages.size() / messagesLength) + (messages.size() % messagesLength != 0 ? 1 : 0);
        int start = messagesLength * (page - 1);
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
                part.add(MessageUtils.escapeMarkdown(messagesMessage));
                body.add(part);
                i++;
            }
            List<String> header = new ArrayList<>();
            header.add("Id");
            header.add("Message");
            channel.sendMessage(MessageUtils.makeAsciiTable(header, body, " Messages Page " + GeneralUtils
                    .getPageOutOfTotal(page, messages, messagesLength))).queue();
        }
    }
}

package stream.flarebot.flarebot.commands.administrator;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

public class WelcomeCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel, sender).queue();
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("enable")) {
                if (!guild.getWelcome().isEnabled()) {
                    guild.getWelcome().setEnabled(true);
                    channel.sendMessage("Welcomes **enabled**!").queue();
                } else {
                    channel.sendMessage("Welcomes already **enabled**!").queue();
                }
            } else if (args[0].equalsIgnoreCase("disable")) {
                if (guild.getWelcome().isEnabled()) {
                    guild.getWelcome().setEnabled(false);
                    channel.sendMessage("Welcomes **disabled**!").queue();
                } else {
                    channel.sendMessage("Welcomes already **disabled**!").queue();
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                if (guild.getWelcome() != null) {
                    guild.getWelcome().setChannelId(channel.getId());
                    channel.sendMessage("Welcomes set to appear in this channel!").queue();
                } else {
                    channel.sendMessage("Welcomes are not enabled!").queue();
                }
            } else if (args[0].equalsIgnoreCase("message")) {
                channel.sendMessage(sender.getAsMention() +
                        " To set a new message do " + FlareBot.getPrefixes().get(channel.getGuild()
                        .getId()) + "welcome message (message)\n" +
                        "Known variables are:\n" +
                        "``%user%`` for the username,\n" +
                        "``%mention%`` to mention the user, and\n" +
                        "``%guild%`` for the guild name.\n" +
                        (guild.getWelcome() == null ? "" : "The current message is: ```md\n"
                                + guild.getWelcome().getMessage() + "```")).queue();
            } else {
                MessageUtils.getUsage(this, channel, sender).queue();
            }
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("message")) {
                if (guild.getWelcome() != null) {
                    String msg = MessageUtils.getMessage(args, 1);
                    guild.getWelcome().setMessage(msg);
                    channel.sendMessage("Set welcome message to ```" + msg + "```").queue();
                } else {
                    channel.sendMessage("Welcomes are not enabled!").queue();
                }
            } else {
                MessageUtils.getUsage(this, channel, sender).queue();
            }
        } else {
            MessageUtils.getUsage(this, channel, sender).queue();
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
        return "`{%}welcome <enable/disable>` - Enables or disables welcomes\n"
                + "`{%}welcome <set/message>` - Sets or views the current welcome message";
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

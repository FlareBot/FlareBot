package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

public class NINOCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
                boolean enabled = args[0].equalsIgnoreCase("enable");
                guild.getNINO().setEnabled(enabled);
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription(GeneralUtils.formatCommandPrefix(channel,
                        "I have "
                                + (!enabled ? "disabled anti-invite protection!" : "enabled anti-invite protection!\n"
                                + "To see the whitelist do `{%}nino whitelist list` and to post the invite attempts to "
                                + " the modlog enable it with `{%}modlog enable Invite Posted <#channel>`")))
                        .setColor(enabled ? ColorUtils.GREEN : ColorUtils.RED).build()).queue();
            } else if (args[0].equalsIgnoreCase("whitelist")) {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("list")) {
                        StringBuilder sb = new StringBuilder();
                        for (String invite : guild.getNINO().getWhitelist())
                            sb.append("`").append(invite).append("`").append("\n");
                        channel.sendMessage(MessageUtils.getEmbed(sender).setColor(ColorUtils.FLAREBOT_BLUE)
                                .addField("Whitelisted invites", sb.toString(), false).build()).queue();
                    } else
                        MessageUtils.sendUsage(this, channel, sender, args);
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                        String invite = args[2];
                        if (invite != null) {
                            if (args[1].equalsIgnoreCase("add")) {
                                guild.getNINO().addInvite(invite);
                                MessageUtils.sendSuccessMessage("Added `" + invite + "` to the whitelist!", channel, sender);
                            } else {
                                if (guild.getNINO().getWhitelist().contains(invite)) {
                                    guild.getNINO().removeInvite(invite);
                                    MessageUtils.sendWarningMessage("Removed `" + invite + "` from the whitelist!",
                                            channel, sender);
                                } else
                                    MessageUtils.sendWarningMessage("That invite is not whitelisted!", channel, sender);
                            }
                        } else
                            MessageUtils.sendWarningMessage("That is not a valid invite! If you believe it is please tell " +
                                    "us on our support server!", channel, sender);
                    } else
                        MessageUtils.sendUsage(this, channel, sender, args);
                } else
                    MessageUtils.sendUsage(this, channel, sender, args);
            } else if (args[0].equalsIgnoreCase("set-message")) {
                String msg = MessageUtils.getMessage(args, 1);
                if (msg.length() > 250) {
                    MessageUtils.sendWarningMessage("That message is a tad too long, please make it 250 characters max!" +
                            "\n```\n" + msg + "\n```", channel, sender);
                    return;
                }
                guild.getNINO().setRemoveMessage(msg);
                MessageUtils.sendSuccessMessage("Message has been set!\n```" + msg + "\n```", channel, sender);
            } else
                MessageUtils.sendUsage(this, channel, sender, args);
        } else
            MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "nino";
    }

    @Override
    public String getDescription() {
        return "Configure and enable FlareBot's anti-invite protection!";
    }

    @Override
    public String getUsage() {
        return "`{%}nino enable|disable` - Enable or disable the anti-invite!\n" +
                "`{%}nino whitelist list` - List the current whitelisted invites.\n" +
                "`{%}nino whitelist add|remove <invite>` - Configure the whitelist.\n" +
                "`{%}nino set-message <message>` - Set the message sent on remove!";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    public boolean isBetaTesterCommand() {
        return true;
    }
}

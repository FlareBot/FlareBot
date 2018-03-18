package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.FormatUtils;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.pagination.PagedEmbedBuilder;
import stream.flarebot.flarebot.util.pagination.PaginationList;
import stream.flarebot.flarebot.util.pagination.PaginationUtil;

import java.util.List;

public class NINOCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
                boolean enabled = args[0].equalsIgnoreCase("enable");
                guild.getNINO().setEnabled(enabled);
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription(FormatUtils.formatCommandPrefix(guild,
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
            } else if (args[0].equalsIgnoreCase("message")) {
                if (args.length == 1) {
                    MessageUtils.sendUsage(this, channel, sender, args);
                    return;
                }

                String msg = null;
                if (args.length >= 3) {
                    msg = MessageUtils.getMessage(args, 2);
                    if (msg.length() > 250) {
                        MessageUtils.sendWarningMessage("That message is a tad too long, please make it 250 characters max!" +
                                "\n```\n" + msg + "\n```", channel, sender);
                        return;
                    }
                }

                if (args[1].equalsIgnoreCase("set")) {
                    if (msg != null) {
                        guild.getNINO().setRemoveMessage(msg);
                        MessageUtils.sendSuccessMessage("Message has been set!\n```" + msg + "\n```", channel, sender);
                    } else
                        MessageUtils.sendWarningMessage("Please specify a message!", channel);
                } else if (args[1].equalsIgnoreCase("add")) {
                    if (msg != null) {
                        guild.getNINO().addRemoveMessage(msg);
                        MessageUtils.sendSuccessMessage("Message has been added!\n```" + msg + "\n```", channel, sender);
                    } else
                        MessageUtils.sendWarningMessage("Please specify a message!", channel);
                } else if (args[1].equalsIgnoreCase("remove")) {
                    int messageId = 1;
                    if (args.length > 2)
                        messageId = GeneralUtils.getInt(args[2], 1);

                    int size = guild.getNINO().getRemoveMessages().size();

                    if (size == 0) {
                        MessageUtils.sendInfoMessage("This guild has no remove messages!", channel);
                        return;
                    }

                    if (messageId <= 0 || messageId >= size) {
                        MessageUtils.sendWarningMessage("Invalid message ID!", channel);
                        return;
                    }
                    String tmp = guild.getNINO().getRemoveMessages().get(messageId);
                    guild.getNINO().getRemoveMessages().remove(tmp);
                    MessageUtils.sendSuccessMessage("Successfully removed message with ID " + messageId
                            + "!\n```\n" + tmp + "\n```", channel);
                } else if (args[1].equalsIgnoreCase("clear")) {
                    guild.getNINO().clearRemoveMessages();
                    MessageUtils.sendSuccessMessage("Successfully removed **all** messages from this guild!", channel);
                } else if (args[1].equalsIgnoreCase("list")) {
                    int page = 1;
                    if (args.length > 2)
                        page = GeneralUtils.getInt(args[2], 1);
                    listMessages(guild, page, channel, sender);
                } else
                    MessageUtils.sendUsage(this, channel, sender, args);
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
                "`{%}nino message set|add|remove <message>` - Set, add or remove messages for removal.\n" +
                "`{%}nino message list` - List the messages currently set for NINO!";
    }

    @Override
    public String getExtraInfo() {
        return "If you are setting a message it will overwrite the current one (If there's only one!) otherwise " +
                "it will add to the messages.\n" +
                "On message remove - If there's only one message it will just remove that rather than prompting " +
                "which to remove";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isBetaTesterCommand() {
        return true;
    }

    private void listMessages(GuildWrapper wrapper, int page, TextChannel channel, User user) {
        List<String> messages = wrapper.getNINO().getRemoveMessages();
        if(messages.size() == 0) {
            MessageUtils.sendWarningMessage("There are no messages for this guild!", channel);
            return;
        }
        PaginationList<String> list = new PaginationList<>(messages);
        list.createGroups(Math.min(list.getPages(), 1));
        PagedEmbedBuilder<String> pagedEmbedBuilder = new PagedEmbedBuilder<>(list);
        pagedEmbedBuilder.useGroups(4, "Message #");
        pagedEmbedBuilder.setTitle("NINO Message List");
        PaginationUtil.sendEmbedPagedMessage(pagedEmbedBuilder.build(), page - 1, channel, user);
    }
}

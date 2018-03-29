package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.text.WordUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.nino.NINOMode;
import stream.flarebot.flarebot.mod.nino.URLCheckFlag;
import stream.flarebot.flarebot.mod.nino.URLChecker;
import stream.flarebot.flarebot.mod.nino.URLConstants;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.ColorUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.buttons.ButtonGroupConstants;
import stream.flarebot.flarebot.util.general.FormatUtils;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;
import stream.flarebot.flarebot.util.pagination.PagedEmbedBuilder;
import stream.flarebot.flarebot.util.pagination.PaginationList;
import stream.flarebot.flarebot.util.pagination.PaginationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NINOCommand implements Command {

    private static final Pattern seperator = Pattern.compile(", ?");

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable")) {
                boolean enabled = args[0].equalsIgnoreCase("enable");
                ArrayList<URLCheckFlag> flags = new ArrayList<>();

                if (args.length >= 2) {
                    if (args[1].equalsIgnoreCase("all"))
                        flags.addAll(Arrays.asList(URLCheckFlag.values));
                    else if (args[1].equalsIgnoreCase("default"))
                        flags.addAll(URLCheckFlag.getDefaults());
                    else {
                        String[] strFlags = seperator.split(MessageUtils.getMessage(args, 1));
                        URLCheckFlag checkFlag;
                        for (String flag : strFlags) {
                            if ((checkFlag = URLCheckFlag.getFlag(flag)) != null)
                                flags.add(checkFlag);
                            else {
                                MessageUtils.sendWarningMessage("'" + flag + "' is not a valid flag!", channel);
                                return;
                            }
                        }
                    }
                }

                if (enabled) {
                    if (flags.isEmpty())
                        flags.addAll(URLCheckFlag.getDefaults());

                    guild.getNINO().getURLFlags().addAll(flags);
                } else {
                    if (flags.isEmpty())
                        guild.getNINO().getURLFlags().clear();
                    else
                        guild.getNINO().getURLFlags().removeAll(flags);
                }

                boolean all = (enabled ? guild.getNINO().getURLFlags().size() == URLCheckFlag.getAllFlags().size()
                        : guild.getNINO().getURLFlags().isEmpty());

                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription(
                        FormatUtils.formatCommandPrefix(guild, "I have "
                                + (enabled ? "enabled " : "disabled ")
                                + (all ? "all the flags." : "the " + listToString(flags) + " flag(s)!")
                                + "\n\nTo see the whitelist do `{%}nino whitelist list` and to post the"
                                + " attempts to the modlog enable it with `{%}modlog enable NINO <#channel>`")
                ).setColor(enabled ? ColorUtils.GREEN : ColorUtils.RED).build()).queue();
            } else if (args[0].equalsIgnoreCase("whitelist")) {
                if (args.length == 2) {
                    if (args[1].equalsIgnoreCase("list")) {
                        StringBuilder sb = new StringBuilder();
                        StringBuilder channels = new StringBuilder();
                        for (String invite : guild.getNINO().getWhitelist())
                            sb.append("`").append(invite).append("`").append("\n");

                        for (long channelId : guild.getNINO().getChannels()) {
                            if (guild.getGuild().getTextChannelById(channelId) != null)
                                channels.append(guild.getGuild().getTextChannelById(channelId).getAsMention()).append("\n");
                            else
                                guild.getNINO().getChannels().remove(channelId);
                        }

                        channel.sendMessage(MessageUtils.getEmbed(sender).setColor(ColorUtils.FLAREBOT_BLUE)
                                .addField("Whitelisted Invites", sb.toString(), false)
                                .addField("Whitelisted Channels", channels.toString(), false)
                                .build()).queue();
                    } else
                        MessageUtils.sendUsage(this, channel, sender, args);
                } else if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                        String whitelist = args[2];
                        boolean added = args[1].equalsIgnoreCase("add");

                        TextChannel tc = GuildUtils.getChannel(whitelist, guild);
                        if (tc != null) {
                            if (added)
                                guild.getNINO().addChannel(tc.getIdLong());
                            else
                                guild.getNINO().removeChannel(tc.getIdLong());

                            MessageUtils.sendSuccessMessage((added ? "Added " : "Removed ") + tc.getAsMention()
                                    + (added ? " to" : " from") + " the whitelist!", channel, sender);
                        } else if (URLConstants.URL_PATTERN_NO_PROTOCOL.matcher(whitelist).matches()) {
                            if (added)
                                guild.getNINO().addUrl(whitelist);
                            else
                                guild.getNINO().removeUrl(whitelist);

                            MessageUtils.sendSuccessMessage((added ? "Added " : "Removed ") + "`" + whitelist
                                    + "` " + (added ? "to" : "from") + " the whitelist!", channel, sender);
                        } else {
                            MessageUtils.sendWarningMessage("That is an invalid input! " +
                                    "Please try and whitelist a Discord invite or ", channel);
                        }
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
            } else if (args[0].equalsIgnoreCase("flags")) {
                Set<URLCheckFlag> flags = guild.getNINO().getURLFlags();
                boolean all = flags.size() == URLCheckFlag.getAllFlags().size();

                EmbedBuilder eb = MessageUtils.getEmbed(sender);
                if (!flags.isEmpty())
                    eb.addField("Enabled", flags.stream()
                            .map(URLCheckFlag::toString)
                            .collect(Collectors.joining("\n")), false);

                if (!all)
                    eb.addField("Disabled", URLCheckFlag.getAllFlags().stream().filter(f -> !flags.contains(f))
                            .map(URLCheckFlag::toString)
                            .collect(Collectors.joining("\n")), false);
                channel.sendMessage(eb.build()).queue();
            } else if (args[0].equalsIgnoreCase("mode")) {
                if (args.length == 2) {
                    NINOMode mode = NINOMode.getMode(args[1]);
                    if (mode == null)
                        MessageUtils.sendWarningMessage("That is an invalid mode!", channel);
                    else {
                        guild.getNINO().setMode(mode.getMode());
                        MessageUtils.sendSuccessMessage("Changed NINO's mode to " + mode.toString(), channel, sender);
                    }
                } else {
                    StringBuilder sb = new StringBuilder();
                    for (NINOMode mode : NINOMode.values())
                        sb.append(mode.toString()).append(" (").append(mode.getMode()).append(") - ")
                                .append(mode.getExplanation()).append("\n");

                    MessageUtils.sendMessage("Current Mode: " + guild.getNINO().getNINOMode().toString() +
                            "\nNINO will " + WordUtils.uncapitalize(guild.getNINO().getNINOMode().getExplanation(), '~') +
                            "\n\n**Available Modes**\n" +
                            sb.toString().trim() + "\n\n\n" +
                            "Protocol is the `http://` or `https://` at the start of a URL. Example: `https://google.com`\n" +
                            "Following a URL is when we will check to see if it is a masked link " +
                            "(something like goo.gl, bit.ly etc) and if so follow the redirects and if we see a bad " +
                            "link then it will be caught. This means people can't hide bad links. " +
                            "It does have the disadvantage of being a bit slower though (it will still catch before " +
                            "any human can click it). Example: `https://goo.gl/NfYi94`", channel, sender);
                }
            } else if (args[0].equalsIgnoreCase("test")) {
                if (!PerGuildPermissions.isCreator(sender)) return;
                String links = MessageUtils.getMessage(args, 1);

                URLChecker.instance().runTests(links.split("\n"), channel);
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
        return "Configure and enable FlareBot's URL protection!";
    }

    @Override
    public String getUsage() {
        return "`{%}nino enable|disable <flags...>` - Enable or disable a flag or multiple by comma separation!\n" +
                "`{%}nino whitelist list` - List the current whitelisted URLs.\n" +
                "`{%}nino whitelist add|remove <url>` - Configure the whitelist.\n" +
                "`{%}nino message set|add|remove <message>` - Set, add or remove messages for removal.\n" +
                "`{%}nino message list` - List the messages currently set for NINO!\n" +
                "`{%}nino flags` - List the flags enabled and disabled\n" +
                "`{%}nino mode <mode>` - Change the mode for NINO, it is recommended for most guilds to use mode 1.";
    }

    @Override
    public Permission getPermission() {
        return Permission.NINO_COMMAND;
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
        if (messages.size() == 0) {
            MessageUtils.sendWarningMessage("There are no messages for this guild!", channel);
            return;
        }
        PaginationList<String> list = new PaginationList<>(messages);
        list.createGroups(Math.min(list.getPages(), 1));
        PagedEmbedBuilder<String> pagedEmbedBuilder = new PagedEmbedBuilder<>(list);
        pagedEmbedBuilder.useGroups(4, "Message #");
        pagedEmbedBuilder.setTitle("NINO Message List");
        PaginationUtil.sendEmbedPagedMessage(pagedEmbedBuilder.build(), page - 1, channel, user, ButtonGroupConstants.NINO);
    }

    private String listToString(Collection<URLCheckFlag> c) {
        return "`" + c.stream().map(URLCheckFlag::toString).collect(Collectors.joining(", ")) + "`";
    }
}

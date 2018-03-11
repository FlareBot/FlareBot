package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.general.GeneralUtils;
import stream.flarebot.flarebot.util.general.GuildUtils;

import java.util.stream.Collectors;

public class SettingsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("list")) {
                channel.sendMessage(MessageUtils.getEmbed(sender).setTitle("Guild Settings")
                        .setDescription(String.format(
                                "`delete-commands` - %b\n" +
                                        "`blacklisted-channels` - %s\n" +
                                        "`blaclisted-users` - %s",
                                guild.getSettings().shouldDeleteCommands(),
                                getBlacklistedChannels(guild),
                                getBlacklistedUsers(guild))
                        ).build()).queue();
            } else if (args[0].equalsIgnoreCase("change")) {
                if (args.length >= 3) {
                    if (args[1].equalsIgnoreCase("delete-commands")) {
                        if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("yes")
                                || args[2].equalsIgnoreCase("y")) {
                            guild.getSettings().setDeleteCommands(true);
                            MessageUtils.sendSuccessMessage("FlareBot will now delete the commands sent by users!", channel);
                        } else if (args[2].equalsIgnoreCase("false") || args[2].equalsIgnoreCase("no")
                                || args[2].equalsIgnoreCase("n")) {
                            guild.getSettings().setDeleteCommands(false);
                            MessageUtils.sendWarningMessage("FlareBot will no longer delete the commands sent by users!", channel);
                        } else
                            MessageUtils.sendWarningMessage("Invalid value! Expecting `yes` or `no`", channel);
                    } else
                        MessageUtils.sendWarningMessage("Invalid option or invalid option which can be changed with " +
                                "the `change` argument. Do `{%}usage " + getCommand() + "` and see if you can find " +
                                "the argument required!", channel);
                } else
                    MessageUtils.sendUsage(this, channel, sender, args);
            } else if (args[0].equalsIgnoreCase("blacklist")) {
                if (args.length >= 3) {
                    if (args[1].equalsIgnoreCase("list")) {
                        if (args[2].equalsIgnoreCase("channels") || args[2].equalsIgnoreCase("channel"))
                            MessageUtils.sendInfoMessage(getBlacklistedChannels(guild), channel);
                        else if (args[2].equalsIgnoreCase("users") || args[2].equalsIgnoreCase("user"))
                            MessageUtils.sendInfoMessage(getBlacklistedUsers(guild), channel);
                        else
                            MessageUtils.sendWarningMessage("Invalid blacklist item!", channel);
                    } else if (args[1].equalsIgnoreCase("add")) {
                        TextChannel tc = GeneralUtils.getChannel(args[2], guild);
                        if (tc != null) {
                            guild.getSettings().getChannelBlacklist().add(tc.getIdLong());
                            MessageUtils.sendSuccessMessage("Added " + tc.getAsMention() + " to the blacklist!",
                                    channel);
                            return;
                        }
                        User user = GuildUtils.getUser(args[2], guild.getGuildId());
                        if (user != null) {
                            guild.getSettings().getUserBlacklist().add(user.getIdLong());
                            MessageUtils.sendSuccessMessage("Added " + user.getAsMention() + " to the blacklist!",
                                    channel);
                            return;
                        }
                        MessageUtils.sendWarningMessage("Invalid channel or user! Try the ID if you're sure the " +
                                "entity is valid", channel);
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        TextChannel tc = GeneralUtils.getChannel(args[2], guild);
                        if (tc != null) {
                            if (!guild.getSettings().getChannelBlacklist().contains(tc.getIdLong())) {
                                MessageUtils.sendWarningMessage("That channel is not blacklisted!", channel);
                                return;
                            }
                            guild.getSettings().getChannelBlacklist().remove(tc.getIdLong());
                            MessageUtils.sendSuccessMessage("Removed " + tc.getAsMention() + " from the blacklist!",
                                    channel);
                            return;
                        }
                        User user = GuildUtils.getUser(args[2], guild.getGuildId());
                        if (user != null) {
                            if (!guild.getSettings().getUserBlacklist().contains(user.getIdLong())) {
                                MessageUtils.sendWarningMessage("That user is not blacklisted!", channel);
                                return;
                            }
                            guild.getSettings().getUserBlacklist().remove(user.getIdLong());
                            MessageUtils.sendSuccessMessage("Removed " + user.getAsMention() + " from the blacklist!",
                                    channel);
                            return;
                        }
                        MessageUtils.sendWarningMessage("Invalid channel or user! Try the ID if you're sure the " +
                                "entity is valid", channel);
                    } else
                        MessageUtils.sendUsage(this, channel, sender, args);
                } else
                    MessageUtils.sendUsage(this, channel, sender, args);
            } else
                MessageUtils.sendUsage(this, channel, sender, args);
        } else
            MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "settings";
    }

    @Override
    public String getDescription() {
        return "Change the bot settings for this guild.";
    }

    @Override
    public String getUsage() {
        return "`{%}settings change <setting> <new-value>` - Change a settings value.\n" +
                "`{%}settings blacklist list users|channels` - List the blacklist of users or channels.\n" +
                "`{%}settings blacklist add|remove <user|channel>` - Blacklist a user or channel.\n" +
                "`{%}settings list` - List the settings and their current values.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isBetaTesterCommand() {
        return true;
    }

    private String getBlacklistedChannels(GuildWrapper wrapper) {
        if (wrapper.getSettings().getChannelBlacklist().isEmpty())
            return "No blacklisted channels!";
        return wrapper.getSettings().getChannelBlacklist().stream().map(channelId -> {
            TextChannel tc = wrapper.getGuild().getTextChannelById(channelId);
            if (tc == null) {
                wrapper.getSettings().getChannelBlacklist().remove(channelId);
                return "";
            }
            return tc.getAsMention();
        }).collect(Collectors.joining(", "));
    }

    private String getBlacklistedUsers(GuildWrapper wrapper) {
        if (wrapper.getSettings().getChannelBlacklist().isEmpty())
            return "No blacklisted users!";
        return wrapper.getSettings().getUserBlacklist().stream().map(userId -> {
            Member mber = wrapper.getGuild().getMemberById(userId);
            if (mber == null) {
                wrapper.getSettings().getUserBlacklist().remove(userId);
                return String.valueOf(userId);
            }
            return mber.getUser().getAsMention();
        }).collect(Collectors.joining(", "));
    }
}

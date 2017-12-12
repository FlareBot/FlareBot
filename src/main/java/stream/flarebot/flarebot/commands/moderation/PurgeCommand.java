package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.mod.modlog.ModlogHandler;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PurgeCommand implements Command {

    private Map<String, Long> cooldowns = new HashMap<>();
    private static final long cooldown = 60000;

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            User targetUser = null;
            int amount;
            if (args.length == 1 && args[0].matches("\\d+")) {
                amount = GeneralUtils.getInt(args[0], -1);
            } else if (args.length == 2 && args[1].matches("\\d+")) {
                amount = GeneralUtils.getInt(args[1], -1);
                try {
                    targetUser = GeneralUtils.getUser(args[0], guild.getGuildId(), true);
                } catch (ErrorResponseException e) {
                    MessageUtils.sendErrorMessage("That target user cannot be found, try mentioning them, using the user ID or using `all` to clear the entire chat.", channel);
                    return;
                }
            } else {
                MessageUtils.sendUsage(this, channel, sender, args);
                return;
            }

            // 2 messages min
            if (amount < 1) {
                MessageUtils.sendErrorMessage("You must purge at least 1 message, please give a valid purge amount.", channel);
                return;
            }

            // This will be a successful delete so limit here.
            if (!PerGuildPermissions.isCreator(sender)) {
                long riotPolice = cooldowns.computeIfAbsent(channel.getGuild().getId(), n -> 0L);
                if (System.currentTimeMillis() - riotPolice < cooldown) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String
                                    .format("You are on a cooldown! %s seconds left!",
                                            (cooldown - (System
                                                    .currentTimeMillis() - riotPolice)) / 1000))
                            .build()).queue();
                    return;
                }
            }

            if (!guild.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)) {
                MessageUtils.sendErrorMessage("I do not have the perms `Manage Messages` and `Read Message History` please make sure I have these and do the command again.", channel);
                return;
            }
            MessageHistory history = new MessageHistory(channel);
            int toRetrieve = amount + 1;
            int i = 0;
            outer:
            while (toRetrieve > 0) {
                if (history.retrievePast((targetUser == null ? Math.min(toRetrieve, 100) : 100)).complete().isEmpty()) {
                    break;
                }

                List<Message> toDelete = new ArrayList<>();
                for (Message msg : history.getRetrievedHistory()) {
                    if (msg.getCreationTime().plusWeeks(2).isBefore(OffsetDateTime.now())) break outer;
                    if ((targetUser != null && msg.getAuthor().getId().equals(targetUser.getId())) || targetUser == null) {
                        toDelete.add(msg);
                        // This is to fix stuff like purges being logged.
                        FlareBot.getInstance().getEvents().getRemovedByMeList().add(msg.getIdLong());
                        i++;
                        toRetrieve--;
                    }
                    if (toRetrieve == 0) break;
                }
                channel.deleteMessages(toDelete).complete();
                toDelete.clear();
            }
            ModlogHandler.getInstance().postToModlog(guild, ModlogEvent.FLAREBOT_PURGE, targetUser, sender, null,
                    new MessageEmbed.Field("Messages purged", String.valueOf((i - 1)), true));
            MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String.format("Deleted `%s` messages!", i - 1)).build(),
                    TimeUnit.SECONDS.toMillis(5), channel);
        }
    }

    @Override
    public String getCommand() {
        return "purge";
    }

    @Override
    public String getDescription() {
        return "Removes last X messages.";
    }

    @Override
    public String getUsage() {
        return "`{%}purge <user> <amount>` - Purges a certain amount of messages from a user.\n" +
                "`{%}purge <amount>` - Purges a certain amount of messages.";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public String[] getAliases() {
        return new String[]{"clean"};
    }

    @Override
    public EnumSet<Permission> getDiscordPermission() {
        return EnumSet.of(Permission.MESSAGE_MANAGE);
    }
}

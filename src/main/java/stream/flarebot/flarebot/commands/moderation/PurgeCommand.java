package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.FlarebotTask;
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
        if (args.length == 1 && args[0].matches("\\d+")) {
            if (!FlareBotManager.getInstance().getGuild(channel.getId()).getPermissions().isCreator(sender)) {
                long calmitdood = cooldowns.computeIfAbsent(channel.getGuild().getId(), n -> 0L);
                if (System.currentTimeMillis() - calmitdood < cooldown) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String
                                    .format("You are on a cooldown! %s seconds left!",
                                            (cooldown - (System
                                                    .currentTimeMillis() - calmitdood)) / 1000))
                            .build()).queue();
                    return;
                }
            }
            int count;
            try {
                count = Integer.parseInt(args[0]) + 1;
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage("The number entered is too high!", channel);
                return;
            }
            if (count < 2) {
                channel.sendMessage(MessageUtils.getEmbed(sender)
                        .setDescription("Can't purge less than 2 messages!").build()).queue();
            }
            List<Permission> perms = channel.getGuild().getSelfMember().getPermissions(channel);
            if (perms.contains(Permission.MESSAGE_HISTORY) && perms.contains(Permission.MESSAGE_MANAGE)) {
                try {
                    MessageHistory history = new MessageHistory(channel);
                    int toRetrieve = count;
                    while (history.getRetrievedHistory().size() < count) {
                        if (history.retrievePast(Math.min(toRetrieve, 100)).complete().isEmpty())
                            break;
                        toRetrieve -= Math.min(toRetrieve, 100);
                        if (toRetrieve < 2)
                            toRetrieve = 2;
                    }
                    int i = 0;
                    List<Message> toDelete = new ArrayList<>();
                    for (Message m : history.getRetrievedHistory()) {
                        if (m.getCreationTime().plusWeeks(2).isAfter(OffsetDateTime.now())) {
                            i++;
                            toDelete.add(m);
                        }
                        if (toDelete.size() == 100) {
                            channel.deleteMessages(toDelete).complete();
                            toDelete.clear();
                        }
                    }
                    if (!toDelete.isEmpty()) {
                        if (toDelete.size() != 1)
                            channel.deleteMessages(toDelete).complete();
                        else toDelete.forEach(mssage -> mssage.delete().complete());
                    }
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String.format("Deleted `%s` messages!", i)).build())
                            .queue(s -> new FlarebotTask("Delete Message " + s) {
                                @Override
                                public void run() {
                                    s.delete().queue();
                                }
                            }.delay(TimeUnit.SECONDS.toMillis(5)));
                } catch (Exception e) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String
                                    .format("Failed to bulk delete or load messages! Error: `%s`", e))
                            .build()).queue();
                }
            } else {
                channel.sendMessage("Insufficient permissions! I need `Manage Messages` and `Read Message History`")
                        .queue();
            }
        } else {
            MessageUtils.sendUsage(this, channel, sender);
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
        return "`{%}purge <messages>` - Purges a certain amount of messages";
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
    public boolean deleteMessage() {
        return false;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

    @Override
    public EnumSet<Permission> getDiscordPermission() {
        return EnumSet.of(Permission.MESSAGE_MANAGE);
    }
}

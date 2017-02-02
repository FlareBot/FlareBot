package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Purge implements Command {
    private Map<String, Long> cooldowns = new HashMap<>();
    private static final long cooldown = 60000;

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1 && args[0].matches("\\d+")) {
            if (!FlareBot.getInstance().getPermissions(channel).isCreator(sender)) {
                long calmitdood = cooldowns.computeIfAbsent(channel.getGuild().getId(), n -> 0L);
                if (System.currentTimeMillis() - calmitdood < cooldown) {
                    MessageUtils.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String.format("You are on a cooldown! %s seconds left!",
                                    (cooldown - (System.currentTimeMillis() - calmitdood)) / 1000)), channel);
                    return;
                }
            }
            int count = Integer.parseInt(args[0]) + 1;
            if (count < 2) {
                MessageUtils.sendMessage(MessageUtils
                        .getEmbed(sender).setDescription("Can't purge less than 2 messages!"), channel);
            }
            List<Permission> perms = channel.getGuild().getSelfMember().getPermissions(channel);
            if (perms.contains(Permission.MESSAGE_HISTORY) && perms.contains(Permission.MESSAGE_MANAGE)) {
                try {
                    int i = 0;
                    List<Message> history = new ArrayList<>();
                    while (i <= count) {
                        if (i + 100 < count)
                            i += 100;
                        else i += count - i;
                        history.addAll(channel.getHistory().retrievePast(Math.min(count - i, 100)).complete());
                    }
                    List<Message> toDelete = new ArrayList<>();
                    while (!history.isEmpty()) {
                        toDelete.add(history.get(0));
                        history.remove(0);
                        if (toDelete.size() == 100) {
                            channel.deleteMessages(toDelete).queue();
                            toDelete.clear();
                        }
                    }
                    if (toDelete.size() > 2)
                        channel.deleteMessages(toDelete).queue();
                    else toDelete.forEach(m -> m.deleteMessage().queue());
                } catch (Exception e) {
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setDescription(String.format("Failed to bulk delete or load messages! Error: `%s`", e)).build()).queue();
                }
            } else {
                channel.sendMessage("Insufficient permissions! I need `Manage Messages` and `Read Message History`").queue();
            }
        } else {
            MessageUtils.sendMessage(MessageUtils
                    .getEmbed(sender).setDescription("Bad arguments!\n" + getDescription()), channel);
        }
    }

    @Override
    public String getCommand() {
        return "purge";
    }

    @Override
    public String getDescription() {
        return "Removes last X messages. Usage: `purge MESSAGES`";
    }

    @Override
    public CommandType getType() {
        return CommandType.ADMINISTRATIVE;
    }

    @Override
    public String getPermission() {
        return "flarebot.purge";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"clean"};
    }
}

package stream.flarebot.flarebot.commands.automod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.mod.Action;
import stream.flarebot.flarebot.mod.AutoModConfig;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AutoModCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
            return;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("config")) {
                AutoModConfig config = guild.getAutoModConfig();
                EmbedBuilder builder = new EmbedBuilder().setColor(Color.white).addField("Auto Mod Enabled", String
                        .valueOf(config.isEnabled()), true)
                        .addField("Messages per minute", String
                                .valueOf(config.getMaxMessagesPerMinute()), true)
                        .addField("Mod Log Channel", (config
                                .hasModLog() ? config.getModLogChannel()
                                .getAsMention() + " (" + config
                                .getModLog() + ")" : "Not Set"), true);
                StringBuilder sb = new StringBuilder();
                config.getPunishments().forEach((points, punishment) -> sb.append(points).append(" points = ")
                        .append(punishment.getName())
                        .append(punishment
                                .getDuration() > 0 ? " (" + FlareBot
                                .getInstance().formatTime(punishment
                                        .getDuration(), TimeUnit.SECONDS, true, false)
                                .trim() + ")\n" : ""));
                builder.addField("Punishments", sb.toString(), false);
                channel.sendMessage(builder.build()).queue();
            } else if (args[0].equalsIgnoreCase("punishments")) {
                StringBuilder sb = new StringBuilder();
                guild.getAutoModConfig().getPunishments()
                        .forEach((points, punishment) -> {
                            sb.append(points).append(" = ")
                                    .append(punishment.getName());
                            if (punishment.getDuration() > 0)
                                sb.append(" (").append(FlareBot.getInstance().formatTime(punishment
                                        .getDuration(), TimeUnit.SECONDS, true, false)).append(")");
                            sb.append("\n");
                        });
                channel.sendMessage(new EmbedBuilder().setTitle("Guild Punishments", null).setDescription(sb.toString())
                        .build()).queue();
            } else if (args[0].equalsIgnoreCase("enable")) {
                guild.getAutoModConfig().setEnabled(true);
                channel.sendMessage(new EmbedBuilder().setColor(Color.green).setTitle("Enabled AutoMod", null).setDescription("Successfully enabled automod! " +
                        "Check the configuration with `" + getPrefix(channel.getGuild()) + "automod config`").build()).queue();
            } else if (args[0].equalsIgnoreCase("disable")) {
                guild.getAutoModConfig().setEnabled(false);
                channel.sendMessage(new EmbedBuilder().setColor(Color.orange).setTitle("Disabled AutoMod", null).setDescription("Successfully disabled automod! " +
                        "I will no longer be the guardian bot to your server.").build()).queue();
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("punishments")) {
                if (args[1].equalsIgnoreCase("set")) {

                } else if (args[1].equalsIgnoreCase("reset")) {
                    guild.getAutoModConfig().resetPunishments();
                    channel.sendMessage(new EmbedBuilder().setColor(Color.green)
                            .setDescription("Reset the punishments back to default")
                            .build()).queue();
                } else {
                    MessageUtils.sendUsage(this, channel, sender, args);
                }
            } else if (args[0].equalsIgnoreCase("whitelist")) {
                //TODO: Walshy - Add this to usage when finished
                if (args[1].equalsIgnoreCase("list")) {
                    AutoModConfig config = guild.getAutoModConfig();
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(Color.white).setTitle("Whitelists", null);
                    config.getWhitelist().keySet().forEach(action -> builder.addField(action.getName(),
                            config.getWhitelist(action).stream().collect(Collectors.joining("\n")), false));
                    channel.sendMessage(builder.build()).queue();
                } else if (args[1].equalsIgnoreCase("add")) {

                } else if (args[1].equalsIgnoreCase("remove")) {

                } else {

                }
            }
        } else {
            if (args[0].equalsIgnoreCase("whitelist")) {
                //TODO: Walshy - Add this to usage when finished
                if (args.length >= 4) {
                    Action action = Action.getAction(args[2]);
                    if (action != null && action.canBeWhitelisted()) {
                        String whitelist = MessageUtils.getMessage(args, 3);

                        guild.getAutoModConfig().getWhitelist(action)
                                .add(whitelist);
                        channel.sendMessage(new EmbedBuilder().setColor(Color.green)
                                .setDescription("Added `" + whitelist + "` to the `" + action
                                        .getNameWithUnderscore() + "` whitelist!")
                                .build()).queue();
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (Action a : Action.values) {
                            if (!a.canBeWhitelisted()) continue;
                            sb.append(a.getNameWithUnderscore()).append("\n");
                        }
                        MessageUtils
                                .sendErrorMessage("Invalid action! Possible actions you can add a whitelist to are:\n```\n" + sb
                                        .toString() + "\n```", channel);
                    }
                }
            }
        }
    }

    @Override
    public String getCommand() {
        return "automod";
    }

    @Override
    public String getDescription() {
        return "Control all aspects of automod with this command!";
    }

    @Override
    public String getUsage() {
        return "{%}automod enable|disable - Enable or disable automod for your server!\n"
                + "{%}automod config - View config/punishments for a server\n"
                + "{%}automod punishments [reset] - View punishments. Reset punishments with the optional argument\n"
                + "{%}automod punishments set <punishment> <points> [duration] - Description\n"
                + "{%}automod whitelist list|add|remove [whitelist_item] - Description\n"
                //TODO: Walshy - See above
                + "{%}automod";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

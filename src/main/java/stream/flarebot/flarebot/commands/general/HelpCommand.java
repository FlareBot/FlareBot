package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.MessageUtils;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates") // IntelliJ IDEA Ultimate is bitching about it.
public class HelpCommand implements Command {

    @Override
    public void onCommand(User sender, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("here")) {
                sendCommands(channel.getGuild(), sender.openPrivateChannel().complete(), sender);
                return;
            }
            CommandType type;
            try {
                type = CommandType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException ignored) {
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("No such category!").build()).queue();
                return;
            }
            if (type != CommandType.HIDDEN) {
                EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
                embedBuilder.setDescription("***FlareBot " + type + " commands!***");
                List<String> help = type.getCommands()
                                        .stream().filter(cmd -> FlareBot.getInstance().getPermissions(channel)
                                                                        .hasPermission(member, cmd.getPermission()))
                                        .map(command -> get(channel.getGuild()) + command.getCommand() + " - " + command
                                                .getDescription() + '\n')
                                        .collect(Collectors.toList());
                StringBuilder sb = new StringBuilder();
                int page = 0;
                for (String s : help) {
                    if (sb.length() + s.length() > 1024) {
                        embedBuilder
                                .addField(type + (page++ != 0 ? " (cont. " + page + ")" : ""), sb.toString(), false);
                        sb.setLength(0);
                    }
                    sb.append(s);
                }
                embedBuilder.addField(type + (page++ != 0 ? " (cont. " + page + ")" : ""), sb.toString(), false);
                channel.sendMessage(embedBuilder.build()).queue();
            } else {
                channel.sendMessage(MessageUtils.getEmbed(sender).setDescription("No such category!").build()).queue();
            }
        } else {
            sendCommands(channel.getGuild(), channel, sender);
        }
    }

    private char get(Guild guild) {
        if (guild != null) {
            return FlareBot.getPrefixes().get(guild.getId());
        }
        return FlareBot.getPrefixes().get(null);
    }

    private void sendCommands(Guild guild, MessageChannel channel, User sender) {
        EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
        for (CommandType c : CommandType.getTypes()) {
            List<String> help = c.getCommands()
                                 .stream().filter(cmd -> cmd.getPermission() != null && FlareBot.getInstance()
                                                                                                .getPermissions(channel)
                                                                                                .hasPermission(guild
                                                                                                        .getMember(sender), cmd
                                                                                                        .getPermission()))
                                 .map(command -> get(guild) + command.getCommand() + " - " + command
                                         .getDescription() + '\n')
                                 .collect(Collectors.toList());
            StringBuilder sb = new StringBuilder();
            int page = 0;
            for (String s : help) {
                if (sb.length() + s.length() > 1024) {
                    embedBuilder.addField(c + (page++ != 0 ? " (cont. " + page + ")" : ""), sb.toString(), false);
                    sb.setLength(0);
                }
                sb.append(s);
            }
            if (sb.toString().trim().isEmpty()) continue;
            embedBuilder.addField(c + (page++ != 0 ? " (cont. " + page + ")" : ""), sb.toString(), false);
        }
        channel.sendMessage(embedBuilder.build()).queue();
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"commands"};
    }

    @Override
    public String getDescription() {
        return "See a list of all commands.";
    }

    @Override
    public String getUsage() {
        return "`{%}help [here]` - Gives a list of commands [in this channel]\n"
                + "`{%}help <category>` - Gives a list of commands in a specific category";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }
}

package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.*;

public class GuildCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel, sender).queue();
        } else {
            if (args[0].equalsIgnoreCase("block")) {
                if (args.length == 1) {
                    if (guild.isBlocked()) {
                        MessageUtils.sendErrorMessage("Guild already blocked!", channel);
                        return;
                    }
                    guild.addBlocked("");
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.RED).setDescription("This guild has been blocked!").build(), 5000, channel);
                    return;
                } else if (args.length == 2) {
                    Guild guild1 = FlareBot.getInstance().getGuildByID(args[1]);
                    if (guild1 == null) {
                        MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
                        return;
                    }
                    if (FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked()) {
                        MessageUtils.sendErrorMessage("Guild already blocked!", channel);
                        return;
                    }
                    FlareBotManager.getInstance().getGuild(guild1.getId()).addBlocked("");
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.RED).setDescription("That guild has been blocked!").build(), 5000, channel);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("unblock")) {
                if (args.length == 1) {
                    if (!guild.isBlocked()) {
                        MessageUtils.sendErrorMessage("Guild not blocked!", channel);
                        return;
                    }
                    guild.revokeBlock();
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.GREEN).setDescription("This guild has been unblocked!").build(), 5000, channel);
                    return;
                } else if (args.length == 2) {
                    Guild guild1 = FlareBot.getInstance().getGuildByID(args[1]);
                    if (guild1 == null) {
                        MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
                        return;
                    }
                    if (!FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked()) {
                        MessageUtils.sendErrorMessage("Guild not blocked!", channel);
                        return;
                    }
                    FlareBotManager.getInstance().getGuild(guild1.getId()).revokeBlock();
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.GREEN).setDescription("That guild has been unblocked!").build(), 5000, channel);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                if (args.length == 1) {
                    EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
                    embedBuilder.setColor(guild.isBlocked() ? Color.RED : Color.GREEN);
                    embedBuilder.setDescription("This guild " +
                            (guild.isBlocked() ? "is blocked!" : "is not blocked!"));
                    channel.sendMessage(embedBuilder.build()).queue();
                    return;
                } else if (args.length == 2) {
                    Guild guild1 = FlareBot.getInstance().getGuildByID(args[1]);
                    if (guild1 == null) {
                        MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
                        return;
                    }
                    EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
                    embedBuilder.setColor(FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked() ? Color.RED : Color.GREEN);
                    embedBuilder.setDescription("That guild " +
                            (FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked() ? "is blocked!" : "is not blocked!"));
                    channel.sendMessage(embedBuilder.build()).queue();
                    return;
                }
            }
            MessageUtils.getUsage(this, channel, sender).queue();
        }
    }

    @Override
    public String getCommand() {
        return "guild";
    }

    @Override
    public String getDescription() {
        return "Allows the owners to block guilds";
    }

    @Override
    public String getUsage() {
        return "`{%}guild block [guildID]` - Blocks this guild [or another guild]\n" +
                "`{%}guild unblock [guildID]` - Unblocks this guild [or another guild]\n" +
                "`{%}guild status [guildID]` - Shows the status of this guild [or another guild]";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }
}

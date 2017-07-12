package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.FlareBotManager;
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
                    if (FlareBotManager.getInstance().isBlockedGuild(guild.getGuild().getId())) {
                        MessageUtils.sendErrorMessage("Guild already blocked!", channel);
                        return;
                    }
                    FlareBotManager.getInstance().addBlockedGuild(guild.getGuild().getId(), "");
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.RED).setDescription("This guild has been blocked!").build(), 5000, channel);
                    return;
                } else if (args.length == 2) {
                    Guild guild1 = FlareBot.getInstance().getGuildByID(args[1]);
                    if (guild1 == null) {
                        MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
                        return;
                    }
                    if (FlareBotManager.getInstance().isBlockedGuild(guild1.getId())) {
                        MessageUtils.sendErrorMessage("Guild already blocked!", channel);
                        return;
                    }
                    FlareBotManager.getInstance().addBlockedGuild(guild1.getId(), "");
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.RED).setDescription("That guild has been blocked!").build(), 5000, channel);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("unblock")) {
                if (args.length == 1) {
                    if (!FlareBotManager.getInstance().isBlockedGuild(guild.getGuild().getId())) {
                        MessageUtils.sendErrorMessage("Guild not blocked!", channel);
                        return;
                    }
                    FlareBotManager.getInstance().removeBlockedGuild(guild.getGuild().getId());
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.GREEN).setDescription("This guild has been unblocked!").build(), 5000, channel);
                    return;
                } else if (args.length == 2) {
                    Guild guild1 = FlareBot.getInstance().getGuildByID(args[1]);
                    if (guild1 == null) {
                        MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
                        return;
                    }
                    if (!FlareBotManager.getInstance().isBlockedGuild(guild1.getId())) {
                        MessageUtils.sendErrorMessage("Guild not blocked!", channel);
                        return;
                    }
                    FlareBotManager.getInstance().removeBlockedGuild(guild1.getId());
                    MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed(sender).setColor(Color.GREEN).setDescription("That guild has been unblocked!").build(), 5000, channel);
                    return;
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                if (args.length == 1) {
                    EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
                    embedBuilder.setColor(FlareBotManager.getInstance().isBlockedGuild(guild.getGuild().getId()) ? Color.RED : Color.GREEN);
                    embedBuilder.setDescription("This guild " +
                            (FlareBotManager.getInstance().isBlockedGuild(guild.getGuild().getId()) ? "is blocked!" : "is not blocked!"));
                    channel.sendMessage(embedBuilder.build()).queue();
                    return;
                } else if (args.length == 2) {
                    Guild guild1 = FlareBot.getInstance().getGuildByID(args[1]);
                    if (guild1 == null) {
                        MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
                        return;
                    }
                    EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
                    embedBuilder.setColor(FlareBotManager.getInstance().isBlockedGuild(guild.getGuild().getId()) ? Color.RED : Color.GREEN);
                    embedBuilder.setDescription("That guild " +
                            (FlareBotManager.getInstance().isBlockedGuild(guild.getGuild().getId()) ? "is blocked!" : "is not blocked!"));
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

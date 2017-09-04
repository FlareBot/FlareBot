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

import java.awt.Color;

public class GuildCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.getUsage(this, channel, sender).queue();
        } else {
            if (args[0].equalsIgnoreCase("block")) {
                if (args.length == 1) {
                    handleBlock(channel, channel.getGuild().getId(), null);
                } else if (args.length == 2) {
                    handleBlock(channel, args[1], null);
                }else if(args.length >= 3) {
                    handleBlock(channel, args[1], FlareBot.getMessage(args, 2));
                }
            } else if (args[0].equalsIgnoreCase("unblock")) {
                if (args.length == 1) {
                    handleUnblock(channel, channel.getGuild().getId());
                } else if (args.length == 2) {
                    handleUnblock(channel, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                if (args.length == 1) {
                    EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender);
                    embedBuilder.setColor(guild.isBlocked() ? Color.RED : Color.GREEN);
                    embedBuilder.setDescription("This guild " +
                            (guild.isBlocked() ? "is blocked!" : "is not blocked!"))
                            .addField("Reason", (guild.getBlockReason() == null ? "No reason provided!" : guild.getBlockReason()), false);
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
                            (FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked() ? "is blocked!" : "is not blocked!"))
                            .addField("Reason", (guild.getBlockReason() == null ? "No reason provided!" : guild.getBlockReason()), false);
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
        return "`{%}guild block [guildID] [reason]` - Blocks this guild [or another guild]\n" +
                "`{%}guild unblock [guildID]` - Unblocks this guild [or another guild]\n" +
                "`{%}guild status [guildID]` - Shows the status of this guild [or another guild]";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }

    private void handleBlock(TextChannel channel, String guildId, String reason) {
        Guild guild1 = FlareBot.getInstance().getGuildByID(guildId);
        if (guild1 == null) {
            MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
            return;
        }
        if (FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked()) {
            MessageUtils.sendErrorMessage("Guild already blocked!", channel);
            return;
        }
        FlareBotManager.getInstance().getGuild(guild1.getId()).addBlocked(reason);
        MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed().setColor(Color.RED).setDescription("Guild has been blocked!").build(), 5000, channel);
        return;
    }

    private void handleUnblock(TextChannel channel, String guildId) {
        Guild guild1 = FlareBot.getInstance().getGuildByID(guildId);
        if (guild1 == null) {
            MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
            return;
        }
        if (!FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked()) {
            MessageUtils.sendErrorMessage("Guild not blocked!", channel);
            return;
        }
        FlareBotManager.getInstance().getGuild(guild1.getId()).revokeBlock();
        MessageUtils.sendAutoDeletedMessage(MessageUtils.getEmbed().setColor(Color.GREEN).setDescription("Guild has been unblocked!").build(), 5000, channel);
        return;
    }
}

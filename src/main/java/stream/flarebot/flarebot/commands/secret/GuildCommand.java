package stream.flarebot.flarebot.commands.secret;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
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
import stream.flarebot.flarebot.util.MessageType;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class GuildCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 0) {
            MessageUtils.sendUsage(this, channel, sender, args);
        } else {
            if (args[0].equalsIgnoreCase("block")) {
                if (args.length == 1) {
                    handleBlock(channel, channel.getGuild().getId(), null);
                } else if (args.length == 2) {
                    handleBlock(channel, args[1], null);
                } else {
                    handleBlock(channel, args[1], FlareBot.getMessage(args, 2));
                }
            } else if (args[0].equalsIgnoreCase("unblock")) {
                if (args.length == 1) {
                    handleUnblock(channel, channel.getGuild().getId());
                } else if (args.length == 2) {
                    handleUnblock(channel, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("status")) {
                GuildWrapper wrapper = guild;
                if (args.length == 2) {
                    if (FlareBot.getInstance().getGuildById(args[1]) == null) {
                        MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
                        return;
                    }
                    wrapper = FlareBotManager.getInstance().getGuild(args[1]);
                }
                Guild g = wrapper.getGuild();

                EmbedBuilder embedBuilder = MessageUtils.getEmbed(sender)
                        .setColor(guild.isBlocked() ? Color.RED : Color.GREEN);
                embedBuilder.setTitle(g.getName(), null).addField("Beta", String.valueOf(wrapper.getBetaAccess()), true)
                        .addField("Blocked", guild.isBlocked() + (guild.isBlocked() ? " (`" + wrapper.getBlockReason()
                                + "`)" : ""), true);
                channel.sendMessage(embedBuilder.build()).queue();
            } else if (args[0].equalsIgnoreCase("beta")) {
                if (args.length == 1) {
                    guild.setBetaAccess(!guild.getBetaAccess());
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setColor(guild.getBetaAccess() ? Color.GREEN : Color.RED)
                            .setDescription("This guild has successfully been " + (guild.getBetaAccess() ? "given"
                                    : "removed from") + " beta access!")
                            .build()).queue();
                } else if (args.length == 2) {
                    GuildWrapper guildWrapper = FlareBotManager.getInstance().getGuild(args[1]);
                    if (guildWrapper.getGuild() == null) {
                        MessageUtils.sendErrorMessage("That guild does not exist!", channel);
                    } else {
                        guildWrapper.setBetaAccess(!guildWrapper.getBetaAccess());
                        channel.sendMessage(MessageUtils.getEmbed(sender)
                                .setColor(guildWrapper.getBetaAccess() ? Color.GREEN : Color.RED)
                                .setDescription("The guild `" + guildWrapper.getGuild().getName() + "` has successfully " +
                                        "been " + (guildWrapper.getBetaAccess() ? "given" : "removed from") + " beta access!")
                                .build()).queue();
                    }
                }
            } else if (args[0].equalsIgnoreCase("data")) {
                GuildWrapper wrapper = guild;
                if (args.length == 2)
                    wrapper = FlareBotManager.getInstance().getGuild(args[1]);
                if (wrapper.getGuild() == null) {
                    MessageUtils.sendErrorMessage("That guild does not exist!", channel);
                } else {
                    try {
                        PrintWriter out = new PrintWriter("data.json");
                        out.println(FlareBot.GSON.toJson(wrapper));
                        out.close();
                    } catch (FileNotFoundException e) {
                        FlareBot.LOGGER.error("Failed to write data", e);
                    }
                    sender.openPrivateChannel().complete().sendFile(new File("data.json"), new MessageBuilder()
                            .append('\u200B').build()).queue();
                }
            } else if (args[0].equalsIgnoreCase("save")) {
                GuildWrapper wrapper = guild;
                if (args.length >= 2) {
                    wrapper = FlareBotManager.getInstance().getGuild(FlareBot.getInstance().getGuildById(args[1]).getId());
                }
                if (wrapper.getGuild() == null) {
                    MessageUtils.sendErrorMessage("Invalid guild ID!", channel);
                    return;
                }
                FlareBotManager.getInstance().saveGuild(wrapper.getGuildId(), wrapper, -1);
                MessageUtils.sendSuccessMessage("Saved " + wrapper.getGuildId() + "'s guild data!", channel);
            } else
                MessageUtils.sendUsage(this, channel, sender, args);
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
                "`{%}guild status [guildID]` - Shows the status of this guild [or another guild]\n" +
                "`{%}guild beta [guildID]` - Gives beta access to this guild [or another guild]\n" +
                "`{%}guild data [guildID]` - Gets the JSON data for the current guild [or another guild]\n" +
                "`{%}guild save [guildID]` - Save guild data";
    }

    @Override
    public CommandType getType() {
        return CommandType.SECRET;
    }

    private void handleBlock(TextChannel channel, String guildId, String reason) {
        Guild guild1 = FlareBot.getInstance().getGuildById(guildId);
        if (guild1 == null) {
            MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
            return;
        }
        if (FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked()) {
            MessageUtils.sendErrorMessage("Guild already blocked!", channel);
            return;
        }
        FlareBotManager.getInstance().getGuild(guild1.getId()).addBlocked(reason);
        MessageUtils.sendErrorMessage("Guild has been blocked!", channel);
    }

    private void handleUnblock(TextChannel channel, String guildId) {
        Guild guild1 = FlareBot.getInstance().getGuildById(guildId);
        if (guild1 == null) {
            MessageUtils.sendErrorMessage("That guild ID is not valid!", channel);
            return;
        }
        if (!FlareBotManager.getInstance().getGuild(guild1.getId()).isBlocked()) {
            MessageUtils.sendErrorMessage("Guild not blocked!", channel);
            return;
        }
        FlareBotManager.getInstance().getGuild(guild1.getId()).revokeBlock();
        MessageUtils.sendMessage(MessageType.SUCCESS, "Guild has been unblocked!", channel, 5000);
    }
}

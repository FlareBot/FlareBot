package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.objects.RestActionWrapper;
import stream.flarebot.flarebot.util.ConfirmLib;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class PruneCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("server") && args.length == 2) {
                if (getPermissions(channel).hasPermission(member, "flarebot.prune.server")) {
                    int amount;
                    try {
                        amount = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        MessageUtils.sendErrorMessage("Please enter a valid amount of days!", channel);
                        return;
                    }

                    if (amount == 0) {
                        MessageUtils.sendErrorMessage("The amount of days has to be more that 0!", channel);
                    }

                    int userSize = guild.getGuild().getPrunableMemberCount(amount).complete();
                    channel.sendMessage(MessageUtils.getEmbed(sender)
                            .setColor(Color.RED)
                            .setDescription(GeneralUtils.formatCommandPrefix(channel, "Are you sure you want to prune " + userSize + " members?\n" +
                                    "To confirm type `{%}prune confirm` within 1 minute!"))
                            .build()).queue();

                    ConfirmLib.pushAction(sender.getId(),
                            new RestActionWrapper(guild.getGuild().getController()
                                    .prune(amount)
                                    .reason("Pruned by user: " + MessageUtils.getTag(sender)), this.getClass()));
                    return;

                } else {
                    MessageUtils.sendErrorMessage("You need the permission `flarebot.prune.server` to do this!", channel);
                }
            } else if (args[0].equalsIgnoreCase("confirm")) {
                if (ConfirmLib.checkExists(sender.getId(), this.getClass())) {
                    ConfirmLib.get(sender.getId(), this.getClass()).queue();
                    ConfirmLib.remove(sender.getId(), this.getClass());
                } else {
                    MessageUtils.sendErrorMessage("You haven't got any action to confirm!", channel);
                }
                return;
            }
        }
        MessageUtils.getUsage(this, channel, sender).queue();
    }

    @Override
    public String getCommand() {
        return "prune";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.ConfirmUtil;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.objects.RestActionRunnable;
import stream.flarebot.flarebot.util.objects.RunnableWrapper;

public class PruneCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length != 0) {
            if (args[0].equalsIgnoreCase("server") && args.length == 2) {
                // Re-add sub-permission when roles is added
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage("Please enter a valid amount of days!", channel);
                    return;
                }

                if (amount == 0) {
                    MessageUtils.sendErrorMessage("The amount of days has to be more that 0!", channel);
                    return;
                }
                if (amount > 30) {
                    MessageUtils.sendErrorMessage("You can only prune a maximum of 30 days!", channel);
                    return;
                }

                int userSize = guild.getGuild().getPrunableMemberCount(amount).complete();
                MessageUtils.sendErrorMessage(GeneralUtils.formatCommandPrefix(channel, "Are you sure you want to prune " + userSize + " members?\n" +
                        "To confirm type `{%}prune confirm` within 1 minute!"), channel);

                ConfirmUtil.pushAction(sender.getId(),
                        new RunnableWrapper(
                                new RestActionRunnable(guild.getGuild().getController()
                                        .prune(amount)
                                        .reason("Pruned by user: " + MessageUtils.getTag(sender))), this.getClass()));
                return;
            } else if (args[0].equalsIgnoreCase("confirm")) {
                if (ConfirmUtil.checkExists(sender.getId(), this.getClass())) {
                    ConfirmUtil.run(sender.getId(), this.getClass());
                    ConfirmUtil.remove(sender.getId(), this.getClass());
                } else {
                    MessageUtils.sendErrorMessage("You haven't got any action to confirm!", channel);
                }
                return;
            }
        }
        MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "prune";
    }

    @Override
    public String getDescription() {
        return "Allows server mods to easily prune members from a discord";
    }

    @Override
    public String getUsage() {
        return "`{%}prune server <days>` - Prunes the entire server." +
                " Only members inactive longer than the specified amount of days will be removed\n" +
                "`{%}prune confirm` - Confirms a user's actions";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}

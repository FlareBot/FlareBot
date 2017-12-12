package stream.flarebot.flarebot.mod.modlog;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.joda.time.Period;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.scheduler.FutureAction;
import stream.flarebot.flarebot.scheduler.Scheduler;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class ModlogHandler {

    private static ModlogHandler instance;

    public static ModlogHandler getInstance() {
        if (instance == null) instance = new ModlogHandler();
        return instance;
    }

    /**
     * This will get the TextChannel of a certain event, this could also return null in two cases.<br>
     * <ol>
     * <li>They don't have a channel set for the event</li>
     * <li>The channel set is in another guild.</li>
     * </ol>
     *
     * @param wrapper GuildWrapper of the desired guild to check.
     * @param event   The event to check.
     * @return The TextChannel of the desired event in the desired guild or null in the two cases listed above.
     */
    public TextChannel getModlogChannel(GuildWrapper wrapper, ModlogEvent event) {
        for (ModlogAction modlogAction : wrapper.getModConfig().getEnabledActions())
            if (modlogAction.getEvent() == event)
                return wrapper.getGuild().getTextChannelById(modlogAction.getModlogChannelId());
        return null;
    }

    public void postToModlog(GuildWrapper wrapper, ModlogEvent event, User user) {
        postToModlog(wrapper, event, user, null, null, new MessageEmbed.Field[0]);
    }

    public void postToModlog(GuildWrapper wrapper, ModlogEvent event, User user, MessageEmbed.Field... fields) {
        postToModlog(wrapper, event, user, null, null, fields);
    }

    public void postToModlog(GuildWrapper wrapper, ModlogEvent event, User user, EmbedBuilder builder) {
        postToModlog(wrapper, event, user, null, null, builder.getFields().toArray(new
                MessageEmbed.Field[builder.getFields().size()]));
    }

    public void postToModlog(GuildWrapper wrapper, ModlogEvent event, User target, User responsible, String reason) {
        postToModlog(wrapper, event, target, responsible, reason, new MessageEmbed.Field[0]);
    }

    public void postToModlog(GuildWrapper wrapper, ModlogEvent event, User target, User responsible, String reason,
                             MessageEmbed.Field... extraFields) {
        if (!wrapper.getModeration().isEventEnabled(wrapper, event)) return;
        TextChannel tc = getModlogChannel(wrapper, event);
        // They either don't have a channel or set it to another guild.
        if (tc != null) {
            EmbedBuilder eb = event.getEventEmbed(target, responsible, reason);
            if (extraFields != null && extraFields.length > 0) {
                for (MessageEmbed.Field field : extraFields)
                    eb.addField(field);
            }
            tc.sendMessage(eb.build()).queue();
        }
    }

    /**
     * @param channel
     * @param target
     * @param sender
     * @param modAction
     * @param reason
     */
    public void handleAction(GuildWrapper wrapper, TextChannel channel, User sender, User target, ModAction modAction,
                             String reason) {
        handleAction(wrapper, channel, sender, target, modAction, reason, -1);
    }

    /**
     * @param channel
     * @param target
     * @param sender
     * @param modAction
     * @param reason
     * @param duration
     */
    public void handleAction(GuildWrapper wrapper, TextChannel channel, User sender, User target, ModAction modAction,
                             String reason, long duration) {
        String rsn = (reason == null ? "" : "(`" + reason.replaceAll("`", "'") + "`)");
        Member member = wrapper.getGuild().getMember(target);
        if (member == null && modAction != ModAction.FORCE_BAN) {
            MessageUtils.sendErrorMessage("That user isn't in this guild! You can try to forceban the user if needed.", channel);
            return;
        }

        // Make sure the target user isn't the guild owner
        if (member != null && member.isOwner()) {
            MessageUtils.sendErrorMessage(String.format("Cannot %s **%s** because they're the guild owner!",
                    modAction.getLowercaseName(), MessageUtils.getTag(target)), channel);
            return;
        }

        // Make sure the target user isn't themselves
        if (target.getIdLong() == sender.getIdLong()) {
            MessageUtils.sendErrorMessage(String.format("You cannot %s yourself you daft person!",
                    modAction.getLowercaseName()), channel);
            return;
        }

        // Check if the person is below the target in role hierarchy
        // TODO: ^

        // Check if there role is higher therefore we can't take action, this should be something applied to everything
        // not just kick, ban etc.
        if (member != null && !wrapper.getGuild().getSelfMember().canInteract(member)) {
            MessageUtils.sendErrorMessage(String.format("Cannot " + modAction.getLowercaseName() + " %s! " +
                            "Their highest role is higher than my highest role or they're the guild owner.",
                    MessageUtils.getTag(target)), channel);
            return;
        }

        try {
            // BAN
            if (modAction == ModAction.BAN) {
                channel.getGuild().getController().ban(target, 7, reason).queue(aVoid ->
                        channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN)
                                .setDescription("The ban hammer has been struck on " + target.getName()
                                        + " <:banhammer:368861419602575364>\nReason: `" + rsn + "`")
                                .setImage(channel.getGuild().getId().equals(Constants.OFFICIAL_GUILD) ?
                                        "https://flarebot.stream/img/banhammer.png" : null)
                                .build()).queue());
            } else if (modAction == ModAction.FORCE_BAN) {
                channel.getGuild().getController().ban(target.getId(), 7, reason).queue(aVoid ->
                        channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN)
                                .setDescription("The ban hammer has been forcefully struck on " + target.getName()
                                        + " <:banhammer:368861419602575364>\nReason: `" + rsn + "`")
                                .setImage(channel.getGuild().getId().equals(Constants.OFFICIAL_GUILD) ?
                                        "https://flarebot.stream/img/banhammer.png" : null)
                                .build()).queue());
            } else if (modAction == ModAction.TEMP_BAN) {
                Period period = new Period(duration);
                channel.getGuild().getController().ban(channel.getGuild().getMember(target), 7, reason).queue(aVoid -> {
                    channel.sendMessage(new EmbedBuilder()
                            .setDescription("The ban hammer has been struck on " + target.getName() + " for "
                                    + GeneralUtils.formatJodaTime(period) + "\nReason: `" + rsn + "`")
                            .setImage(channel.getGuild().getId()
                                    .equals(Constants.OFFICIAL_GUILD) ? "https://flarebot.stream/img/banhammer.png" : null)
                            .setColor(Color.WHITE).build()).queue();
                    Scheduler.queueFutureAction(channel.getGuild().getIdLong(), channel.getIdLong(), sender.getIdLong(),
                            target.getIdLong(), reason, period, FutureAction.Action.TEMP_BAN);
                });
            } else if (modAction == ModAction.UNBAN) {
                wrapper.getGuild().getController().unban(target).queue();

                MessageUtils.sendSuccessMessage("Unbanned " + target.getAsMention() + "!", channel, sender);
                // MUTE
            } else if (modAction == ModAction.MUTE) {
                try {
                    wrapper.getModeration().muteUser(wrapper, wrapper.getGuild().getMember(target));
                } catch (HierarchyException e) {
                    MessageUtils.sendErrorMessage("Cannot apply the mute role, make sure it is below FlareBot in the " +
                                    "role hierarchy.",
                            channel);
                    return;
                }

                MessageUtils.sendSuccessMessage("Muted " + target.getAsMention() + (reason == null ? "" : " (`"
                        + reason.replaceAll("`", "'") + "`)"), channel, sender);
            } else if (modAction == ModAction.TEMP_MUTE) {
                try {
                    wrapper.getModeration().muteUser(wrapper, wrapper.getGuild().getMember(target));
                } catch (HierarchyException e) {
                    MessageUtils.sendErrorMessage("Cannot apply the mute role, make sure it is below FlareBot in the " +
                                    "role hierarchy.",
                            channel);
                    return;
                }

                Period period = new Period(duration);
                Scheduler.queueFutureAction(channel.getGuild().getIdLong(), channel.getIdLong(), sender.getIdLong(),
                        target.getIdLong(), reason, period, FutureAction.Action.TEMP_BAN);

                MessageUtils.sendSuccessMessage("Temporarily Muted " + target.getAsMention() + " for "
                        + GeneralUtils.formatJodaTime(period) + (reason == null ? "" : " (`" +
                        reason.replaceAll("`", "'") + "`)"), channel, sender);
            } else if (modAction == ModAction.UNMUTE) {
                if (wrapper.getGuild().getMember(target).getRoles().contains(wrapper.getMutedRole())) {
                    wrapper.getGuild().getController().removeSingleRoleFromMember(wrapper.getGuild().getMember(target),
                            wrapper.getMutedRole()).queue();
                    MessageUtils.sendSuccessMessage("Unmuted " + target.getAsMention(), channel, sender);
                } else {
                    MessageUtils.sendErrorMessage("That user isn't muted!!", channel);
                }

                // KICK and WARN
            } else if (modAction == ModAction.KICK) {
                channel.getGuild().getController().kick(member, reason).queue(aVoid ->
                        MessageUtils.sendSuccessMessage(target.getName() + " has been kicked from the server!" + rsn,
                                channel, sender));
            } else if (modAction == ModAction.WARN) {
                wrapper.addWarning(target, (reason != null ? reason : "No reason provided - action done by "
                        + sender.getName()));
                EmbedBuilder eb = new EmbedBuilder();
                eb.appendDescription("\u26A0 Warned " + MessageUtils.getTag(target)
                        + (reason != null ? " (`" + reason.replaceAll("`", "'") + "`)" : ""))
                        .setColor(Color.WHITE);
                channel.sendMessage(eb.build()).queue();
            } else {
                throw new IllegalArgumentException("An illegal ModAction was attempted to be handled - "
                        + modAction.toString());
            }
        } catch (PermissionException e) {
            MessageUtils.sendErrorMessage(String.format("Cannot " + modAction.getLowercaseName() + " %s! " +
                            "I do not have the `" + e.getPermission().getName() + "` permission!",
                    MessageUtils.getTag(target)), channel);
        }
        // TODO: Infraction
        postToModlog(wrapper, modAction.getEvent(), sender, target, rsn);
    }
}

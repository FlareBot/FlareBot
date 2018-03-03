package stream.flarebot.flarebot.mod.modlog;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.joda.time.Period;
import stream.flarebot.flarebot.FlareBot;
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
            if (!tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS)) {
                tc.getGuild().getOwner().getUser().openPrivateChannel().queue(pc -> {
                    pc.sendMessage("Please give me permission to read message, write messages and embed links in the modlog channel: "
                            + tc.getAsMention()
                            + " or set the modlog channel to one I have access to!").queue();
                });
                return;
            }
            if (!wrapper.getModeration().isEventCompacted(event)) {
                EmbedBuilder eb = event.getEventEmbed(target, responsible, reason);
                if (extraFields != null && extraFields.length > 0) {
                    for (MessageEmbed.Field field : extraFields)
                        eb.addField(field);
                }

                tc.sendMessage(eb.build()).queue();
            } else {
                StringBuilder sb = new StringBuilder(event.getEventText(target, responsible, reason));
                if (extraFields != null && extraFields.length > 0) {
                    sb.append("\n");
                    for (MessageEmbed.Field field : extraFields) {
                        if (field == null) continue;
                        sb.append("**").append(field.getName()).append("**: ").append(field.getValue()).append("\t");
                    }
                }
                sb.append("\n** **");
                tc.sendMessage(sb.toString().trim()).queue();
            }
        }
    }

    /**
     * Handle a ModAction, this will do a bunch of checks and if they pass it will handle said action. For example if
     * you want to ban someone it will do checks like if you can ban that user, ig they're the owner, if you're trying
     * to ban yourself etc. After those pass it will then do the actual banning, post to the modlog and handle any tmp
     * stuff if needed. <br />
     * This will run the {@link #handleAction(GuildWrapper, TextChannel, User, User, ModAction, String, long)} method
     * with a -1 duration.
     *
     * @param wrapper   The GuildWrapper of the guild this is being done in.
     * @param channel   The channel this was executed, this is used for failire messages in the checks.
     * @param sender    The person who sent that said action, the user responsible.
     * @param target    The target user to have the action taken against.
     * @param modAction The ModAction to be performed.
     * @param reason    The reason this was done, if this is null it will default to "No Reason Given".
     */
    public void handleAction(GuildWrapper wrapper, TextChannel channel, User sender, User target, ModAction modAction,
                             String reason) {
        handleAction(wrapper, channel, sender, target, modAction, reason, -1);
    }

    /**
     * Handle a ModAction, this will do a bunch of checks and if they pass it will handle said action. For example if
     * you want to ban someone it will do checks like if you can ban that user, ig they're the owner, if you're trying
     * to ban yourself etc. After those pass it will then do the actual banning, post to the modlog and handle any tmp
     * stuff if needed.<br />
     * See also {@link #handleAction(GuildWrapper, TextChannel, User, User, ModAction, String)}
     *
     * @param wrapper   The GuildWrapper of the guild this is being done in.
     * @param channel   The channel this was executed, this is used for failire messages in the checks.
     * @param sender    The person who sent that said action, the user responsible.
     * @param target    The target user to have the action taken against.
     * @param modAction The ModAction to be performed.
     * @param reason    The reason this was done, if this is null it will default to "No Reason Given".
     * @param duration  The duration of said action, this only applies to temp actions, -1 should be passed otherwise.
     */
    public void handleAction(GuildWrapper wrapper, TextChannel channel, User sender, User target, ModAction modAction,
                             String reason, long duration) {
        String rsn = (reason == null ? "No reason given!" : "(`" + reason.replaceAll("`", "'") + "`)");
        Member member = null;
        if (target != null) {
            member = wrapper.getGuild().getMember(target);
        }
        if (channel == null) return;
        if (member == null && modAction != ModAction.BAN && modAction != ModAction.FORCE_BAN) {
            MessageUtils.sendErrorMessage("That user isn't in this server!"
                    + (modAction == ModAction.BAN ? " You can forceban with `{%}forceban <id>`." : ""), channel);
            return;
        }

        // Make sure the target user isn't the guild owner
        if (member != null && member.isOwner()) {
            MessageUtils.sendErrorMessage(String.format("Cannot %s **%s** because they're the guild owner!",
                    modAction.getLowercaseName(), MessageUtils.getTag(target)), channel);
            return;
        }

        // Make sure the target user isn't themselves
        if (target != null && sender != null && target.getIdLong() == sender.getIdLong()) {
            MessageUtils.sendErrorMessage(String.format("You cannot %s yourself you daft person!",
                    modAction.getLowercaseName()), channel);
            return;
        }

        if (target != null && target.getIdLong() == FlareBot.getInstance().getClient().getSelfUser().getIdLong()) {
            if (modAction == ModAction.UNBAN || modAction == ModAction.UNMUTE)
                MessageUtils.sendWarningMessage("W-why would you want to do that in the first place. Meanie :(", channel);
            else
                MessageUtils.sendWarningMessage(String.format("T-that's meannnnnnn :( I can't %s myself and I hope you don't want to either :(",
                    modAction.getLowercaseName()), channel);
            return;
        }

        // Check if the person is below the target in role hierarchy
        if (member != null && !member.getRoles().isEmpty() && member.getRoles().get(0).getPosition() > wrapper.getGuild().getMember(target).getRoles().get(0).getPosition()) {
            MessageUtils.sendErrorMessage(String.format("You cannot %s a user who is higher than you in the role hierarchy!",
                    modAction.getLowercaseName()), channel);
            return;
        }

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
            switch (modAction) {
                case BAN:
                    channel.getGuild().getController().ban(target, 7, reason).queue(aVoid ->
                            channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN)
                                    .setDescription("The ban hammer has been struck on " + target.getName()
                                            + " <:banhammer:368861419602575364>\nReason: " + rsn)
                                    .setImage(channel.getGuild().getId().equals(Constants.OFFICIAL_GUILD) ?
                                            "https://flarebot.stream/img/banhammer.png" : null)
                                    .build()).queue());
                    break;
                case FORCE_BAN:
                    channel.getGuild().getController().ban(target.getId(), 7, reason).queue(aVoid ->
                            channel.sendMessage(new EmbedBuilder().setColor(Color.GREEN)
                                    .setDescription("The ban hammer has been forcefully struck on " + target.getName()
                                            + " <:banhammer:368861419602575364>\nReason: " + rsn)
                                    .setImage(channel.getGuild().getId().equals(Constants.OFFICIAL_GUILD) ?
                                            "https://flarebot.stream/img/banhammer.png" : null)
                                    .build()).queue());
                    break;
                case TEMP_BAN: {
                    Period period = new Period(duration);
                    channel.getGuild().getController().ban(channel.getGuild().getMember(target), 7, reason).queue(aVoid -> {
                        channel.sendMessage(new EmbedBuilder()
                                .setDescription("The ban hammer has been struck on " + target.getName() + " for "
                                        + GeneralUtils.formatJodaTime(period) + "\nReason: " + rsn)
                                .setImage(channel.getGuild().getId()
                                        .equals(Constants.OFFICIAL_GUILD) ? "https://flarebot.stream/img/banhammer.png" : null)
                                .setColor(Color.WHITE).build()).queue();
                        Scheduler.queueFutureAction(channel.getGuild().getIdLong(), channel.getIdLong(), sender.getIdLong(),
                                target.getIdLong(), reason, period, FutureAction.Action.TEMP_BAN);
                    });
                    break;
                }
                case UNBAN:
                    wrapper.getGuild().getController().unban(target).queue();

                    MessageUtils.sendSuccessMessage("Unbanned " + target.getAsMention() + "!", channel, sender);
                    // MUTE
                    break;
                case MUTE:
                    try {
                        wrapper.getModeration().muteUser(wrapper, wrapper.getGuild().getMember(target));
                    } catch (HierarchyException e) {
                        MessageUtils.sendErrorMessage("Cannot apply the mute role, make sure it is below FlareBot in the " +
                                        "role hierarchy.",
                                channel);
                        return;
                    }

                    MessageUtils.sendSuccessMessage("Muted " + target.getAsMention() + "\nReason: " + rsn, channel, sender);
                    break;
                case TEMP_MUTE: {
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
                            target.getIdLong(), reason, period, FutureAction.Action.TEMP_MUTE);

                    MessageUtils.sendSuccessMessage("Temporarily Muted " + target.getAsMention() + " for "
                            + GeneralUtils.formatJodaTime(period) + "\nReason: " + rsn, channel, sender);
                    break;
                }
                case UNMUTE:
                    if (wrapper.getGuild().getMember(target).getRoles().contains(wrapper.getMutedRole())) {
                        wrapper.getGuild().getController().removeSingleRoleFromMember(wrapper.getGuild().getMember(target),
                                wrapper.getMutedRole()).queue();
                        MessageUtils.sendSuccessMessage("Unmuted " + target.getAsMention(), channel, sender);
                    } else {
                        MessageUtils.sendErrorMessage("That user isn't muted!!", channel);
                    }

                    // KICK and WARN
                    break;
                case KICK:
                    channel.getGuild().getController().kick(member, reason).queue(aVoid ->
                            MessageUtils.sendSuccessMessage(target.getName() + " has been kicked from the server!\nReason: " + rsn,
                                    channel, sender));
                    break;
                case WARN:
                    wrapper.addWarning(target, (reason != null ? reason : "No reason provided - action done by "
                            + sender.getName()));
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.appendDescription("\u26A0 Warned " + MessageUtils.getTag(target)
                            + "\nReason: " + rsn)
                            .setColor(Color.WHITE);
                    channel.sendMessage(eb.build()).queue();
                    break;
                default:
                    throw new IllegalArgumentException("An illegal ModAction was attempted to be handled - "
                            + modAction.toString());
            }
        } catch (PermissionException e) {
            MessageUtils.sendErrorMessage(String.format("Cannot " + modAction.getLowercaseName() + " %s! " +
                            "I do not have the `" + e.getPermission().getName() + "` permission!",
                    MessageUtils.getTag(target)), channel);
            return;
        }
        // TODO: Infraction
        postToModlog(wrapper, modAction.getEvent(), target, sender, rsn);
    }
}

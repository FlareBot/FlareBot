package stream.flarebot.flarebot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.audit.ActionType;
import net.dv8tion.jda.core.audit.AuditLogChange;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent;
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.guild.update.GenericGuildUpdateEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateExplicitContentLevelEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.GenericRoleUpdateEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import stream.flarebot.flarebot.database.RedisController;
import stream.flarebot.flarebot.database.RedisMessage;
import stream.flarebot.flarebot.mod.modlog.ModlogEvent;
import stream.flarebot.flarebot.mod.modlog.ModlogHandler;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModlogEvents extends ListenerAdapter {

    private long roleResponseNumber = 0;
    private long guildResponseNumber = 0;

    @Override
    public void onGuildBan(GuildBanEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.USER_BANNED)) return;
        event.getGuild().getAuditLogs().limit(1).queue(auditLogEntries -> {
            AuditLogEntry entry = auditLogEntries.get(0);
            ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.USER_BANNED, event.getUser(),
                    entry.getType() == ActionType.BAN ? entry.getUser() : null,
                    entry.getType() == ActionType.BAN ? entry.getReason() : null);
        });
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MEMBER_JOIN)) return;
        ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.MEMBER_JOIN, event.getUser());
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MEMBER_LEAVE)) return;
        event.getGuild().getAuditLogs().limit(1).queue(auditLogEntries -> {
            AuditLogEntry entry = auditLogEntries.get(0);
            User responsible = null;
            String reason = null;
            // We have GuildBanEvent
            if (entry.getType() == ActionType.BAN) return;
            if (entry.getType() == ActionType.KICK) {
                responsible = entry.getUser();
                reason = entry.getReason();
            }
            ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), responsible != null ?
                            ModlogEvent.USER_KICKED : ModlogEvent.MEMBER_LEAVE, event.getUser(),
                    responsible, reason);
        });
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MEMBER_VOICE_JOIN)) return;
        ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.MEMBER_VOICE_JOIN,
                event.getMember().getUser(), new MessageEmbed.Field("Channel", event.getChannelJoined().getName()
                        + " (" + event.getChannelJoined().getId() + ")", true));
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MEMBER_VOICE_LEAVE)) return;
        ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.MEMBER_VOICE_LEAVE,
                event.getMember().getUser(), new MessageEmbed.Field("Channel", event.getChannelLeft().getName()
                        + " (" + event.getChannelLeft().getId() + ")", true));
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.ROLE_CREATE)) return;
        event.getGuild().getAuditLogs().queue(auditLog -> {
            AuditLogEntry entry = auditLog.get(0);
            ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.ROLE_CREATE,
                    entry.getUser(), new MessageEmbed.Field("Role", event.getRole().getName()
                            + " (" + event.getRole().getId() + ")", true));
        });

    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.ROLE_DELETE)) return;
        event.getGuild().getAuditLogs().queue(auditLog -> {
            AuditLogEntry entry = auditLog.get(0);
            ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.ROLE_DELETE,
                    entry.getUser(), new MessageEmbed.Field("Role", event.getRole().getName()
                            + " (" + event.getRole().getId() + ")", true));
        });
    }

    @Override
    public void onGenericRoleUpdate(GenericRoleUpdateEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.ROLE_EDIT)) return;
        if (event instanceof RoleUpdatePositionEvent) {
            return;
        }
        if (event.getResponseNumber() == roleResponseNumber) {
            return;
        }
        roleResponseNumber = event.getResponseNumber();
        event.getGuild().getAuditLogs().limit(1).queue(auditLogs -> {
            AuditLogEntry entry = auditLogs.get(0);
            Map<String, AuditLogChange> changes = entry.getChanges();
            EmbedBuilder permissionsBuilder = new EmbedBuilder();
            permissionsBuilder.addField("Role", event.getRole().getName() + " (" + event.getRole().getId() + ")", true);
            if (changes.containsKey("permissions")) {
                AuditLogChange change = changes.get("permissions");
                Map<Boolean, List<Permission>> permChanges = GeneralUtils.getChangedPerms(
                        Permission.getPermissions(((Integer) change.getOldValue()).longValue()),
                        Permission.getPermissions(((Integer) change.getNewValue()).longValue()));
                if (permChanges.get(true).size() > 0) {
                    StringBuilder added = new StringBuilder();
                    for (Permission addedPerm : permChanges.get(true)) {
                        added.append(addedPerm.getName()).append("\n");
                    }
                    permissionsBuilder.addField("Added Perms", "```\n" + added.toString() + "```", false);
                }
                if (permChanges.get(false).size() > 0) {
                    StringBuilder removed = new StringBuilder();
                    for (Permission removedPerm : permChanges.get(false)) {
                        removed.append(removedPerm.getName()).append("\n");
                    }
                    permissionsBuilder.addField("Removed Perms", "```\n" + removed.toString() + "```", false);
                }
            }
            if (changes.containsKey("name")) {
                AuditLogChange change = changes.get("name");
                permissionsBuilder.addField("Name Change", "`" + change.getOldValue() + "` -> `" + change.getNewValue()
                        + "`", true);
            }
            if (changes.containsKey("mentionable")) {
                AuditLogChange change = changes.get("mentionable");
                permissionsBuilder.addField("Mentionable", "`" + change.getNewValue() + "`", true);
            }
            if (changes.containsKey("hoist")) {
                AuditLogChange change = changes.get("hoist");
                permissionsBuilder.addField("Displayed Separately", "`" + change.getNewValue() + "`", true);
            }
            if (changes.containsKey("color")) {
                AuditLogChange change = changes.get("color");
                permissionsBuilder.addField("Color Change", "`#" + Integer.toHexString(change.getOldValue()) + "` -> `#"
                        + Integer.toHexString(change.getNewValue()) + "`", true);

            }
            ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.ROLE_EDIT, entry.getUser(),
                    permissionsBuilder);
        });
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MEMBER_ROLE_GIVE)) return;
        AuditLogEntry entry = event.getGuild().getAuditLogs().complete().get(0);
        Map<String, AuditLogChange> changes = entry.getChanges();
        AuditLogChange change = changes.get("$add");
        @SuppressWarnings("unchecked")
        HashMap<String, String> role = ((ArrayList<HashMap<String, String>>) change.getNewValue()).get(0);

        if (getGuild(event.getGuild()).getAutoAssignRoles().contains(role.get("id"))
                && ((System.currentTimeMillis() / 1000) - event.getMember().getJoinDate().toEpochSecond()) < 10) {
            return;
        }

        ModlogHandler.getInstance().postToModlog(getGuild(entry.getGuild()), ModlogEvent.MEMBER_ROLE_GIVE,
                event.getUser(), entry.getUser(), null,
                new MessageEmbed.Field("Role", role.get("name") + " (" + role.get("id") + ")", true));
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MEMBER_ROLE_REMOVE)) return;
        AuditLogEntry entry = event.getGuild().getAuditLogs().complete().get(0);
        Map<String, AuditLogChange> changes = entry.getChanges();
        AuditLogChange change = changes.get("$remove");
        @SuppressWarnings("unchecked")
        HashMap<String, String> role = ((ArrayList<HashMap<String, String>>) change.getNewValue()).get(0);

        ModlogHandler.getInstance().postToModlog(getGuild(entry.getGuild()), ModlogEvent.MEMBER_ROLE_REMOVE,
                event.getUser(), entry.getUser(), null,
                new MessageEmbed.Field("Role", role.get("name") + " (" + role.get("id") + ")", true));
    }

    @Override
    public void onTextChannelCreate(TextChannelCreateEvent event) {
        handleChannelCreate(getGuild(event.getGuild()), event.getChannel());
    }

    @Override
    public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {
        handleChannelCreate(getGuild(event.getGuild()), event.getChannel());
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        handleChannelDelete(getGuild(event.getGuild()), event.getChannel());
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        handleChannelDelete(getGuild(event.getGuild()), event.getChannel());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MESSAGE_EDIT)) return;
        if (event.getAuthor().isBot()) return;
        if (!RedisController.exists(event.getMessageId())) return;
        RedisMessage old = GeneralUtils.toRedisMessage(RedisController.get(event.getMessageId()));
        ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.MESSAGE_EDIT, event.getAuthor(),
                new MessageEmbed.Field("Old Message", GeneralUtils.truncate(1024, old.getContent(), true), false),
                new MessageEmbed.Field("New Message", GeneralUtils.truncate(1024, event.getMessage().getContentDisplay(), true), false),
                new MessageEmbed.Field("Channel", event.getTextChannel().getName() + " (" + event.getTextChannel().getId() + ")", true));
        RedisController.set(event.getMessageId(), GeneralUtils.getRedisMessageJson(event.getMessage()), "xx", "ex", 61200);
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MESSAGE_DELETE)) return;
        AuditLogEntry entry = event.getGuild().getAuditLogs().complete().get(0);
        if (entry.getUser().isBot()) return;
        User responsible = null;
        if (FlareBot.getInstance().getEvents().getRemovedByMeList().contains(event.getMessageIdLong())) {
            FlareBot.getInstance().getEvents().getRemovedByMeList().remove(event.getMessageIdLong());
            return;
        }
        if (!RedisController.exists(event.getMessageId())) return;
        RedisMessage deleted = GeneralUtils.toRedisMessage(RedisController.get(event.getMessageId()));
        if (entry.getType() == ActionType.MESSAGE_DELETE && entry.getTargetId().equals(deleted.getAuthorID())) {
            if (entry.getUser().isBot()) return;
            responsible = entry.getUser();
        }
        User sender = GeneralUtils.getUser(deleted.getAuthorID());
        ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.MESSAGE_DELETE, sender,
                (responsible != null ? new MessageEmbed.Field("Deleted By", MessageUtils.getUserAndId(responsible), true)
                        : null),
                new MessageEmbed.Field("Message", GeneralUtils.truncate(1024, deleted.getContent(), true), true),
                new MessageEmbed.Field("Channel", event.getTextChannel().getName() + " (" + deleted.getChannelID() + ")", true),
                new MessageEmbed.Field("Sent", GeneralUtils.formatTime(Instant.ofEpochMilli(deleted.getTimestamp())
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()), true)
        );
        RedisController.del(event.getMessageId());
    }

    @Override
    public void onGuildUpdateExplicitContentLevel(GuildUpdateExplicitContentLevelEvent e) {
        if (cannotHandle(e.getGuild(), ModlogEvent.GUILD_EXPLICIT_FILTER_CHANGE)) return;
        AuditLogEntry entry = e.getGuild().getAuditLogs().complete().get(0);
        AuditLogChange levelChange = entry.getChanges().get("explicit_content_filter");

        ModlogHandler.getInstance().postToModlog(getGuild(e.getGuild()), ModlogEvent.GUILD_EXPLICIT_FILTER_CHANGE, entry.getUser(),
                new MessageEmbed.Field("Old level", Guild.ExplicitContentLevel.fromKey(levelChange.getOldValue()).getDescription(), true),
                new MessageEmbed.Field("New level", Guild.ExplicitContentLevel.fromKey(levelChange.getNewValue()).getDescription(), true));
    }

    @Override
    public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MEMBER_NICK_CHANGE)) return;
        ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.MEMBER_NICK_CHANGE,
                event.getMember().getUser(),
                new MessageEmbed.Field("Previous nick", event.getPrevNick() != null ? event.getPrevNick() : event.getUser().getName(), true),
                new MessageEmbed.Field("New nick", event.getNewNick() != null ? event.getNewNick() : event.getUser().getName(), true));
    }

    @Override
    public void onGenericGuildUpdate(GenericGuildUpdateEvent event) {
        if (event instanceof GuildUpdateExplicitContentLevelEvent) {
            return;
        }
        if (cannotHandle(event.getGuild(), ModlogEvent.GUILD_UPDATE)) return;
        if (event.getResponseNumber() == guildResponseNumber) {
            return;
        }
        guildResponseNumber = event.getResponseNumber();
        event.getGuild().getAuditLogs().limit(1).queue(auditLogs -> {
            AuditLogEntry entry = auditLogs.get(0);
            Map<String, AuditLogChange> changes = entry.getChanges();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            if (changes.containsKey("region")) {
                embedBuilder.addField("Region change", "`" + changes.get("region").getOldValue() + "` -> `"
                        + changes.get("region").getNewValue() + "`", true);
            }
            if (changes.containsKey("name")) {
                embedBuilder.addField("Name", "`" + changes.get("name").getOldValue() + "` -> `" + changes.get("name")
                        .getNewValue() + "`", true);
            }
            if (changes.containsKey("afk_channel_id")) {
                AuditLogChange change = changes.get("afk_channel_id");
                String oldChannel;
                String newChannel;
                if (change.getOldValue() == null) {
                    oldChannel = "none";
                } else {
                    oldChannel = event.getGuild().getVoiceChannelById(change.getOldValue()).getName();
                }
                if (change.getNewValue() == null) {
                    newChannel = "none";
                } else {
                    newChannel = event.getGuild().getVoiceChannelById(change.getNewValue()).getName();
                }
                embedBuilder.addField("AFK channel", "`" + oldChannel + "` -> `" + newChannel + "`", true);
            }
            if (changes.containsKey("afk_timeout")) {
                embedBuilder.addField("AFK timeout (minutes)", "`" + ((int) changes.get("afk_timeout").getOldValue() / 60)
                        + "` -> `" + ((int) changes.get("afk_timeout").getNewValue() / 60) + "`", true);
            }
            if (changes.containsKey("system_channel_id")) {
                AuditLogChange change = changes.get("system_channel_id");
                String oldChannel;
                String newChannel;
                if (change.getOldValue() == null) {
                    oldChannel = "none";
                } else {
                    oldChannel = event.getGuild().getTextChannelById(change.getOldValue()).getName();
                }
                if (change.getNewValue() == null) {
                    newChannel = "none";
                } else {
                    newChannel = event.getGuild().getTextChannelById(change.getNewValue()).getName();
                }
                embedBuilder.addField("Welcome channel", "`" + oldChannel + "` -> `" + newChannel + "`", true);
            }
            if (changes.containsKey("default_message_notifications")) {
                String oldValue;
                String newValue;
                if ((int) changes.get("default_message_notifications").getOldValue() == 0) {
                    oldValue = "All messages";
                    newValue = "Only mentions";
                } else {
                    oldValue = "Only mentions";
                    newValue = "All messages";
                }
                embedBuilder.addField("Notification", "`" + oldValue + "` -> `" + newValue + "`", true);
            }
            if (changes.containsKey("verification_level")) {
                embedBuilder.addField("Verification level", "`" +
                        GeneralUtils.getVerificationString(Guild.VerificationLevel.fromKey(changes.get("verification_level").getOldValue())) + " ` -> `" +
                        GeneralUtils.getVerificationString(Guild.VerificationLevel.fromKey(changes.get("verification_level").getNewValue())) + "`", true);
            }
            if (changes.containsKey("mfa_level")) {
                boolean tfa = false;
                if ((int) changes.get("mfa_level").getOldValue() == 0) {
                    tfa = true;
                }
                embedBuilder.addField("Two Factor authorization required", tfa ? "Yes" : "No", true);
            }
            ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.GUILD_UPDATE, entry.getUser(),
                    embedBuilder);
        });
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (cannotHandle(event.getGuild(), ModlogEvent.MEMBER_VOICE_MOVE)) return;
        ModlogHandler.getInstance().postToModlog(getGuild(event.getGuild()), ModlogEvent.MEMBER_VOICE_MOVE,
                event.getMember().getUser(),
                new MessageEmbed.Field("Channel", "`" + event.getChannelLeft().getName() + "` -> `"
                        + event.getChannelJoined().getName() + "`", true));
    }

    private void handleChannelCreate(GuildWrapper wrapper, Channel channel) {
        if (cannotHandle(wrapper.getGuild(), ModlogEvent.CHANNEL_CREATE)) return;
        AuditLogEntry entry = wrapper.getGuild().getAuditLogs().complete().get(0);
        EmbedBuilder builder = new EmbedBuilder()
                .addField("Type", channel.getType().name().toLowerCase(), true)
                .addField("Name", channel.getName(), true);
        if (channel.getParent() != null) {
            builder.addField("Category", channel.getParent().getName(), true);
        }
        ModlogHandler.getInstance().postToModlog(wrapper, ModlogEvent.CHANNEL_CREATE, entry.getUser(),
                builder);
    }

    private void handleChannelDelete(GuildWrapper wrapper, Channel channel) {
        if (cannotHandle(wrapper.getGuild(), ModlogEvent.CHANNEL_DELETE)) return;
        AuditLogEntry entry = wrapper.getGuild().getAuditLogs().complete().get(0);
        EmbedBuilder builder = new EmbedBuilder()
                .addField("Type", channel.getType().name().toLowerCase(), true)
                .addField("Name", channel.getName(), true);
        if (channel.getParent() != null) {
            builder.addField("Category", channel.getParent().getName(), true);
        }
        ModlogHandler.getInstance().postToModlog(wrapper, ModlogEvent.CHANNEL_DELETE, entry.getUser(),
                builder);
    }

    private boolean cannotHandle(Guild guild, ModlogEvent event) {
        return guild == null || getGuild(guild) == null 
                || !getGuild(guild).getModeration().isEventEnabled(getGuild(guild), event)
                || !guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS);
    }

    private GuildWrapper getGuild(Guild guild) {
        return FlareBotManager.getInstance().getGuild(guild.getId());
    }
}

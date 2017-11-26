package stream.flarebot.flarebot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.audit.AuditLogChange;
import net.dv8tion.jda.core.audit.AuditLogEntry;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
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
import net.dv8tion.jda.core.events.message.MessageUpdateEvent;
import net.dv8tion.jda.core.events.role.RoleCreateEvent;
import net.dv8tion.jda.core.events.role.RoleDeleteEvent;
import net.dv8tion.jda.core.events.role.update.GenericRoleUpdateEvent;
import net.dv8tion.jda.core.events.role.update.RoleUpdatePositionEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import stream.flarebot.flarebot.mod.ModlogAction;
import stream.flarebot.flarebot.mod.ModlogEvent;
import stream.flarebot.flarebot.mod.Punishment;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ModlogEvents extends ListenerAdapter {

    private long roleResponseNumber = 0;
    private long guildResponceNumber = 0;

    @Override
    public void onGuildBan(GuildBanEvent event) {
        if (!checkModlog(event.getGuild())) return;
        Guild guild = event.getGuild();
        AuditLogEntry entry = guild.getAuditLogs().complete().get(0);
        FlareBotManager.getInstance().getGuild(guild.getId()).getAutoModConfig().postToModLog(event.getUser(), entry.getUser(), new Punishment(ModlogAction.BAN), true);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (!checkModlog(event.getGuild())) return;
        FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_JOIN.getEventEmbed(event.getUser(), null)
                .build(), ModlogEvent.MEMBER_JOIN);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        if (!checkModlog(event.getGuild())) return;
        FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_LEAVE.getEventEmbed(event.getUser(), null)
                .build(), ModlogEvent.MEMBER_LEAVE);
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (!checkModlog(event.getGuild())) return;
        FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_VOICE_JOIN.getEventEmbed(event.getMember().getUser(), null)
                .addField("Channel", event.getChannelJoined().getName() + " (" + event.getChannelJoined().getId() + ")", true)
                .build(), ModlogEvent.MEMBER_VOICE_JOIN);
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (!checkModlog(event.getGuild())) return;
        FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_VOICE_LEAVE.getEventEmbed(event.getMember().getUser(), null)
                .addField("Channel", event.getChannelLeft().getName() + " (" + event.getChannelLeft().getId() + ")", true)
                .build(), ModlogEvent.MEMBER_VOICE_LEAVE);
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        if (!checkModlog(event.getGuild())) return;
        event.getGuild().getAuditLogs().queue(auditLog -> {
            AuditLogEntry entry = auditLog.get(0);
            FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                    .getAutoModConfig().postToModLog(ModlogEvent.ROLE_CREATE.getEventEmbed(null, entry.getUser())
                    .addField("Role", event.getRole().getName() + " (" + event.getRole().getId() + ")", true)
                    .build(), ModlogEvent.ROLE_CREATE);
        });

    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        if (!checkModlog(event.getGuild())) return;
        event.getGuild().getAuditLogs().queue(auditLog -> {
            AuditLogEntry entry = auditLog.get(0);
            FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                    .getAutoModConfig().postToModLog(ModlogEvent.ROLE_DELETE.getEventEmbed(null, entry.getUser())
                    .addField("Role", event.getRole().getName() + " (" + event.getRole().getId() + ")", true)
                    .build(), ModlogEvent.ROLE_DELETE);
        });
    }

    @Override
    public void onGenericRoleUpdate(GenericRoleUpdateEvent event) {
        if (!checkModlog(event.getGuild())) return;
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
            EmbedBuilder permissionsBuilder = ModlogEvent.ROLE_EDIT.getEventEmbed(null, entry.getUser());
            permissionsBuilder.setTitle("Role Change");
            permissionsBuilder.addField("Role", event.getRole().getName() + " (" + event.getRole().getId() + ")", true);
            if (changes.containsKey("permissions")) {
                AuditLogChange change = changes.get("permissions");
                Map<Boolean, List<Permission>> permChanges = GeneralUtils.getChangedPerms(Permission.getPermissions(((Integer) change.getOldValue()).longValue()), Permission.getPermissions(((Integer) change.getNewValue()).longValue()));
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
                permissionsBuilder.addField("Name Change", "`" + change.getOldValue() + "` -> `" + change.getNewValue() + "`", true);
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
                permissionsBuilder.addField("Color Change", "`#" + Integer.toHexString(change.getOldValue()) + "` -> `#" + Integer.toHexString(change.getNewValue()) + "`", true);

            }
            FlareBotManager.getInstance().getGuild(event.getGuild().getId()).getAutoModConfig().postToModLog(permissionsBuilder.build(), ModlogEvent.ROLE_EDIT);
        });
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (!checkModlog(event.getGuild())) return;
        AuditLogEntry entry = event.getGuild().getAuditLogs().complete().get(0);
        Map<String, AuditLogChange> changes = entry.getChanges();
        AuditLogChange change = changes.get("$add");
        @SuppressWarnings("unchecked")
        HashMap<String, String> role = ((ArrayList<HashMap<String, String>>) change.getNewValue()).get(0);

        if (FlareBotManager.getInstance().getGuild(event.getGuild().getId()).getAutoAssignRoles().contains(role.get("id")) && ((System.currentTimeMillis() / 1000) - event.getMember().getJoinDate().toEpochSecond()) < 10)) {
                return;
        }

        FlareBotManager.getInstance().getGuild(entry.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_ROLE_GIVE.getEventEmbed(event.getUser(), entry.getUser())
                .addField("Role", role.get("name") + " (" + role.get("id") + ")", true)
                .build(), ModlogEvent.MEMBER_ROLE_GIVE);
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (!checkModlog(event.getGuild())) return;
        AuditLogEntry entry = event.getGuild().getAuditLogs().complete().get(0);
        Map<String, AuditLogChange> changes = entry.getChanges();
        AuditLogChange change = changes.get("$remove");
        @SuppressWarnings("unchecked")
        HashMap<String, String> role = ((ArrayList<HashMap<String, String>>) change.getNewValue()).get(0);

        FlareBotManager.getInstance().getGuild(entry.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_ROLE_REMOVE.getEventEmbed(event.getUser(), entry.getUser())
                .addField("Role", role.get("name") + " (" + role.get("id") + ")", true)
                .build(), ModlogEvent.MEMBER_ROLE_REMOVE);
    }

    @Override
    public void onTextChannelCreate(TextChannelCreateEvent event) {
        handleChannelCreate(FlareBotManager.getInstance().getGuild(event.getGuild().getId()), event.getChannel());
    }

    @Override
    public void onVoiceChannelCreate(VoiceChannelCreateEvent event) {
        handleChannelCreate(FlareBotManager.getInstance().getGuild(event.getGuild().getId()), event.getChannel());
    }

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        handleChannelDelete(FlareBotManager.getInstance().getGuild(event.getGuild().getId()), event.getChannel());
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        handleChannelDelete(FlareBotManager.getInstance().getGuild(event.getGuild().getId()), event.getChannel());
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        //TODO message caching
    }

    @Override
    public void onGuildUpdateExplicitContentLevel(GuildUpdateExplicitContentLevelEvent e) {
        if (!checkModlog(e.getGuild())) return;
        AuditLogEntry entry = e.getGuild().getAuditLogs().complete().get(0);
        AuditLogChange levelChange = entry.getChanges().get("explicit_content_filter");

        FlareBotManager.getInstance().getGuild(entry.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.GUILD_EXPLICIT_FILTER_CHANGE.getEventEmbed(null, entry.getUser())
                .addField("Old level", Guild.ExplicitContentLevel.fromKey(levelChange.getOldValue()).getDescription(), true)
                .addField("New level", Guild.ExplicitContentLevel.fromKey(levelChange.getNewValue()).getDescription(), true)
                .build(), ModlogEvent.GUILD_EXPLICIT_FILTER_CHANGE);
    }

    @Override
    public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
        if (!checkModlog(event.getGuild())) return;
        EmbedBuilder embedBuilder = ModlogEvent.MEMBER_NICK_CHANGE.getEventEmbed(event.getUser(), null);
        embedBuilder.addField("Previous nick", event.getPrevNick(), true);
        embedBuilder.addField("New nick", event.getNewNick(), true);
        FlareBotManager.getInstance().getGuild(event.getGuild().getId()).getAutoModConfig().postToModLog(embedBuilder.build(), ModlogEvent.MEMBER_NICK_CHANGE);
    }

    @Override
    public void onGenericGuildUpdate(GenericGuildUpdateEvent event) {
        if (event instanceof GuildUpdateExplicitContentLevelEvent) {
            return;
        }
        if (!checkModlog(event.getGuild())) return;
        if (event.getResponseNumber() == guildResponceNumber) {
            return;
        }
        guildResponceNumber = event.getResponseNumber();
        event.getGuild().getAuditLogs().limit(1).queue(auditLogs -> {
            AuditLogEntry entry = auditLogs.get(0);
            Map<String, AuditLogChange> changes = entry.getChanges();
            Iterator<Map.Entry<String, AuditLogChange>> it = changes.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, AuditLogChange> pair = it.next();
                System.out.println(pair.getKey() + ": " + pair.getValue());
            }
            EmbedBuilder embedBuilder = ModlogEvent.GUILD_UPDATE.getEventEmbed(null, entry.getUser());
            if (changes.containsKey("region")) {
                embedBuilder.addField("Region change", "`" + changes.get("region").getOldValue() + "` -> `" + changes.get("region").getNewValue() + "`", true);
            }
            if (changes.containsKey("name")) {
                embedBuilder.addField("Name", "`" + changes.get("name").getOldValue() + "` -> `" + changes.get("name").getNewValue() + "`", true);
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
                embedBuilder.addField("AFK timeout (minutes)", "`" + ((int)changes.get("afk_timeout").getOldValue() / 60) + "` -> `" + ((int)changes.get("afk_timeout").getNewValue() /60) + "`", true);
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
                if ((int)changes.get("default_message_notifications").getOldValue() == 0) {
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
                if ((int)changes.get("mfa_level").getOldValue() == 0) {
                    tfa = true;
                }
                embedBuilder.addField("Two Factor authorization required", tfa ? "Yes" : "No", true);
            }
            FlareBotManager.getInstance().getGuild(event.getGuild().getId()).getAutoModConfig().postToModLog(embedBuilder.build(), ModlogEvent.GUILD_UPDATE);
        });
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (!checkModlog(event.getGuild())) return;
        FlareBotManager.getInstance().getGuild(event.getGuild().getId()).getAutoModConfig().postToModLog(
                ModlogEvent.MEMBER_VOICE_MOVE.getEventEmbed(event.getMember().getUser(), null)
                .addField("Channel", "`" + event.getChannelLeft().getName() + "` -> `" + event.getChannelJoined().getName() + "`", true)
                .build(), ModlogEvent.MEMBER_VOICE_MOVE);
    }

    private void handleChannelCreate(GuildWrapper wrapper, Channel channel) {
        if (!checkModlog(wrapper.getGuild())) return;
        AuditLogEntry entry = wrapper.getGuild().getAuditLogs().complete().get(0);
        wrapper.getAutoModConfig().postToModLog(ModlogEvent.CHANNEL_CREATE.getEventEmbed(null, entry.getUser())
                .addField("Type", channel.getType().name().toLowerCase(), true)
                .addField("Name", channel.getName(), true)
                .build(), ModlogEvent.CHANNEL_CREATE);
    }

    private void handleChannelDelete(GuildWrapper wrapper, Channel channel) {
        if (!checkModlog(wrapper.getGuild())) return;
        AuditLogEntry entry = wrapper.getGuild().getAuditLogs().complete().get(0);
        wrapper.getAutoModConfig().postToModLog(ModlogEvent.CHANNEL_DELETE.getEventEmbed(null, entry.getUser())
                .addField("Type", channel.getType().name().toLowerCase(), true)
                .addField("Name", channel.getName(), true)
                .build(), ModlogEvent.CHANNEL_DELETE);
    }

    public static boolean checkModlog(Guild guild) {
        if (FlareBotManager.getInstance().getGuild(guild.getId()).getAutoModConfig().hasModLog() && guild.getSelfMember().hasPermission(Permission.VIEW_AUDIT_LOGS)) {
                return true;
        }
        return false;
    }
}

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
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
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
import java.util.List;
import java.util.Map;

public class ModlogEvents extends ListenerAdapter {

    private long genericResponseNumber = 0;
    private long moveResponceNumber = 0;

    @Override
    public void onGuildBan(GuildBanEvent event) {
        Guild guild = event.getGuild();
        AuditLogEntry entry = guild.getAuditLogs().complete().get(0);
        FlareBotManager.getInstance().getGuild(guild.getId()).getAutoModConfig().postToModLog(event.getUser(), entry.getUser(), new Punishment(ModlogAction.BAN), true);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_JOIN.getEventEmbed(event.getUser(), null)
                .build(), ModlogEvent.MEMBER_JOIN);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_LEAVE.getEventEmbed(event.getUser(), null)
                .build(), ModlogEvent.MEMBER_LEAVE);
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_VOICE_JOIN.getEventEmbed(event.getMember().getUser(), null)
                .addField("Channel", event.getChannelJoined().getName() + " (" + event.getChannelJoined().getId() + ")", true)
                .build(), ModlogEvent.MEMBER_VOICE_JOIN);
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        FlareBotManager.getInstance().getGuild(event.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_VOICE_LEAVE.getEventEmbed(event.getMember().getUser(), null)
                .addField("Channel", event.getChannelLeft().getName() + " (" + event.getChannelLeft().getId() + ")", true)
                .build(), ModlogEvent.MEMBER_VOICE_LEAVE);
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
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
        if (event instanceof RoleUpdatePositionEvent) {
            return;
        }
        if (event.getResponseNumber() == genericResponseNumber) {
            return;
        }
        genericResponseNumber = event.getResponseNumber();
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
        AuditLogEntry entry = event.getGuild().getAuditLogs().complete().get(0);
        Map<String, AuditLogChange> changes = entry.getChanges();
        AuditLogChange change = changes.get("$add");
        @SuppressWarnings("unchecked")
        HashMap<String, String> role = ((ArrayList<HashMap<String, String>>) change.getNewValue()).get(0);

        FlareBotManager.getInstance().getGuild(entry.getGuild().getId())
                .getAutoModConfig().postToModLog(ModlogEvent.MEMBER_ROLE_GIVE.getEventEmbed(event.getUser(), entry.getUser())
                .addField("Role", role.get("name") + " (" + role.get("id") + ")", true)
                .build(), ModlogEvent.MEMBER_ROLE_GIVE);
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
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

    private void handleChannelCreate(GuildWrapper wrapper, Channel channel) {
        AuditLogEntry entry = wrapper.getGuild().getAuditLogs().complete().get(0);
        wrapper.getAutoModConfig().postToModLog(ModlogEvent.CHANNEL_CREATE.getEventEmbed(null, entry.getUser())
                .addField("Type", channel.getType().name().toLowerCase(), true)
                .addField("Name", channel.getName(), true)
                .build(), ModlogEvent.CHANNEL_CREATE);
    }

    private void handleChannelDelete(GuildWrapper wrapper, Channel channel) {
        AuditLogEntry entry = wrapper.getGuild().getAuditLogs().complete().get(0);
        wrapper.getAutoModConfig().postToModLog(ModlogEvent.CHANNEL_DELETE.getEventEmbed(null, entry.getUser())
                .addField("Type", channel.getType().name().toLowerCase(), true)
                .addField("Name", channel.getName(), true)
                .build(), ModlogEvent.CHANNEL_DELETE);
    }
}

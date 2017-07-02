package stream.flarebot.flarebot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.FlareBotManager;
import stream.flarebot.flarebot.util.MessageUtils;

import java.util.Optional;

public enum Language {

    // General
    GENERAL_INVALIDARGUMENT("general.invalid-argument"),
    GENERAL_USAGE("general.usage"),
    GENERAL_SUCCESS("general.success"),
    GENERAL_USER("general.user"),
    GENERAL_UNKNOWN("general.unknown"),
    GENERAL_REQUESTEDBY("general.requested-by"),
    GENERAL_PAGE("general.page"),
    // Auto Assign
    AUTOASSIGN_CURRENT("autoassign.current"),
    AUTOASSIGN_NOROLES("autoassign.nonroles"),
    AUTOASSIGN_INVALIDROLE("autoassign.invalid-role"),
    AUTOASSIGN_ADDASSIGNED("autoassign.add.success"),
    AUTOASSIGN_REMOVEASSIGNED("autoassign.remove.success"),
    AUTOASSIGN_ALREADYASSIGNED("autoassign.add.exists"),
    AUTOASSIGN_NOTAUTOASSIGNROLE("autoassign.remove.not-exists"),
    AUTOASSIGN_DESCRIPTION("autoassign.description"),
    AUTOASSIGN_USAGE("autoassign.usage"),
    // Ban
    BAN_USERNOTFOUND("ban.user-not-found"),
    BAN_BANHAMMERSTRUCK("ban.ban-hammer-struck"),
    BAN_CANTBAN("ban.cant-ban-user"),
    BAN_DESCRIPTION("ban.description"),
    BAN_USAGE("ban.usage"),
    // Permissions
    PERMISSIONS_USERNOEXIST("permissions.user-doesnt-exist"),
    PERMISSIONS_GROUPNOEXIST("permissions.group-doesnt-exist"),
    PERMISSIONS_USERHASGROUP("permissions.user-has-group"),
    PERMISSIONS_USERNEVERHADGROUP("permissions.user-doesnt-have-group"),
    PERMISSIONS_GROUPS("permissions.groups"),
    PERMISSIONS_GROUPHASPERM("permissions.group-has-perm"),
    PERMISSIONS_GROUPDOESNTHAVEPERM("permissions.group-doesnt-have-perm"),
    PERMISSIONS_PERMSFORGROUP("permissions.perms-for-group"),
    PERMISSIONS_COULDNOTSAVE("permissions.could-not-save"),
    PERMISSIONS_DESCRIPTION("permissions.description"),
    PERMISSIONS_USAGE("permissions.usage"),
    // Pin
    PIN_DESCRIPTION("pin.description"),
    PIN_USAGE("pin.usage"),
    // Purge
    PURGE_COOLDOWN("purge.cooldown"),
    PURGE_TOOHIGH("purge.too-high"),
    PURGE_MIN("purge.min-purge"),
    PURGE_DELETED("purge.deleted"),
    PURGE_FAILED("purge.failed"),
    PURGE_NOPERMS("purge.no-perms"),
    PURGE_DESCRIPTION("purge.description"),
    PURGE_USAGE("purge.usage"),
    // Roles
    ROLES_SERVERROLES("roles.server-roles"),
    ROLES_DESCRIPTION("roles.description"),
    ROLES_USAGE("roles.usage"),
    // Prefix
    PREFIX_RESET("prefix.reset"),
    PREFIX_TOOLONG("prefix.too-long"),
    PREFIX_SETPREFIX("prefix.set-prefix"),
    PREFIX_CURRENT("prefix.current"),
    PREFIX_DESCRIPTION("prefix.description"),
    PREFIX_USAGE("prefix.usage"),
    // Welcomes
    WELCOMES_ENABLED("welcomes.enabled"),
    WELCOMES_ALREADYENABLED("welcomes.already-enabled"),
    WELCOMES_DISABLED("welcomes.disabled"),
    WELCOMES_ALREADYDISABLED("welcomes.already-disabled"),
    WELCOMES_APPEARSET("welcomes.set-to-appear"),
    WELCOMES_NOTENABLED("welcomes.not-enabled"),
    WELCOMES_SETHELP_LINE1("welcomes.set-help.line-1"),
    WELCOMES_SETHELP_LINE2("welcomes.set-help.line-2"),
    WELCOMES_SETHELP_LINE3("welcomes.set-help.line-3"),
    WELCOMES_SETHELP_LINE4("welcomes.set-help.line-4"),
    WELCOMES_SETHELP_LINE5("welcomes.set-help.line-5"),
    WELCOMES_SETHELP_LINE6("welcomes.set-help.line-6"),
    WELCOMES_CURRENTMESSAGE("welcomes.current-message"),
    WELCOMES_SET("welcomes.set"),
    WELCOMES_DESCRIPTION("welcomes.description"),
    WELCOMES_USAGE("welcomes.usage"),
    // Automod
    //TODO: To be done once feature is ready for prod
    // Help
    HELP_NOSUCHCATEGORY("help.no-such-category"),
    HELP_HEADER("help.header"),
    HELP_CONTINUED("help.continued"),
    HELP_DESCRIPTION("help.description"),
    HELP_USAGE("help.usage"),
    // Report
    REPORT_STATUS_OPEN("report.status.open"),
    REPORT_STATUS_ONHOLD("report.status.on-hold"),
    REPORT_STATUS_RESOLVED("report.status.resolved"),
    REPORT_STATUS_CLOSED("report.status.closed"),
    REPORT_STATUS_UNDERREVIEW("report.status.under-review"),
    REPORT_STATUS_DUPLICATE("report.status.duplicate"),
    // Info
    INFO_HEADER("info.header"),
    INFO_INFONOTFOUND("info.info-not-found"),
    INFO_DESCRIPTION("info.description"),
    INFO_USAGE("info.usage"),
    INFO_CONTENT_SERVERS("info.content.servers"),
    INFO_CONTENT_TOTALUSERS("info.content.total-users"),
    INFO_CONTENT_VOICECONNECTIONS("info.content.voice-connections"),
    INFO_CONTENT_MUSICCHANNELS("info.content.music-channels"),
    INFO_CONTENT_TEXTCHANNELS("info.content.text-channels"),
    INFO_CONTENT_UPTIME("info.content.uptime"),
    INFO_CONTENT_MEMUSAGE("info.content.memory-usage"),
    INFO_CONTENT_MEMFREE("info.content.memory-free"),
    INFO_CONTENT_VIDTHREADS("info.content.video-threads"),
    INFO_CONTENT_TOTALTHREADS("info.content.total-threads"),
    INFO_CONTENT_VERSION("info.content.version"),
    INFO_CONTENT_JDAVERSION("info.content.jda-version"),
    INFO_CONTENT_GITREVISION("info.content.git-revision"),
    INFO_CONTENT_SUPPORTSERVER("info.content.support-server"),
    INFO_CONTENT_DONATE("info.content.donate"),
    INFO_CONTENT_OURPATREON("info.content.our-patreon"),
    INFO_CONTENT_PATREON("info.content.patreon"),
    INFO_CONTENT_WEBSITE("info.content.website"),
    INFO_CONTENT_TWITTER("info.content.twitter"),
    INFO_CONTENT_INVITE("info.content.invite"),
    INFO_CONTENT_MADEBY("info.content.made-by"),
    INFO_CONTENT_MADEBYCONTENT("info.content.made-by-content"),
    INFO_CONTENT_SOURCE("info.content.source"),
    // Invite
    INVITE_INVITATION("invite.invitation"),
    INVITE_DESCRIPTION("invite.description"),
    INVITE_USAGE("invite.usage"),
    // Join
    JOIN_CURRENTLYCONNECTING("join.currently-connecting"),
    JOIN_JOINOTHERPERM("join.join-other-perm"),
    JOIN_USERLIMIT("join.user-limit"),
    JOIN_NOPERMS("join.no-perms"),
    JOIN_DESCRIPTION("join.description"),
    JOIN_USAGE("join.usage"),
    // Leave
    LEAVE_LEAVEOTHERPERM("leave.leave-other-perm"),
    LEAVE_DESCRIPTION("leave.description"),
    LEAVE_USAGE("leave.usage"),
    // Poll
    // TODO: Add when rewritten
    // Report
    REPORT_INVALIDUSER("report.invalid-reason"),
    REPORT_DESCRIPTION("report.description"),
    REPORT_USAGE("report.usage"),
    // Reports
    REPORTS_INVALIDPAGE(""),
    REPORTS_PAGENOTEXIST(""),
    REPORTS_NOREPORTSFORGUILD(""),
    REPORTS_PAGETITLE(""),
    REPORTS_NEEDLISTPERMS(""),
    REPORTS_INVALIDREPORTNUM(""),
    REPORTS_REPORTNOTEXIST(""),
    REPORTS_NEEDVIEWPERMS(""),
    REPORTS_INVALIDSTATUS(""),
    REPORTS_STATUSES(""),
    REPORTS_CURRENTSTATUS(""),
    REPORTS_CHANGEDSTATUS(""),
    REPORTS_NEEDSTATUSPERMS(""),
    REPORTS_TABLEHEADERS_ID(""),
    REPORTS_TABLEHEADERS_REPORTED(""),
    REPORTS_TABLEHEADERS_TIME(""),
    REPORTS_TABLEHEADERS_STATUS(""),
    REPORTS_DESCRIPTION(""),
    REPORTS_USAGE(""),
    // Self Assign
    SELFASSIGN_ADMINPERMNEEDED(""),
    SELFASSIGN_ROLESLIST(""),
    SELFASSIGN_ROLEDOESNTEXIST(""),
    SELFASSIGN_CANNOTSELFASSIGN(""),
    SELFASSIGN_USEROLEID(""),
    SELFASSIGN_ADDEDTOLIST(""),
    SELFASSIGN_REMOVEDFROMLIST(""),
    SELFASSIGN_ASSIGNED(""),
    SELFASSIGN_UNASSIGNED(""),
    SELFASSIGN_DESCRIPTION(""),
    SELFASSIGN_USAGE(""),
    // Shard info
    SHARDINFO_SHARDID(""),
    SHARDINFO_STATUS(""),
    SHARDINFO_GUILDCOUNT(""),
    SHARDINFO_DESCRIPTION(""),
    SHARDINFO_USAGE(""),
    // User info
    USERINFO_USERNOTFOUND(""),
    USERINFO_USERINFO_TITLE(""),
    USERINFO_USERINFO_AVATAR(""),
    USERINFO_USERINFO_DEFAULTAVATAR(""),
    USERINFO_GENERALINFO_TITLE(""),
    USERINFO_GENERALINFO_SERVERS(""),
    USERINFO_GENERALINFO_ROLES(""),
    USERINFO_GENERALINFO_USERNOTINSERVER(""),
    USERINFO_GENERALINFO_STATUS(""),
    USERINFO_GENERALINFO_CURRENTSHARD(""),
    USERINFO_TIMEDATA_TITLE(""),
    USERINFO_TIMEDATA_CREATED(""),
    USERINFO_TIMEDATA_JOINED(""),
    USERINFO_TIMEDATA_USERNOTINSERVER(""),
    USERINFO_TIMEDATA_LASTSEEN(""),
    USERINFO_TIMEDATA_LASTSPOKE(""),
    USERINFO_DESCRIPTION(""),
    USERINFO_USAGE(""),
    // Delete
    DELETE_REMOVEDPLAYLIST(""),
    DELETE_PLAYLISTDOESNTEXIST(""),
    DELETE_DATABASEERROR(""),
    DELETE_DESCRIPTION(""),
    DELETE_USAGE(""),
    // Load
    LOAD_DESCRIPTION(""),
    LOAD_USAGE(""),
    // Loop
    LOOP_LOOPINGON(""),
    LOOP_LOOPINGOFF(""),
    LOOP_DESCRIPTION(""),
    LOOP_USAGE(""),
    // Music Announce
    ANNOUNCE_SETCHANNEL(""),
    ANNOUNCE_DATABASEERROR(""),
    ANNOUNCE_DISABLEANNOUCEMENTS(""),
    ANNOUNCE_DESCRIPTION(""),
    ANNOUNCE_USAGE(""),
    // Pause
    PAUSE_DESCRIPTION(""),
    PAUSE_USAGE(""),
    // Play
    PLAY_NOMUSICPLAYING(""),
    PLAY_RESUMING(""),
    PLAY_DESCRIPTION(""),
    PLAY_USAGE(""),
    // Playlist
    PLAYLIST_CLEARED(""),
    PLAYLIST_INVALIDNUMBER(""),
    PLAYLIST_NOSONGWITHINDEX(""),
    PLAYLIST_REMOVEDSONG(""),
    PLAYLIST_NOSONGS(""),
    //TODO: Add desc and usage when fixed
    // Playlists
    PLAYLISTS_CHANGEDSCOPE(""),
    PLAYLISTS_INVALIDSCOPE(""),
    PLAYLISTS_PLAYLISTNOTFOUND(""),
    PLAYLISTS_GLOBALPLAYLISTS(""),
    PLAYLISTS_NOGLOBALPLAYLISTS(""),
    PLAYLISTS_NOPLAYLISTS(""),
    PLAYLISTS_DESCRIPTION(""),
    PLAYLISTS_USAGE(""),
    // Random
    RANDOM_INVALIDAMOUNT(""),
    RANDOM_DESCRIPTION(""),
    RANDOM_USAGE(""),
    // Resume
    RESUME_NOTPLAYING(""),
    RESUME_RESUMING(""),
    RESUME_DESCRIPTION(""),
    RESUME_USAGE(""),
    // Save
    SAVE_TOOLONG(""),
    SAVE_EMPTYPLAYLIST(""),
    SAVE_DESCRIPTION(""),
    SAVE_USAGE(""),
    // Search
    SEARCH_DEPRECATED(""),
    SEARCH_DESCRIPTION(""),
    SEARCH_USAGE(""),
    // Shuffle
    SHUFFLE_DESCRIPTION(""),
    SHUFFLE_USAGE(""),
    // Skip
    SKIP_NOTPLAYING(""),
    SKIP_USERNOTINCHANNEL(""),
    SKIP_CANTSTARTVOTE(""),
    SKIP_VOTESFORYES(""),
    SKIP_VOTESFORNO(""),
    SKIP_MISSINGFORCEPERM(""),
    SKIP_MISSINGCANCELPERM(""),
    SKIP_ALREADYVOTED(""),
    SKIP_VOTEDFOR(""),
    SKIP_CURRENTVOTESFOR(""),
    SKIP_USEYESNO(""),
    SKIP_ONLYPERSONINCHANNEL(""),
    SKIP_RESULTS_PREFACE(""),
    SKIP_RESULTS_SKIP(""),
    SKIP_RESULTS_KEEP(""),
    SKIP_VOTESTARTED(""),
    SKIP_DESCRIPTION(""),
    SKIP_USAGE(""),
    // Song
    SONG_CURRENT(""),
    SONG_AMOUNTPLAYED(""),
    SONG_TIME(""),
    SONG_NOSONGPLAYING(""),
    SONG_DESCRIPTION(""),
    SONG_USAGE(""),
    // Song nick
    SONGNICK_ENABLED(""),
    SONGNICK_DISABLED(""),
    SONGNICK_DESCRIPTION(""),
    SONGNICK_USAGE(""),
    // Stop
    STOP_DESCRIPTION(""),
    STOP_USAGE(""),;

    private final String path;

    Language(String path) {
        this.path = path;
    }


    public EmbedBuilder getEmbed(String guildId, Object... args) {
        return this.getEmbed(null, guildId, args);
    }

    public EmbedBuilder getEmbed(User user, String guildId, Object... args) {
        if (user != null)
            return MessageUtils.getEmbed(user).setDescription(FlareBotManager.getInstance().getLang(this, guildId));
        else
            return MessageUtils.getEmbed().setDescription(FlareBotManager.getInstance().getLang(this, guildId));
    }

    public EmbedBuilder getErrorEmbed(String guildId, Object... args) {
        return this.getErrorEmbed(Optional.empty(), guildId, args);
    }

    public EmbedBuilder getErrorEmbed(Optional<User> user, String guildId, Object... args) {
        if (user.isPresent())
            return MessageUtils.getEmbed(user.get()).setDescription(get(guildId, this, args));
        else
            return MessageUtils.getEmbed().setDescription(FlareBotManager.getInstance().getLang(this, guildId));
    }

    public Message send(MessageChannel channel, String guildId, Object... args) {
        return channel.sendMessage(String.format(FlareBotManager.getInstance().getLang(this, guildId), args)).complete();
    }

    public String get(String guildId, Object... args) {
        return String.format(FlareBotManager.getInstance().getLang(this, guildId), args);
    }

    public Message sendEmbed(MessageChannel channel, String guildId, Object... args) {
        return channel.sendMessage(getEmbed(guildId, this, args).build()).complete();
    }

    public enum Locales {

        ENGLISH_UK("en_uk"),
        ENGLISH_US("en_us"),
        FRENCH("fr");

        private String code;

        Locales(String code) {
            this.code = code;
        }

        public static Locales from(String s) {
            for (Locales l : Locales.values()) {
                if (s.equalsIgnoreCase(l.getCode()))
                    return l;
            }
            throw new IllegalArgumentException("Unknown language code (" + s + ") !");
        }

        public String getCode() {
            return code;
        }
    }
}

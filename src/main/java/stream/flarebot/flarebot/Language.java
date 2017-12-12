package stream.flarebot.flarebot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
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
    REPORTS_INVALID_PAGE("reports.invalid-page"),
    REPORTS_PAGE_DOES_NOT_EXIST("reports.page-does-not-exist"),
    REPORTS_NO_REPORTS_FOR_GUILD("reports.no-reports-for-guild"),
    REPORTS_PAGE_TITLE("reports.page-title"),
    REPORTS_NEED_LIST_PERMS("reports.need-list-perms"),
    REPORTS_INVALID_REPORT_NUM("reports.invalid-report-num"),
    REPORTS_REPORT_DOES_NOT_EXIST("reports.report-does-not-exist"),
    REPORTS_NEED_VIEW_PERMS("reports.need-view-perms"),
    REPORTS_INVALID_STATUS("reports.invalid-status"),
    REPORTS_STATUSES("reports.statuses"),
    REPORTS_CURRENT_STATUS("reports.current-status"),
    REPORTS_CHANGED_STATUS("reports.changed-status"),
    REPORTS_NEED_STATUS_PERMS("reports.need-status-perms"),
    REPORTS_TABLE_HEADERS_ID("reports.table-headers.id"),
    REPORTS_TABLE_HEADERS_REPORTED("reports.table-headers.reported"),
    REPORTS_TABLE_HEADERS_TIME("reports.table-headers.time"),
    REPORTS_TABLE_HEADERS_STATUS("reports.table-headers.status"),
    REPORTS_DESCRIPTION("reports.description"),
    REPORTS_USAGE("reports.usage"),
    //SelfAssign
    SELFASSIGN_ADMIN_PERM_NEEDED("selfassign.admin-perm-needed"),
    SELFASSIGN_ROLES_LIST("selfassign.roles-list"),
    SELFASSIGN_ROLE_DOES_NOT_EXIST("selfassign.role-does-not-exist"),
    SELFASSIGN_CANNOT_SELF_ASSIGN("selfassign.cannot-self-assign"),
    SELFASSIGN_USE_ROLE_ID("selfassign.use-role-id"),
    SELFASSIGN_ADDED_TO_LIST("selfassign.added-to-list"),
    SELFASSIGN_REMOVED_FROM_LIST("selfassign.removed-from-list"),
    SELFASSIGN_ASSIGNED("selfassign.assigned"),
    SELFASSIGN_UNASSIGNED("selfassign.unassigned"),
    SELFASSIGN_DESCRIPTION("selfassign.description"),
    SELFASSIGN_USAGE("selfassign.usage"),
    //Shardinfo
    SHARDINFO_SHARD_ID("shardinfo.shard-id"),
    SHARDINFO_STATUS("shardinfo.status"),
    SHARDINFO_GUILD_COUNT("shardinfo.guild-count"),
    SHARDINFO_DESCRIPTION("shardinfo.description"),
    SHARDINFO_USAGE("shardinfo.usage"),
    //Userinfo
    USERINFO_USER_NOT_FOUND("userinfo.user-not-found"),
    USERINFO_USERINFO_TITLE("userinfo.userinfo.title"),
    USERINFO_USERINFO_AVATAR("userinfo.avatar"),
    USERINFO_USERINFO_DEFAULT_AVATAR("userinfo.default-avatar"),
    USERINFO_GENERAL_INFO_TITLE("userinfo.general-info.title"),
    USERINFO_GENERAL_INFO_SERVERS("userinfo.general-info.servers"),
    USERINFO_GENERAL_INFO_ROLES("userinfo.general-info.role"),
    USERINFO_GENERAL_INFO_USER_NOT_IN_SERVER("userinfo.general-info.user-not-in-server"),
    USERINFO_GENERAL_INFO_STATUS("userinfo.general-info.status"),
    USERINFO_GENERAL_INFO_CURRENT_SHARD("userinfo.general-info.current-shard"),
    USERINFO_TIME_DATA_TITLE("userinfo.time-data.title"),
    USERINFO_TIME_DATA_CREATED("userinfo.time-data.created"),
    USERINFO_TIME_DATA_JOINED("userinfo.time-data.joined"),
    USERINFO_TIME_DATA_USER_NOT_IN_SERVER("userinfo.time-data.user-not-in-server"),
    USERINFO_TIME_DATA_LAST_SEEN("userinfo.time-data.last-seen"),
    USERINFO_TIME_DATA_LAST_SPOKE("userinfo.time-data.last-spoke"),
    USERINFO_DESCRIPTION("userinfo.description"),
    USERINFO_USAGE("userinfo.usage"),
    //Delete
    DELETE_REMOVED_PLAYLIST("delete.removed-playlist"),
    DELETE_PLAYLIST_DOES_NOT_EXIST("delete.playlist-does-not-exist"),
    DELETE_DATABASE_ERROR("delete.database-error"),
    DELETE_DESCRIPTION("delete.description"),
    DELETE_USAGE("delete.usage"),
    //Load
    LOAD_DESCRIPTION("load.description"),
    LOAD_USAGE("load.usage"),
    //Loop
    LOOP_LOOPING_ON("loop.looping-on"),
    LOOP_LOOPING_OFF("loop.looping-off"),
    LOOP_DESCRIPTION("loop.description"),
    LOOP_USAGE("loop.usage"),
    //MusicAnnounce
    ANNOUNCE_SET_CHANNEL("announce.set-channel"),
    ANNOUNCE_DATABASE_ERROR("announce.database-error"),
    ANNOUNCE_DISABLE_ANNOUCEMENTS("announce.disable-announcements"),
    ANNOUNCE_DESCRIPTION("announce.description"),
    ANNOUNCE_USAGE("announce.usage"),
    //Pause
    PAUSE_PAUSED("pause.paused"),
    PAUSE_UNPAUSED("pause.unpaused"),
    PAUSE_DESCRIPTION("pause.description"),
    PAUSE_USAGE("pause.usage"),
    //Play
    PLAY_NO_MUSIC_PLAYING("play.no-music-playing"),
    PLAY_RESUMING("play.resuming"),
    PLAY_DESCRIPTION("play.description"),
    PLAY_USAGE("play.usage"),
    //Playlist
    PLAYLIST_CLEARED("playlist.cleared"),
    PLAYLIST_INVALID_NUMBER("playlist.invalid-number"),
    PLAYLIST_NO_SONG_WITH_INDEX("playlist.no-song-with-index"),
    PLAYLIST_REMOVED_SONG("playlist.removed-song"),
    PLAYLIST_NO_SONGS("playlist.no-songs"),
    //TODO:Adddescandusagewhenfixed
    //Playlists
    PLAYLISTS_CHANGED_SCOPE("playlists.changed-scope"),
    PLAYLISTS_INVALID_SCOPE("playlists.invalid-scope"),
    PLAYLISTS_PLAYLIST_NOT_FOUND("playlists.playlist-not-found"),
    PLAYLISTS_GLOBAL_PLAYLISTS("playlists.global-playlists"),
    PLAYLISTS_NO_GLOBAL_PLAYLISTS("playlists.no-global-playlists"),
    PLAYLISTS_NO_PLAYLISTS("playlists.no-playlists"),
    PLAYLISTS_DESCRIPTION("playlists.description"),
    PLAYLISTS_USAGE("playlists.usage"),
    //Random
    RANDOM_INVALIDAMOUNT("random.invalid-amount"),
    RANDOM_DESCRIPTION("random.description"),
    RANDOM_USAGE("random.usage"),
    //Resume
    RESUME_NOT_PLAYING("resume.not-playing"),
    RESUME_RESUMING("resume.resuming"),
    RESUME_DESCRIPTION("resume.description"),
    RESUME_USAGE("resume.usage"),
    //Save
    SAVE_TOO_LONG("save.too-long"),
    SAVE_EMPTY_PLAYLIST("save.empty-playlist"),
    SAVE_DESCRIPTION("save.description"),
    SAVE_USAGE("save.usage"),
    //Search
    SEARCH_DEPRECATED("search.deprecated"),
    SEARCH_DESCRIPTION("search.description"),
    SEARCH_USAGE("search.usage"),
    //Shuffle
    SHUFFLE_DESCRIPTION("shuffle.description"),
    SHUFFLE_USAGE("shuffle.usage"),
    //Skip
    SKIP_NOT_PLAYING("skip.not-playing"),
    SKIP_USER_NOT_IN_CHANNEL("skip.user-not-in-channel"),
    SKIP_CANT_START_VOTE("skip.cant-start-vote"),
    SKIP_VOTES_FOR_YES("skip.votes-for-yes"),
    SKIP_VOTES_FOR_NO("skip.votes-for-no"),
    SKIP_MISSING_FORCE_PERM("skip.missing-force-perm"),
    SKIP_MISSING_CANCEL_PERM("skip.missing-cancel-perm"),
    SKIP_ALREADY_VOTED("skip.already-voted"),
    SKIP_VOTED_FOR("skip.voted-for"),
    SKIP_CURRENT_VOTES_FOR("skip.current-votes-for"),
    SKIP_USE_YES_NO("skip.use-yes-no"),
    SKIP_ONLY_PERSON_IN_CHANNEL("skip.only-person-in-channel"),
    SKIP_RESULTS_PREFACE("skip.results.preface"),
    SKIP_RESULTS_SKIP("skip.results.skip"),
    SKIP_RESULTS_KEEP("skip.results.keep"),
    SKIP_VOTESTARTED("skip.vote-started"),
    SKIP_DESCRIPTION("skip.description"),
    SKIP_USAGE("skip.usage"),
    //Song
    SONG_CURRENT("song.current"),
    SONG_AMOUNT_PLAYED("song.amount-played"),
    SONG_TIME("song.time"),
    SONG_NO_SONG_PLAYING("song.no-song-playing"),
    SONG_DESCRIPTION("song.description"),
    SONG_USAGE("song.usage"),
    //Songnick
    SONGNICK_ENABLED("songnick.enabled"),
    SONGNICK_DISABLED("songnick.disabled"),
    SONGNICK_DESCRIPTION("songnick.description"),
    SONGNICK_USAGE("songnick.usage"),
    //Stop
    STOP_DESCRIPTION("stop.description"),
    STOP_USAGE("stop.usage");

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
        return user.map(user1 -> MessageUtils.getEmbed(user1).setDescription(get(guildId, this, args)))
                .orElseGet(() -> MessageUtils.getEmbed().setDescription(FlareBotManager.getInstance().getLang(this, guildId)));
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

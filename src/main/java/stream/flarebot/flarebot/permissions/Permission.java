package stream.flarebot.flarebot.permissions;

import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.currency.ConvertCommand;
import stream.flarebot.flarebot.commands.currency.CurrencyCommand;
import stream.flarebot.flarebot.commands.general.*;
import stream.flarebot.flarebot.commands.moderation.*;
import stream.flarebot.flarebot.commands.moderation.mod.*;
import stream.flarebot.flarebot.commands.music.*;
import stream.flarebot.flarebot.commands.random.AvatarCommand;
import stream.flarebot.flarebot.commands.useful.RemindCommand;
import stream.flarebot.flarebot.commands.useful.TagsCommand;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.util.Map;

public enum Permission {

    // Currency
    CONVERT_COMMAND("convert", true, ConvertCommand.class),
    CURRENCY_COMMAND("currency", true, CurrencyCommand.class),
    // General
    USAGE_COMMAND("usage", true, CommandUsageCommand.class),
    HELP_COMMAND("help", true, HelpCommand.class),
    INFO_COMMAND("info", true, InfoCommand.class),
    INVITE_COMMAND("invite", true, InviteCommand.class),
    REPORT_COMMAND("report", true, ReportCommand.class),
    SELFASSIGN_COMMAND("selfassign", true, SelfAssignCommand.class),
    SELFASSIGN_ADMIN("selfassign.admin", false),
    SERVERINFO_COMMAND("serverinfo", true, ServerInfoCommand.class),
    SHARDINFO_COMMAND("shardinfo", true, ShardInfoCommand.class),
    STATS_COMMAND("stats", true, StatsCommand.class),
    STATUS_COMMAND("status", true, StatusCommand.class),
    USERINFO_COMMAND("userinfo", true, UserInfoCommand.class),
    USERINFO_OTHER("userinfo.other", true),
    // Moderation
    BAN_COMMAND("ban", false, BanCommand.class),
    FORCEBAN_COMMAND("forceban", false, ForceBanCommand.class),
    KICK_COMMAND("kick", false, KickCommand.class),
    MODLOG_COMMAND("modlog", false, ModlogCommand.class),
    MUTE_COMMAND("mute", false, MuteCommand.class),
    TEMPBAN_COMMAND("tempban", false, TempBanCommand.class),
    TEMPMUTE_COMMAND("tempmute", false, TempMuteCommand.class),
    UNBAN_COMMAND("unban", false, UnbanCommand.class),
    UNMUTE_COMMAND("unmute", false, UnmuteCommand.class),
    WARN_COMMAND("warn", false, WarnCommand.class),
    WARNINGS_COMMAND("warnings", false, WarningsCommand.class),
    // Server Moderation
    AUTOASSIGN_COMMAND("autoassign", false, AutoAssignCommand.class),
    FIX_COMMAND("fix", false, FixCommand.class),
    LOCKCHAT_COMMAND("lockchat", false, LockChatCommand.class),
    PERMISSIONS_COMMAND("permissions", false, PermissionsCommand.class),
    PIN_COMMAND("pin", false, PinCommand.class),
    PRUNE_COMMAND("prune", false, PruneCommand.class),
    PURGE_COMMAND("purge", false, PurgeCommand.class),
    REPORTS_COMMAND("reports", true, ReportsCommand.class),
    REPORTS_LIST("reports.list", false),
    REPORTS_VIEW("reports.view", false),
    REPORTS_STATUS("reports.status", false),
    ROLES_COMMAND("roles", false, RolesCommand.class),
    SETPREFIX_COMMAND("setprefix", false, SetPrefixCommand.class),
    WELCOME_COMMAND("welcome", false, WelcomeCommand.class),
    // Music
    DELETE_COMMAND("playlist.delete", false, DeleteCommand.class),
    JOIN_COMMAND("join", true, JoinCommand.class),
    JOIN_OTHER("join.other", false),
    LEAVE_COMMAND("leave", true, LeaveCommand.class),
    LEAVE_OTHER("leave.other", false),
    LOAD_COMMAND("playlist.load", true, LoadCommand.class),
    LOOP_COMMAND("loop", true, LoopCommand.class),
    MUSICANNOUNCE_COMMAND("songannounce", false, MusicAnnounceCommand.class),
    PAUSE_COMMAND("pause", true, PauseCommand.class),
    PLAY_COMMAND("play", true, PlayCommand.class),
    PLAYLIST_COMMAND("playlist", true, PlaylistCommand.class),
    PLAYLIST_CLEAR("playlist.clear", true),
    PLAYLISTS_COMMAND("playlists", true, PlaylistsCommand.class),
    REPEAT_COMMAND("repeat", true, RepeatCommand.class),
    RESUME_COMMAND("resume", true, ResumeCommand.class),
    SAVE_COMMAND("playlist.save", true, SaveCommand.class),
    SAVE_OVERWRITE("playlist.save.overwrite", false),
    SEARCH_COMMAND("search", true, SearchCommand.class),
    SEEK_COMMAND("seek", true, SeekCommand.class),
    SHUFFLE_COMMAND("shuffle", true, ShuffleCommand.class),
    SKIP_COMMAND("skip", true, SkipCommand.class),
    SKIP_FORCE("skip.force", false),
    SKIP_CANCEL("skip.cancel", false),
    SONG_COMMAND("song", true, SongCommand.class),
    SONGNICK_COMMAND("songnick", false, SongNickCommand.class),
    STOP_COMMAND("stop", false, StopCommand.class),
    // Misc
    AVATAR_COMMAND("avatar", true, AvatarCommand.class),
    AVATAR_OTHER("avatar.other", false),
    REMIND_COMMAND("remind", true, RemindCommand.class),
    TAGS_COMMAND("tags", true, TagsCommand.class),
    TAGS_ADMIN("tags.admin", false);

    private String permission;
    private boolean defaultPerm;
    private Class<? extends Command> command;

    private static final Map<Class<? extends Command>, Permission> COMMAND_PERMISSION_MAP = GeneralUtils.getReverseMapping(
            Permission.class,
            Permission::getCommand);
    private static final Map<String, Permission> PERMISSION_MAP = GeneralUtils.getReverseMapping(
            Permission.class,
            p -> p.getPermission().toLowerCase());

    Permission(String permission, boolean defaultPerm) {
        this.permission = "flarebot." + permission;
        this.defaultPerm = defaultPerm;
    }

    Permission(String permission, boolean defaultPerm, Class<? extends Command> command) {
        this.permission = "flarebot." + permission;
        this.defaultPerm = defaultPerm;
        this.command = command;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isDefaultPerm() {
        return defaultPerm;
    }

    public Class<? extends Command> getCommand() {
        return command;
    }

    public static Permission getPermission(Class<? extends Command> command) {
        return COMMAND_PERMISSION_MAP.get(command);
    }

    public static Permission getPermission(String permission) {
        return PERMISSION_MAP.get(permission.toLowerCase());
    }

    public static boolean isValidPermission(String permission) {
        return getPermission(permission.substring(permission.startsWith("-") ? 1 : 0)) != null;
    }

    @Override
    public String toString() {
        return getPermission();
    }

    public enum Reply {
        ALLOW,
        DENY,
        NEUTRAL
    }

}

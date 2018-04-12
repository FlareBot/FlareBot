package stream.flarebot.flarebot.permissions;

import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.util.general.GeneralUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;

public enum Permission {

    // All Permissions
    ALL_PERMISSIONS("*"),
    // Categories
    CAGEGORY_GENERAL("category.general", false, CommandType.GENERAL),
    CAGEGORY_MODERATION("category.moderation", false, CommandType.MODERATION),
    CAGEGORY_MUSIC("category.music", false, CommandType.MUSIC),
    CAGEGORY_USEFUL("category.useful", false, CommandType.USEFUL),
    CAGEGORY_CURRENCY("category.currency", false, CommandType.CURRENCY),
    CAGEGORY_RANDOM("category.random", false, CommandType.RANDOM),
    // Currency
    CONVERT_COMMAND("convert", true),
    CURRENCY_COMMAND("currency", true),
    // General
    USAGE_COMMAND("usage", true),
    HELP_COMMAND("help", true),
    INFO_COMMAND("info", true),
    INVITE_COMMAND("invite", true),
    REPORT_COMMAND("report", true),
    SELFASSIGN_COMMAND("selfassign", true),
    SELFASSIGN_ADMIN("selfassign.admin", false),
    SERVERINFO_COMMAND("serverinfo", true),
    SHARDINFO_COMMAND("shardinfo", true),
    STATS_COMMAND("stats", true),
    STATUS_COMMAND("status", true),
    USERINFO_COMMAND("userinfo", true),
    USERINFO_OTHER("userinfo.other", true),
    // Moderation
    BAN_COMMAND("ban", false, net.dv8tion.jda.core.Permission.BAN_MEMBERS),
    SOFTBAN_COMMAND("softban", false, net.dv8tion.jda.core.Permission.BAN_MEMBERS),
    FORCEBAN_COMMAND("forceban", false, net.dv8tion.jda.core.Permission.BAN_MEMBERS),
    KICK_COMMAND("kick", false, net.dv8tion.jda.core.Permission.KICK_MEMBERS),
    MODLOG_COMMAND("modlog", false),
    MUTE_COMMAND("mute", false),
    TEMPBAN_COMMAND("tempban", false, net.dv8tion.jda.core.Permission.BAN_MEMBERS),
    TEMPMUTE_COMMAND("tempmute", false),
    UNBAN_COMMAND("unban", false, net.dv8tion.jda.core.Permission.BAN_MEMBERS),
    UNMUTE_COMMAND("unmute", false),
    WARN_COMMAND("warn", false),
    WARNINGS_COMMAND("warnings", false),
    NINO_COMMAND("nino", false),
    SETTINGS_COMMAND("settings", false),
    // Server Moderation
    AUTOASSIGN_COMMAND("autoassign", false),
    FIX_COMMAND("fix", false),
    LOCKCHAT_COMMAND("lockchat", false),
    PERMISSIONS_COMMAND("permissions", false, net.dv8tion.jda.core.Permission.MANAGE_SERVER),
    PIN_COMMAND("pin", false, net.dv8tion.jda.core.Permission.MESSAGE_MANAGE),
    PRUNE_COMMAND("prune", false, net.dv8tion.jda.core.Permission.MANAGE_SERVER),
    PURGE_COMMAND("purge", false, net.dv8tion.jda.core.Permission.MESSAGE_MANAGE),
    REPORTS_COMMAND("reports", false),
    REPORTS_LIST("reports.list", false),
    REPORTS_VIEW("reports.view", false),
    REPORTS_STATUS("reports.status", false),
    ROLES_COMMAND("roles", false),
    SETPREFIX_COMMAND("setprefix", false, net.dv8tion.jda.core.Permission.MANAGE_SERVER),
    WELCOME_COMMAND("welcome", false),
    // Music
    DELETE_COMMAND("playlist.delete", false),
    JOIN_COMMAND("join", true),
    JOIN_OTHER("join.other", false),
    LEAVE_COMMAND("leave", true),
    LEAVE_OTHER("leave.other", false),
    LOAD_COMMAND("playlist.load", true),
    LOOP_COMMAND("loop", true),
    MUSICANNOUNCE_COMMAND("songannounce", false),
    PAUSE_COMMAND("pause", true),
    PLAY_COMMAND("play", true),
    QUEUE_COMMAND("queue", true),
    QUEUE_CLEAR("queue.clear", true),
    PLAYLISTS_COMMAND("playlists", true),
    REPEAT_COMMAND("repeat", true),
    RESUME_COMMAND("resume", true),
    SAVE_COMMAND("playlist.save", true),
    SAVE_OVERWRITE("playlist.save.overwrite", false),
    SEARCH_COMMAND("search", true),
    SEEK_COMMAND("seek", true),
    SHUFFLE_COMMAND("shuffle", true),
    SKIP_COMMAND("skip", true),
    SKIP_FORCE("skip.force", false),
    SKIP_CANCEL("skip.cancel", false),
    SONG_COMMAND("song", true),
    SONGNICK_COMMAND("songnick", false),
    STOP_COMMAND("stop", false),
    KILL_COMMAND("kill", false),
    // Misc
    AVATAR_COMMAND("avatar", true),
    AVATAR_OTHER("avatar.other", false),
    REMIND_COMMAND("remind", true),
    TAGS_COMMAND("tags", true),
    TAGS_ADMIN("tags.admin", false),
    BLACKLIST_BYPASS("blacklist.bypass", false),
    COLOR_COMMAND("color", true),
    JUMBO_COMMAND("jumbo", true);

    public static final Permission[] VALUES = Permission.values();

    private String permission;
    private boolean defaultPerm;
    private EnumSet<net.dv8tion.jda.core.Permission> discordPerm = EnumSet.noneOf(net.dv8tion.jda.core.Permission.class);
    private CommandType commandType;

    private static final Map<CommandType, Permission> COMMAND_TYPE_MAP =
            GeneralUtils.getReverseMapping(
                    Permission.class,
                    Permission::getCommandType);
    private static final Map<String, Permission> PERMISSION_MAP = GeneralUtils.getReverseMapping(
            Permission.class,
            p -> p.getPermission().toLowerCase());

    Permission(String permission, boolean defaultPerm) {
        this.permission = "flarebot." + permission;
        this.defaultPerm = defaultPerm;
    }

    Permission(String permission, boolean defaultPerm, CommandType commandType) {
        this.permission = "flarebot." + permission;
        this.defaultPerm = defaultPerm;
        this.commandType = commandType;
    }

    Permission(String permission, boolean defaultPerm, net.dv8tion.jda.core.Permission... discordPerm) {
        this.permission = "flarebot." + permission;
        this.defaultPerm = defaultPerm;
        this.discordPerm = EnumSet.noneOf(net.dv8tion.jda.core.Permission.class);
        this.discordPerm.addAll(Arrays.asList(discordPerm));
    }

    Permission(String permission) {
        this.permission = permission;
        this.defaultPerm = false;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isDefaultPerm() {
        return defaultPerm;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public static Permission getPermission(CommandType commandType) {
        return COMMAND_TYPE_MAP.get(commandType);
    }

    public static Permission getPermission(String permission) {
        return PERMISSION_MAP.get(permission.toLowerCase());
    }

    public static boolean isValidPermission(String permission) {
        if (permission.contains("*") && permission.contains(".")) {
            PermissionNode node = new PermissionNode(permission);
            for (Permission perm : Permission.VALUES) {
                if (perm != Permission.ALL_PERMISSIONS) {
                    if (node.test(perm.getPermission())) return true;
                }
            }
        }
        return getPermission(permission.substring(permission.startsWith("-") ? 1 : 0)) != null;
    }

    public EnumSet<net.dv8tion.jda.core.Permission> getDiscordPerm() {
        return discordPerm;
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

    public static class Presets {

        public static EnumSet<Permission> MODERATION = EnumSet.of(
                Permission.PURGE_COMMAND,
                Permission.LOCKCHAT_COMMAND,
                Permission.PIN_COMMAND,
                Permission.BAN_COMMAND,
                Permission.KICK_COMMAND,
                Permission.MUTE_COMMAND,
                Permission.TEMPMUTE_COMMAND,
                Permission.TEMPBAN_COMMAND,
                Permission.UNBAN_COMMAND,
                Permission.UNMUTE_COMMAND,
                Permission.WARN_COMMAND
        );

    }

}

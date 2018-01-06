package stream.flarebot.flarebot.permissions;

import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.currency.ConvertCommand;
import stream.flarebot.flarebot.commands.currency.CurrencyCommand;
import stream.flarebot.flarebot.commands.general.*;
import stream.flarebot.flarebot.commands.moderation.*;
import stream.flarebot.flarebot.commands.moderation.mod.*;

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
    WELCOME_COMMAND("welcome", false, WelcomeCommand.class),;

    private String permission;
    private boolean defaultPerm;
    private Class<? extends Command> command;

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
        for (Permission permission : Permission.values()) {
            if (permission.getCommand().equals(command)) return permission;
        }
        return null;
    }

    @Override
    public String toString() {
        return getPermission();
    }
}

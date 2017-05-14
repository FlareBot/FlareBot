package stream.flarebot.flarebot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.FlareBotManager;

import java.util.Optional;

public enum Language {

    // General
    GENERAL_INVALIDARGUMENT,
    GENERAL_USAGE,
    GENERAL_SUCCESS,
    GENERAL_USER,
    GENERAL_UNKNOWN,
    GENERAL_REQUESTEDBY,
    GENERAL_PAGE,
    // Auto Assign
    AUTOASSIGN_CURRENT,
    AUTOASSIGN_NOROLES,
    AUTOASSIGN_INVALIDROLE,
    AUTOASSIGN_ADDASSIGNED,
    AUTOASSIGN_REMOVEASSIGNED,
    AUTOASSIGN_ALREADYASSIGNED,
    AUTOASSIGN_NOTAUTOASSIGNROLE,
    AUTOASSIGN_DESCRIPTION,
    AUTOASSIGN_USAGE,
    // Ban
    BAN_USERNOTFOUND,
    BAN_BANHAMMERSTRUCK,
    BAN_CANTBAN,
    BAN_DESCRIPTION,
    BAN_USAGE,
    // Permissions
    PERMISSIONS_USERNOEXIST,
    PERMISSIONS_GROUPNOEXIST,
    PERMISSIONS_USERHADGROUP,
    PERMISSIONS_USERNEVERHADGROUP,
    PERMISSIONS_GROUPS,
    PERMISSIONS_GROUPHADPERM,
    PERMISSIONS_GROUPNEVERHADPERM,
    PERMISSIONS_PERMSFORGROUP,
    PERMISSIONS_COULDNOTSAVE,
    PERMISSIONS_DESCRIPTION,
    PERMISSIONS_USAGE,
    // Pin
    PIN_DESCRIPTION,
    PIN_USAGE,
    // Purge
    PURGE_COOLDOWN,
    PURGE_MIN,
    PURGE_DELETED,
    PURGE_FAILED,
    PURGE_NOPERMS,
    PURGE_DESCRIPTION,
    PURGE_USAGE,
    // Roles
    ROLES_SERVERROLES,
    ROLES_DESCRIPTION,
    ROLES_USAGE,
    // Prefix
    PREFIX_TOOLONG,
    PREFIX_SETPREFIX,
    PREFIX_CURRENT,
    PREFIX_DESCRIPTION,
    PREFIX_USAGE,
    // Welcomes
    WELCOMES_ENABLED,
    WELCOMES_ALREADYENABLED,
    WELCOMES_DISABLED,
    WELCOMES_ALREADYDISABLED,
    WELCOMES_APPEARSET,
    WELCOMES_NOTENABLED,
    WELCOMES_SETHELP,
    WELCOMES_CURRENTMESSAGE,
    WELCOMES_SET,
    WELCOMES_DESCRIPTION,
    WELCOMES_USAGE,
    // Automod
    //TODO: To be done once feature is ready for prod
    // Help
    HELP_NOSUCHCATEGORY,
    HELP_HEADER,
    HELP_CONTINUED,
    HELP_DESCRIPTION,
    // Info
    INFO_HEADER,
    INFO_INFONOTFOUND,
    INFO_DESCRIPTION,
    INFO_USAGE,
    INFO_CONTENT_SERVERS,
    INFO_CONTENT_TOTALUSERS,
    INFO_CONTENT_VOICECONNECTIONS,
    INFO_CONTENT_MUSICCHANNELS,
    INFO_CONTENT_TEXTCHANNELS,
    INFO_CONTENT_UPTIME,
    INFO_CONTENT_MEMUSAGE,
    INFO_CONTENT_MEMFREE,
    INFO_CONTENT_VIDTHREADS,
    INFO_CONTENT_TOTALTHREADS,
    INFO_CONTENT_VERSION,
    INFO_CONTENT_JDAVERSION,
    INFO_CONTENT_GITREVISION,
    INFO_CONTENT_SUPPORTSERVER,
    INFO_CONTENT_DONATE,
    INFO_CONTENT_OURPATREON,
    INFO_CONTENT_PATREON,
    INFO_CONTENT_WEBSITE,
    INFO_CONTENT_TWITTER,
    INFO_CONTENT_INVITE,
    INFO_CONTENT_MADEBY,
    INFO_CONTENT_MADEBYCONTENT,
    INFO_CONTENT_SOURCE,
    // Invite
    INVITE_INVITATION,
    INVITE_DESCRIPTION,
    INVITE_USAGE,
    // Join
    JOIN_CURRENTLYCONNECTING,
    JOIN_JOINOTHERPERM,
    JOIN_USERLIMIT,
    JOIN_NOPERMS,
    JOIN_DESCRIPTION,
    JOIN_USAGE,
    // Leave
    LEAVE_LEAVEOTHERPERM,
    LEAVE_DESCRIPTION,
    LEAVE_USAGE,
    // Poll
    // TODO: Add when rewritten
    // Self Assign
    SELFASSIGN_ADMINPERMNEEDED,
    SELFASSIGN_ROLESLIST,
    SELFASSIGN_ROLEDOESNTEXIST,
    SELFASSIGN_CANNOTSELFASSIGN,
    SELFASSIGN_USEROLEID,
    SELFASSIGN_ADDEDTOLIST,
    SELFASSIGN_REMOVEDFROMLIST,
    SELFASSIGN_ASSIGNED,
    SELFASSIGN_UNASSIGNED,
    SELFASSIGN_DESCRIPTION,
    SELFASSIGN_USAGE,
    // User info
    USERINFO_USERNOTFOUND,
    USERINFO_USERINFO_TITLE,
    USERINFO_USERINFO_AVATAR,
    USERINFO_USERINFO_DEFAULTAVATAR,
    USERINFO_GENERALINFO_TITLE,
    USERINFO_GENERALINFO_SERVERS,
    USERINFO_GENERALINFO_ROLES,
    USERINFO_GENERALINFO_USERNOTINSERVER,
    USERINFO_GENERALINFO_STATUS,
    USERINFO_GENERALINFO_CURRENTSHARD,
    USERINFO_TIMEDATA_TITLE,
    USERINFO_TIMEDATA_CREATED,
    USERINFO_TIMEDATA_JOINED,
    USERINFO_TIMEDATA_USERNOTINSERVER,
    USERINFO_TIMEDATA_LASTSEEN,
    USERINFO_TIMEDATA_LASTSPOKE,
    USERINFO_DESCRIPTION,
    USERINFO_USAGE,
    // Delete
    DELETE_REMOVEDPLAYLIST,
    DELETE_PLAYLISTDOESNTEXIST,
    DELETE_DATABASEERROR,
    DELETE_DESCRIPTION,
    DELETE_USAGE,
    // Load
    LOAD_DESCRIPTION,
    LOAD_USAGE,
    // Loop
    LOOP_LOOPINGON,
    LOOP_LOOPINGOFF,
    LOOP_DESCRIPTION,
    LOOP_USAGE,
    // Music Announce
    ANNOUNCE_SETCHANNEL,
    ANNOUNCE_DATABASEERROR,
    ANNOUNCE_DISABLEANNOUCEMENTS,
    ANNOUNCE_DESCRIPTION,
    ANNOUNCE_USAGE,
    // Pause
    PAUSE_DESCRIPTION,
    PAUSE_USAGE,
    // Play
    PLAY_NOMUSICPLAYING,
    PLAY_RESUMING,
    PLAY_DESCRIPTION,
    PLAY_USAGE,
    // Playlist
    PLAYLIST_CLEARED,
    PLAYLIST_INVALIDNUMBER,
    PLAYLIST_NOSONGWITHINDEX,
    PLAYLIST_REMOVEDSONG,
    PLAYLIST_NOSONGS,
    //TODO: Add desc and usage when fixed


    ;

    public EmbedBuilder getEmbed(String guildId, Language lang, Object... args) {
        return getEmbed(Optional.empty(), guildId, lang, args);
    }

    public EmbedBuilder getEmbed(Optional<User> user, String guildId, Language lang, Object... args) {
        if (user.isPresent())
            return MessageUtils.getEmbed(user.get()).setDescription(FlareBotManager.getInstance().getLang(lang, guildId));
        else
            return MessageUtils.getEmbed().setDescription(FlareBotManager.getInstance().getLang(lang, guildId));
    }

    public EmbedBuilder getErrorEmbed(String guildId, Language lang, Object... args) {
        return getErrorEmbed(Optional.empty(), guildId, lang, args);
    }

    public EmbedBuilder getErrorEmbed(Optional<User> user, String guildId, Language lang, Object... args) {
        if (user.isPresent())
            return MessageUtils.getEmbed(user.get()).setDescription(get(guildId, lang, args));
        else
            return MessageUtils.getEmbed().setDescription(FlareBotManager.getInstance().getLang(lang, guildId));
    }

    public Message send(MessageChannel channel, String guildId, Language lang, Object... args) {
        return channel.sendMessage(String.format(FlareBotManager.getInstance().getLang(lang, guildId), args)).complete();
    }

    public String get(String guildId, Language lang, Object... args) {
        return String.format(FlareBotManager.getInstance().getLang(lang, guildId), args);
    }

    public Message sendEmbed(MessageChannel channel, String guildId, Language lang, Object... args) {
        return channel.sendMessage(getEmbed(guildId, lang, args).build()).complete();
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

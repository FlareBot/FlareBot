package stream.flarebot.flarebot;

public enum Language {

    // General
    GENERAL_INVALIDARGUMENT,
    GENERAL_USAGE,
    GENERAL_SUCCESS,
    GENERAL_USER,
    // Auto Assign
    AUTOASSIGN_CURRENT,
    AUTOASSIGN_NOROLES,
    AUTOASSIGN_INVALIDROLE,
    AUTOASSIGN_ADDASSIGNED,
    AUTOASSIGN_REMOVEASSIGNED,
    AUTOASSIGN_ALREADYASSIGNED,
    AUTOASSIGN_NOTAUTOASSIGNROLE,
    AUTOASSIGN_DESCRIPTION,
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
    // Roles
    ROLES_SERVERROLES,
    ROLES_DESCRIPTION,
    // Prefix
    PREFIX_TOOLONG,
    PREFIX_SETPREFIX,
    PREFIX_CURRENT,
    PREFIX_DESCRIPTION,
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
    // Automod
    //TODO: To be done once feature is ready for prod
    // Help
    HELP_NOSUCHCATEGORY,
    HELP_HEADER,
    HELP_CONTINUED,
    HELP_DESCRIPTION,
    // Info
    INFO_HEADER;


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

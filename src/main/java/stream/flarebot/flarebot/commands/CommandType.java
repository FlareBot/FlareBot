package stream.flarebot.flarebot.commands;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.Constants;

import java.util.Arrays;
import java.util.Set;

public enum CommandType {

    GENERAL(true),
    MODERATION(true),
    MUSIC(true),
    USEFUL(false),
    CURRENCY(false),
    RANDOM(false),
    INFORMATIONAL(false),
    SECRET(false, Constants.DEVELOPER_ID),
    INTERNAL(false, Constants.STAFF_ID, Constants.CONTRIBUTOR_ID, Constants.DEVELOPER_ID);

    private static final CommandType[] values = values();
    private static final CommandType[] defaultTypes = fetchDefaultTypes();
    
    // If it shows up in help
    private boolean defaultType;
    // If it is restricted to staff, contribs or devs
    private long[] internalRoleIds = new long[] {};

    CommandType(boolean defaultType) {
        this.defaultType = defaultType;
    }
    
    CommandType(boolean defaultType, long internalRole) {
        this.defaultType = defaultType;
        this.internalRoleIds = new long[] {internalRole};
    }

    CommandType(boolean defaultType, long... internalRoles) {
        this.defaultType = defaultType;
        this.internalRoleIds = internalRoles;
    }

    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
    
    public boolean isInternal() {
        return internalRoleIds.length > 0;
    }

    public static CommandType[] getTypes() {
        return defaultTypes;
    }
    
    public static CommandType[] fetchDefaultTypes() {
        return Arrays.stream(values).filter(type -> type.defaultType).toArray(CommandType[]::new);
    }
    
    /**
     * Role ID needed to execute the internal command, this will be -1 if 
     * the command is not internal.
    */
    public long getRoleId() {
        return internalRoleIds[0];
    }

    public long[] getRoleIds() {
        return internalRoleIds;
    }

    public Set<Command> getCommands() {
        return FlareBot.getCommandManager().getCommandsByType(this);
    }
}

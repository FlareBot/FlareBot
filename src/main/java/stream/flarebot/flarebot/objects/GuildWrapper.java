package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.mod.Moderation;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.ReportManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class GuildWrapper {

    private String guildId;
    private Welcome welcome = new Welcome();
    private PerGuildPermissions permissions = new PerGuildPermissions();
    private Set<String> autoAssignRoles = new HashSet<>();
    private Set<String> selfAssignRoles = new HashSet<>();
    private boolean songnick = false;
    // Should be moved to their own manager.
    private boolean blocked = false;
    private long unBlockTime = -1;
    private String blockReason = null;
    // TODO: Move to Moderation fully - This will be a breaking change so we will basically just refer to the new location
    private String mutedRoleID = null;
    private ReportManager reportManager = new ReportManager();
    private Map<String, List<String>> warnings = new ConcurrentHashMap<>();
    private Map<String, String> tags = new ConcurrentHashMap<>();
    private Moderation moderation;
    private NINO nino = null;

    // oooo special!
    private boolean betaAccess = false;

    /**
     * <b>Do not use</b>
     *
     * @param guildId Guild Id of the desired new GuildWrapper
     */
    public GuildWrapper(String guildId) {
        this.guildId = guildId;
    }

    public Guild getGuild() {
        return FlareBot.getInstance().getGuildById(guildId);
    }

    public String getGuildId() {
        return this.guildId;
    }

    public long getGuildIdLong() {
        return Long.parseLong(this.guildId);
    }

    public Welcome getWelcome() {
        if (welcome == null) {
            welcome = new Welcome();
            welcome.setDmEnabled(false);
            welcome.setGuildEnabled(false);
        }
        return this.welcome;
    }

    public PerGuildPermissions getPermissions() {
        if (permissions == null) {
            permissions = new PerGuildPermissions();
        }
        return permissions;
    }

    public void setPermissions(PerGuildPermissions permissions) {
        this.permissions = permissions;
    }

    public Set<String> getAutoAssignRoles() {
        return this.autoAssignRoles;
    }

    public Set<String> getSelfAssignRoles() {
        return this.selfAssignRoles;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public void addBlocked(String reason) {
        blocked = true;
        blockReason = reason;
        unBlockTime = -1; //-1 represents both infinite and unblocked
    }

    public void addBlocked(String reason, long unBlockTime) {
        blocked = true;
        blockReason = reason;
        this.unBlockTime = unBlockTime;
    }

    public void revokeBlock() {
        blocked = false;
        blockReason = "";
        unBlockTime = -1; //-1 represents both infinite and unblocked
    }

    public String getBlockReason() {
        return blockReason;
    }

    public long getUnBlockTime() {
        return unBlockTime;
    }

    public boolean isSongnickEnabled() {
        return songnick;
    }

    public void setSongnick(boolean songnick) {
        this.songnick = songnick;
    }

    public Role getMutedRole() {
        if (mutedRoleID == null) {
            Role mutedRole =
                    GeneralUtils.getRole("Muted", getGuild()).isEmpty() ? null : GeneralUtils.getRole("Muted", getGuild()).get(0);
            if (mutedRole == null) {
                if (!getGuild().getSelfMember().hasPermission(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS))
                    return null;
                try {
                    mutedRole = getGuild().getController().createRole().setName("Muted").submit().get();
                    if (!getGuild().getSelfMember().getRoles().isEmpty())
                        getGuild().getController().modifyRolePositions().selectPosition(mutedRole)
                                .moveTo(getGuild().getSelfMember().getRoles().get(0).getPosition() - 1).queue();
                    mutedRoleID = mutedRole.getId();
                    handleMuteChannels(mutedRole);
                    return mutedRole;
                } catch (InterruptedException | ExecutionException e) {
                    FlareBot.LOGGER.error("Error creating role!", e);
                    return null;
                }
            } else {
                mutedRoleID = mutedRole.getId();
                handleMuteChannels(mutedRole);
                return mutedRole;
            }
        } else {
            Role mutedRole = getGuild().getRoleById(mutedRoleID);
            if (mutedRole == null) {
                mutedRoleID = null;
                return getMutedRole();
            } else {
                handleMuteChannels(mutedRole);
                return mutedRole;
            }
        }
    }

    /**
     * This will go through all the channels in a guild, if there is no permission override or it doesn't block message write then deny it.
     *
     * @param muteRole This is the muted role of the server, the role which will have MESSAGE_WRITE denied.
     */
    private void handleMuteChannels(Role muteRole) {
        getGuild().getTextChannels().forEach(channel -> {
            if (!getGuild().getSelfMember().hasPermission(channel, Permission.MANAGE_PERMISSIONS)) return;
            if (channel.getPermissionOverride(muteRole) != null &&
                    !channel.getPermissionOverride(muteRole).getDenied().contains(Permission.MESSAGE_WRITE))
                channel.getPermissionOverride(muteRole).getManager().deny(Permission.MESSAGE_WRITE).queue();
            else if (channel.getPermissionOverride(muteRole) == null)
                channel.createPermissionOverride(muteRole).setDeny(Permission.MESSAGE_WRITE).queue();
        });
    }

    public ReportManager getReportManager() {
        if (reportManager == null) reportManager = new ReportManager();
        return reportManager;
    }

    public List<String> getUserWarnings(User user) {
        if (warnings == null) warnings = new ConcurrentHashMap<>();
        return warnings.getOrDefault(user.getId(), new ArrayList<>());
    }

    public void addWarning(User user, String reason) {
        List<String> warningsList = getUserWarnings(user);
        warningsList.add(reason);
        warnings.put(user.getId(), warningsList);
    }

    public Map<String, List<String>> getWarningsMap() {
        return warnings;
    }

    public boolean isBetaAccess() {
        return betaAccess;
    }

    public void setBetaAccess(boolean betaAccess) {
        this.betaAccess = betaAccess;
    }

    public boolean getBetaAccess() {
        return betaAccess;
    }

    public Map<String, String> getTags() {
        if (tags == null) tags = new ConcurrentHashMap<>();
        return tags;
    }

    public Moderation getModeration() {
        if (this.moderation == null) this.moderation = new Moderation();
        return this.moderation;
    }

    public Moderation getModConfig() {
        return getModeration();
    }

    public NINO getNINO() {
        if (nino == null)
            nino = new NINO();
        return nino;
    }
}

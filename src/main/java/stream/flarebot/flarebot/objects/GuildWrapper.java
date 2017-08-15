package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.requests.RestAction;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModConfig;
import stream.flarebot.flarebot.mod.AutoModGuild;
import stream.flarebot.flarebot.mod.Punishment;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.util.GeneralUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class GuildWrapper {

    private String guildId;
    private AutoModGuild autoModGuild = new AutoModGuild();
    private Welcome welcome = new Welcome();
    private PerGuildPermissions permissions = new PerGuildPermissions();
    private LinkedList<Poll> polls = new LinkedList<>();
    private Set<String> autoAssignRoles = new HashSet<>();
    private Set<String> selfAssignRoles = new HashSet<>();
    private Language.Locales locale = Language.Locales.ENGLISH_UK;
    private boolean blocked = false;
    private boolean songnick = false;
    private long unBlockTime = -1;
    private String blockReason = null;
    private String mutedRoleID = null;
    private Map<String, Map<Punishment.EPunishment, Long>> punishments = new HashMap<>();

    protected GuildWrapper(String guildId) {
        this.guildId = guildId;
    }

    public Guild getGuild() {
        return FlareBot.getInstance().getGuildByID(guildId);
    }

    public String getGuildId() {
        return this.guildId;
    }

    public AutoModGuild getAutoModGuild() {
        return this.autoModGuild;
    }

    protected void setAutoModGuild(AutoModGuild autoModGuild) {
        this.autoModGuild = autoModGuild;
    }

    public AutoModConfig getAutoModConfig() {
        return (this.autoModGuild == null ? null : this.autoModGuild.getConfig());
    }

    public Welcome getWelcome() {
        if (welcome == null) {
            welcome = new Welcome();
            welcome.setDmEnabled(false);
            welcome.setGuildEnabled(false);
        }
        return this.welcome;
    }

    protected void setWelcome(Welcome welcome) {
        this.welcome = welcome;
    }

    public PerGuildPermissions getPermissions(){
        if(permissions == null){
            permissions = new PerGuildPermissions();
        }
        return permissions;
    }

    protected void setPermissions(PerGuildPermissions permissions) {
        this.permissions = permissions;
    }

    public LinkedList<Poll> getPolls() {
        return this.polls;
    }

    protected void setPolls(LinkedList<Poll> polls) {
        this.polls = polls;
    }

    public Set<String> getAutoAssignRoles() {
        return this.autoAssignRoles;
    }

    protected void setAutoAssignRoles(Set<String> roles) {
        this.autoAssignRoles = roles;
    }

    public Set<String> getSelfAssignRoles() {
        return this.selfAssignRoles;
    }

    protected void setSelfAssignRoles(Set<String> roles) {
        this.selfAssignRoles = roles;
    }

    public Language.Locales getLocale() {
        return this.locale;
    }

    protected void setLocale(Language.Locales locale) {
        this.locale = locale;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    protected void setBlocked(boolean blocked) {
        this.blocked = blocked;
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

    public Role getMutedRole(){
        if(mutedRoleID == null){
            Role mutedRole = GeneralUtils.getRole("Muted", getGuild()).isEmpty() ? null : GeneralUtils.getRole("Muted", getGuild()).get(0);
            if(mutedRole == null){
                try {
                    mutedRole = getGuild().getController().createRole().setName("Muted").submit().get();
                    if(!getGuild().getSelfMember().getRoles().isEmpty())
                        getGuild().getController().modifyRolePositions().selectPosition(mutedRole)
                                .moveTo(getGuild().getSelfMember().getRoles().get(0).getPosition()-1).queue();
                    Role finalMutedRole = mutedRole;
                    getGuild().getTextChannels().forEach(channel -> channel.createPermissionOverride(finalMutedRole).setDeny(Permission.MESSAGE_WRITE).queue());
                    mutedRoleID = mutedRole.getId();
                    return mutedRole;
                } catch (InterruptedException | ExecutionException e) {
                    FlareBot.LOGGER.error("Error creating role!", e);
                    return null;
                }
            } else {
                mutedRoleID = mutedRole.getId();
                return mutedRole;
            }
        } else {
            Role mutedRole = getGuild().getRoleById(mutedRoleID);
            if(mutedRole == null){
                mutedRoleID= null;
                return getMutedRole();
            } else {
                return mutedRole;
            }
        }
    }
}

package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModConfig;
import stream.flarebot.flarebot.mod.AutoModGuild;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.ReportManager;

import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Column;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

//TODO: Change the table name
@Table(keyspace = "flarebot", name = "guild_data_experimental", 
       readConsistency = "QUORUM", writeConsistency = "QUORUM")
public class GuildWrapper {

    /*
        CREATE TABLE flarebot.guild_data (
            guild_id text PRIMARY KEY,
            data text,
            last_retrieved timestamp
        ) WITH bloom_filter_fp_chance = 0.01
        AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}
        AND comment = ''
        AND compaction = {'class': 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy', 'max_threshold': '32', 'min_threshold': '4'}
        AND compression = {'chunk_length_in_kb': '64', 'class': 'org.apache.cassandra.io.compress.LZ4Compressor'}
        AND crc_check_chance = 1.0
        AND dclocal_read_repair_chance = 0.1
        AND default_time_to_live = 0
        AND gc_grace_seconds = 864000
        AND max_index_interval = 2048
        AND memtable_flush_period_in_ms = 0
        AND min_index_interval = 128
        AND read_repair_chance = 0.0
        AND speculative_retry = '99PERCENTILE';
    */
    
    @PartitionKey
    @Column(name = "guild_id")
    private String guildId;
    //private AutoModGuild autoModGuild = new AutoModGuild();
    private Welcome welcome = new Welcome();
    //private PerGuildPermissions permissions = new PerGuildPermissions();
    //private LinkedList<Poll> polls = new LinkedList<>();
    private Set<String> autoAssignRoles = new HashSet<>();
    private Set<String> selfAssignRoles = new HashSet<>();
    private Language.Locales locale = Language.Locales.ENGLISH_UK;
    private boolean blocked = false;
    private boolean songnick = false;
    private long unBlockTime = -1;
    private String blockReason = null;
    private String mutedRoleID = null;
    //private ReportManager reportManager = new ReportManager();
    private Map<String, List<String>> warnings = new ConcurrentHashMap<>();
    private Map<String, String> tags = new ConcurrentHashMap<>();

    // oooo special!
    private boolean betaAccess = false;

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
        if (this.autoModGuild == null)
            this.autoModGuild = new AutoModGuild();
        return this.autoModGuild.getConfig();
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

    public PerGuildPermissions getPermissions() {
        if (permissions == null) {
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
            if (mutedRole == null) {
                mutedRoleID = null;
                return getMutedRole();
            } else {
                return mutedRole;
            }
        }
    }

    public ReportManager getReportManager() {
        if(reportManager == null) reportManager = new ReportManager();
        return reportManager;
    }

    public void setReportManager(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

    public List<String> getUserWarnings(User user) {
        return warnings.get(user.getId());
    }

    public void addWarning(User user, String reason) {
        List<String> warningsList = warnings.getOrDefault(user.getId(), new ArrayList<>());
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
}

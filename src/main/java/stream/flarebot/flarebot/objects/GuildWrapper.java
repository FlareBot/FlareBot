package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.entities.Guild;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModConfig;
import stream.flarebot.flarebot.mod.AutoModGuild;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
}

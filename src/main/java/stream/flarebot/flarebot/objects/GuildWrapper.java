package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.entities.Guild;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModConfig;
import stream.flarebot.flarebot.mod.AutoModGuild;

import java.util.List;
import java.util.Set;

public class GuildWrapper {

    private String guildId;
    private AutoModGuild autoModGuild;
    private Welcome welcome;
    private List<Poll> polls;
    private Set<String> autoAssignRoles;
    private Set<String> selfAssignRoles;
    private List<Report> reports;
    private Language.Locales locale;
    private boolean blocked;
    private long unBlockTime;
    private String blockReason;

    public GuildWrapper(String guildId, AutoModGuild autoModGuild, Welcome welcome, List<Poll> polls, Set<String> autoAssignRoles, Set<String> selfAssignRoles, List<Report> reports, Language.Locales locale, boolean blocked, long unBlockTime, String blockReason) {
        this.guildId = guildId;
        this.autoModGuild = autoModGuild;
        this.welcome = welcome;
        this.polls = polls;
        this.autoAssignRoles = autoAssignRoles;
        this.selfAssignRoles = selfAssignRoles;
        this.reports = reports;
        this.locale = locale;
        this.blocked = blocked;
        this.unBlockTime = unBlockTime;
        this.blockReason = blockReason;
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

    public AutoModConfig getAutoModConfig() {
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

    public List<Poll> getPolls() {
        return this.polls;
    }

    public Set<String> getAutoAssignRoles() {
        return this.autoAssignRoles;
    }

    public Set<String> getSelfAssignRoles() {
        return this.selfAssignRoles;
    }

    public Language.Locales getLocale() {
        return this.locale;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public void addBlocked(String reason) {
        this.blocked = true;
        this.blockReason = reason;
        this.unBlockTime = -1; //-1 represents both infinite and unblocked
    }

    public void addBlocked(String reason, long unBlockTime) {
        this.blocked = true;
        this.blockReason = reason;
        this.unBlockTime = unBlockTime;
    }

    public void revokeBlock() {
        this.blocked = false;
        this.blockReason = "";
        this.unBlockTime = -1; //-1 represents both infinite and unblocked
    }

    public String getBlockReason() {
        return blockReason;
    }

    public long getUnBlockTime() {
        return unBlockTime;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }
}

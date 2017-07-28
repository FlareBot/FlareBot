package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.entities.Guild;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModConfig;
import stream.flarebot.flarebot.mod.AutoModGuild;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;

import java.util.LinkedList;
import java.util.Set;

public class GuildWrapper {

    private String guildId;
    private AutoModGuild autoModGuild;
    private Welcome welcome;
    private PerGuildPermissions permissions;
    private LinkedList<Poll> polls;
    private Set<String> autoAssignRoles;
    private Set<String> selfAssignRoles;
    private Language.Locales locale;
    private boolean blocked;
    private boolean songnick;
    private long unBlockTime;
    private String blockReason;

    public GuildWrapper(String guildId, AutoModGuild autoModGuild, Welcome welcome,
                        PerGuildPermissions permissions, LinkedList<Poll> polls, Set<String> autoAssignRoles,
                        Set<String> selfAssignRoles, Language.Locales locale, boolean blocked,
                        boolean songnick, long unBlockTime, String blockReason) {
        this.guildId = guildId;
        this.autoModGuild = autoModGuild;
        this.welcome = welcome;
        this.polls = polls;
        this.autoAssignRoles = autoAssignRoles;
        this.selfAssignRoles = selfAssignRoles;
        this.locale = locale;
        this.blocked = blocked;
        this.unBlockTime = unBlockTime;
        this.blockReason = blockReason;
        this.permissions = permissions;
        this.songnick = songnick;
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

    public PerGuildPermissions getPermissions(){
        if(permissions == null){
            permissions = new PerGuildPermissions();
        }
        return permissions;
    }

    public LinkedList<Poll> getPolls() {
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

    public void setSongnickEnabled(boolean songnick) {
        this.songnick = songnick;
    }
}

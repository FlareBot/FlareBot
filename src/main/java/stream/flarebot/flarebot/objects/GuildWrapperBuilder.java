package stream.flarebot.flarebot.objects;

import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModGuild;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;
import stream.flarebot.flarebot.permissions.PermissionNode;

import java.util.LinkedList;
import java.util.Set;

public class GuildWrapperBuilder {

    private String guildId;
    private AutoModGuild autoModGuild = new AutoModGuild();
    private Welcome welcome = null;
    private LinkedList<Poll> polls = new LinkedList<>();
    private Set<String> autoAssignRoles = new ConcurrentHashSet<>();
    private Set<String> selfAssignRoles = new ConcurrentHashSet<>();
    private Language.Locales locale = Language.Locales.ENGLISH_UK;
    private PerGuildPermissions permissions;
    private boolean blocked;
    private boolean songnick;

    private GuildWrapperBuilder() {
    }

    public GuildWrapperBuilder(String guildId) {
        this.guildId = guildId;
    }

    public GuildWrapperBuilder setAutoMod(AutoModGuild autoMod) {
        this.autoModGuild = autoMod;
        return this;
    }

    public GuildWrapperBuilder setWelcome(Welcome welcome) {
        this.welcome = welcome;
        return this;
    }

    public GuildWrapperBuilder setPolls(LinkedList<Poll> polls) {
        this.polls = polls;
        return this;
    }

    public GuildWrapperBuilder addPoll(Poll poll) {
        this.polls.add(poll);
        return this;
    }

    public GuildWrapperBuilder setSelfAssignRoles(Set<String> roles) {
        this.selfAssignRoles = roles;
        return this;
    }

    public GuildWrapperBuilder setAutoAssignRoles(Set<String> roles) {
        this.autoAssignRoles = roles;
        return this;
    }

    public GuildWrapperBuilder setLanguage(Language.Locales locale) {
        this.locale = locale;
        return this;
    }

    public GuildWrapperBuilder setPermissions(PerGuildPermissions permissions){
        this.permissions = permissions;
        return this;
    }

    public GuildWrapperBuilder setBlocked(boolean blocked){
        this.blocked = blocked;
        return this;
    }

    public GuildWrapperBuilder setSongnick(boolean songnick) {
        this.songnick = songnick;
        return this;
    }

    public GuildWrapper build() {
        return new GuildWrapper(guildId, autoModGuild, welcome, permissions, polls, autoAssignRoles, selfAssignRoles, locale, blocked, songnick, 0l, "");
    }
}

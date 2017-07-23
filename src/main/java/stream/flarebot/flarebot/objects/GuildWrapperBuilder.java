package stream.flarebot.flarebot.objects;

import org.eclipse.jetty.util.ConcurrentHashSet;
import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModGuild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class GuildWrapperBuilder {

    private String guildId;
    private AutoModGuild autoModGuild = new AutoModGuild();
    private Welcome welcome = null;
    private List<Poll> polls = Collections.synchronizedList(new ArrayList<>());
    private Set<String> autoAssignRoles = new ConcurrentHashSet<>();
    private Set<String> selfAssignRoles = new ConcurrentHashSet<>();
    private List<Report> reports = Collections.synchronizedList(new ArrayList<>());
    private Language.Locales locale = Language.Locales.ENGLISH_UK;

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

    public GuildWrapperBuilder setReports(List<Report> reports){
        this.reports = reports;
        return this;
    }

    public GuildWrapper build() {
        return new GuildWrapper(guildId, autoModGuild, welcome, polls, autoAssignRoles, selfAssignRoles, reports, locale, false, 0l, "");
    }
}

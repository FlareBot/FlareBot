package stream.flarebot.flarebot.objects;

import stream.flarebot.flarebot.mod.AutoModGuild;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class GuildWrapperBuilder {

    private String guildId;
    private AutoModGuild autoModGuild = null;
    private Welcome welcome = null;
    private Set<Poll> polls = new HashSet<>();
    private Set<String> selfAssignRoles = new HashSet<>();
    private Locale locale = Locale.ENGLISH;

    private GuildWrapperBuilder(){}

    public GuildWrapperBuilder(String guildId){
        this.guildId = guildId;
    }

    public GuildWrapperBuilder setAutoMod(AutoModGuild autoMod){
        this.autoModGuild = autoMod;
        return this;
    }

    public GuildWrapperBuilder setWelcome(Welcome welcome){
        this.welcome = welcome;
        return this;
    }

    public GuildWrapperBuilder setPolls(Set<Poll> polls){
        this.polls = polls;
        return this;
    }

    public GuildWrapperBuilder addPoll(Poll poll){
        this.polls.add(poll);
        return this;
    }

    public GuildWrapperBuilder setSelfAssignRoles(Set<String> roles){
        this.selfAssignRoles = roles;
        return this;
    }

    public GuildWrapperBuilder addSelfAssignRole(String role){
        this.selfAssignRoles.add(role);
        return this;
    }

    public GuildWrapperBuilder setLanguage(Locale locale){
        this.locale = locale;
        return this;
    }

    public GuildWrapper build(){
        return new GuildWrapper(guildId, autoModGuild, welcome, polls, selfAssignRoles, locale);
    }
}
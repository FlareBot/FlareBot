package stream.flarebot.flarebot.objects;

import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModGuild;
import stream.flarebot.flarebot.permissions.PerGuildPermissions;

import java.util.LinkedList;
import java.util.Set;

public class GuildWrapperBuilder {

    private GuildWrapper wrapper;

    public GuildWrapperBuilder(String guildId) {
        this.wrapper = new GuildWrapper(guildId);
    }

    public GuildWrapperBuilder setAutoMod(AutoModGuild autoMod) {
        this.wrapper.setAutoModGuild(autoMod);
        return this;
    }

    public GuildWrapperBuilder setWelcome(Welcome welcome) {
        this.wrapper.setWelcome(welcome);
        return this;
    }

    public GuildWrapperBuilder setPermissions(PerGuildPermissions permissions) {
        this.wrapper.setPermissions(permissions);
        return this;
    }

    public GuildWrapperBuilder setPolls(LinkedList<Poll> polls) {
        this.wrapper.setPolls(polls);
        return this;
    }

    public GuildWrapperBuilder addPoll(Poll poll) {
        this.wrapper.getPolls().add(poll);
        return this;
    }

    public GuildWrapperBuilder setAutoAssignRoles(Set<String> roles) {
        this.wrapper.setAutoAssignRoles(roles);
        return this;
    }

    public GuildWrapperBuilder setSelfAssignRoles(Set<String> roles) {
        this.wrapper.setSelfAssignRoles(roles);
        return this;
    }


    public GuildWrapperBuilder setLanguage(Language.Locales locale) {
        this.wrapper.setLocale(locale);
        return this;
    }

    public GuildWrapperBuilder setBlocked(boolean blocked) {
        this.wrapper.setBlocked(blocked);
        return this;

    }

    public GuildWrapperBuilder setSongnick(boolean songnick) {
        this.wrapper.setSongnick(songnick);
        return this;
    }

    public GuildWrapper build() {
        return wrapper;
    }
}

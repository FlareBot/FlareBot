package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.entities.Guild;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Language;
import stream.flarebot.flarebot.mod.AutoModConfig;
import stream.flarebot.flarebot.mod.AutoModGuild;

import java.util.LinkedList;
import java.util.Set;

public class GuildWrapper {

    private String guildId;
    private AutoModGuild autoModGuild;
    private Welcome welcome;
    private LinkedList<Poll> polls;
    private Set<String> autoAssignRoles;
    private Set<String> selfAssignRoles;
    private Language.Locales locale;

    public GuildWrapper(String guildId, AutoModGuild autoModGuild, Welcome welcome, LinkedList<Poll> polls, Set<String> autoAssignRoles, Set<String> selfAssignRoles, Language.Locales locale){
        this.guildId = guildId;
        this.autoModGuild = autoModGuild;
        this.welcome = welcome;
        this.polls = polls;
        this.autoAssignRoles = autoAssignRoles;
        this.selfAssignRoles = selfAssignRoles;
        this.locale = locale;
    }

    public Guild getGuild(){
        return FlareBot.getInstance().getGuildByID(guildId);
    }

    public String getGuildId() {
        return this.guildId;
    }

    public AutoModGuild getAutoModGuild(){
        return this.autoModGuild;
    }

    public AutoModConfig getAutoModConfig() {
        return this.autoModGuild.getConfig();
    }

    public Welcome getWelcome(){
        if(welcome == null){
            welcome = new Welcome(getGuild().getPublicChannel().getId());
            welcome.setEnabled(false);
        }
        return this.welcome;
    }

    public LinkedList<Poll> getPolls(){
        return this.polls;
    }

    public Set<String> getAutoAssignRoles() {
        return this.autoAssignRoles;
    }

    public Set<String> getSelfAssignRoles(){
        return this.selfAssignRoles;
    }

    public Language.Locales getLocale(){
        return this.locale;
    }
}

package stream.flarebot.flarebot.objects;

import net.dv8tion.jda.core.entities.Guild;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.mod.AutoModGuild;

import java.util.Locale;
import java.util.Set;

public class GuildWrapper {

    private String guildId;
    private AutoModGuild autoModGuild;
    private Welcome welcome;
    private Set<Poll> polls;
    private Set<String> selfAssignRoles;
    private Locale locale;

    public GuildWrapper(String guildId, AutoModGuild autoModGuild, Welcome welcome, Set<Poll> polls, Set<String> selfAssignRoles, Locale locale){
        this.guildId = guildId;
        this.autoModGuild = autoModGuild;
        this.welcome = welcome;
        this.polls = polls;
        this.selfAssignRoles = selfAssignRoles;
        this.locale = locale;
    }

    public Guild getGuild(){
        return FlareBot.getInstance().getGuildByID(guildId);
    }

    public AutoModGuild getAutoModGuild(){
        return this.autoModGuild;
    }

    public Welcome getWelcome(){
        if(welcome == null){
            welcome = new Welcome(getGuild().getPublicChannel().getId());
            welcome.setEnabled(false);
        }
        return this.welcome;
    }

    public Set<Poll> getPolls(){
        return this.polls;
    }

    public Set<String> getSelfAssignRoles(){
        return this.selfAssignRoles;
    }

    public Locale getLocale(){
        return this.locale;
    }
}

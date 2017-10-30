package stream.flarebot.flarebot.mod;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import stream.flarebot.flarebot.FlareBotManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AutoModGuild {

    private AutoModConfig config;

    private Map<String, Integer> userPoints = new ConcurrentHashMap<>();

    public AutoModGuild() {
        config = new AutoModConfig();
        for (Action action : Action.values)
            config.getActions().put(action, action.getDefaultPoints());
        config.resetPunishments();
    }

    public AutoModConfig getConfig() {
        return config;
    }

    public Map<String, Integer> getUserPoints() {
        return this.userPoints;
    }

    public void setPoints(String userId, int i) {
        getUserPoints().put(userId, i);
    }

    public int getPointsForUser(String userId) {
        return this.userPoints.getOrDefault(userId, 0);
    }

    public String addPoints(Guild guild, String userId, int points) {
        this.userPoints.put(userId, userPoints.containsKey(userId) ? userPoints.get(userId) + points : points);

        for(int punishmentPoints : config.getPunishments().keySet()){
            if(userPoints.get(userId) >= punishmentPoints && userPoints.get(userId) - points < punishmentPoints){
                Punishment punishment = config.getPunishments().get(punishmentPoints);
                switch(punishment.getPunishment()) {
                    case TEMP_MUTE:
                    case MUTE:
                        if(guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES))
                            muteUser(guild, guild.getMemberById(userId));
                        else
                            return "Unable to mute user! (ID: " + userId + ") I do not have the 'Manage Roles' permission!";
                        break;
                    case TEMP_BAN:
                    case BAN:
                        if(guild.getSelfMember().hasPermission(Permission.BAN_MEMBERS))
                            guild.getController().ban(userId, 30, "AutoMod punishment - " + userPoints.get(userId) + " points.").queue();
                        else
                            return "Unable to ban user! (ID: " + userId + ") I do not have the 'Ban Members' permission!";
                        break;
                    case KICK:
                        if(guild.getSelfMember().hasPermission(Permission.KICK_MEMBERS))
                            guild.getController().kick(userId, "AutoMod punishment - " + userPoints.get(userId) + " points.").queue();
                        else
                            return "Unable to kick user! (ID: " + userId + ") I do not have the 'Kick Members' permission!";
                        break;
                    case PURGE:

                }
                config.postAutoModAction(punishment, guild.getMemberById(userId).getUser());
            }
        }
        return null;
    }

    public void muteUser(Guild guild, Member member) throws HierarchyException {
        guild.getController().addRolesToMember(member, FlareBotManager.getInstance().getGuild(guild.getId()).getMutedRole()).complete();
    }
}

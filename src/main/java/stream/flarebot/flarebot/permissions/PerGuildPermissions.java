package stream.flarebot.flarebot.permissions;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerGuildPermissions {

    private final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public boolean hasPermission(Member user, stream.flarebot.flarebot.permissions.Permission permission) {
        // So we can go into servers and figure out any issues they have.
        if (isCreator(user.getUser()))
            return true;
        if (user.isOwner())
            return true;
        if (user.getPermissions().contains(Permission.ADMINISTRATOR))
            return true;
        // Change done by Walshy: Internal review needed
        if (isContributor(user.getUser()) && FlareBot.instance().isTestBot())
            return true;
        if (getUser(user).hasPermission(permission))
            return true;
        for (Group g : getGroups().values()) {
            if (!g.hasPermission(permission)) continue;
            if (getUser(user).getGroups().contains(g.getName())) return true;
            if (g.getRoleId() != null && user.getGuild().getRoleById(g.getRoleId()) != null) {
                if (user.getRoles().contains(user.getGuild().getRoleById(g.getRoleId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public User getUser(Member user) {
        return users.computeIfAbsent(user.getUser().getId(), key -> new User());
    }

    public Group getGroup(String group) {
        return groups.get(group);
    }

    public boolean addGroup(String group) {
        if (groups.containsKey(group)) {
            return false;
        } else {
            groups.put(group, new Group(group));
            return true;
        }
    }

    public void deleteGroup(String group) {
        groups.remove(group);
    }

    public boolean hasGroup(String group) {
        return groups.containsKey(group);
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public List<Group> getListGroups() {
        return new ArrayList<>(groups.values());
    }

    private static boolean checkOfficialGuildForRole(net.dv8tion.jda.core.entities.User user, long roleId) {
        if (!FlareBot.instance().isReady() || Constants.getOfficialGuild() == null) return false;
        return Constants.getOfficialGuild().getMember(user) != null && Constants.getOfficialGuild().getMember(user).getRoles()
                .contains(Constants.getOfficialGuild().getRoleById(roleId));
    }

    public static boolean isCreator(net.dv8tion.jda.core.entities.User user) {
        return checkOfficialGuildForRole(user, Constants.DEVELOPER_ID);
    }

    public static boolean isContributor(net.dv8tion.jda.core.entities.User user) {
        return checkOfficialGuildForRole(user, Constants.CONTRIBUTOR_ID);
    }

    public static boolean isStaff(net.dv8tion.jda.core.entities.User user) {
        return checkOfficialGuildForRole(user, Constants.STAFF_ID);
    }
}

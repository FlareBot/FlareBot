package stream.flarebot.flarebot.permissions;

import net.dv8tion.jda.core.entities.Member;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PerGuildPermissions {

    private final List<Group> groups = Collections.synchronizedList(new ArrayList<>());
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public boolean hasPermission(Member user, Permission permission) {
        // So we can go into servers and figure out any issues they have.
        if (isCreator(user.getUser()))
            return true;
        if (user.isOwner())
            return true;
        if (user.getPermissions().contains(net.dv8tion.jda.core.Permission.ADMINISTRATOR))
            return true;
        if (isContributor(user.getUser()) && FlareBot.instance().isTestBot())
            return true;
        if (getUser(user).hasPermission(permission) == Permission.Reply.ALLOW)
            return true;
        if (getUser(user).hasPermission(permission) == Permission.Reply.DENY) {
            return false;
        }
        Permission.Reply hasPerm = Permission.Reply.NEUTRAL;
        synchronized (groups) {
            for (Group g : groups) {
                if (g.hasPermission(permission) == Permission.Reply.NEUTRAL) continue;
                if (g.getRoleId() != null && user.getGuild().getRoleById(g.getRoleId()) != null) {
                    if (user.getRoles().contains(user.getGuild().getRoleById(g.getRoleId()))) {
                        hasPerm = g.hasPermission(permission);
                    }
                }
                if (getUser(user).getGroups().contains(g.getName())) {
                    hasPerm = g.hasPermission(permission);
                }
            }
        }
        if (hasPerm == Permission.Reply.NEUTRAL) {
            return (!permission.getDiscordPerm().isEmpty() && user.hasPermission(permission.getDiscordPerm()))
                    || permission.isDefaultPerm();
        } else {
            return hasPerm == Permission.Reply.ALLOW;
        }
    }

    public User getUser(Member user) {
        return users.computeIfAbsent(user.getUser().getId(), key -> new User());
    }

    public Group getGroup(String group) {
        synchronized (groups) {
            for (Group g : groups) {
                if (g.getName().equalsIgnoreCase(group)) return g;
            }
        }
        return null;
    }

    public boolean addGroup(String group) {
        if (hasGroup(group)) {
            return false;
        } else {
            groups.add(new Group(group));
            return true;
        }
    }

    public void deleteGroup(String group) {
        groups.remove(getGroup(group));
    }

    public boolean hasGroup(String group) {
        synchronized (groups) {
            for (Group g : groups) {
                if (g.getName().equals(group)) return true;
            }
        }
        return false;
    }

    public List<Group> getGroups() {
        return groups;
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

    public static boolean isAdmin(net.dv8tion.jda.core.entities.User user) {
        return checkOfficialGuildForRole(user, Constants.ADMINS_ID);
    }

    public void moveGroup(Group group, int pos) {
        int index = groups.indexOf(group);
        groups.remove(index);
        groups.add(pos, group);
    }

    public boolean cloneGroup(Group group, String newGroupName) {
        if (hasGroup(newGroupName))
            return false;
        else {
            Group newGroup = new Group(newGroupName);
            for(String permission : group.getPermissions())
                newGroup.addPermission(permission);
            groups.add(newGroup);
            return true;
        }
    }

    public boolean renameGroup(Group group, String name) {
        if (hasGroup(name))
            return false;
        else {
            group.setName(name);
            return true;
        }
    }
}

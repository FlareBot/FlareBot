package stream.flarebot.flarebot.permissions;

import com.google.gson.annotations.Expose;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerGuildPermissions {

    private final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    @Expose(deserialize = false, serialize = false)
    private static final FlareBot fb = FlareBot.getInstance();

    public PerGuildPermissions() {
        if (!hasGroup("Default")) {
            createDefaultGroup();
        }
    }

    public boolean hasPermission(Member user, String permission) {
        // So we can go into servers and figure out any issues they have.
        if (isCreator(user.getUser()))
            return true;
        if (user.isOwner())
            return true;
        if (user.getPermissions().contains(Permission.ADMINISTRATOR))
            return true;
        // Change done by Walshy: Internal review needed
        if (isContributor(user.getUser()) && FlareBot.getInstance().isTestBot())
            return true;
        PermissionNode node = new PermissionNode(permission);
        for (Group g : getGroups().values()) {
            if (!g.hasPermission(node)) continue;
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

    private boolean checkOfficialGuildForRole(net.dv8tion.jda.core.entities.User user, long roleId) {
        if (fb.getOfficialGuild().getMember(user) == null) return false;
        return fb.getOfficialGuild().getMember(user).getRoles().contains(fb.getOfficialGuild().getRoleById(roleId));
    }

    public boolean isCreator(net.dv8tion.jda.core.entities.User user) {
        return checkOfficialGuildForRole(user, 226788297156853771L);
    }

    public boolean isContributor(net.dv8tion.jda.core.entities.User user) {
        return checkOfficialGuildForRole(user, 226788297156853771L);
    }

    public boolean isStaff(net.dv8tion.jda.core.entities.User user) {
        return checkOfficialGuildForRole(user, 226788297156853771L);
    }

    public void createDefaultGroup() {
        if (hasGroup("Default")) {
            deleteGroup("Default");
        }
        Group defaults = new Group("Default");
        for (Command command : FlareBot.getInstance().getCommands()) {
            if (command.isDefaultPermission()) {
                defaults.addPermission(command.getPermission());
            }
        }
        defaults.addPermission("flarebot.userinfo.other");
        defaults.addPermission("flarebot.playlist.clear");
        groups.put("Default", defaults);
    }
}

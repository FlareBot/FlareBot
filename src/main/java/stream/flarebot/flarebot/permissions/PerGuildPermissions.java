package stream.flarebot.flarebot.permissions;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerGuildPermissions {

    private final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public PerGuildPermissions() {
        if (!hasGroup("Default")) {
            for (Command command : FlareBot.getInstance().getCommands()) {
                if (command.isDefaultPermission()) {
                    addPermission("Default", command.getPermission());
                }
            }
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
        PermissionNode node = new PermissionNode(permission);
        return getUser(user).getGroups().stream()
                .map(this::getGroup)
                .map(Group::getPermissions)
                .flatMap(Collection::stream)
                .map(PermissionNode::new)
                .anyMatch(e -> e.test(node));
    }

    public boolean addPermission(String group, String permission) {
        return getGroup(group).getPermissions().add(permission);
    }

    public boolean removePermission(String group, String permission) {
        boolean had = getGroup(group).getPermissions().remove(permission);
        if (getGroup(group).getPermissions().size() == 0) {
            groups.remove(group);
        }
        return had;
    }

    public User getUser(Member user) {
        return users.computeIfAbsent(user.getUser().getId(), key -> new User(user));
    }

    public Group getGroup(String group) {
        return groups.computeIfAbsent(group, key -> new Group(group));
    }

    public boolean deleteGroup(String group) {
        return groups.remove(group) != null;
    }

    public boolean hasGroup(String group) {
        return groups.containsKey(group);
    }

    public Map<String, Group> getGroups() {
        Map<String, Group> groups = new HashMap<>();
        groups.putAll(this.groups);
        return groups;
    }

    public boolean isCreator(net.dv8tion.jda.core.entities.User user) {
        return user.getId().equals("158310004187725824") || user.getId().equals("155954930191040513");
    }

    public boolean isContributor(net.dv8tion.jda.core.entities.User user){
        return user.getId().equals("215644829969809421") || user.getId().equals("203894491784937472");
    }
}

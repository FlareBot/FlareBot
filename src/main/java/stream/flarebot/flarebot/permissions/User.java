package stream.flarebot.flarebot.permissions;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Set;

public class User {

    private final Set<String> groups = new ConcurrentHashSet<>();
    private final Set<String> permissions = new ConcurrentHashSet<>();

    User() {
        groups.add("Default");
    }

    public Set<String> getGroups() {
        return groups;
    }

    public boolean addGroup(Group group) {
        return groups.add(group.getName());
    }

    public boolean removeGroup(Group group) {
        return groups.remove(group.getName());
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(PermissionNode node) {
        for (String s : permissions) {
            if (new PermissionNode(s).test(node))
                return true;
        }
        return false;
    }

    public boolean addPermission(String permission) {
        return permissions.add(permission);
    }

    public boolean removePermission(String permission) {
        return permissions.remove(permission);
    }
}

package stream.flarebot.flarebot.permissions;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Set;


public class Group {

    private final ConcurrentHashSet<String> permissions = new ConcurrentHashSet<>();
    private String name;
    private String roleId;

    private Group() {
    }

    Group(String name) {
        this.name = name;
    }


    public ConcurrentHashSet<String> getPermissions() {
        return permissions;
    }

    public String getName() {
        return name;
    }

    public boolean addPermission(String permission) {
        return permissions.add(permission);
    }

    public boolean removePermission(String permission) {
        return permissions.remove(permission);
    }

    public boolean hasPermission(String permission) {
        return hasPermission(new PermissionNode(permission));
    }

    public boolean hasPermission(PermissionNode node) {
        for (String s : permissions) {
            if (new PermissionNode(s).test(node))
                return true;
        }
        return false;
    }

    public void linkRole(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }
}

package stream.flarebot.flarebot.permissions;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Set;


public class Group {

    private final Set<String> permissions = new ConcurrentHashSet<>();
    private String name;
    private String roleId;

    private Group() {
    }

    Group(String name) {
        this.name = name;
    }


    public Set<String> getPermissions() {
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

    public boolean hasPermission(Permission permission) {
        for (String s : permissions) {
            if (s.startsWith("-")) {
                if (new PermissionNode(s.substring(1)).test(permission.getPermission()))
                    return false;
            }
            if (new PermissionNode(s).test(permission.getPermission()))
                return true;
        }
        return permission.isDefaultPerm();
    }

    public void linkRole(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }
}

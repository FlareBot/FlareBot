package stream.flarebot.flarebot.permissions;

import org.eclipse.jetty.util.ConcurrentHashSet;


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

    public Permission.Reply hasPermission(Permission permission) {
        for (String s : permissions) {
            boolean hasPermission =
                    new PermissionNode(s.substring(s.startsWith("-") ? 1 : 0)).test(permission.getPermission());
            if (s.startsWith("-") && hasPermission)
                return Permission.Reply.DENY;
            if (hasPermission)
                return Permission.Reply.ALLOW;
        }
        return Permission.Reply.NEUTRAL;
    }

    public void linkRole(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }
}

package stream.flarebot.flarebot.permissions;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Set;


public class Group {
    private final Set<String> permissions = new ConcurrentHashSet<>();
    private String name;

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

    public boolean addPermission(String permission){
        return permissions.add(permission);
    }

    public boolean removePermission(String permission){
        return permissions.remove(permission);
    }
}

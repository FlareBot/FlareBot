package stream.flarebot.flarebot.permissions;


import net.dv8tion.jda.core.entities.Member;

import java.util.HashSet;
import java.util.Set;

public class User {
    private final HashSet<String> groups = new HashSet<>();

    User() {
        groups.add("Default");
    }

    public Set<String> getGroups() {
        Set<String> groups = new HashSet<>();
        groups.addAll(this.groups);
        return groups;
    }

    public boolean addGroup(Group group) {
        return groups.add(group.getName());
    }

    public boolean removeGroup(Group group) {
        return groups.remove(group.getName());
    }
}

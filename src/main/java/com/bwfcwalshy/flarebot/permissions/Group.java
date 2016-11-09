package com.bwfcwalshy.flarebot.permissions;

import java.util.HashSet;
import java.util.Set;


public class Group {
    final HashSet<String> permissions = new HashSet<>();
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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Group)) {
            return false;
        }
        Group otherGroup = (Group) other;
        return otherGroup.getName().equals(getName());
    }

    @Override
    public int hashCode() {
        int result = permissions.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}

package com.bwfcwalshy.flarebot.permissions;

import java.util.HashSet;
import java.util.Set;


public class Group {
    final HashSet<String> permissions = new HashSet<>();
    private String name;
    private PerGuildPermissions parent;

    private Group() {
    }

    Group(String name, PerGuildPermissions parent) {
        this.name = name;
        this.parent = parent;
    }

    public PerGuildPermissions getParent() {
        return parent;
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
}

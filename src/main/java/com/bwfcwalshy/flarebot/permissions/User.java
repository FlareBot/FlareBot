package com.bwfcwalshy.flarebot.permissions;


import net.dv8tion.jda.core.entities.Member;

import java.util.HashSet;
import java.util.Set;

public class User {
    private final HashSet<String> groups = new HashSet<>();
    protected String userID;

    User(Member user) {
        userID = user.getUser().getId();
        groups.add("Default");
    }

    public String getUserID() {
        return userID;
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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof User)) {
            return false;
        }
        User otherUser = (User) other;
        return otherUser.getUserID().equals(getUserID());
    }

    @Override
    public int hashCode() {
        int result = groups.hashCode();
        result = 31 * result + userID.hashCode();
        return result;
    }
}

package com.bwfcwalshy.flarebot.permissions;

public class PrivateChannelUser extends User {
    public PrivateChannelUser(net.dv8tion.jda.core.entities.User u) {
        super.userID = u.getId();
    }

    @Override
    public boolean addGroup(Group group) {
        return true;
    }

    @Override
    public boolean removeGroup(Group group) {
        return true;
    }
}

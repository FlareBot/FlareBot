package com.bwfcwalshy.flarebot.permissions;

import sx.blah.discord.handle.obj.IUser;


public class PrivateChannelUser extends User {
    public PrivateChannelUser(IUser u) {
        super.userID = u.getID();
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

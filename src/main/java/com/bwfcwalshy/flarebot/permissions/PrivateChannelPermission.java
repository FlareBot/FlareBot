package com.bwfcwalshy.flarebot.permissions;

import net.dv8tion.jda.core.entities.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.Map;

public class PrivateChannelPermission extends PerGuildPermissions {
    public PrivateChannelPermission(Channel guild) {
        super.id = guild.getID();
    }

    @Override
    public boolean addPermission(String group, String permission) {
        return true;
    }

    @Override
    public Map<String, Group> getGroups() {
        return new HashMap<>(0);
    }

    @Override
    public boolean deleteGroup(String group) {
        return true;
    }

    @Override
    public String getGuildID() {
        return super.getGuildID();
    }

    @Override
    public User getUser(IUser user) {
        return super.getUser(user);
    }

    @Override
    public boolean hasPermission(IUser user, String permission) {
        return true;
    }
}

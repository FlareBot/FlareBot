package com.bwfcwalshy.flarebot.permissions;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.HashMap;
import java.util.Map;

public class PrivateChannelPermission extends PerGuildPermissions {
    public PrivateChannelPermission(MessageChannel guild) {
        super.id = guild.getId();
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
    public User getUser(Member user) {
        return super.getUser(user);
    }

    @Override
    public boolean hasPermission(Member user, String permission) {
        return true;
    }
}

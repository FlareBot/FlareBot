package com.bwfcwalshy.flarebot.permissions;

import com.bwfcwalshy.flarebot.FlareBot;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class PerGuildPermissions {
    private final HashMap<String, Group> groups = new HashMap<>();
    private final HashMap<String, User> users = new HashMap<>();
    protected String id;

    protected PerGuildPermissions() {
    }

    public PerGuildPermissions(String id) {
        this.id = id;
        if (!hasGroup("Default")) {
            addPermission("Default", "flarebot.skip");
            addPermission("Default", "flarebot.leave");
            addPermission("Default", "flarebot.play");
            addPermission("Default", "flarebot.pause");
            addPermission("Default", "flarebot.info");
            addPermission("Default", "flarebot.join");
            addPermission("Default", "flarebot.skip");
            addPermission("Default", "flarebot.search");
            addPermission("Default", "flarebot.playlist.save");
            addPermission("Default", "flarebot.playlist.load");
            addPermission("Default", "flarebot.help");
        }
    }

    public boolean hasPermission(IUser user, String permission) {
        // So we can go into servers and figure out any issues they have.
        if (isCreator(user))
            return true;
        if (FlareBot.getInstance().getClient().getGuildByID(getGuildID()).getOwner().equals(user))
            return true;
        if (user.getPermissionsForGuild(user.getClient().getGuildByID(getGuildID())).contains(Permissions.ADMINISTRATOR))
            return true;
        AtomicBoolean has = new AtomicBoolean(false);
        PermissionNode node = new PermissionNode(permission);
        getUser(user).getGroups().forEach((group) ->
                getGroups().computeIfAbsent(group, Group::new).getPermissions().forEach(s -> {
                    if (new PermissionNode(s).test(node))
                        has.set(true);
                }));
        return has.get();
    }

    public boolean addPermission(String group, String permission) {
        return getGroup(group).getPermissions().add(permission);
    }

    public boolean removePermission(String group, String permission) {
        boolean had = getGroup(group).getPermissions().remove(permission);
        if (getGroup(group).getPermissions().size() == 0) {
            groups.remove(group);
        }
        return had;
    }

    public User getUser(IUser user) {
        return users.computeIfAbsent(user.getID(), key -> new User(user));
    }

    public Group getGroup(String group) {
        return groups.computeIfAbsent(group, key -> new Group(group));
    }

    public boolean deleteGroup(String group) {
        return groups.remove(group) != null;
    }

    public boolean hasGroup(String group) {
        return groups.containsKey(group);
    }

    public Map<String, Group> getGroups() {
        Map<String, Group> groups = new HashMap<>();
        groups.putAll(this.groups);
        return groups;
    }

    /**
     * Gets the associated guilds ID
     *
     * @return The ID to get
     */
    public String getGuildID() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PerGuildPermissions)) {
            return false;
        }
        PerGuildPermissions otherGuild = (PerGuildPermissions) other;
        return otherGuild.getGuildID().equals(getGuildID());
    }

    public boolean isCreator(IUser user) {
        return user.getID().equals("158310004187725824") || user.getID().equals("155954930191040513");
    }

    @Override
    public int hashCode() {
        int result = groups.hashCode();
        result = 31 * result + users.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}

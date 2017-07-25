package stream.flarebot.flarebot.permissions;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerGuildPermissions {

    private final ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Member, User> users = new ConcurrentHashMap<>();

    public PerGuildPermissions() {
        if (!hasGroup("Default")) {
            for (Command command : FlareBot.getInstance().getCommands()) {
                if (command.isDefaultPermission()) {
                    Group defaults = new Group("Default");
                    groups.put("Default", defaults);
                    defaults.addPermission(command.getPermission());
                }
            }
        }
    }

    public boolean hasPermission(Member user, String permission) {
        // So we can go into servers and figure out any issues they have.
        if (isCreator(user.getUser()))
            return true;
        if (user.isOwner())
            return true;
        if (user.getPermissions().contains(Permission.ADMINISTRATOR))
            return true;
        if(isContributor(user.getUser()))
            return true;
        PermissionNode node = new PermissionNode(permission);
        for(Group g: getListGroups()) {
            if (g.getPermissions().contains(node.getNode())) {
                if (getUser(user).getGroups().contains(g)) {
                    return true;
                } else {
                    for(Role role: user.getRoles()){
                        if(g.getRoleId().equals(role.getId())){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public User getUser(Member user) {
        return users.computeIfAbsent(user, key -> new User());
    }

    public Group getGroup(String group) {
        return groups.get(group);
    }

    public boolean addGroup(String group){
        if(groups.containsKey(group)){
            return false;
        } else {
            groups.put(group, new Group(group));
            return true;
        }
    }

    public void deleteGroup(String group) {
        groups.remove(group);
    }

    public boolean hasGroup(String group) {
        return groups.containsKey(group);
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public List<Group> getListGroups(){
        return new ArrayList<>(groups.values());
    }

    public boolean isCreator(net.dv8tion.jda.core.entities.User user) {
        return user.getId().equals("158310004187725824") || user.getId().equals("155954930191040513");
    }

    public boolean isContributor(net.dv8tion.jda.core.entities.User user){
        return user.getId().equals("215644829969809421") || user.getId().equals("203894491784937472");
    }

}

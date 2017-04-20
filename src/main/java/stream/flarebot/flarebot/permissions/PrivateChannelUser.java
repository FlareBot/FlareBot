package stream.flarebot.flarebot.permissions;

import net.dv8tion.jda.core.entities.Member;

public class PrivateChannelUser extends User {
    public PrivateChannelUser(Member u) {
        super(u);
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

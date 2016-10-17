package com.bwfcwalshy.flarebot.permissions;

import com.bwfcwalshy.flarebot.FlareBot;
import sx.blah.discord.handle.obj.IChannel;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

public class Permissions {
    private HashMap<String, PerGuildPermissions> perms = new HashMap<>();

    public void save() throws IOException {
        try (Writer fw = new FileWriter(FlareBot.PERMS_FILE)) {
            FlareBot.GSON.toJson(this, fw);
            fw.flush();
            fw.close();
        }
    }

    public PerGuildPermissions getPermissions(IChannel channel) {
        if (channel.isPrivate())
            return new PrivateChannelPermission(channel);
        return perms.computeIfAbsent(channel.getGuild().getID(), PerGuildPermissions::new);
    }
}
